package org.wso2.carbon.appmgt.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.impl.dto.Environment;
import org.wso2.carbon.appmgt.impl.idp.sso.SSOConfiguratorUtil;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import java.sql.*;
import java.util.*;

/**
 * The default implementation of DefaultAppRepository which uses RDBMS and Carbon registry for persistence.
 */
public class DefaultAppRepository implements AppRepository {

    private static final Log log = LogFactory.getLog(DefaultAppRepository.class);

    private static final String POLICY_GROUP_TABLE_NAME = "APM_POLICY_GROUP";
    private static final String POLICY_GROUP_PARTIAL_MAPPING_TABLE_NAME = "APM_POLICY_GRP_PARTIAL_MAPPING";

    private RegistryService registryService;
    private Registry registry;
    private int tenantId;
    private String tenantDomain;
    private String username;

    public DefaultAppRepository() throws AppManagementException {
        try {
            String loggedInUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();

            this.username = MultitenantUtils.getTenantAwareUsername(loggedInUsername);
            this.tenantDomain = MultitenantUtils.getTenantDomain(username);
            ;
            this.tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            AppManagerUtil.loadTenantRegistry(tenantId);
            this.registry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceUserRegistry(username, tenantId);

        } catch (RegistryException e) {
            handleException("Error while instantiating Registry for user : " + username, e);
        } catch (UserStoreException e) {
            handleException("Error while obtaining user registry for user:" + username, e);
        }
    }

    @Override
    public String saveApp(App app) throws AppManagementException {

        if (AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())) {
            return persistWebApp((WebApp) app);
        }


