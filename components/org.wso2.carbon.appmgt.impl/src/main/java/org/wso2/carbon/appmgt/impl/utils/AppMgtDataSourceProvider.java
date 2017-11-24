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

package org.wso2.carbon.appmgt.impl.utils;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.DBConfiguration;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * This class is dedicated to provide Datasource Connections
 */
public class AppMgtDataSourceProvider {
    private static final Log log = LogFactory.getLog(APIMgtDBUtil.class);

    private static volatile DataSource dataSource = null;

    private static final String DB_CONFIG = "Database.";
    private static final String DB_DRIVER = DB_CONFIG + "Driver";
    private static final String DB_URL = DB_CONFIG + "URL";
    private static final String DB_USER = DB_CONFIG + "Username";
    private static final String DB_PASSWORD = DB_CONFIG + "Password";
    private static final String DB_CHECK_SQL = "SELECT * FROM resource WHERE";

    public static void initialize() throws Exception {
        if (dataSource != null) {
            return;
        }
        AppManagerConfiguration config = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        synchronized (AppMgtDataSourceProvider.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source");
                }
                String dataSourceName = config
                        .getFirstProperty(AppMConstants.APPM_DATA_SOURCES_STORAGE);

                if (dataSourceName != null) {
                    dataSource = initializeDataSource(dataSourceName);
                } else {
                    DBConfiguration configuration = getDBConfig(config);
                    String dbUrl = configuration.getDbUrl();
                    String driver = configuration.getDriverName();
                    String username = configuration.getUserName();
                    String password = configuration.getPassword();
                    if (dbUrl == null || driver == null || username == null
                            || password == null) {
                        log.warn("Required DB configuration parameters unspecified. So WebApp Store and WebApp Publisher "
                                + "will not work as expected.");
                    }

                    BasicDataSource basicDataSource = new BasicDataSource();
                    basicDataSource.setDriverClassName(driver);
                    basicDataSource.setUrl(dbUrl);
                    basicDataSource.setUsername(username);
                    basicDataSource.setPassword(password);
                    dataSource = basicDataSource;
                }
            }
            setupStorageDB();
        }
    }

    /**
     * Initialize received data source
     *
     * @param dataSourceName
     *            : Data source name needs to be initialized
     * @return DataSource
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     *             if an error occurs while initializing the data source
     */
    public static DataSource initializeDataSource(String dataSourceName)
            throws AppManagementException {
        DataSource ds = null;
        if (dataSourceName != null) {
            try {
                Context ctx = new InitialContext();
                ds = (DataSource) ctx.lookup(dataSourceName);
            } catch (NamingException e) {
                log.error("An exception occurred while initializing the DataSource "
                        + dataSourceName + " - " + e.getMessage(), e);
                throw new AppManagementException(
                        "Error while looking up the data " + "source: "
                                + dataSourceName, e);
            }
        }
        return ds;
    }

    /**
     * Creates the APIManager Database if not created already.
     *
     * @throws Exception if an error occurs while creating the APIManagerDatabase.
     */
    private static void setupStorageDB() throws Exception {

        String value = System.getProperty("setup");
        if (value != null) {
            LocalDatabaseCreator databaseCreator = new LocalDatabaseCreator(dataSource);
            try {
                if (!databaseCreator.isDatabaseStructureCreated(DB_CHECK_SQL)) {
                    databaseCreator.createRegistryDatabase();
                } else {
                    log.info("APIManager database already exists. Not creating a new database.");
                }
            } catch (Exception e) {
                String msg = "Error in creating the APIManager database";
                throw new Exception(msg, e);
            }
        }
    }

    /**
     * Utility method to get a new database connection (to AppMgt Database)
     *
     * @return Connection
     * @throws java.sql.SQLException if failed to get Connection
     */
    public static Connection getStorageDBConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("Data source is not configured properly.");
    }
    /**
     * Return the DBConfiguration
     *
     * @param config AppManagerConfiguration containing the JDBC settings
     * @return DBConfiguration
     */
    private static DBConfiguration getDBConfig(AppManagerConfiguration config) {
        DBConfiguration dbConfiguration = new DBConfiguration();
        dbConfiguration.setDbUrl(config.getFirstProperty(DB_URL));
        dbConfiguration.setDriverName(config.getFirstProperty(DB_DRIVER));
        dbConfiguration.setUserName(config.getFirstProperty(DB_USER));
        dbConfiguration.setPassword(config.getFirstProperty(DB_PASSWORD));
        return dbConfiguration;
    }


}
