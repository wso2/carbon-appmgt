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

package org.wso2.carbon.appmgt.impl.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.appmgt.api.AppManagementException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Configuration for application discovery handlers
 *
 */
public class AppDiscoveryConfiguration {

    private static final String TAG_DISCOVERY_CONFIGURATION = "DiscoveryConfiguration";
    private static final String TAG_DISCOVERY_HANDLERS = "DiscoveryHandlers";
    private static final String TAG_DISCOVERY_HANDLER = "Handler";
    private static final String ATTRIBUTE_HANDLER_NAME = "name";
    private static final String ATTRIBUTE_HANDLER_CLASS_NAME = "class";

    private Map<String, String> handlers = Collections.emptyMap();

    /**
     * Returns a map of handler name-> class for available handlers
     * The resultant map is a one generated form internal data.
     * Any changes to resultant map in the client code will not affect future calls to this method.
     *
     * @return
     */
    public Map<String, String> getHandlersMap() {
        return Collections.unmodifiableMap(handlers);
    }

    /**
     * Loads the discovery handler configuration from the given file path
     * Load method will reset the internal structure.
     *
     * @param filePath
     * @throws AppManagementException
     */
    public void load(String filePath) throws AppManagementException {

        try {
            InputStream in = FileUtils.openInputStream(new File(filePath));
            load(in);
        } catch (IOException e) {
            throw new AppManagementException("Error while parsing the WebApp manager " +
                    "configuration: " + filePath, e);
        }

    }

    /**
     * Loads the configuration data from an XML which is in the given input stream
     * @param inputStream
     * @throws AppManagementException
     */
    protected void load(InputStream inputStream) throws AppManagementException {
        Map<String, String> result = new HashMap<String, String>();
        try {
            StAXOMBuilder builder = new StAXOMBuilder(inputStream);
            readChildElements(builder.getDocumentElement(), result);
            handlers = result;
        } catch (XMLStreamException e) {
            throw new AppManagementException(
                    "Error while parsing the WebApp manager configuration ", e);
        } catch (OMException e) {
            throw new AppManagementException(
                    "Error while parsing the WebApp manager configuration ", e);
        } finally {
            IOUtils.closeQuietly(inputStream);                                                                                                                        ;
        }
    }

    /**
     * Skip all the other elements and read only the interested elements related to App Discovery
     * @param documentElement
     * @param result
     */
    private void readChildElements(OMElement documentElement, Map<String, String> result) {
        for (Iterator childElements = documentElement.getChildElements(); childElements
                .hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            if (TAG_DISCOVERY_CONFIGURATION.equals(element.getLocalName())) {
                readDiscoveryConfigurationElement(element, result);
            }
        }
    }

    private void readDiscoveryConfigurationElement(OMElement discoveryConfigElement, Map<String, String> result) {
        for (Iterator childElements = discoveryConfigElement.getChildElements(); childElements
                .hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            if (TAG_DISCOVERY_HANDLERS.equals(element.getLocalName())) {
                readDiscoveryHandlersElement(element, result);
            }
        }
    }

    private void readDiscoveryHandlersElement(OMElement discoveryHandlersElement, Map<String, String> result) {
        for (Iterator childElements = discoveryHandlersElement.getChildElements(); childElements
                .hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            if (TAG_DISCOVERY_HANDLER.equals(element.getLocalName())) {
                String name = element.getAttributeValue(new QName(null, ATTRIBUTE_HANDLER_NAME));
                String className = element.getAttributeValue(new QName(null, ATTRIBUTE_HANDLER_CLASS_NAME));
                if (name != null && className != null) {
                    result.put(name, className);
                }
            }
        }
    }
}
