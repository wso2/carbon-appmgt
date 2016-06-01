package org.wso2.carbon.appmgt.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.impl.dto.Environment;
import org.wso2.carbon.appmgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.appmgt.impl.idp.sso.SSOConfiguratorUtil;
import org.wso2.carbon.appmgt.impl.idp.sso.model.SSOEnvironment;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.impl.utils.AppMgtDataSourceProvider;
import org.wso2.carbon.appmgt.impl.workflow.*;
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
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;

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
    public String createNewVersion(App app) throws AppManagementException {

        if(AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())){
            WebApp newVersion = createNewWebAppVersion((WebApp)app);
            return newVersion.getUUID();
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
        }else if(result.isEmpty()) {
            return null;
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

	@Override
    public int addSubscription(String subscriberName, WebApp webApp, String applicationName) throws AppManagementException {
        Connection connection = null;
        int subscriptionId = -1;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            connection = getRDBMSConnectionWithoutAutoCommit();
            //Check for subscriber existence
            Subscriber subscriber = getSubscriber(connection, subscriberName);
            int applicationId = -1;
            int subscriberId = -1;
            if (subscriber == null) {
                subscriber = new Subscriber(subscriberName);
                subscriber.setSubscribedDate(new Date());
                subscriber.setEmail("");
                subscriber.setTenantId(tenantId);
                subscriberId = addSubscriber(connection, subscriber);

                subscriber.setId(subscriberId);
                // Add default application
                Application defaultApp = new Application(applicationName, subscriber);
                defaultApp.setTier(AppMConstants.UNLIMITED_TIER);
                applicationId = addApplication(connection, defaultApp, subscriber);
            }else{
                applicationId = getApplicationId(connection, AppMConstants.DEFAULT_APPLICATION_NAME, subscriber);
            }
            APIIdentifier appIdentifier = webApp.getId();

            /* Tenant based validation for subscription*/
            String userTenantDomain = MultitenantUtils.getTenantDomain(subscriberName);
            String appProviderTenantDomain = MultitenantUtils.getTenantDomain(
                    AppManagerUtil.replaceEmailDomainBack(appIdentifier.getProviderName()));
            boolean subscriptionAllowed = false;
            if (!userTenantDomain.equals(appProviderTenantDomain)) {
                String subscriptionAvailability = webApp.getSubscriptionAvailability();
                if (AppMConstants.SUBSCRIPTION_TO_ALL_TENANTS.equals(subscriptionAvailability)) {
                    subscriptionAllowed = true;
                } else if (AppMConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS.equals(subscriptionAvailability)) {
                    String subscriptionAllowedTenants = webApp.getSubscriptionAvailableTenants();
                    String allowedTenants[] = null;
                    if (subscriptionAllowedTenants != null) {
                        allowedTenants = subscriptionAllowedTenants.split(",");
                        if (allowedTenants != null) {
                            for (String tenant : allowedTenants) {
                                if (tenant != null && userTenantDomain.equals(tenant.trim())) {
                                    subscriptionAllowed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                subscriptionAllowed = true;
            }

            if (!subscriptionAllowed) {
                throw new AppManagementException("Subscription is not allowed for " + userTenantDomain);
            }
            subscriptionId =
                    persistSubscription(connection, webApp, applicationId, Subscription.SUBSCRIPTION_TYPE_INDIVIDUAL, null);
        } catch (SQLException e) {
            handleException("Error occurred in obtaining database connection.", e);
        }
        return subscriptionId;
    }

    // ------------------- END : Repository API implementation methods. ----------------------------------

    private AppFactory getAppFactory(String appType) {
        if(AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)){
            return new WebAppFactory();
        }else if(AppMConstants.MOBILE_ASSET_TYPE.equals(appType)){
            return new MobileAppFactory();
        }else{
            return null;
        }
    }

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

        String query = "SELECT GRP.*,PARTIAL_MAPPING.POLICY_PARTIAL_ID " +
                                        "FROM " +
                                        "APM_POLICY_GROUP GRP " +
                                        "LEFT JOIN APM_POLICY_GRP_PARTIAL_MAPPING PARTIAL_MAPPING " +
                                        "ON GRP.POLICY_GRP_ID=PARTIAL_MAPPING.POLICY_GRP_ID, " +
                                        "APM_POLICY_GROUP_MAPPING MAPPING " +
                                        "WHERE " +
                                        "MAPPING.POLICY_GRP_ID=GRP.POLICY_GRP_ID " +
                                        "AND MAPPING.APP_ID=? " +
                                        "ORDER BY GRP.POLICY_GRP_ID";

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

    private String persistWebApp(WebApp webApp) throws AppManagementException {

        Connection connection = null;

        try {
            connection = getRDBMSConnectionWithoutAutoCommit();
        } catch (SQLException e) {
            handleException("Can't get the database connection.", e);
        }

        try {

            webApp.setCreatedTime(String.valueOf(new Date().getTime()));

            // Persist master data first.
            persistPolicyGroups(webApp.getAccessPolicyGroups(), connection);

            // Add the registry artifact
            registry.beginTransaction();
            String uuid = saveRegistryArtifact(webApp);
            webApp.setUUID(uuid);
            registry.commitTransaction();

            // Persist web app data to the database (RDBMS)
            int webAppDatabaseId = persistWebAppToDatabase(webApp, connection);

            associatePolicyGroupsWithWebApp(webApp.getAccessPolicyGroups(), webAppDatabaseId, connection);

            persistURLTemplates(new ArrayList<URITemplate>(webApp.getUriTemplates()), webApp.getAccessPolicyGroups(), webAppDatabaseId, connection);

            if(!StringUtils.isEmpty(webApp.getJavaPolicies())){
                persistJavaPolicyMappings(webApp.getJavaPolicies(), webAppDatabaseId, connection);
            }

            persistLifeCycleEvent(webAppDatabaseId, null, APIStatus.CREATED, connection);

            createSSOProvider(webApp);

            // Commit JDBC and Registry transactions.
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

    private WebApp createNewWebAppVersion(WebApp targetApp) throws AppManagementException {

        // Get the attributes of the source.
        WebApp sourceApp = (WebApp) getApp(targetApp.getType(), targetApp.getUUID());

        // Clear the ID.
        sourceApp.setUUID(null);

        // Set New Version.

        sourceApp.setOriginVersion(targetApp.getId().getVersion());
        sourceApp.setVersion(targetApp.getId().getVersion());
        sourceApp.setDefaultVersion(targetApp.isDefaultVersion());

        // Clear URL Template database IDs.
        for(URITemplate template : sourceApp.getUriTemplates()){
            template.setId(-1);

            String policyGroupName = getPolicyGroupName(sourceApp.getAccessPolicyGroups(), template.getPolicyGroupId());
            template.setPolicyGroupName(policyGroupName);

            template.setPolicyGroupId(-1);
        }

        // Clear Policy Group database IDs.
        for(EntitlementPolicyGroup policyGroup : sourceApp.getAccessPolicyGroups()){
            policyGroup.setPolicyGroupId(-1);
        }

        // Set the other properties accordingly.
        sourceApp.setDisplayName(targetApp.getDisplayName());
        sourceApp.setCreatedTime(String.valueOf(new Date().getTime()));

        saveApp(sourceApp);

        return sourceApp;
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

            // Set the Status from the existing app in the repository.
            // TODO : Only a thin version should be fetched from the database.
            WebApp existingApp = (WebApp) getApp(AppMConstants.WEBAPP_ASSET_TYPE, webApp.getUUID());
            webApp.setStatus(existingApp.getStatus());

            webApp.setCreatedTime(String.valueOf(new Date().getTime()));

            // Add and/or update policy groups.
            addAndUpdatePolicyGroups(webApp, webAppDatabaseId, connection);

            // Add / Update / Delete URL templates.
            addUpdateDeleteURLTemplates(webApp, webAppDatabaseId, connection);

            // Delete the existing policy groups in the repository which are not in the updating web app.
            // URI templates should be passed too, since the association between templates and policy groups should be checked.
            deletePolicyGroupsNotIn(webApp.getAccessPolicyGroups(), webApp.getUriTemplates(),webAppDatabaseId, connection);

            updateRegistryArtifact(webApp);

            //Set default versioning details
            persistDefaultVersionDetails(webApp, connection);

            connection.commit();
        } catch (SQLException e) {
            rollbackTransactions(webApp, registry, connection);
            handleException(String.format("Error while updating web app '%s'", webApp.getUUID()), e);
        } catch (RegistryException e) {
            rollbackTransactions(webApp, registry, connection);
            handleException(String.format("Error while updating web app '%s'", webApp.getUUID()), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, connection, null);
        }
    }

    private void updateRegistryArtifact(App app) throws RegistryException {

        if(AppMConstants.WEBAPP_ASSET_TYPE.equalsIgnoreCase(app.getType())){
            updateWebAppRegistryArtifact((WebApp) app);
        }
    }

    private void updateWebAppRegistryArtifact(WebApp webApp) throws RegistryException {

        GenericArtifactManager artifactManager = getArtifactManager(registry, AppMConstants.WEBAPP_ASSET_TYPE);

        GenericArtifact updatedWebAppArtifact = buildWebAppRegistryArtifact(artifactManager, webApp);
        updatedWebAppArtifact.setId(webApp.getUUID());
        artifactManager.updateGenericArtifact(updatedWebAppArtifact);
    }

    private void addUpdateDeleteURLTemplates(WebApp webApp, int webAppDatabaseId, Connection connection) throws SQLException {

        List<URITemplate> urlTemplatesToBeUpdated = new ArrayList<URITemplate>();
        List<URITemplate> urlTemplatesToBeAdded = new ArrayList<URITemplate>();

        for(URITemplate template : webApp.getUriTemplates()){
            if(template.getId() > 0){
                urlTemplatesToBeUpdated.add(template);
            }else{
                urlTemplatesToBeAdded.add(template);
            }
        }

        persistURLTemplates(urlTemplatesToBeAdded, webApp.getAccessPolicyGroups(), webAppDatabaseId, connection);
        updateURLTemplates(urlTemplatesToBeUpdated, webApp.getAccessPolicyGroups(), connection);
        deleteURLTemplatesNotIn(webApp.getUriTemplates(), webAppDatabaseId, connection);
    }

    private void deleteURLTemplatesNotIn(Set<URITemplate> uriTemplates, int webAppDatabaseId, Connection connection) throws SQLException {

        String queryTemplate = "DELETE FROM APM_APP_URL_MAPPING WHERE APP_ID=%d AND URL_MAPPING_ID NOT IN (%s)";
        PreparedStatement preparedStatement = null;

        try{

            StringBuilder templateIdsBuilder = new StringBuilder();
            for(URITemplate uriTemplate : uriTemplates){
                templateIdsBuilder.append(uriTemplate.getId()).append(",");
            }
            String templateIds = templateIdsBuilder.toString();

            if(templateIds.endsWith(",")){
                templateIds = templateIds.substring(0, templateIds.length() - 1);
            }

            String query = String.format(queryTemplate, webAppDatabaseId, templateIds);

            preparedStatement = connection.prepareStatement(query);

            preparedStatement.executeUpdate();

        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }

    private void updateURLTemplates(List<URITemplate> urlTemplatesToBeUpdated, List<EntitlementPolicyGroup> accessPolicyGroups, Connection connection) throws SQLException {

        String query = "UPDATE APM_APP_URL_MAPPING SET URL_PATTERN=?, HTTP_METHOD=?, POLICY_GRP_ID=? WHERE URL_MAPPING_ID=?";
        PreparedStatement preparedStatement = null;

        try{
            preparedStatement = connection.prepareStatement(query);

            for(URITemplate urlTemplate : urlTemplatesToBeUpdated){
                preparedStatement.setString(1, urlTemplate.getUriTemplate());
                preparedStatement.setString(2, urlTemplate.getHTTPVerb());

                int policyGroupId = urlTemplate.getPolicyGroupId();
                if(urlTemplate.getPolicyGroupId() <= 0){
                    policyGroupId = getPolicyGroupId(accessPolicyGroups, urlTemplate.getPolicyGroupName());
                    urlTemplate.setPolicyGroupId(policyGroupId);
                }

                preparedStatement.setInt(3, policyGroupId);
                preparedStatement.setInt(4, urlTemplate.getId());

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();

        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
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

    private void addAndUpdatePolicyGroups(WebApp webApp, int webAppDatabaseId, Connection connection) throws SQLException {

        List<EntitlementPolicyGroup> groupsToBeAdded = new ArrayList<>();
        List<EntitlementPolicyGroup> groupsToBeUpdated = new ArrayList<>();

        for(EntitlementPolicyGroup policyGroup : webApp.getAccessPolicyGroups()){
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
	}

    private void deletePolicyGroupsNotIn(List<EntitlementPolicyGroup> groupsToBeRetained, Set<URITemplate> uriTemplates, int webAppDatabaseId, Connection connection) throws SQLException {

        // Get all the policy groups for the web app.

        String queryToGetPolicyGroupsForApp = "SELECT POLICY_GRP_ID FROM APM_POLICY_GROUP_MAPPING WHERE APP_ID=?";
        PreparedStatement preparedStatementToGetPolicyGroupsForApp = null;
        ResultSet policyGroupsResultSet = null;
        PreparedStatement preparedStatementToDeletePolicyGroups = null;

        try{
            preparedStatementToGetPolicyGroupsForApp = connection.prepareStatement(queryToGetPolicyGroupsForApp);
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

            List<Integer> policyGroupIdsToBeDeleted = new ArrayList<Integer>();

            // Omit the policy groups which has associations with URI templates.
            List<Integer> candidatePolicyGroupIdsToBeDeleted = ListUtils.subtract(policyGroupIdsForApp, retainedPolicyGroupIds);

            for(final Integer id : candidatePolicyGroupIdsToBeDeleted){

                if(!CollectionUtils.exists(uriTemplates, new Predicate() {
                    @Override
                    public boolean evaluate(Object o) {
                        URITemplate template = (URITemplate) o;
                        return template.getPolicyGroupId() == id;
                    }
                })){
                    policyGroupIdsToBeDeleted.add(id);
                }

            }

            disassociatePolicyGroupsFromWebApp(policyGroupIdsToBeDeleted, webAppDatabaseId, connection);

            String queryToDeletePolicyMappings = String.format("DELETE FROM %s WHERE POLICY_GRP_ID=?", POLICY_GROUP_TABLE_NAME);
            preparedStatementToDeletePolicyGroups = connection.prepareStatement(queryToDeletePolicyMappings);

            for (Integer id : policyGroupIdsToBeDeleted) {
                preparedStatementToDeletePolicyGroups.setInt(1, id);
                preparedStatementToDeletePolicyGroups.addBatch();
            }

            preparedStatementToDeletePolicyGroups.executeBatch();

        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatementToGetPolicyGroupsForApp, null, policyGroupsResultSet);
            APIMgtDBUtil.closeAllConnections(preparedStatementToDeletePolicyGroups, null, null);
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
        artifact.setAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_URL, webApp.getUrl());
        artifact.setAttribute(AppMConstants.APP_IMAGES_THUMBNAIL, webApp.getThumbnailUrl());
        artifact.setAttribute(AppMConstants.APP_IMAGES_BANNER, webApp.getBanner());
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
        artifact.setAttribute(AppMConstants.APP_OVERVIEW_MAKE_AS_DEFAULT_VERSION, String.valueOf(webApp.isDefaultVersion()));

        if(webApp.getOriginVersion() != null){
            artifact.setAttribute(AppMConstants.APP_OVERVIEW_OLD_VERSION, webApp.getOriginVersion());
        }

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

            int policyGroupId = uriTemplate.getPolicyGroupId();
            if(policyGroupId <= 0){
                policyGroupId = getPolicyGroupId(webApp.getAccessPolicyGroups(), uriTemplate.getPolicyGroupName());
            }

            artifact.setAttribute("uriTemplate_policyGroupId" + counter, String.valueOf(policyGroupId));

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

    private String getPolicyGroupName(List<EntitlementPolicyGroup> accessPolicyGroups, int policyGroupId) {

        for(EntitlementPolicyGroup policyGroup : accessPolicyGroups){
            if(policyGroupId == policyGroup.getPolicyGroupId()){
                return policyGroup.getPolicyGroupName();
            }
        }

        return null;
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

    private void disassociatePolicyGroupsFromWebApp(List<Integer> policyGroupIds, int appDatabaseId, Connection connection) throws SQLException {

        PreparedStatement preparedStatementToDeletePolicyMappings = null;
        String queryToDeletePolicyMappings = "DELETE FROM APM_POLICY_GROUP_MAPPING WHERE APP_ID=? AND POLICY_GRP_ID=?";

        try{
            preparedStatementToDeletePolicyMappings = connection.prepareStatement(queryToDeletePolicyMappings);

            for (Integer policyGroupId : policyGroupIds) {

                // Add mapping query to the batch
                preparedStatementToDeletePolicyMappings.setInt(1, appDatabaseId);
                preparedStatementToDeletePolicyMappings.setInt(2, policyGroupId);
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

                // If the policy group doesn't have entitlement policy, then delete the possible existing entitlement policy mappings for those policy groups.
                if(policyGroup.getPolicyPartials() == null){
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

    private void persistURLTemplates(List<URITemplate> uriTemplates, List<EntitlementPolicyGroup> policyGroups, int webAppDatabaseId, Connection connection) throws SQLException {

        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            String query = "INSERT INTO APM_APP_URL_MAPPING (APP_ID, HTTP_METHOD, URL_PATTERN, POLICY_GRP_ID) VALUES (?,?,?,?)";
            preparedStatement = connection.prepareStatement(query, new String[]{"URL_MAPPING_ID"});

            for(URITemplate uriTemplate : uriTemplates){

                preparedStatement.setInt(1, webAppDatabaseId);
                preparedStatement.setString(2, uriTemplate.getHTTPVerb());
                preparedStatement.setString(3, uriTemplate.getUriTemplate());

                // Set the database ID of the relevant policy group.
                // The URL templates to be persisted, maintain the relationship to the policy groups using the indexes of the policy groups list.
                int policyGroupId = uriTemplate.getPolicyGroupId();
                if(policyGroupId <= 0){
                    policyGroupId = getPolicyGroupId(policyGroups, uriTemplate.getPolicyGroupName());
                    uriTemplate.setPolicyGroupId(policyGroupId);
                }
                preparedStatement.setInt(4, policyGroupId);

                preparedStatement.executeUpdate();

                generatedKeys = preparedStatement.getGeneratedKeys();

                int generatedURLTemplateId = 0;
                if (generatedKeys.next()) {
                    generatedURLTemplateId = Integer.parseInt(generatedKeys.getString(1));
                    uriTemplate.setId(generatedURLTemplateId);
                }
            }

            preparedStatement.executeBatch();
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

    private int persistSubscription(Connection connection, WebApp webApp, int applicationId, String subscriptionType,
                                    String trustedIDPs)throws AppManagementException {

        int subscriptionId = -1;
        APIIdentifier appIdentifier = webApp.getId();
        if (APIStatus.PUBLISHED.equals(webApp.getStatus())) {

            Subscription subscription = getSubscription(connection, appIdentifier, applicationId, subscriptionType);
            //If subscription already exists, then update
            if (subscription != null) {
                subscriptionId = subscription.getSubscriptionId();
                if (Subscription.SUBSCRIPTION_TYPE_ENTERPRISE.equals(subscriptionType)) {
                    updateSubscription(connection, subscriptionId, subscriptionType, trustedIDPs, subscription.getSubscriptionStatus());
                } else if (Subscription.SUBSCRIPTION_TYPE_INDIVIDUAL.equals(subscriptionType)) {
                    updateSubscription(connection, subscriptionId, subscriptionType, trustedIDPs, AppMConstants.SubscriptionStatus.ON_HOLD);
                }
            }else{
                subscriptionId = addSubscription(connection, appIdentifier, subscriptionType,
                        applicationId, AppMConstants.SubscriptionStatus.ON_HOLD, trustedIDPs);
            }
        }
        return subscriptionId;
    }

    private int addSubscription(Connection connection, APIIdentifier appIdentifier, String subscriptionType, int applicationId,
                                String status, String trustedIdps) throws AppManagementException {

        ResultSet appIdResultSet = null;
        ResultSet subscriptionIdResultSet = null;
        PreparedStatement preparedStmtToGetApp = null;
        PreparedStatement preparedStmtToInsertSubscription = null;

        int subscriptionId = -1;
        int apiId = -1;

        try {
            String getAppIdQuery = "SELECT APP_ID FROM APM_APP API WHERE APP_PROVIDER = ? AND APP_NAME = ? AND APP_VERSION = ?";
            preparedStmtToGetApp = connection.prepareStatement(getAppIdQuery);
            preparedStmtToGetApp.setString(1, AppManagerUtil.replaceEmailDomainBack(appIdentifier.getProviderName()));
            preparedStmtToGetApp.setString(2, appIdentifier.getApiName());
            preparedStmtToGetApp.setString(3, appIdentifier.getVersion());
            appIdResultSet = preparedStmtToGetApp.executeQuery();
            if (appIdResultSet.next()) {
                apiId = appIdResultSet.getInt("APP_ID");
            }
            preparedStmtToGetApp.close();

            if (apiId == -1) {
                String msg = "Unable to retrieve the WebApp ID for webapp with name '" + appIdentifier.getApiName() +
                        "' version '"+appIdentifier.getVersion()+ "'";
                log.error(msg);
                throw new AppManagementException(msg);
            }

            // This query to update the APM_SUBSCRIPTION table
            String sqlQuery = "INSERT INTO APM_SUBSCRIPTION (TIER_ID,SUBSCRIPTION_TYPE, APP_ID, " +
                                "APPLICATION_ID,SUB_STATUS, TRUSTED_IDP, SUBSCRIPTION_TIME) " +
                                "VALUES (?,?,?,?,?,?,?)";

            // Adding data to the APM_SUBSCRIPTION table
            preparedStmtToInsertSubscription =
                    connection.prepareStatement(sqlQuery, new String[] {AppMConstants.SUBSCRIPTION_FIELD_SUBSCRIPTION_ID});
            if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                preparedStmtToInsertSubscription = connection.prepareStatement(sqlQuery, new String[] { "subscription_id" });
            }

            byte count = 0;
            preparedStmtToInsertSubscription.setString(++count, appIdentifier.getTier());
            preparedStmtToInsertSubscription.setString(++count, subscriptionType);
            preparedStmtToInsertSubscription.setInt(++count, apiId);
            preparedStmtToInsertSubscription.setInt(++count, applicationId);
            preparedStmtToInsertSubscription.setString(++count, status != null ? status : AppMConstants.SubscriptionStatus.UNBLOCKED);
            preparedStmtToInsertSubscription.setString(++count, trustedIdps);
            preparedStmtToInsertSubscription.setTimestamp(++count, new Timestamp(new java.util.Date().getTime()));

            preparedStmtToInsertSubscription.executeUpdate();
            subscriptionIdResultSet = preparedStmtToInsertSubscription.getGeneratedKeys();
            while (subscriptionIdResultSet.next()) {
                subscriptionId = Integer.valueOf(subscriptionIdResultSet.getString(1)).intValue();
            }

            // finally commit transaction
            connection.commit();

        } catch (SQLException e) {
            handleException("Failed to add subscriber data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStmtToGetApp, null, appIdResultSet);
            APIMgtDBUtil.closeAllConnections(preparedStmtToInsertSubscription, null, subscriptionIdResultSet);
        }
        return subscriptionId;
    }

    private void updateSubscription(Connection connection, int subscriptionId, String subscriptionType,
                                    String trustedIDPs, String subscriptionStatus) throws AppManagementException {

        PreparedStatement preparedStmtToUpdateSubscription = null;
        ResultSet resultSet = null;

        try{
            String queryToUpdateSubscription =
                    "UPDATE APM_SUBSCRIPTION " +
                            "SET SUBSCRIPTION_TYPE = ?, TRUSTED_IDP = ? , SUB_STATUS = ?" +
                            "WHERE SUBSCRIPTION_ID = ?";

            preparedStmtToUpdateSubscription = connection.prepareStatement(queryToUpdateSubscription);
            preparedStmtToUpdateSubscription.setString(1, subscriptionType);
            preparedStmtToUpdateSubscription.setString(2, trustedIDPs);
            preparedStmtToUpdateSubscription.setString(3, subscriptionStatus);
            preparedStmtToUpdateSubscription.setInt(4, subscriptionId);

            preparedStmtToUpdateSubscription.executeUpdate();
            connection.commit();
        }catch (SQLException e){
            handleException(String.format("Failed updating subscription with Id : %d", subscriptionId), e);
        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStmtToUpdateSubscription, connection, resultSet);
        }
    }

    private Subscription getSubscription(Connection connection, APIIdentifier appIdentifier, int applicationId,
                                         String subscriptionTyp) throws AppManagementException {
        PreparedStatement preparedStatement = null;
        ResultSet subscriptionsResultSet = null;
        Subscription subscription = null;

        try{
            String queryToGetSubscriptionId =
                    "SELECT SUBSCRIPTION_ID, SUB.APP_ID, APPLICATION_ID, SUBSCRIPTION_TYPE, SUB_STATUS, TRUSTED_IDP " +
                            "FROM APM_SUBSCRIPTION SUB, APM_APP APP " +
                            "WHERE SUB.APP_ID = APP.APP_ID AND APP.APP_PROVIDER = ? AND APP.APP_NAME = ? " +
                            "AND APP.APP_VERSION = ? AND SUB.APPLICATION_ID = ? AND SUB.SUBSCRIPTION_TYPE = ?";

            preparedStatement = connection.prepareStatement(queryToGetSubscriptionId);
            preparedStatement.setString(1, AppManagerUtil.replaceEmailDomainBack(appIdentifier.getProviderName()));
            preparedStatement.setString(2, appIdentifier.getApiName());
            preparedStatement.setString(3, appIdentifier.getVersion());
            preparedStatement.setInt(4, applicationId);
            preparedStatement.setString(5, subscriptionTyp);
            subscriptionsResultSet = preparedStatement.executeQuery();

            if (subscriptionsResultSet.next()) {
                subscription = new Subscription();
                subscription.setSubscriptionId(subscriptionsResultSet.getInt(AppMConstants.SUBSCRIPTION_FIELD_SUBSCRIPTION_ID));
                subscription.setWebAppId(subscriptionsResultSet.getInt(AppMConstants.SUBSCRIPTION_FIELD_APP_ID));
                subscription.setApplicationId(subscriptionsResultSet.getInt(AppMConstants.APPLICATION_ID));
                subscription.setSubscriptionType(subscriptionsResultSet.getString(AppMConstants.SUBSCRIPTION_FIELD_TYPE));
                subscription.setSubscriptionStatus(subscriptionsResultSet.getString(AppMConstants.SUBSCRIPTION_FIELD_SUB_STATUS));

                String trustedIdpsJson = subscriptionsResultSet.getString(AppMConstants.SUBSCRIPTION_FIELD_TRUSTED_IDP);
                Object decodedJson = null;
                if (trustedIdpsJson != null) {
                    decodedJson = JSONValue.parse(trustedIdpsJson);
                }
                if(decodedJson != null){
                    for(Object item : (JSONArray)decodedJson){
                        subscription.addTrustedIdp(item.toString());
                    }
                }
            }
        }catch (SQLException e){
            handleException(String.format("Failed to get subscription for app identifier : %d and application id : %s",
                    appIdentifier.toString(), appIdentifier), e);
        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, subscriptionsResultSet);
        }
        return subscription;
    }

    private int getApplicationId(Connection connection, String applicationName, Subscriber subscriber) throws AppManagementException {

        PreparedStatement preparedStmtToGetApplicationId = null;
        ResultSet applicationIdResultSet = null;
        int applicationId = 0;

        String sqlQuery = "SELECT APPLICATION_ID FROM APM_APPLICATION WHERE SUBSCRIBER_ID= ? AND  NAME= ?";

        try {
            preparedStmtToGetApplicationId = connection.prepareStatement(sqlQuery);
            preparedStmtToGetApplicationId.setInt(1, subscriber.getId());
            preparedStmtToGetApplicationId.setString(2, applicationName);
            applicationIdResultSet = preparedStmtToGetApplicationId.executeQuery();

            while (applicationIdResultSet.next()) {
                applicationId = applicationIdResultSet.getInt("APPLICATION_ID");
            }

        } catch (SQLException e) {
            handleException("Error occurred while retrieving application '" + applicationName + "' for subscriber '" +
                    subscriber.getName() + "'", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStmtToGetApplicationId, null, applicationIdResultSet);
        }
        return applicationId;
    }

    private Subscriber getSubscriber(Connection connection, String subscriberName) throws AppManagementException {
        Subscriber subscriber = null;
        PreparedStatement preparedStmtToGetSubscriber = null;
        ResultSet subscribersResultSet = null;

        int tenantId = AppManagerUtil.getTenantId(subscriberName);


        String sqlQuery =
                "SELECT SUBSCRIBER_ID, USER_ID, TENANT_ID, EMAIL_ADDRESS, DATE_SUBSCRIBED FROM " +
                        "APM_SUBSCRIBER WHERE USER_ID = ? AND TENANT_ID = ?";
        try {

            preparedStmtToGetSubscriber = connection.prepareStatement(sqlQuery);
            preparedStmtToGetSubscriber.setString(1, subscriberName);
            preparedStmtToGetSubscriber.setInt(2, tenantId);
            subscribersResultSet = preparedStmtToGetSubscriber.executeQuery();

            if (subscribersResultSet.next()) {
                subscriber =
                        new Subscriber(
                                subscribersResultSet.getString(AppMConstants.SUBSCRIBER_FIELD_EMAIL_ADDRESS));
                subscriber.setEmail(subscribersResultSet.getString(AppMConstants.SUBSCRIBER_FIELD_EMAIL_ADDRESS));
                subscriber.setId(subscribersResultSet.getInt(AppMConstants.SUBSCRIBER_FIELD_SUBSCRIBER_ID));
                subscriber.setName(subscriberName);
                subscriber.setSubscribedDate(subscribersResultSet.getDate(AppMConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
                subscriber.setTenantId(subscribersResultSet.getInt(AppMConstants.SUBSCRIBER_FIELD_TENANT_ID));
            }

        } catch (SQLException e) {
            handleException("Failed to get Subscriber for :" + subscriberName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStmtToGetSubscriber, null, subscribersResultSet);
        }
        return subscriber;
    }

    private int addSubscriber(Connection connection, Subscriber subscriber) throws AppManagementException {
        ResultSet subscriberIdResultSet = null;
        PreparedStatement preparedStmtToAddSubscriber = null;
        int subscriberId = -1;
        try {
            String query = "INSERT INTO APM_SUBSCRIBER (USER_ID, TENANT_ID, EMAIL_ADDRESS, " +
                    "DATE_SUBSCRIBED) VALUES (?,?,?,?)";

            preparedStmtToAddSubscriber =
                    connection.prepareStatement(query, new String[]{AppMConstants.SUBSCRIBER_FIELD_SUBSCRIBER_ID});
            preparedStmtToAddSubscriber.setString(1, subscriber.getName());
            preparedStmtToAddSubscriber.setInt(2, subscriber.getTenantId());
            preparedStmtToAddSubscriber.setString(3, subscriber.getEmail());
            preparedStmtToAddSubscriber.setTimestamp(4, new Timestamp(subscriber.getSubscribedDate().getTime()));
            preparedStmtToAddSubscriber.executeUpdate();

            subscriberIdResultSet = preparedStmtToAddSubscriber.getGeneratedKeys();
            if (subscriberIdResultSet.next()) {
                subscriberId = Integer.valueOf(subscriberIdResultSet.getString(1)).intValue();
            }
            connection.commit();
        } catch (SQLException e) {
            handleException("Error occurred while adding subscriber with name '" + subscriber.getName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStmtToAddSubscriber, null, subscriberIdResultSet);
        }
        return subscriberId;
    }

    private int addApplication(Connection connection, Application application, Subscriber subscriber) throws AppManagementException {

        PreparedStatement preparedStmtToAddApplication = null;
        ResultSet applicationIdResultSet = null;
        int applicationId = -1;
        try {
            // This query to update the APM_APPLICATION table
            String sqlQuery = "INSERT INTO APM_APPLICATION " +
                                "(NAME, SUBSCRIBER_ID, APPLICATION_TIER, CALLBACK_URL, DESCRIPTION, APPLICATION_STATUS) " +
                                "VALUES (?,?,?,?,?,?)";

            preparedStmtToAddApplication = connection.prepareStatement(sqlQuery, new String[]{AppMConstants.APPLICATION_ID});
            if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                preparedStmtToAddApplication = connection.prepareStatement(sqlQuery, new String[]{"application_id"});
            }
            preparedStmtToAddApplication.setString(1, application.getName());
            preparedStmtToAddApplication.setInt(2, subscriber.getId());
            preparedStmtToAddApplication.setString(3, application.getTier());
            preparedStmtToAddApplication.setString(4, application.getCallbackUrl());
            preparedStmtToAddApplication.setString(5, application.getDescription());

            if (application.getName().equals(AppMConstants.DEFAULT_APPLICATION_NAME)) {
                preparedStmtToAddApplication.setString(6, AppMConstants.ApplicationStatus.APPLICATION_APPROVED);
            } else {
                preparedStmtToAddApplication.setString(6, AppMConstants.ApplicationStatus.APPLICATION_CREATED);
            }
            preparedStmtToAddApplication.executeUpdate();
            applicationIdResultSet = preparedStmtToAddApplication.getGeneratedKeys();
            while (applicationIdResultSet.next()) {
                applicationId = Integer.parseInt(applicationIdResultSet.getString(1));
            }
        } catch (SQLException e) {
            handleException("Error occurred while adding application '" + application.getName() + "' for subscriber '" +
                    subscriber.getName() + "'", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStmtToAddApplication, null, applicationIdResultSet);
        }
        return applicationId;
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
