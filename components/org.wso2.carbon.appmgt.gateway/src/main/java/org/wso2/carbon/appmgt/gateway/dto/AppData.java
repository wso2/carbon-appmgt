/**
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.appmgt.gateway.dto;

public class AppData {
    private String name;
    private String host;
    private int port;
    private String context;
    private String fileName;
    private ResourceData[] resources;

    /**
     * Get App Name.
     * @return app name as a String.
     */
    public String getName() {
        return name;
    }

    /**
     * Set App Name.
     * @param name application name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get Host Value.
     * @return host as a String.
     */
    public String getHost() {
        return host;
    }

    /**
     * Set Host Value.
     * @param host host name of the app.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get Port Value.
     * @return port in Int.
     */
    public int getPort() {
        return port;
    }

    /**
     * Set Port Value.
     * @param port port value ot the app.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get App Context.
     * @return context in String.
     */
    public String getContext() {
        return context;
    }

    /**
     * Set App Context.
     * @param context context value of the app.
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Get Resource Data.
     * @return array of Resource data.
     */
    public ResourceData[] getResources() {
        return resources;
    }


    /**
     * Set Resource Data.
     * @param resources app resources.
     */
    public void setResources(ResourceData[] resources) {
        this.resources = resources;
    }

    /**
     * Get File Name.
     * @return fileName in String.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set File Name.
     * @param fileName file name of the app.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}