package org.wso2.carbon.appmgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.api.model.EntitlementPolicyGroup;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.core.Registry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The default implementation of DefaultAppRepository which uses RDBMS and Carbon registry for persistence.
 */
public class DefaultAppRepository implements AppRepository{

    private static final Log log = LogFactory.getLog(DefaultAppRepository.class);

    private static final String POLICY_GROUP_TABLE_NAME = "APM_POLICY_GROUP";

    private RegistryService registryService;

    public DefaultAppRepository(){
        this.registryService = ServiceReferenceHolder.getInstance().getRegistryService();
    }

    @Override
    public String saveApp(App app) {

        if(AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())){

            WebApp webApp = (WebApp) app;

            Connection connection = null;
            try {
                connection = getRDBMSConnectionWithoutAutoCommit();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                savePolicyGroups(webApp, connection);
                saveRegistryArtifact(app);
                saveAppToRDMS(webApp, connection);
                saveServiceProvider(webApp, connection);
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error(String.format("Couldn't rollback save operation for the app %s:%s", app.getType(), app.getDisplayName()));
                }
            }

        }


        return null;
    }

    private void savePolicyGroups(WebApp app, Connection connection) throws SQLException {

        for(EntitlementPolicyGroup policyGroup : app.getAccessPolicyGroups()){
            savePolicyGroup(policyGroup, connection);
        }

    }

    private void savePolicyGroup(EntitlementPolicyGroup policyGroup, Connection connection) throws SQLException {

            String query = String.format("INSERT INTO %s(NAME,THROTTLING_TIER,USER_ROLES,URL_ALLOW_ANONYMOUS,DESCRIPTION) VALUES(?,?,?,?,?) ",POLICY_GROUP_TABLE_NAME);

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

                saveEntitlementPolicyMappings(policyGroup, connection);
            } finally {
                closeResultSet(resultSet);
            }

    }

    private void closeResultSet(ResultSet resultSet) {
        if(resultSet != null){
            try {
                resultSet.close();
            } catch (SQLException ignore) {}
        }
    }

    private void saveEntitlementPolicyMappings(EntitlementPolicyGroup policyGroup, Connection connection) {

    }

    private String saveRegistryArtifact(App app){
        return null;
    }

    private long saveAppToRDMS(WebApp app, Connection connection){
        return -1;
    }

    private void saveServiceProvider(WebApp app, Connection connection){

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


}
