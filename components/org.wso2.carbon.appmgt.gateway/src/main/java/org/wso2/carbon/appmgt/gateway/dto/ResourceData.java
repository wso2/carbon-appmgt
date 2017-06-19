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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appmgt.gateway.dto;

import org.apache.synapse.rest.RESTConstants;

/**
 * DTO object which maps to resources in synapse rest APIs.
 */
public class ResourceData {
    private String[] httpMethods;
    private String contentType;
    private String userAgent;
    private int protocol = RESTConstants.PROTOCOL_HTTP_AND_HTTPS;
    private String inSequenceKey;
    private String outSequenceKey;
    private String faultSequenceKey;
    private String uriTemplate;
    private String urlMapping;
    private String inSequenceXml;
    private String outSequenceXml;
    private String faultSequenceXml;

    /**
     * Get http methods.
     *
     * @return Http methods as a string array
     */
    public String[] getHttpMethods() {
        return httpMethods;
    }

    /**
     * Set http methods.
     *
     * @param httpMethods Http methods
     */
    public void setHttpMethods(String[] httpMethods) {
        this.httpMethods = httpMethods;
    }

    /**
     * Get content type.
     *
     * @return Content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Set content type.
     *
     * @param contentType Content type of resource
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get user agent.
     *
     * @return User agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Set user agent.
     *
     * @param userAgent User agent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Get http protocol.
     *
     * @return Resource protocol
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * Set http protocol.
     *
     * @param protocol Http protocol
     */
    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    /**
     * Get in sequence key.
     *
     * @return In sequence key
     */
    public String getInSequenceKey() {
        return inSequenceKey;
    }

    /**
     * Set in sequence key.
     *
     * @param inSequenceKey In sequence key
     */
    public void setInSequenceKey(String inSequenceKey) {
        this.inSequenceKey = inSequenceKey;
    }

    /**
     * Get out sequence key.
     *
     * @return Out sequence key
     */
    public String getOutSequenceKey() {
        return outSequenceKey;
    }

    /**
     * Set out sequence key.
     *
     * @param outSequenceKey Out sequence key
     */
    public void setOutSequenceKey(String outSequenceKey) {
        this.outSequenceKey = outSequenceKey;
    }

    /**
     * Get fault sequence key.
     *
     * @return Fault sequence key
     */
    public String getFaultSequenceKey() {
        return faultSequenceKey;
    }

    /**
     * Set fault sequence key.
     *
     * @param faultSequenceKey Fault sequence key
     */
    public void setFaultSequenceKey(String faultSequenceKey) {
        this.faultSequenceKey = faultSequenceKey;
    }

    /**
     * Get uri template.
     *
     * @return Uri template
     */
    public String getUriTemplate() {
        return uriTemplate;
    }

    /**
     * Set uri template.
     *
     * @param uriTemplate Uri template
     */
    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    /**
     * Get url mapping.
     *
     * @return Url mapping
     */
    public String getUrlMapping() {
        return urlMapping;
    }

    /**
     * Set url mapping.
     *
     * @param urlMapping Url mapping
     */
    public void setUrlMapping(String urlMapping) {
        this.urlMapping = urlMapping;
    }

    /**
     * Get in sequence xml.
     *
     * @return In sequence xml
     */
    public String getInSequenceXml() {
        return inSequenceXml;
    }

    /**
     * Set in sequence xml.
     *
     * @param inSequenceXml In sequence xml
     */
    public void setInSequenceXml(String inSequenceXml) {
        this.inSequenceXml = inSequenceXml;
    }

    /**
     * Get out sequence xml.
     *
     * @return Out sequence xml
     */
    public String getOutSequenceXml() {
        return outSequenceXml;
    }

    /**
     * Set out sequence xml.
     *
     * @param outSequenceXml Out sequence xml
     */
    public void setOutSequenceXml(String outSequenceXml) {
        this.outSequenceXml = outSequenceXml;
    }

    /**
     * Get fault sequence xml.
     *
     * @return Fault sequence xml
     */
    public String getFaultSequenceXml() {
        return faultSequenceXml;
    }

    /**
     * Set fault sequence xml.
     *
     * @param faultSequenceXml Fault sequence xml
     */
    public void setFaultSequenceXml(String faultSequenceXml) {
        this.faultSequenceXml = faultSequenceXml;
    }

}