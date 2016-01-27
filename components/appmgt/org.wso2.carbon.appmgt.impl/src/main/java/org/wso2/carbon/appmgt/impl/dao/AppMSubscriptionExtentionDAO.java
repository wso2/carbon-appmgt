/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.impl.dto.SubscriptionExpiryDTO;
import org.wso2.carbon.appmgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.appmgt.impl.utils.APIMgtDBUtil;

import java.sql.*;

public class AppMSubscriptionExtentionDAO {

    private static final Log log = LogFactory.getLog(AppMSubscriptionExtentionDAO.class);

    public int addSubscription(WorkflowDTO workflowDTO) throws AppManagementException {

        SubscriptionExpiryDTO subscriptionExpiryDTO = null;
        if (workflowDTO instanceof SubscriptionExpiryDTO) {
            subscriptionExpiryDTO = (SubscriptionExpiryDTO) workflowDTO;
        } else {
            throw new AppManagementException("Error in casting....");
        }
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        AppMDAO appMDAO = new AppMDAO();
        int subscriptionId = -1;

        try {
            connection = APIMgtDBUtil.getConnection();
            int appId = appMDAO.getAPIID(new APIIdentifier(subscriptionExpiryDTO.getApiProvider(),
                    subscriptionExpiryDTO.getApiName(), subscriptionExpiryDTO.getApiVersion()), connection);
            int subscriberId = appMDAO.getSubscriber(subscriptionExpiryDTO.getSubscriber()).getId();

            String sqlQuery = "INSERT INTO APM_SUBSCRIPTION_EXT (APP_ID, SUBSCRIBER_ID, SUBSCRIPTION_TYPE, " +
                    "SUBSCRIPTION_TIME, EVALUATION_PERIOD, EXPIRED_ON) VALUES (?,?,?,?,?,?)";

            // Adding data to the APM_SUBSCRIPTION_EXT table.
            preparedStatement = connection.prepareStatement(sqlQuery, new String[]{"SUBSCRIPTION_ID"});
            if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                preparedStatement = connection.prepareStatement(sqlQuery, new String[]{"subscription_id"});
            }

            preparedStatement.setInt(1, appId);
            preparedStatement.setInt(2, subscriberId);
            preparedStatement.setString(3, subscriptionExpiryDTO.getSubscriptionType());
            preparedStatement.setTimestamp(4, new Timestamp(subscriptionExpiryDTO.getSubscriptionTime().getTime()));
            preparedStatement.setInt(5, subscriptionExpiryDTO.getEvaluationPeriod());
            preparedStatement.setTimestamp(6, new Timestamp(subscriptionExpiryDTO.getExpireOn().getTime()));

            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            while (rs.next()) {
                subscriptionId = rs.getInt(1);
            }
            preparedStatement.close();
            connection.commit();

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add subscription ", e);
                }
            }
            handleException("Failed to add subscriber data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return subscriptionId;
    }

    private static void handleException(String msg, Throwable t) throws AppManagementException {
        log.error(msg, t);
        throw new AppManagementException(msg, t);
    }
}
