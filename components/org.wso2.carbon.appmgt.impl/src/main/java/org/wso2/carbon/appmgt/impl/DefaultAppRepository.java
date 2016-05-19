package org.wso2.carbon.appmgt.impl;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.impl.dto.Environment;
import org.wso2.carbon.appmgt.impl.idp.sso.SSOConfiguratorUtil;
import org.wso2.carbon.appmgt.impl.idp.sso.model.SSOEnvironment;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.impl.utils.AppMgtDataSourceProvider;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

/**
 * The default implementation of DefaultAppRepository which uses RDBMS and Carbon registry for persistence.
 */
public class DefaultAppRepository implements AppRepository {

    private static final Log log = LogFactory.getLog(DefaultAppRepository.class);

    private static final String POLICY_GROUP_TABLE_NAME = "APM_POLICY_GROUP";
    private static final String POLICY_GROUP_PARTIAL_MAPPING_TABLE_NAME = "APM_POLICY_GRP_PARTIAL_MAPPING";

    private Registry registry;

    public DefaultAppRepository(Registry registry){
        this.registry = registry;
    }

    // ------------------- START : Repository API implementation methods. ----------------------------------

    @Override
    public String saveApp(App app) throws AppManagementException {

        if (AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())) {
            return persistWebApp((WebApp) app);
        }