        return null;
    }

    private String persistWebApp(WebApp webApp) throws AppManagementException {

        Connection connection = null;

        try {
            connection = getRDBMSConnectionWithoutAutoCommit();
        } catch (SQLException e) {
            String errorMessage = "Can't get the database connection.";
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        }

        try {

            String uuid = saveRegistryArtifact(webApp);
            webApp.setUUID(uuid);

            int webAppDatabaseId = persistAppToDatabase(webApp, connection);

            persistPolicyGroups(webApp, webAppDatabaseId, connection);

            persistURLTemplates(webApp.getUriTemplates(), webApp.getAccessPolicyGroups(), webAppDatabaseId, connection);

            if(!StringUtils.isEmpty(webApp.getJavaPolicies())){
                persistJavaPolicyMappings(webApp.getJavaPolicies(), webAppDatabaseId, connection);
            }

            recordAPILifeCycleEvent(webApp.getId(), null, APIStatus.CREATED, AppManagerUtil.replaceEmailDomainBack(webApp.getId().getProviderName()), connection);

            saveServiceProvider(webApp);

            connection.commit();

            return uuid;
        } catch (SQLException e) {

            try {
                connection.rollback();
            } catch (SQLException e1) {
                log.error(String.format("Can't rollback persist operation for the web app '%s'", webApp.getType(), webApp.getDisplayName()));
            }

            String errorMessage = String.format("Can't persist web app '%s'.", webApp.getDisplayName());
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);

        }finally {
            APIMgtDBUtil.closeAllConnections(null,connection,null);
        }

    }

    private String saveRegistryArtifact(App app) throws AppManagementException {
        String appId = null;
        if (AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())) {
            appId = saveWebAppRegistryArtifact((WebApp) app);
        }
        return appId;
    }

    private String saveMobileAppRegistryArtifact(MobileApp mobileApp) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private String saveWebAppRegistryArtifact(WebApp webApp) throws AppManagementException {
        String artifactId = null;

        GenericArtifactManager artifactManager = null;
        try {
            //Check whether the user has enough permissions to change lifecycle
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(this.tenantDomain, true);

            artifactManager = AppManagerUtil.getArtifactManager(registry,
                    AppMConstants.WEBAPP_ASSET_TYPE);
            registry.beginTransaction();
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(webApp.getId().getApiName()));
            GenericArtifact appArtifact = AppManagerUtil.createWebAppArtifactContent(genericArtifact, webApp);
            artifactManager.addGenericArtifact(appArtifact);
            artifactId = appArtifact.getId();

            //Get system registry for logged in tenant domain
            Registry systemRegistry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceSystemRegistry(tenantId);
            GenericArtifactManager systemRegistryArtifactManager = AppManagerUtil.getArtifactManager(systemRegistry,
                    AppMConstants.WEBAPP_ASSET_TYPE);
            GenericArtifact systemRegistryArtifact = artifactManager.getGenericArtifact(artifactId);


            //Promote app lifecycle 'Initial' --> 'Created'
            systemRegistryArtifact.invokeAction(AppMConstants.LifecycleActions.CREATE, AppMConstants.WEBAPP_LIFE_CYCLE);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, appArtifact.getId());

            Set<String> tagSet = webApp.getTags();
            if (tagSet != null) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
            if (webApp.getAppVisibility() != null) {
                AppManagerUtil.setResourcePermissions(webApp.getId().getProviderName(),
                        AppMConstants.API_RESTRICTED_VISIBILITY, webApp.getAppVisibility(), artifactPath);
            }
            String providerPath = AppManagerUtil.getAPIProviderPath(webApp.getId());
            registry.addAssociation(providerPath, artifactPath, AppMConstants.PROVIDER_ASSOCIATION);



            registry.commitTransaction();
        } catch (RegistryException e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                handleException(
                        "Error while rolling back the transaction for web application : "
                                + webApp.getId().getApiName() + " creation.", re);
            }
            handleException("Error occurred while creating the web application : " + webApp.getId().getApiName(), e);
        }
        return artifactId;
    }

    private int persistAppToDatabase(WebApp app, Connection connection) throws SQLException, AppManagementException {

        String query = "INSERT INTO APM_APP(APP_PROVIDER, TENANT_ID, APP_NAME, APP_VERSION, CONTEXT, TRACKING_CODE, " +
                "UUID, SAML2_SSO_ISSUER, LOG_OUT_URL,APP_ALLOW_ANONYMOUS, APP_ENDPOINT, TREAT_AS_SITE) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            Environment gatewayEnvironment = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                    getAPIManagerConfiguration().getApiGatewayEnvironments().get(0);

            String[] urlArray = gatewayEnvironment.getApiGatewayEndpoint().split(",");
            String prodURL = urlArray[0];

            String logoutURL = app.getLogoutURL();
            if (logoutURL != null && !"".equals(logoutURL.trim())) {
                logoutURL = prodURL.concat(app.getContext()).concat("/" + app.getId().getVersion()).concat(logoutURL);
            }

            int tenantId = -1;
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(app.getId().getProviderName()));
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                String errorMessage = String.format("Can't get the tenant id for the tenant domain '%s'", tenantDomain);
                log.error(errorMessage, e);
                throw new AppManagementException(errorMessage, e);
            }

            preparedStatement = connection.prepareStatement(query, new String[]{"APP_ID"});
            preparedStatement.setString(1, AppManagerUtil.replaceEmailDomainBack(app.getId().getProviderName()));
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setString(3, app.getId().getApiName());
            preparedStatement.setString(4, app.getId().getVersion());
            preparedStatement.setString(5, app.getContext());
            preparedStatement.setString(6, app.getTrackingCode());
            preparedStatement.setString(7, app.getUUID());
            preparedStatement.setString(8, app.getSaml2SsoIssuer());
            preparedStatement.setString(9, logoutURL);
            preparedStatement.setBoolean(10, app.getAllowAnonymous());
            preparedStatement.setString(11, app.getUrl());
            preparedStatement.setBoolean(12, Boolean.parseBoolean(app.getTreatAsASite()));

            preparedStatement.execute();

            generatedKeys = preparedStatement.getGeneratedKeys();
            int webAppId = -1;
            if (generatedKeys.next()) {
                webAppId = generatedKeys.getInt(1);
            }

            //Set default versioning details
            saveDefaultVersionDetails(app, connection);

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

    private void persistPolicyGroups(WebApp app, int appDatabaseId, Connection connection) throws SQLException {

        PreparedStatement preparedStatementToAddPolicyMappings = null;
        String query = "INSERT INTO APM_POLICY_GROUP_MAPPING(APP_ID, POLICY_GRP_ID) VALUES(?,?)";

        try{
            preparedStatementToAddPolicyMappings = connection.prepareStatement(query);

            for (EntitlementPolicyGroup policyGroup : app.getAccessPolicyGroups()) {

                // Don't try to use batch insert for the policy groups since we need the auto-generated IDs.
                persistPolicyGroup(policyGroup, connection);

                // Add mapping query to the batch
                preparedStatementToAddPolicyMappings.setInt(1, appDatabaseId);
                preparedStatementToAddPolicyMappings.setInt(2, policyGroup.getPolicyGroupId());
                preparedStatementToAddPolicyMappings.addBatch();
            }

            preparedStatementToAddPolicyMappings.executeBatch();
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatementToAddPolicyMappings, null, null);
        }
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

            if(policyGroup.getPolicyPartials() != null){
                persistEntitlementPolicyMappings(policyGroup.getPolicyPartials(), generatedPolicyGroupId, connection);
            }

        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, resultSet);
        }
    }

    private void persistEntitlementPolicyMappings(JSONArray policyPartials, int policyGroupId, Connection connection) throws SQLException {

		String query = String.format("INSERT INTO APM_POLICY_GRP_PARTIAL_MAPPING(POLICY_GRP_ID, POLICY_PARTIAL_ID) VALUES(?,?) ", POLICY_GROUP_PARTIAL_MAPPING_TABLE_NAME);
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(query);

            for (int i = 0; i < policyPartials.size(); i++) {

                preparedStatement.setInt(1, policyGroupId);

                int xacmlPolicyId = (Integer)(((JSONObject) policyPartials.get(i)).get("POLICY_PARTIAL_ID"));
                preparedStatement.setInt(2, xacmlPolicyId);

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }

    public void recordAPILifeCycleEvent(APIIdentifier identifier, APIStatus oldStatus,
                                        APIStatus newStatus, String userId, Connection conn)
            throws
            AppManagementException {
        // Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        int tenantId;
        int apiId = -1;
        try {
            tenantId = IdentityTenantUtil.getTenantIdOfUser(userId);
        } catch (IdentityRuntimeException e) {
            String msg = "Failed to get tenant id of user : " + userId;
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        }

        if (oldStatus == null && !newStatus.equals(APIStatus.CREATED)) {
            String msg = "Invalid old and new state combination";
            log.error(msg);
            throw new AppManagementException(msg);
        } else if (oldStatus != null && oldStatus.equals(newStatus)) {
            String msg = "No measurable differences in WebApp state";
            log.error(msg);
            throw new AppManagementException(msg);
        }

        String getAPIQuery =
                "SELECT " + "API.APP_ID FROM APM_APP API" + " WHERE "
                        + "API.APP_PROVIDER = ?" + " AND API.APP_NAME = ?"
                        + " AND API.APP_VERSION = ?";

        String sqlQuery =
                "INSERT "
                        + "INTO APM_APP_LC_EVENT (APP_ID, PREVIOUS_STATE, NEW_STATE, USER_ID, TENANT_ID, EVENT_DATE)"
                        + " VALUES (?,?,?,?,?,?)";

        try {
            // conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(getAPIQuery);
            ps.setString(1, AppManagerUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                apiId = resultSet.getInt("APP_ID");
            }
            resultSet.close();
            ps.close();
            if (apiId == -1) {
                String msg = "Unable to find the WebApp: " + identifier + " in the database";
                log.error(msg);
                throw new AppManagementException(msg);
            }

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            if (oldStatus != null) {
                ps.setString(2, oldStatus.getStatus());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, newStatus.getStatus());
            ps.setString(4, userId);
            ps.setInt(5, tenantId);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();

            // finally commit transaction
            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the API state change record", e);
                }
            }
            handleException("Failed to record API state change", e);
        } finally {
            // APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
    }

    private void saveDefaultVersionDetails(WebApp app, Connection connection) throws SQLException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int recordCount = 0;

        String sqlQuery =
                "SELECT COUNT(*) AS ROWCOUNT FROM APM_APP_DEFAULT_VERSION WHERE APP_NAME=? AND APP_PROVIDER=? AND " +
                        "TENANT_ID=? ";

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, app.getId().getApiName());
            prepStmt.setString(2, app.getId().getProviderName());
            prepStmt.setInt(3, tenantId);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                recordCount = rs.getInt("ROWCOUNT");
            }

            if (recordCount == 0) {
                //if this is the default version and there are no existing records, create a new one
                if (app.isDefaultVersion()) {
                    addDefaultVersionDetails(app, connection);
                }
            } else {
                //if there is an existing record and if this is the latest default, update the status
                if (app.isDefaultVersion()) {
                    updateDefaultVersionDetails(app, connection);
                } else {
                    //If this is an existing record but if this is not the latest default, check if this is the
                    // previous default version
                    String existingDefaultVersion = getDefaultVersion(app.getId().getApiName(), app.getId().getProviderName(),
                            AppDefaultVersion.APP_IS_ANY_LIFECYCLE_STATE, connection);
                    if (existingDefaultVersion.equals(app.getId().getVersion())) {
                        //if this is the ex default version, delete the entry
                        deleteDefaultVersionDetails(app.getId(), connection);
                    }
                }
            }
        } catch (SQLException e) {
            /* In the code it is using a single SQL connection passed from the parent function so the error is logged
             here and throwing the SQLException so the connection will be disposed by the parent function. */
            log.error("Error when getting the default version record count for Application: " +
                    app.getId().getApiName(), e);
            throw e;
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, null, rs);
        }
    }

    public static String getDefaultVersion(String appName, String providerName, AppDefaultVersion appStatus, Connection conn){

        PreparedStatement ps = null;
        ResultSet rs = null;
        String defaultVersion = "";
        try {
            String columnName;
            if (appStatus == AppDefaultVersion.APP_IS_PUBLISHED) {
                columnName = "PUBLISHED_DEFAULT_APP_VERSION";
            } else {
                columnName = "DEFAULT_APP_VERSION";
            }
            String sqlQuery =
                    "SELECT " + columnName +
                            " FROM APM_APP_DEFAULT_VERSION WHERE APP_NAME =? AND APP_PROVIDER=? AND TENANT_ID=? ";

            ps = conn.prepareStatement(sqlQuery);
            if (log.isDebugEnabled()) {
                String msg = String.format("Getting default version details of app : provider:%s ,name :%s"
                        , providerName, appName);
                log.debug(msg);
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

            ps.setString(1, appName);
            ps.setString(2, providerName);
            ps.setInt(3, tenantId);
            rs = ps.executeQuery();
            if (rs.next()) {
                defaultVersion = rs.getString(columnName);
            }
        } catch (SQLException e) {


        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }
        return defaultVersion == null ? "" : defaultVersion;
    }

    private void updateDefaultVersionDetails(WebApp app, Connection connection) throws SQLException {
        PreparedStatement prepStmt = null;
        String query;
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

            if (app.getStatus() == APIStatus.PUBLISHED && app.isDefaultVersion()) {
                query = "UPDATE APM_APP_DEFAULT_VERSION SET DEFAULT_APP_VERSION=?, PUBLISHED_DEFAULT_APP_VERSION=? " +
                        "WHERE APP_NAME=? AND APP_PROVIDER=? AND TENANT_ID=? ";
                prepStmt = connection.prepareStatement(query);
                prepStmt.setString(1, app.getId().getVersion());
                prepStmt.setString(2, app.getId().getVersion());
                prepStmt.setString(3, app.getId().getApiName());
                prepStmt.setString(4, app.getId().getProviderName());
                prepStmt.setInt(5, tenantId);
            } else {
                query =
                        "UPDATE APM_APP_DEFAULT_VERSION SET DEFAULT_APP_VERSION=? WHERE APP_NAME=? AND APP_PROVIDER=?" +
                                " AND TENANT_ID=? ";
                prepStmt = connection.prepareStatement(query);
                prepStmt.setString(1, app.getId().getVersion());
                prepStmt.setString(2, app.getId().getApiName());
                prepStmt.setString(3, app.getId().getProviderName());
                prepStmt.setInt(4, tenantId);
            }
            prepStmt.executeUpdate();
        } catch (SQLException e) {
              /* In the code it is using a single SQL connection passed from the parent function so the error is logged
             here and throwing the SQLException so the connection will be disposed by the parent function. */
            log.error("Error while updating default version details for WebApp : " +
                    app.getId(), e);
            throw e;
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, null, null);
        }
    }

    private void deleteDefaultVersionDetails(APIIdentifier apiIdentifier, Connection connection) throws SQLException {
        PreparedStatement prepStmt = null;
        String query;
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
            query = "DELETE FROM APM_APP_DEFAULT_VERSION WHERE APP_NAME=? AND APP_PROVIDER=? AND TENANT_ID=? ";
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiIdentifier.getApiName());
            prepStmt.setString(2, apiIdentifier.getProviderName());
            prepStmt.setInt(3, tenantId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
              /* In the code it is using a single SQL connection passed from the parent function so the error is logged
             here and throwing the SQLException so the connection will be disposed by the parent function. */
            log.error("Error while deleting default version details for WebApp : " +
                    apiIdentifier.getApiName(), e);
            throw e;
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, null, null);
        }
    }

    private void addDefaultVersionDetails(WebApp app, Connection connection) throws SQLException {
        PreparedStatement prepStmt = null;
        String query =
                "INSERT INTO APM_APP_DEFAULT_VERSION  (APP_NAME, APP_PROVIDER, DEFAULT_APP_VERSION, " +
                        "PUBLISHED_DEFAULT_APP_VERSION, TENANT_ID) VALUES (?,?,?,?,?)";

        if (log.isDebugEnabled()) {
            log.debug("Inserting default version details for AppId -" + app.getId().getApplicationId());
        }

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, app.getId().getApiName());
            prepStmt.setString(2, app.getId().getProviderName());
            prepStmt.setString(3, app.getId().getVersion());
            if (app.getStatus() == APIStatus.PUBLISHED) {
                prepStmt.setString(4, app.getId().getVersion());
            } else {
                prepStmt.setString(4, null);
            }
            prepStmt.setInt(5, tenantId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
             /* In the code it is using a single SQL connection passed from the parent function so the error is logged
             here and throwing the SQLException so the connection will be disposed by the parent function. */
            log.error("Error while inserting default version details for WebApp : " +
                    app.getId(), e);
            throw e;
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, null, null);
        }
    }

    public void persistURLTemplates(Set<URITemplate> uriTemplates, List<EntitlementPolicyGroup> policyGroups, int webAppDatabaseId, Connection connection) throws SQLException {

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
                preparedStatement.setInt(4, policyGroups.get(uriTemplate.getPolicyGroupId()).getPolicyGroupId());

                preparedStatement.executeUpdate();
            }
        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, null, null);
        }
    }


    private void saveServiceProvider(WebApp app) {

        SSOProvider ssoProvider = new SSOProvider();

        String providerName = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.SSO_CONFIGURATOR_NAME);
        String version = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.SSO_CONFIGURATOR_VERSION);
        ssoProvider.setProviderName(providerName);
        ssoProvider.setProviderVersion(version);
        String issuerName = null;
        APIIdentifier appIdentifier = app.getId();
        if (MultitenantConstants.SUPER_TENANT_ID != this.tenantId) {
            issuerName = appIdentifier.getApiName() + "-" + tenantDomain + "-" + appIdentifier.getVersion();
        } else {
            issuerName = appIdentifier.getApiName() + "-" + appIdentifier.getVersion();
        }


        String [] claims = new String[2];
        claims[0] = "http://wso2.org/claims/role";
        claims[1] = "http://wso2.org/claims/otherphone";
        ssoProvider.setIssuerName(issuerName);
        ssoProvider.setClaims(claims);
        if(!StringUtils.isNotEmpty(app.getLogoutURL())){
            ssoProvider.setLogoutUrl(app.getLogoutURL());
        }

        app.setSsoProviderDetails(ssoProvider);
        SSOConfiguratorUtil ssoConfiguratorUtil = new SSOConfiguratorUtil();
        ssoConfiguratorUtil.createSSOProvider(app, false);
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

    protected void handleException(String msg, Exception e) throws AppManagementException {
        log.error(msg, e);
        throw new AppManagementException(msg, e);
    }

    protected void handleException(String msg) throws AppManagementException {
        log.error(msg);
        throw new AppManagementException(msg);
    }

}
