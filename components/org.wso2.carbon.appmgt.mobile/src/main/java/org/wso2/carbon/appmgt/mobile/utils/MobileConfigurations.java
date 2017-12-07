/*
 *
 *   Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.appmgt.mobile.utils;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class which is responsible for reading mobile configuration from app-manager.xml
 */
public class MobileConfigurations {

    private static final String CONFIG_FILE_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator + "app-manager.xml";
    private static MobileConfigurations mobileConfigurations;

    private String mdmServerURL;

    private OMElement documentElement;
    QName mobileConfElement;

    private static final Log log = LogFactory.getLog(MobileConfigurations.class);

    private static String activeMDMBundle;
    private static HashMap<String, String> activeMDMProperties;
    private static HashMap<String, String> mDMConfigs;

    public final static String  ENABLED = "Enabled";
    public final static String  ENABLE_SAMPLE_DEVICES = "EnableSampleDevices";
    public final static String  APP_DOWNLOAD_URL_HOST = "AppDownloadURLHost";
    public final static String  APP_GET_URL = "/store/api/mobileapp/getfile/";
    public final static String  ACTIVE_MDM = "ActiveMDM";
    public final static String  IOS_PLIST_PATH = "IosPlistPath";
    public final static String  ENTERPRISE_OPERATIONS_ENABLED = "EnterpriseOperations_Enabled";
    public final static String  ENTERPRISE_OPERATIONS_AUTHORIZED_ROLE = "EnterpriseOperations_AuthorizedRole";


    private MobileConfigurations(){
        XMLStreamReader parser = null;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(CONFIG_FILE_PATH));
        } catch (XMLStreamException e) {
            log.error("XML Parsing issue :" + e.getMessage());
        } catch (FileNotFoundException e) {
            log.error("App Manager XML not found :" + e.getMessage());
        }
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        documentElement = builder.getDocumentElement();
        mobileConfElement = new QName("MobileAppsConfiguration");

    }


    public static MobileConfigurations getInstance(){
        if(mobileConfigurations == null){
            mobileConfigurations = new MobileConfigurations();
        }
        return mobileConfigurations;
    }

    /**
     *
     * @return list of active MDM properties
     */
    public HashMap<String, String> getActiveMDMProperties(){
        if(activeMDMProperties == null){

            OMElement mdmPropertiesElement = documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMProperties"));

            Iterator<OMElement> iterator = mdmPropertiesElement.getChildElements();
            while (iterator.hasNext()){
                OMElement mdmElement = iterator.next();
                if(getMDMConfigs().get(ACTIVE_MDM).equals(mdmElement.getAttributeValue(new QName("name")))){
                    HashMap<String, String> properties = new HashMap<String, String>();
                    Iterator<OMElement> propertiesElement = mdmElement.getChildElements();
                    while(propertiesElement.hasNext()){
                        OMElement propertyElement = propertiesElement.next();
                        properties.put(propertyElement.getAttributeValue(new QName("name")), propertyElement.getText());
                    }
                    return activeMDMProperties = properties;
                }
            }

        }

        return activeMDMProperties;
    }

    /**
     *
     * @return list of active MDM configurations
     */
    public HashMap<String, String> getMDMConfigs(){
        if(mDMConfigs == null){

            OMElement mDMConfigsElement = documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMConfig"));
            HashMap<String, String> configs = new HashMap<String, String>();
            Iterator<OMElement> iterator = mDMConfigsElement.getChildElements();

            while(iterator.hasNext()){
                OMElement propertyElement = iterator.next();
                configs.put(propertyElement.getAttributeValue(new QName("name")), propertyElement.getText());
            }
            return mDMConfigs = configs;
        }

        return mDMConfigs;
    }

    /**
     *
     * @return the bundle id of the active MDM
     */
    public String getActiveMDMBundle(){
        if(activeMDMBundle == null){

            OMElement mdmPropertiesElement = documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMProperties"));

            Iterator<OMElement> iterator = mdmPropertiesElement.getChildElements();
            while (iterator.hasNext()){
                OMElement mdmElement = iterator.next();
                if(getMDMConfigs().get(ACTIVE_MDM).equals(mdmElement.getAttributeValue(new QName("name")))){
                    return activeMDMBundle =  mdmElement.getAttributeValue(new QName("bundle"));
                }
            }
        }

        return activeMDMBundle;
    }




}
