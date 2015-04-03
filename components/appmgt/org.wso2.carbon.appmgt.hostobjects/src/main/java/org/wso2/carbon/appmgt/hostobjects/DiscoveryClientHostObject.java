/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.hostobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;
import org.wso2.carbon.appmgt.impl.clients.AppServerWebappAdminClient;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.VersionedWebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappsWrapper;

/**
 * Host object to expose discovery service
 * TODO: Move this to factory service and do away with "HostObject" approach.
 */
public class DiscoveryClientHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(DiscoveryClientHostObject.class);

    private static final int MAX_GENERATED_CONTEXT_SUFFIX = 1000;
    private static final String CONTEXT_NOT_GENERATED = "<could-not-generate>";
    private static final String APP_PROPERTY_NAME = "name";
    private static final String APP_PROPERTY_VERSION = "version";
    private static final String APP_PROPERTY_APP_TYPE = "appType";
    private static final String APP_PROPERTY_CONTEXT = "context";
    private static final String APP_PROPERTY_PROXY_CONTEXT = "proxyContext";
    private static final String APP_PROPERTY_STATUS = "status";
    private static final String APP_PROPERTY_STATUS_NEW = "New";
    private static final String APP_PROPERTY_WEB_APP_ID = "webappId";
    private static final String APP_PROPERTY_ID = "id";
    private static final String APP_PROPERTY_HOST = "host";
    private static final String APP_PROPERTY_PROTOCOL = "protocol";
    private static final String APP_PROPERTY_PORT = "port";
    private static final String APP_PROPERTY_HTTP_URL = "httpUrl";
    //    private static final String APP_PROPERTY_HOST = "host";
    private static final String APP_PROPERTY_STATUS_PROXY_CREATED = "ProxyCreated";

    @Override
    public String getClassName() {
        return "DiscoveryClient";
    }

    // The zero-argument constructor used for create instances for runtime
    public DiscoveryClientHostObject() throws AppManagementException {
    }

    /**
     * Calls he backend AS and discover available applications.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return The JSON array of objects with all the necessary data to display an overview in
     *  a table
     * @throws AppManagementException
     */
    public static NativeObject jsFunction_discoverWebapps(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) throws AppManagementException {

        if (args.length != 4) {
            throw new AppManagementException(
                    "Argument mismatch to the method: needs 4 but found " + args.length);
        }

        NativeObject result = new NativeObject();

        String loggedInUsername = String.valueOf(args[1]);
        String appServerURL = String.valueOf(args[0]);
        String userName = String.valueOf(args[1]);
        String password = String.valueOf(args[2]);
        int pageNo = Double.valueOf(String.valueOf(args[3])).intValue();

        AppServerWebappAdminClient webappAdminClient;
        try {
            webappAdminClient = new AppServerWebappAdminClient(userName, password, appServerURL,
                    null, cx.getLocale());
        } catch (AppManagementException ame) {
            throw new AppManagementException(
                    "The application server URL, username or password mismatch", ame);
        }

        NativeArray table = new NativeArray(0);
        result.put("metadataList", result, table);

        WebappsWrapper webappsWrapper = webappAdminClient
                .getPagedWebappsSummary("", "all", "all", pageNo);

        result.put("numberOfPages", result, webappsWrapper.getNumberOfPages());

        VersionedWebappMetadata[] versionedWebappMetadataArray = webappsWrapper.getWebapps();

        String host = webappsWrapper.getHostName();
        int httpPort = webappsWrapper.getHttpPort();
        int httpsPort = webappsWrapper.getHttpsPort();

        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(loggedInUsername);

        int i = 0;
        for (VersionedWebappMetadata versionedWebappMetadata : versionedWebappMetadataArray) {
            WebappMetadata[] webappMetadataArray = versionedWebappMetadata.getVersionGroups();
            for (WebappMetadata webappMetadata : webappMetadataArray) {
                NativeObject row = new NativeObject();
                String version = webappMetadata.getAppVersion();
                String context = webappMetadata.getContextPath();
                row.put(APP_PROPERTY_NAME, row, webappMetadata.getDisplayName());
                row.put(APP_PROPERTY_VERSION, row, version);
                row.put(APP_PROPERTY_APP_TYPE, row, webappMetadata.getWebappType());
                row.put(APP_PROPERTY_CONTEXT, row, context);
                row.put(APP_PROPERTY_PROXY_CONTEXT, row,
                        generateProxyContext(context, apiProvider));
                row.put(APP_PROPERTY_WEB_APP_ID, row, webappMetadata.getWebappFile());
                row.put(APP_PROPERTY_ID, row, generateWebappId(webappMetadata));
                table.put(i, table, row);
                i++;
            }

        }

        return result;
    }

    /**
     * Generates a proxy context. use the original context path if it is free. Generate new context
     * path appending an integer if the context is already used.
     *  e.g. Original context is /xyz then it generates /xyz_0 if /xyz is used.
     *  This is bit expensive call as it may iterates through all artifacts in worst case.
     * @param context
     * @param apiProvider
     * @return
     * @throws AppManagementException
     */
    private static String generateProxyContext(String context, APIProvider apiProvider)
            throws AppManagementException {
        String result = context;
        int i = 0;
        while (apiProvider.isContextExist(result)) {
            //Generate a next available context 
            result = context + "_" + i;
            i++;

            if (i > MAX_GENERATED_CONTEXT_SUFFIX) {
                log.error("Could not generate context. All context from " +
                        context + "_0 to " + context + "_" + i
                        + " are already taken. Aborting the creation");
                return CONTEXT_NOT_GENERATED;
            }
        }
        return result;
    }

    public static NativeObject jsFunction_getDiscoveredWebappInfo(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) throws AppManagementException {

        if (args.length != 5) {
            throw new AppManagementException(
                    "Argument mismatch to the method: needs 5 but found " + args.length);
        }
        String appServerURL = String.valueOf(args[0]);
        String userName = String.valueOf(args[1]);
        String password = String.valueOf(args[2]);
        String containerType = String.valueOf((args[3]));
        String webappId = String.valueOf(args[4]);

        AppServerWebappAdminClient webappAdminClient = new AppServerWebappAdminClient(userName,
                password, appServerURL, null, cx.getLocale());

        int pageNo = 0;
        int totalPages = 0;
        WebappMetadata matchedWebappMetadata = null;
        WebappsWrapper webappsWrapper;
        NativeObject result = null;

        while ((pageNo == 0 || pageNo < totalPages) && matchedWebappMetadata == null) {
            webappsWrapper = webappAdminClient.getPagedWebappsSummary("", "all", "all", pageNo);
            totalPages = webappsWrapper.getNumberOfPages();
            VersionedWebappMetadata[] versionedWebappMetadataArray = webappsWrapper.getWebapps();

            String host = webappsWrapper.getHostName();
            int httpPort = webappsWrapper.getHttpPort();
            int httpsPort = webappsWrapper.getHttpsPort();
            pageNo++;

            searching:
            for (VersionedWebappMetadata versionedWebappMetadata : versionedWebappMetadataArray) {
                WebappMetadata[] webappMetadataArray = versionedWebappMetadata.getVersionGroups();
                for (WebappMetadata webappMetadata : webappMetadataArray) {
                    String currentWebappId = generateWebappId(webappMetadata);
                    String deploymentId = webappMetadata.getWebappFile();
                    if (currentWebappId.equals(webappId)) {
                        result = new NativeObject();
                        String version = webappMetadata.getAppVersion();
                        String context = webappMetadata.getContextPath();
                        result.put(APP_PROPERTY_NAME, result, webappMetadata.getDisplayName());
                        result.put(APP_PROPERTY_VERSION, result, version);
                        result.put(APP_PROPERTY_APP_TYPE, result, webappMetadata.getWebappType());
                        result.put(APP_PROPERTY_CONTEXT, result, context);
                        result.put(APP_PROPERTY_PROXY_CONTEXT, result, context);
                        result.put(APP_PROPERTY_STATUS, result, APP_PROPERTY_STATUS_NEW);
                        result.put(APP_PROPERTY_WEB_APP_ID, result, webappMetadata.getWebappFile());
                        result.put(APP_PROPERTY_HOST, result, host);
                        result.put(APP_PROPERTY_PROTOCOL, result, "http");
                        result.put(APP_PROPERTY_PORT, result, httpPort);
                        result.put(APP_PROPERTY_HTTP_URL, result, "http://" + host + ":" + httpPort + context);

                        matchedWebappMetadata = webappMetadata;
                        break searching;
                    }
                }

            }
        }
        if (result == null) {
            throw new AppManagementException(
                    "Could not find the application given the application ID: " + webappId +
                            " This might have been removed");
        }

        return result;
    }

    private static String generateWebappId(WebappMetadata webappMetadata) {
        return webappMetadata.getWebappFile().replaceAll("[^\\p{Alnum}]", "_");
    }

}
