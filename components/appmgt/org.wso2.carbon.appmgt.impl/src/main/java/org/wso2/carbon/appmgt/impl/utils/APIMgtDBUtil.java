/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.DBConfiguration;
import org.wso2.carbon.appmgt.impl.internal.ServiceReferenceHolder;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class APIMgtDBUtil {

    private static final Log log = LogFactory.getLog(APIMgtDBUtil.class);

    private static volatile DataSource dataSource = null;
    private static final String DB_CHECK_SQL = "SELECT * FROM APM_SUBSCRIBER";
    
    private static final String DB_CONFIG = "Database.";
    private static final String DB_DRIVER = DB_CONFIG + "Driver";
    private static final String DB_URL = DB_CONFIG + "URL";
    private static final String DB_USER = DB_CONFIG + "Username";
    private static final String DB_PASSWORD = DB_CONFIG + "Password";

    private static final String DATA_SOURCE_NAME = "DataSourceName";

    private static volatile DataSource uiActivityPublishDataSource = null;
    private static final String UI_ACTIVITY_PUBLISH_DATA_SOURCE_NAME = "Analytics.UIActivityPublishDataSourceName";
    
    /**
     * Initializes the data source
     *
     * @throws org.wso2.carbon.appmgt.api.AppManagementException if an error occurs while loading DB configuration
     */
	public static void initialize() throws Exception {
		if (dataSource != null) {
			return;
		}
		AppManagerConfiguration config = ServiceReferenceHolder.getInstance()
				.getAPIManagerConfigurationService()
				.getAPIManagerConfiguration();

		synchronized (APIMgtDBUtil.class) {
			if (dataSource == null) {
				if (log.isDebugEnabled()) {
					log.debug("Initializing data source");
				}
				String dataSourceName = config
						.getFirstProperty(DATA_SOURCE_NAME);

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

			// initializing the UI Activity Publish specific data source
			if (uiActivityPublishDataSource == null) {
				if (log.isDebugEnabled()) {
					log.debug("Initializing UI-Activity-Publish data source");
				}
				String dataSourceName = config
						.getFirstProperty(UI_ACTIVITY_PUBLISH_DATA_SOURCE_NAME);

				if (dataSourceName != null) {
					uiActivityPublishDataSource = initializeDataSource(dataSourceName);
				}
			}
			setupAPIManagerDatabase();
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
    private static void setupAPIManagerDatabase() throws Exception {

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
    public static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("Data source is not configured properly.");
    }
  
	/**
	 * Utility method to get a UI activity Publish specific database connection
	 * 
	 * @return Connection
	 * @throws SQLException
	 *             if failed to get Connection
	 */
	public static Connection getUiActivityDBConnection()
			throws SQLException {

		if (uiActivityPublishDataSource != null) {
			return uiActivityPublishDataSource.getConnection();
		}
		throw new SQLException(
				"UI activity Publish Data source is not configured properly.");
	}

    /**
     * Utility method to close the connection streams.
     *
     * @param preparedStatement PreparedStatement
     * @param connection        Connection
     * @param resultSet         ResultSet
     */
    public static void closeAllConnections(PreparedStatement preparedStatement,
                                           Connection connection, ResultSet resultSet) {
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
        closeConnection(connection);
    }

    /**
     * Close Connection
     * @param dbConnection Connection
     */
    private static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close database connection. Continuing with " +
                        "others. - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close ResultSet
     * @param resultSet ResultSet
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close ResultSet  - " + e.getMessage(), e);
            }
        }

    }

    /**
     * Close PreparedStatement
     * @param preparedStatement PreparedStatement
     */
    private static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close PreparedStatement. Continuing with" +
                        " others. - " + e.getMessage(), e);
            }
        }

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