        return null;
    }

    @Override
    public void updateApp(App app) throws AppManagementException {

        if (AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())) {
            updateWebApp((WebApp) app);
        }

    }

    @Override
    public App getApp(String type, String uuid) throws AppManagementException {

        Map<String, String> searchTerms = new HashMap<String, String>();
        searchTerms.put("id", uuid);

        List<App> result = searchApps(type, searchTerms);

        if(result.size() == 1){
            return result.get(0);
        }else{
            //flag an error.
            throw new AppManagementException("Duplicate entries found for the given uuid.");
        }

    }

    @Override
    public List<App> searchApps(String type, Map<String, String> searchTerms) throws AppManagementException {

        List<App> apps = new ArrayList<App>();
        List<GenericArtifact> appArtifacts = null;

        try {
            appArtifacts = getAllAppArtifacts(type);
        } catch (GovernanceException e) {
            handleException(String.format("Error while retrieving registry artifacts during app search for the type '%s'", type), e);
        }

        for(GenericArtifact artifact : appArtifacts){
            if(isSearchHit(artifact, searchTerms)){
                apps.add(getApp(type, artifact));
            }
        }

        return apps;

    }

    @Override
    public void persistStaticContents(FileContent fileContent) throws AppManagementException {
        Connection connection = null;

        PreparedStatement preparedStatement = null;
        String query = "INSERT INTO resource (UUID,TENANTID,FILENAME,CONTENTLENGTH,CONTENTTYPE,CONTENT) VALUES (?,?,?,?,?,?)";
        try {
            connection = AppMgtDataSourceProvider.getStorageDBConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, fileContent.getUuid());
            preparedStatement.setString(2, getTenantDomainOfCurrentUser());
            preparedStatement.setString(3, fileContent.getFileName());
            preparedStatement.setInt(4, fileContent.getContentLength());
            preparedStatement.setString(5, fileContent.getContentType());
            preparedStatement.setBlob(6, fileContent.getContent());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                handleException(String.format("Couldn't rollback save operation for the static content"), e1);
            }
            handleException("Error occurred while saving static content :" + fileContent.getFileName(), e);
        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    @Override
    public FileContent getStaticContent(String contentId)throws AppManagementException {
        Connection connection = null;
        FileContent fileContent = null;

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = "SELECT CONTENT,CONTENTTYPE FROM resource WHERE FILENAME = ? AND TENANTID = ?";
            connection = AppMgtDataSourceProvider.getStorageDBConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, contentId);
            preparedStatement.setString(2, getTenantDomainOfCurrentUser());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Blob staticContentBlob = resultSet.getBlob("CONTENT");
                InputStream inputStream = staticContentBlob.getBinaryStream();
                fileContent = new FileContent();
                fileContent.setContentType(resultSet.getString("CONTENTTYPE"));
                fileContent.setContent(inputStream);
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                handleException(String.format("Couldn't rollback retrieve operation for the static content '"+contentId+"'"), e1);
            }
            handleException("Error occurred while saving static content :" + contentId, e);
        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
        return fileContent;

    }

    // ------------------- END : Repository API implementation methods. ----------------------------------

    private WebApp getApp(String type, GenericArtifact appArtifact) throws AppManagementException {

        if (AppMConstants.WEBAPP_ASSET_TYPE.equals(type)) {
            return getWebApp(appArtifact);
        }
        return null;

    }

    private boolean isSearchHit(GenericArtifact artifact, Map<String, String> searchTerms) throws AppManagementException {

        boolean isSearchHit = true;

        for(Map.Entry<String, String> term : searchTerms.entrySet()){
            try {
                if("ID".equalsIgnoreCase(term.getKey())) {
                    if(!artifact.getId().equals(term.getValue())){
                        isSearchHit = false;
                        break;
                    }
                }else if(!term.getValue().equals(artifact.getAttribute(getRxtAttributeName(term.getKey())))){
                    isSearchHit = false;
                    break;
                }
            } catch (GovernanceException e) {
                String errorMessage = String.format("Error while determining whether artifact '%s' is a search hit.", artifact.getId());
                throw new AppManagementException(errorMessage, e);
            }
        }

        return isSearchHit;
    }

    private String getRxtAttributeName(String searchKey) {

        String rxtAttributeName = null;

        if(searchKey.equalsIgnoreCase("NAME")){
            rxtAttributeName = AppMConstants.API_OVERVIEW_NAME;
        }else if(searchKey.equalsIgnoreCase("PROVIDER")){
            rxtAttributeName = AppMConstants.API_OVERVIEW_PROVIDER;
        }else if(searchKey.equalsIgnoreCase("VERSION")){
            rxtAttributeName = AppMConstants.API_OVERVIEW_VERSION;
        }

        return rxtAttributeName;
    }

    private List<GenericArtifact> getAllAppArtifacts(String appType) throws GovernanceException, AppManagementException {

        List<GenericArtifact> appArtifacts = new ArrayList<GenericArtifact>();

        GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry, appType);
        GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
        for (GenericArtifact artifact : artifacts) {
            appArtifacts.add(artifact);
        }

        return appArtifacts;
    }

    private WebApp getWebApp(GenericArtifact webAppArtifact) throws AppManagementException {

        Connection connection = null;

        try {

            AppFactory appFactory = getAppFactory(AppMConstants.WEBAPP_ASSET_TYPE);
            WebApp webApp = (WebApp) appFactory.createApp(webAppArtifact, registry);

            try {
                connection = getRDBMSConnectionWithoutAutoCommit();
            } catch (SQLException e) {
                handleException("Can't get the database connection.", e);
            }

            int webAppDatabaseId = getDatabaseId(webApp, connection);

            List<EntitlementPolicyGroup> policyGroups = getPolicyGroups(webAppDatabaseId, connection);
            webApp.setAccessPolicyGroups(policyGroups);

            Set<URITemplate> uriTemplates = getURITemplates(webAppDatabaseId, connection);
            webApp.setUriTemplates(uriTemplates);

            return webApp;
        } catch (SQLException e) {
            handleException(String.format("Error while building the app for the web app registry artifact '%s'", webAppArtifact.getId()), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, connection, null);
        }

        return null;
    }

    private Set<URITemplate> getURITemplates(int webAppDatabaseId, Connection connection) throws SQLException {

        String query = "SELECT * FROM APM_APP_URL_MAPPING WHERE APP_ID=?";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, webAppDatabaseId);
            resultSet = preparedStatement.executeQuery();

            Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
            while(resultSet.next()){
                URITemplate uriTemplate = new URITemplate();
                uriTemplate.setId(resultSet.getInt("URL_MAPPING_ID"));
                uriTemplate.setUriTemplate(resultSet.getString("URL_PATTERN"));
                uriTemplate.setHTTPVerb(resultSet.getString("HTTP_METHOD"));
                uriTemplate.setPolicyGroupId(resultSet.getInt("POLICY_GRP_ID"));

                uriTemplates.add(uriTemplate);
            }

            return uriTemplates;

        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, resultSet);
        }

    }

    private List<EntitlementPolicyGroup> getPolicyGroups(int webAppDatabaseId, Connection connection) throws SQLException {

        String query = "SELECT `GROUP`.*,PARTIAL_MAPPING.POLICY_PARTIAL_ID " +
                                        "FROM " +
                                        "APM_POLICY_GROUP `GROUP` " +
                                        "LEFT JOIN APM_POLICY_GRP_PARTIAL_MAPPING PARTIAL_MAPPING " +
                                        "ON `GROUP`.POLICY_GRP_ID=PARTIAL_MAPPING.POLICY_GRP_ID, " +
                                        "APM_POLICY_GROUP_MAPPING MAPPING " +
                                        "WHERE " +
                                        "MAPPING.POLICY_GRP_ID=GROUP.POLICY_GRP_ID " +
                                        "AND MAPPING.APP_ID=? " +
                                        "ORDER BY `GROUP`.POLICY_GRP_ID";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, webAppDatabaseId);

            resultSet = preparedStatement.executeQuery();

            List<EntitlementPolicyGroup> policyGroups = new ArrayList<EntitlementPolicyGroup>();
            while(resultSet.next()){

                EntitlementPolicyGroup policyGroup = new EntitlementPolicyGroup();

                policyGroup.setPolicyGroupId(resultSet.getInt("POLICY_GRP_ID"));
                policyGroup.setPolicyGroupName(resultSet.getString("NAME"));
                policyGroup.setPolicyDescription(resultSet.getString("DESCRIPTION"));
                policyGroup.setThrottlingTier(resultSet.getString("THROTTLING_TIER"));
                policyGroup.setUserRoles(resultSet.getString("USER_ROLES"));
                policyGroup.setAllowAnonymous(resultSet.getBoolean("URL_ALLOW_ANONYMOUS"));

                Integer entitlementPolicyId = resultSet.getInt("POLICY_PARTIAL_ID");

                if(entitlementPolicyId > 0){
                    policyGroup.setEntitlementPolicyId(entitlementPolicyId);
                }

                policyGroups.add(policyGroup);

            }

            return policyGroups;
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, resultSet);
        }

    }

    private AppFactory getAppFactory(String appType) {
        if(AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)){
            return new WebAppFactory();
        }else if(AppMConstants.MOBILE_ASSET_TYPE.equals(appType)){
            return new MobileAppFactory();
        }else{
            return null;
        }
    }

    private GenericArtifact getRegistryArtifact(String type, String uuid) throws RegistryException {
        GenericArtifactManager artifactManager = getArtifactManager(registry,type);
        return artifactManager.getGenericArtifact(uuid);
    }

    private String persistWebApp(WebApp webApp) throws AppManagementException {

        Connection connection = null;

        try {
            connection = getRDBMSConnectionWithoutAutoCommit();
        } catch (SQLException e) {
            handleException("Can't get the database connection.", e);
        }

        try {

            // Persist master data first.
            persistPolicyGroups(webApp.getAccessPolicyGroups(), connection);

            // Add the registry artifact
            registry.beginTransaction();
            String uuid = saveRegistryArtifact(webApp);
            webApp.setUUID(uuid);

            // Persist web app data to the database (RDBMS)
            int webAppDatabaseId = persistWebAppToDatabase(webApp, connection);

            associatePolicyGroupsWithWebApp(webApp.getAccessPolicyGroups(), webAppDatabaseId, connection);

            persistURLTemplates(webApp.getUriTemplates(), webApp.getAccessPolicyGroups(), webAppDatabaseId, connection);

            if(!StringUtils.isEmpty(webApp.getJavaPolicies())){
                persistJavaPolicyMappings(webApp.getJavaPolicies(), webAppDatabaseId, connection);
            }

            persistLifeCycleEvent(webAppDatabaseId, null, APIStatus.CREATED, connection);

            createSSOProvider(webApp);

            // Commit JDBC and Registry transactions.
            registry.commitTransaction();
            connection.commit();

            return uuid;
        } catch (SQLException e) {
            rollbackTransactions(webApp, registry, connection);
            handleException(String.format("Can't persist web app '%s'.", webApp.getDisplayName()), e);
        } catch (RegistryException e) {
            rollbackTransactions(webApp, registry, connection);
            handleException(String.format("Can't persist web app '%s'.", webApp.getDisplayName()), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null,connection,null);
        }

        // Return null to make the compiler doesn't complain.
        return null;
    }

    private void updateWebApp(WebApp webApp) throws AppManagementException {

        Connection connection = null;

        try {
            connection = getRDBMSConnectionWithoutAutoCommit();
        } catch (SQLException e) {
            handleException("Can't get the database connection.", e);
        }

        try {
            int webAppDatabaseId = getDatabaseId(webApp, connection);
            updatePolicyGroups(webApp.getAccessPolicyGroups(), webAppDatabaseId, connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private int getDatabaseId(WebApp webApp, Connection connection) throws SQLException {

        String query = "SELECT APP_ID FROM APM_APP WHERE UUID=? AND TENANT_ID=?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, webApp.getUUID());
        preparedStatement.setInt(2, getTenantIdOfCurrentUser());

        resultSet = preparedStatement.executeQuery();

        while(resultSet.next()){
            return resultSet.getInt("APP_ID");
        }

        return -1;
    }

    private void updatePolicyGroups(List<EntitlementPolicyGroup> accessPolicyGroups, int webAppDatabaseId, Connection connection) throws SQLException {

        List<EntitlementPolicyGroup> groupsToBeAdded = new ArrayList<>();
        List<EntitlementPolicyGroup> groupsToBeUpdated = new ArrayList<>();

        for(EntitlementPolicyGroup policyGroup : accessPolicyGroups){
            if(policyGroup.getPolicyGroupId() > 0){
                groupsToBeUpdated.add(policyGroup);
            }else{
                groupsToBeAdded.add(policyGroup);
            }
        }

        // Update existing policy groups
        String queryToUpdateGroups = String.format("UPDATE %s SET DESCRIPTION=?,THROTTLING_TIER=?,USER_ROLES=?,URL_ALLOW_ANONYMOUS=? WHERE POLICY_GRP_ID=?", POLICY_GROUP_TABLE_NAME);
        PreparedStatement preparedStatementToUpdateGroups = connection.prepareStatement(queryToUpdateGroups);

        for(EntitlementPolicyGroup policyGroup : groupsToBeUpdated){
            preparedStatementToUpdateGroups.setString(1,policyGroup.getPolicyDescription());
            preparedStatementToUpdateGroups.setString(2,policyGroup.getThrottlingTier());
            preparedStatementToUpdateGroups.setString(3,policyGroup.getUserRoles());
            preparedStatementToUpdateGroups.setBoolean(4, policyGroup.isAllowAnonymous());
            preparedStatementToUpdateGroups.setInt(5, policyGroup.getPolicyGroupId());

            preparedStatementToUpdateGroups.addBatch();

        }
        preparedStatementToUpdateGroups.executeBatch();
        updateEntitlementPolicyMappings(groupsToBeUpdated, connection);
        deleteUnlinkedEntitlementPolicyMappings(groupsToBeUpdated, connection);

        // Add new policy groups
        persistPolicyGroups(groupsToBeAdded, connection);
        associatePolicyGroupsWithWebApp(groupsToBeAdded, webAppDatabaseId, connection);

        // Remove unused policy groups.
        deletePolicyGroupsNotIn(groupsToBeUpdated, webAppDatabaseId, connection);
    }

    private void deletePolicyGroupsNotIn(List<EntitlementPolicyGroup> groupsToBeRetained, int webAppDatabaseId, Connection connection) throws SQLException {

        // Get all the policy groups for the web app.

        String queryToGetPolicyGroupsForApp = "SELECT POLICY_GRP_ID FROM APM_POLICY_GROUP_MAPPING WHERE APP_ID=?";
        PreparedStatement preparedStatementToGetPolicyGroupsForApp = null;
        ResultSet policyGroupsResultSet = null;

        try{
            connection.prepareStatement(queryToGetPolicyGroupsForApp);
            preparedStatementToGetPolicyGroupsForApp.setInt(1, webAppDatabaseId);

            policyGroupsResultSet = preparedStatementToGetPolicyGroupsForApp.executeQuery();

            List<Integer> policyGroupIdsForApp = new ArrayList<Integer>();

            while (policyGroupsResultSet.next()){
                policyGroupIdsForApp.add(policyGroupsResultSet.getInt("POLICY_GRP_ID"));
            }

            List<Integer> retainedPolicyGroupIds = new ArrayList<Integer>();
            if(groupsToBeRetained != null){
                for(EntitlementPolicyGroup policyGroup : groupsToBeRetained){
                    retainedPolicyGroupIds.add(policyGroup.getPolicyGroupId());
                }
            }


            List<Integer> policyGroupIdsToBeDeleted = ListUtils.subtract(policyGroupIdsForApp, retainedPolicyGroupIds);

            disassociatePolicyGroupsWithWebApp(policyGroupIdsToBeDeleted, webAppDatabaseId, connection);

            PreparedStatement preparedStatementToDeletePolicyGroups = null;
            String queryToDeletePolicyMappings = String.format("DELETE FROM %s WHERE POLICY_GRP_ID=?", POLICY_GROUP_TABLE_NAME);
            preparedStatementToDeletePolicyGroups = connection.prepareStatement(queryToDeletePolicyMappings);

            for (Integer id : policyGroupIdsToBeDeleted) {
                preparedStatementToDeletePolicyGroups.setInt(1, id);
                preparedStatementToDeletePolicyGroups.addBatch();
            }

            preparedStatementToDeletePolicyGroups.executeBatch();

        }finally {

        }

    }

    private String saveRegistryArtifact(App app) throws AppManagementException, RegistryException {
        String appId = null;
        if (AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())) {
            appId = saveWebAppRegistryArtifact((WebApp) app);
        }
        return appId;
    }

    private String saveWebAppRegistryArtifact(WebApp webApp) throws RegistryException, AppManagementException {

        String artifactId = null;

        GenericArtifactManager artifactManager = getArtifactManager(registry, AppMConstants.WEBAPP_ASSET_TYPE);

        GenericArtifact appArtifact = buildWebAppRegistryArtifact(artifactManager, webApp);
        artifactManager.addGenericArtifact(appArtifact);

        artifactId = appArtifact.getId();

        // Set the life cycle for the persisted artifact
        GenericArtifact persistedArtifact = artifactManager.getGenericArtifact(artifactId);
        persistedArtifact.invokeAction(AppMConstants.LifecycleActions.CREATE, AppMConstants.WEBAPP_LIFE_CYCLE);

        // Apply tags
        String artifactPath = GovernanceUtils.getArtifactPath(registry, artifactId);
        if (webApp.getTags() != null) {
            for (String tag : webApp.getTags()) {
                registry.applyTag(artifactPath, tag);
            }
        }

        // Set resources permissions based on app visibility.
        if (webApp.getAppVisibility() != null) {
            AppManagerUtil.setResourcePermissions(webApp.getId().getProviderName(), AppMConstants.API_RESTRICTED_VISIBILITY, webApp.getAppVisibility(), artifactPath);
        }

        // Add registry associations.
        String providerPath = AppManagerUtil.getAPIProviderPath(webApp.getId());
        registry.addAssociation(providerPath, artifactPath, AppMConstants.PROVIDER_ASSOCIATION);

        return artifactId;
    }

    public static GenericArtifactManager getArtifactManager(Registry registry, String key) throws RegistryException {

        GenericArtifactManager artifactManager = null;

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        if (GovernanceUtils.findGovernanceArtifactConfiguration(key, registry) != null) {
            artifactManager = new GenericArtifactManager(registry, key);
        }

        return artifactManager;
    }

    private GenericArtifact buildWebAppRegistryArtifact(GenericArtifactManager artifactManager, WebApp webApp) throws GovernanceException {

        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName(webApp.getId().getApiName()));

        artifact.setAttribute(AppMConstants.API_OVERVIEW_NAME, webApp.getId().getApiName());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_VERSION, webApp.getId().getVersion());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_CONTEXT, webApp.getContext());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_DISPLAY_NAME, webApp.getDisplayName());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_PROVIDER, AppManagerUtil.replaceEmailDomainBack(webApp.getId().getProviderName()));
        artifact.setAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION, webApp.getDescription());
        artifact.setAttribute(AppMConstants.APP_OVERVIEW_TREAT_AS_A_SITE, webApp.getTreatAsASite());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_URL, webApp.getUrl()); //
        artifact.setAttribute(AppMConstants.APP_IMAGES_THUMBNAIL, ""); //webApp.getThumbnailUrl()
        artifact.setAttribute(AppMConstants.APP_IMAGES_BANNER, "");
        artifact.setAttribute(AppMConstants.API_OVERVIEW_LOGOUT_URL, webApp.getLogoutURL());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER, webApp.getBusinessOwner());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER_EMAIL, webApp.getBusinessOwnerEmail());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_VISIBILITY, StringUtils.join(webApp.getAppVisibility()));
        artifact.setAttribute(AppMConstants.API_OVERVIEW_VISIBLE_TENANTS, webApp.getVisibleTenants());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_TRANSPORTS, webApp.getTransports());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_TIER, "Unlimited");
        artifact.setAttribute(AppMConstants.APP_TRACKING_CODE, webApp.getTrackingCode());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_CREATED_TIME, webApp.getCreatedTime());
        artifact.setAttribute(AppMConstants.API_OVERVIEW_ALLOW_ANONYMOUS, Boolean.toString(webApp.getAllowAnonymous()));
        artifact.setAttribute(AppMConstants.API_OVERVIEW_SKIP_GATEWAY, Boolean.toString(webApp.getSkipGateway()));
        artifact.setAttribute(AppMConstants.APP_OVERVIEW_ACS_URL, webApp.getAcsURL());

        // Add policy groups
        if(webApp.getAccessPolicyGroups() != null){
            int[] policyGroupIds = new int[webApp.getAccessPolicyGroups().size()];

            for(int i = 0; i < webApp.getAccessPolicyGroups().size(); i++){
                policyGroupIds[i] = webApp.getAccessPolicyGroups().get(i).getPolicyGroupId();
            }

            artifact.setAttribute("uriTemplate_policyGroupIds", policyGroupIds.toString());

        }


        // Add URI Template attributes
        int counter = 0;
        for(URITemplate uriTemplate : webApp.getUriTemplates()){
            artifact.setAttribute("uriTemplate_urlPattern" + counter, uriTemplate.getUriTemplate());
            artifact.setAttribute("uriTemplate_httpVerb" + counter, uriTemplate.getHTTPVerb());
            artifact.setAttribute("uriTemplate_policyGroupId" + counter, String.valueOf(getPolicyGroupId(webApp.getAccessPolicyGroups(), uriTemplate.getPolicyGroupName())));

            counter++;
        }

        return artifact;
    }

    private int getPolicyGroupId(List<EntitlementPolicyGroup> accessPolicyGroups, String policyGroupName) {

        for(EntitlementPolicyGroup policyGroup : accessPolicyGroups){
            if(policyGroupName.equals(policyGroup.getPolicyGroupName())){
                return policyGroup.getPolicyGroupId();
            }
        }

        return -1;
    }

    private int persistWebAppToDatabase(WebApp webApp, Connection connection) throws SQLException, AppManagementException {

        String query = "INSERT INTO APM_APP(APP_PROVIDER, TENANT_ID, APP_NAME, APP_VERSION, CONTEXT, TRACKING_CODE, " +
                            "UUID, SAML2_SSO_ISSUER, LOG_OUT_URL, APP_ALLOW_ANONYMOUS, APP_ENDPOINT, TREAT_AS_SITE) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            Environment gatewayEnvironment = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                                                getAPIManagerConfiguration().getApiGatewayEnvironments().get(0);

            String gatewayUrl = gatewayEnvironment.getApiGatewayEndpoint().split(",")[0];

            String logoutURL = webApp.getLogoutURL();
            if (logoutURL != null && !"".equals(logoutURL.trim())) {
                logoutURL = gatewayUrl.concat(webApp.getContext()).concat("/" + webApp.getId().getVersion()).concat(logoutURL);
            }

            preparedStatement = connection.prepareStatement(query, new String[]{"APP_ID"});
            preparedStatement.setString(1, AppManagerUtil.replaceEmailDomainBack(webApp.getId().getProviderName()));
            preparedStatement.setInt(2, getTenantIdOfCurrentUser());
            preparedStatement.setString(3, webApp.getId().getApiName());
            preparedStatement.setString(4, webApp.getId().getVersion());
            preparedStatement.setString(5, webApp.getContext());
            preparedStatement.setString(6, webApp.getTrackingCode());
            preparedStatement.setString(7, webApp.getUUID());
            preparedStatement.setString(8, webApp.getSaml2SsoIssuer());
            preparedStatement.setString(9, logoutURL);
            preparedStatement.setBoolean(10, webApp.getAllowAnonymous());
            preparedStatement.setString(11, webApp.getUrl());
            preparedStatement.setBoolean(12, Boolean.parseBoolean(webApp.getTreatAsASite()));

            preparedStatement.execute();

            generatedKeys = preparedStatement.getGeneratedKeys();
            int webAppId = -1;
            if (generatedKeys.next()) {
                webAppId = generatedKeys.getInt(1);
            }

            //Set default versioning details
            persistDefaultVersionDetails(webApp, connection);

            return webAppId;
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, generatedKeys);
        }

    }

    private void persistJavaPolicyMappings(String javaPolicies, int webAppDatabaseId, Connection connection) throws SQLException {

        JSONArray javaPolicyIds = (JSONArray) JSONValue.parse(javaPolicies);

        PreparedStatement preparedStatement = null;
        String query = " INSERT INTO APM_APP_JAVA_POLICY_MAPPING(APP_ID, JAVA_POLICY_ID) VALUES(?,?) ";

        try {
            preparedStatement = connection.prepareStatement(query);

            for (Object policyId : javaPolicyIds) {
                preparedStatement.setInt(1, webAppDatabaseId);
                preparedStatement.setInt(2, Integer.parseInt(policyId.toString()));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();

        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }

    private void persistPolicyGroups(List<EntitlementPolicyGroup> policyGroups, Connection connection) throws SQLException {

        for (EntitlementPolicyGroup policyGroup : policyGroups) {

            // Don't try to use batch insert for the policy groups since we need the auto-generated IDs.
            persistPolicyGroup(policyGroup, connection);
        }

        persistEntitlementPolicyMappings(policyGroups, connection);

    }

    private void persistPolicyGroup(EntitlementPolicyGroup policyGroup, Connection connection) throws SQLException {

        String query = String.format("INSERT INTO %s(NAME,THROTTLING_TIER,USER_ROLES,URL_ALLOW_ANONYMOUS,DESCRIPTION) VALUES(?,?,?,?,?) ", POLICY_GROUP_TABLE_NAME);

        PreparedStatement preparedStatement = null;

        ResultSet resultSet = null;

        try {

            preparedStatement = connection.prepareStatement(query, new String[]{"POLICY_GRP_ID"});
            preparedStatement.setString(1, policyGroup.getPolicyGroupName());
            preparedStatement.setString(2, policyGroup.getThrottlingTier());
            preparedStatement.setString(3, policyGroup.getUserRoles());
            preparedStatement.setBoolean(4, policyGroup.isAllowAnonymous());
            preparedStatement.setString(5, policyGroup.getPolicyDescription());
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();

            int generatedPolicyGroupId = 0;
            if (resultSet.next()) {
                generatedPolicyGroupId = Integer.parseInt(resultSet.getString(1));
                policyGroup.setPolicyGroupId(generatedPolicyGroupId);
            }

        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, resultSet);
        }
    }


    private void associatePolicyGroupsWithWebApp(List<EntitlementPolicyGroup> policyGroups, int appDatabaseId, Connection connection) throws SQLException {

        PreparedStatement preparedStatementToPersistPolicyMappings = null;
        String queryToPersistPolicyMappings = "INSERT INTO APM_POLICY_GROUP_MAPPING(APP_ID, POLICY_GRP_ID) VALUES(?,?)";

        try{
            preparedStatementToPersistPolicyMappings = connection.prepareStatement(queryToPersistPolicyMappings);

            for (EntitlementPolicyGroup policyGroup : policyGroups) {

                // Add mapping query to the batch
                preparedStatementToPersistPolicyMappings.setInt(1, appDatabaseId);
                preparedStatementToPersistPolicyMappings.setInt(2, policyGroup.getPolicyGroupId());
                preparedStatementToPersistPolicyMappings.addBatch();
            }

            preparedStatementToPersistPolicyMappings.executeBatch();
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatementToPersistPolicyMappings, null, null);
        }
    }

    private void disassociatePolicyGroupsWithWebApp(List<Integer> policyGroupsIds, int appDatabaseId, Connection connection) throws SQLException {

        PreparedStatement preparedStatementToDeletePolicyMappings = null;
        String queryToDeletePolicyMappings = "DELETE FROM APM_POLICY_GROUP_MAPPING WHERE APP_ID=? AND POLICY_GRP_ID=?";

        try{
            preparedStatementToDeletePolicyMappings = connection.prepareStatement(queryToDeletePolicyMappings);

            for (Integer policyGroupsId : policyGroupsIds) {

                // Add mapping query to the batch
                preparedStatementToDeletePolicyMappings.setInt(1, appDatabaseId);
                preparedStatementToDeletePolicyMappings.setInt(2, policyGroupsId);
                preparedStatementToDeletePolicyMappings.addBatch();
            }

            preparedStatementToDeletePolicyMappings.executeBatch();
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatementToDeletePolicyMappings, null, null);
        }
    }

    private void persistEntitlementPolicyMappings(List<EntitlementPolicyGroup> policyGroups, Connection connection) throws SQLException {

		String query = String.format("INSERT INTO %s(POLICY_GRP_ID, POLICY_PARTIAL_ID) VALUES(?,?) ", POLICY_GROUP_PARTIAL_MAPPING_TABLE_NAME);
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(query);

            for(EntitlementPolicyGroup policyGroup : policyGroups){

                if(policyGroup.getPolicyPartials() != null){
                    preparedStatement.setInt(1, policyGroup.getPolicyGroupId());
                    preparedStatement.setInt(2, policyGroup.getFirstEntitlementPolicyId());
                    preparedStatement.addBatch();
                }
            }

            preparedStatement.executeBatch();

        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }

    private void updateEntitlementPolicyMappings(List<EntitlementPolicyGroup> policyGroups, Connection connection) throws SQLException {

        String query = String.format("UPDATE %s SET POLICY_PARTIAL_ID=? WHERE POLICY_GRP_ID=? ", POLICY_GROUP_PARTIAL_MAPPING_TABLE_NAME);
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(query);

            for(EntitlementPolicyGroup policyGroup : policyGroups){

                if(policyGroup.getPolicyPartials() != null){
                    preparedStatement.setInt(1, policyGroup.getFirstEntitlementPolicyId());
                    preparedStatement.setInt(2, policyGroup.getPolicyGroupId());
                    preparedStatement.addBatch();
                }
            }

            preparedStatement.executeBatch();
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }

    private void deleteUnlinkedEntitlementPolicyMappings(List<EntitlementPolicyGroup> policyGroups, Connection connection) throws SQLException {

        String query = String.format("DELETE FROM %s WHERE POLICY_GRP_ID=? ", POLICY_GROUP_PARTIAL_MAPPING_TABLE_NAME);
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(query);

            for(EntitlementPolicyGroup policyGroup : policyGroups){

                if(policyGroup.getPolicyPartials() != null){
                    preparedStatement.setInt(1, policyGroup.getPolicyGroupId());
                    preparedStatement.addBatch();
                }
            }

            preparedStatement.executeBatch();
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }

    private void persistLifeCycleEvent(int webAppDatabaseId, APIStatus oldStatus, APIStatus newStatus, Connection conn)
            throws SQLException {

        PreparedStatement preparedStatement = null;

        String query = "INSERT INTO APM_APP_LC_EVENT (APP_ID, PREVIOUS_STATE, NEW_STATE, USER_ID, TENANT_ID, EVENT_DATE)"
                            + " VALUES (?,?,?,?,?,?)";

        try {

            preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, webAppDatabaseId);

            if (oldStatus != null) {
                preparedStatement.setString(2, oldStatus.getStatus());
            } else {
                preparedStatement.setNull(2, Types.VARCHAR);
            }

            preparedStatement.setString(3, newStatus.getStatus());
            preparedStatement.setString(4, getUsernameOfCurrentUser());
            preparedStatement.setInt(5, getTenantIdOfCurrentUser());
            preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            preparedStatement.executeUpdate();

        } finally {
             APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }

    private void persistDefaultVersionDetails(WebApp webApp, Connection connection) throws SQLException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int recordCount = 0;

        String sqlQuery = "SELECT COUNT(*) AS ROWCOUNT FROM APM_APP_DEFAULT_VERSION WHERE APP_NAME=? AND APP_PROVIDER=? AND " +
                        "TENANT_ID=? ";

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

            preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString(1, webApp.getId().getApiName());
            preparedStatement.setString(2, webApp.getId().getProviderName());
            preparedStatement.setInt(3, tenantId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                recordCount = resultSet.getInt("ROWCOUNT");
            }

            // If there are no 'default version' records for this app identity, then set this app as the default version.
            if (recordCount == 0 ) {
                setAsDefaultVersion(webApp, false, connection);
            } else if(webApp.isDefaultVersion()){
                // If there is an existing record, update that record to make this app the 'default version'.
               setAsDefaultVersion(webApp, true, connection);
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, resultSet);
        }
    }

    private void setAsDefaultVersion(WebApp app, boolean update, Connection connection) throws SQLException {

        if(update){
            updateDefaultVersion(app, connection);
        }else{
            addDefaultVersion(app, connection);
        }
    }

    private void updateDefaultVersion(WebApp app, Connection connection) throws SQLException {

        PreparedStatement preparedStatement = null;
        String query = "UPDATE APM_APP_DEFAULT_VERSION SET DEFAULT_APP_VERSION=?, PUBLISHED_DEFAULT_APP_VERSION=? WHERE APP_NAME=? AND APP_PROVIDER=? AND TENANT_ID=? ";
        try {

            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, app.getId().getVersion());

            String publishedDefaultAppVersion = null;
            if(APIStatus.PUBLISHED.equals(app.getStatus())){
                publishedDefaultAppVersion = app.getId().getVersion();
            }
            preparedStatement.setString(2, publishedDefaultAppVersion);

            preparedStatement.setString(3, app.getId().getApiName());
            preparedStatement.setString(4, app.getId().getProviderName());

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
            preparedStatement.setInt(5, tenantId);

            preparedStatement.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }

    private void addDefaultVersion(WebApp app, Connection connection) throws SQLException {

        PreparedStatement preparedStatement = null;
        String query = "INSERT INTO APM_APP_DEFAULT_VERSION  (APP_NAME, APP_PROVIDER, DEFAULT_APP_VERSION, " +
                            "PUBLISHED_DEFAULT_APP_VERSION, TENANT_ID) VALUES (?,?,?,?,?)";

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, app.getId().getApiName());
            preparedStatement.setString(2, app.getId().getProviderName());
            preparedStatement.setString(3, app.getId().getVersion());

            if (app.getStatus() == APIStatus.PUBLISHED) {
                preparedStatement.setString(4, app.getId().getVersion());
            } else {
                preparedStatement.setString(4, null);
            }

            preparedStatement.setInt(5, tenantId);

            preparedStatement.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }

    private void persistURLTemplates(Set<URITemplate> uriTemplates, List<EntitlementPolicyGroup> policyGroups, int webAppDatabaseId, Connection connection) throws SQLException {

        PreparedStatement preparedStatement = null;

        try {
            String query = "INSERT INTO APM_APP_URL_MAPPING (APP_ID, HTTP_METHOD, URL_PATTERN, POLICY_GRP_ID) VALUES (?,?,?,?)";
            preparedStatement = connection.prepareStatement(query);

            for(URITemplate uriTemplate : uriTemplates){

                preparedStatement.setInt(1, webAppDatabaseId);
                preparedStatement.setString(2, uriTemplate.getHTTPVerb());
                preparedStatement.setString(3, uriTemplate.getUriTemplate());

                // Set the database ID of the relevant policy group.
                // The URL templates to be persisted, maintain the relationship to the policy groups using the indexes of the policy groups list.
                preparedStatement.setInt(4, getPolicyGroupId(policyGroups, uriTemplate.getPolicyGroupName()));

                preparedStatement.executeUpdate();
            }
        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }

    private void createSSOProvider(WebApp app) {

        SSOProvider ssoProvider = app.getSsoProviderDetails();

        if(ssoProvider == null){
            ssoProvider = getDefaultSSOProvider();
            app.setSsoProviderDetails(ssoProvider);
        }

        // Build the issuer name.
        APIIdentifier appIdentifier = app.getId();
        String tenantDomain = getTenantDomainOfCurrentUser();

        String issuerName = null;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            issuerName = appIdentifier.getApiName() + "-" + appIdentifier.getVersion();
        } else {
            issuerName = appIdentifier.getApiName() + "-" + tenantDomain + "-" + appIdentifier.getVersion();
        }

        ssoProvider.setIssuerName(issuerName);

        // Set the logout URL
        if(!StringUtils.isNotEmpty(app.getLogoutURL())){
            ssoProvider.setLogoutUrl(app.getLogoutURL());
        }

        SSOConfiguratorUtil ssoConfiguratorUtil = new SSOConfiguratorUtil();
        ssoConfiguratorUtil.createSSOProvider(app, false);
    }

    private SSOProvider getDefaultSSOProvider() {

        SSOProvider ssoProvider = new SSOProvider();

        SSOEnvironment defaultSSOEnv = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                                        getAPIManagerConfiguration().getSsoEnvironments().get(0);

        ssoProvider.setProviderName(defaultSSOEnv.getName());
        ssoProvider.setProviderVersion(defaultSSOEnv.getVersion());

        ssoProvider.setClaims(new String[0]);

        return ssoProvider;

    }

    private int getTenantIdOfCurrentUser(){
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private String getUsernameOfCurrentUser(){
        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    private String getTenantDomainOfCurrentUser() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    private Connection getRDBMSConnectionWithoutAutoCommit() throws SQLException {
        return getRDBMSConnection(false);
    }

    private Connection getRDBMSConnectionWithAutoCommit() throws SQLException {
        return getRDBMSConnection(true);
    }

    private Connection getRDBMSConnection(boolean setAutoCommit) throws SQLException {

        Connection connection = APIMgtDBUtil.getConnection();
        connection.setAutoCommit(setAutoCommit);

        return connection;
    }

    private void rollbackTransactions(App app, Registry registry, Connection connection) {

        try {
            if(registry != null){
                registry.rollbackTransaction();
            }

            if(connection != null){
                connection.rollback();
            }
        } catch (RegistryException e) {
            // No need to throw this exception.
            log.error(String.format("Can't rollback registry persist operation for the app '%s:%s'", app.getType(), app.getDisplayName()));
        } catch (SQLException e) {
            // No need to throw this exception.
            log.error(String.format("Can't rollback RDBMS persist operation for the app '%s:%s'", app.getType(), app.getDisplayName()));
        }
    }

    private void handleException(String msg, Exception e) throws AppManagementException {
        log.error(msg, e);
        throw new AppManagementException(msg, e);
    }

}
