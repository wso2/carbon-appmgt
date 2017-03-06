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

/**
 * DTO object which maps to synapse rest APIs.
 */
public class WebAppData {
    private String name;
    private String host;
    private int port;
    private String context;
    private String fileName;
    private ResourceData[] resources;

    /**
     * Get application name.
     *
     * @return Application name
     */
    public String getName() {
        return name;
    }

    /**
     * Set application name.
     *
     * @param name Application name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get host value.
     *
     * @return Host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Set host value.
     *
     * @param host Host name
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get port value.
     *
     * @return Port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set port value.
     *
     * @param port Port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get app context.
     *
     * @return Context
     */
    public String getContext() {
        return context;
    }

    /**
     * Set app context.
     *
     * @param context Context
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Get resource data.
     *
     * @return Array of {@link ResourceData}
     */
    public ResourceData[] getResources() {
        return resources;
    }


    /**
     * Set resource data.
     *
     * @param resources Array of {@link ResourceData}
     */
    public void setResources(ResourceData[] resources) {
        this.resources = resources;
    }

    /**
     * Get file name.
     *
     * @return File name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set file name.
     *
     * @param fileName file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}