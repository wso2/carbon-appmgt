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


public class MobileConfigurations {

    private static final String CONFIG_FILE_PATH = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "conf" + File.separator + "app-manager.xml";
    private static MobileConfigurations mobileConfigurations;

    private String mdmServerURL;

    private OMElement documentElement;
    QName mobileConfElement;

    private static final Log log = LogFactory.getLog(MobileConfigurations.class);

    private static String activeMDMBundle;
    private static HashMap<String, String> activeMDMProperties;
    private static String activeMDM;
    private static String appDownloadHost;
    private static String iosPlistPath;
    private static Boolean sampleDevicesEnabled;
    private static Boolean mdmEnabled;

    private static Boolean isEnterpriseOperationsEnabled;
    private static String  enterpriseAuthorizedRole;

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


    public boolean isMDMEnabled(){
        if(mdmEnabled == null){
            return mdmEnabled =  Boolean.valueOf(documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMSettings"))
                    .getFirstChildWithName(new QName("Enabled"))
                    .getText());

        }
        return  mdmEnabled;
    }

    public boolean isSampleDevicesEnabled(){
        if(sampleDevicesEnabled == null){
            return sampleDevicesEnabled = Boolean.valueOf(documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMSettings"))
                    .getFirstChildWithName(new QName("EnableSampleDevices"))
                    .getText());

        }
        return  sampleDevicesEnabled;
    }

    public boolean isEnterpriseOperationsEnabled(){
        if(isEnterpriseOperationsEnabled == null){
            return isEnterpriseOperationsEnabled = Boolean.valueOf(documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMSettings"))
                    .getFirstChildWithName(new QName("EnterpriseOperations"))
                    .getFirstChildWithName(new QName("Enabled"))
                    .getText());

        }
        return  isEnterpriseOperationsEnabled;
    }


    public String getEnterpriseAuthorizedRole(){
        if(enterpriseAuthorizedRole == null){
            return enterpriseAuthorizedRole = documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMSettings"))
                    .getFirstChildWithName(new QName("EnterpriseOperations"))
                    .getFirstChildWithName(new QName("AuthorizedRole"))
                    .getText();

        }
        return enterpriseAuthorizedRole;
    }

    public String getAppDownloadHost(){
        if(appDownloadHost == null){
            return appDownloadHost = documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMSettings"))
                    .getFirstChildWithName(new QName("AppDownloadURLHost"))
                    .getText();

        }
        return appDownloadHost;
    }

    public String getActiveMDM(){
        if(activeMDM == null){
            return activeMDM = documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMSettings"))
                    .getFirstChildWithName(new QName("ActiveMDM"))
                    .getText();
        }
        return activeMDM;
    }


    public String getIosPlistPath(){
        if(iosPlistPath == null){
            return iosPlistPath = documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMSettings"))
                    .getFirstChildWithName(new QName("IosPlistPath"))
                    .getText();
        }
        return iosPlistPath;
    }

    public HashMap<String, String> getActiveMDMProperties(){
        if(activeMDMProperties == null){

            OMElement mdmPropertiesElement = documentElement.getFirstChildWithName(mobileConfElement)
                    .getFirstChildWithName(new QName("MDMProperties"));

            Iterator<OMElement> iterator = mdmPropertiesElement.getChildElements();
            while (iterator.hasNext()){
                OMElement mdmElement = iterator.next();
                if(getActiveMDM().equals(mdmElement.getAttributeValue(new QName("name")))){
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

    public String getActiveMDMBundle(){
        if(activeMDMBundle == null){

            OMElement mdmPropertiesElement = documentElement.getFirstChildWithName(mobileConfElement).getFirstChildWithName(new QName("MDMProperties"));

            Iterator<OMElement> iterator = mdmPropertiesElement.getChildElements();
            while (iterator.hasNext()){
                OMElement mdmElement = iterator.next();
                if(getActiveMDM().equals(mdmElement.getAttributeValue(new QName("name")))){
                    return activeMDMBundle =  mdmElement.getAttributeValue(new QName("bundle"));
                }
            }
        }

        return activeMDMBundle;
    }




}
