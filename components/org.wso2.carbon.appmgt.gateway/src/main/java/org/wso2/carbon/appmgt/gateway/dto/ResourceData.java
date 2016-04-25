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

public class ResourceData {
    private String[] methods = new String[4];
    private String contentType;
    private String userAgent;
    private int protocol = RESTConstants.PROTOCOL_HTTP_AND_HTTPS;
    private String inSequenceKey;
    private String outSequenceKey;
    private String faultSequenceKey;
    private String uriTemplate;
    private String urlMapping;
    private String inSeqXml;
    private String outSeqXml;
    private String faultSeqXml;

    /**
     * Get Http methods.
     * @return http methods as a string array.
     */
    public String[] getMethods() {
        return methods;
    }

    /**
     * Set Http methods.
     * @param methods in String[].
     */
    public void setMethods(String[] methods) {
        this.methods = methods;
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
    public String getInSeqXml() {
        return inSeqXml;
    }

    /**
     * Set In Sequence Xml.
     * @param inSeqXml in String.
     */
    public void setInSeqXml(String inSeqXml) {
        if (inSeqXml == null) {
            return;
        }
        this.inSeqXml = inSeqXml.trim().replaceAll("&gt", ">").replaceAll("&lt", "<").replaceAll("\n", "").replaceAll(
                "\t", " ");
    }

    /**
     * Get Out Sequence Xml.
     * @return out sequence xml in String.
     */
    public String getOutSeqXml() {
        return outSeqXml;
    }

    /**
     * Set Out Sequence Xml.
     * @param outSeqXml in String.
     */
    public void setOutSeqXml(String outSeqXml) {
        if (outSeqXml == null) {
            return;
        }
        this.outSeqXml = outSeqXml.trim().replaceAll("&gt", ">").replaceAll("&lt", "<").replaceAll("\n", "").replaceAll(
                "\t", " ");
    }

    /**
     * Get Fault Sequence Xml.
     * @return fault sequence xml in String.
     */
    public String getFaultSeqXml() {
        return faultSeqXml;
    }

    /**
     * Set Fault Sequence Xml.
     * @param faultSeqXml in String.
     */
    public void setFaultSeqXml(String faultSeqXml) {
        if (faultSeqXml == null) {
            return;
        }
        this.faultSeqXml = faultSeqXml.trim().replaceAll("&gt", ">").replaceAll("&lt", "<").replaceAll("\n", "")
                .replaceAll("\t", " ");
    }

}