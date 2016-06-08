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
 * Http Resources Data.
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
     * Get Http methods.
     * @return http methods as a string array.
     */
    public String[] getHttpMethods() {
        return httpMethods;
    }

    /**
     * Set Http methods.
     * @param httpMethods in String[].
     */
    public void setHttpMethods(String[] httpMethods) {
        this.httpMethods = httpMethods;
    }

    /**
     * Get Content Type.
     * @return content type in String.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Set Content Type.
     * @param contentType in String.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get User Agent.
     * @return user agent in String.
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Set User Agent.
     * @param userAgent in String.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Get Http Protocol.
     * @return protocol in int.
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * Set Http Protocol.
     * @param protocol int.
     */
    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    /**
     * Get In Sequence Key.
     * @return in sequence key in String.
     */
    public String getInSequenceKey() {
        return inSequenceKey;
    }

    /**
     * Set In Sequence Key.
     * @param inSequenceKey String.
     */
    public void setInSequenceKey(String inSequenceKey) {
        this.inSequenceKey = inSequenceKey;
    }

    /**
     * Get Out Sequence Key.
     * @return out sequence key in String.
     */
    public String getOutSequenceKey() {
        return outSequenceKey;
    }

    /**
     * Set Out Sequence Key.
     * @param outSequenceKey String.
     */
    public void setOutSequenceKey(String outSequenceKey) {
        this.outSequenceKey = outSequenceKey;
    }

    /**
     * Get Fault Sequence Key.
     * @return fault sequence key in String.
     */
    public String getFaultSequenceKey() {
        return faultSequenceKey;
    }

    /**
     * Set Fault Sequence Key.
     * @param faultSequenceKey String.
     */
    public void setFaultSequenceKey(String faultSequenceKey) {
        this.faultSequenceKey = faultSequenceKey;
    }

    /**
     * Get Uri Template.
     * @return uri template in String.
     */
    public String getUriTemplate() {
        return uriTemplate;
    }

    /**
     * Set Uri Template.
     * @param uriTemplate String.
     */
    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    /**
     * Get Url Mapping.
     * @return url mapping in String.
     */
    public String getUrlMapping() {
        return urlMapping;
    }

    /**
     * Set Url Mapping.
     * @param urlMapping String.
     */
    public void setUrlMapping(String urlMapping) {
        this.urlMapping = urlMapping;
    }

    /**
     * Get In Sequence Xml.
     * @return in sequence xml in String.
     */
    public String getInSequenceXml() {
        return inSequenceXml;
    }

    /**
     * Set In Sequence Xml.
     * @param inSequenceXml in String.
     */
    public void setInSequenceXml(String inSequenceXml) {
        this.inSequenceXml = inSequenceXml;
    }

    /**
     * Get Out Sequence Xml.
     * @return out sequence xml in String.
     */
    public String getOutSequenceXml() {
        return outSequenceXml;
    }

    /**
     * Set Out Sequence Xml.
     * @param outSequenceXml in String.
     */
    public void setOutSequenceXml(String outSequenceXml) {
        this.outSequenceXml = outSequenceXml;
    }

    /**
     * Get Fault Sequence Xml.
     * @return fault sequence xml in String.
     */
    public String getFaultSequenceXml() {
        return faultSequenceXml;
    }

    /**
     * Set Fault Sequence Xml.
     * @param faultSequenceXml in String.
     */
    public void setFaultSequenceXml(String faultSequenceXml) {
        this.faultSequenceXml = faultSequenceXml;
    }

}