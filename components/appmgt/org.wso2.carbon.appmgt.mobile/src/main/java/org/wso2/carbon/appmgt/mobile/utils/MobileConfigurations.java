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


public class MobileConfigurations {

    private static final String CONFIG_FILE_PATH = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "conf" + File.separator + "app-manager.xml";
    private static MobileConfigurations mobileConfigurations;

    private String mdmServerURL;

    private OMElement documentElement;
    QName mobileConfElement;

    private static final Log log = LogFactory.getLog(MobileConfigurations.class);

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


    public String getMDMServerURL() {
        return documentElement.getFirstChildWithName(mobileConfElement).getFirstChildWithName(new QName("MDM")).getFirstChildWithName(new QName("ServerUrl")).getText();
    }


    public boolean isMDMEnabled(){
        return Boolean.valueOf(documentElement.getFirstChildWithName(mobileConfElement).getFirstChildWithName(new QName("MDM")).getFirstChildWithName(new QName("Enabled")).getText());
    }

    public String getMDMOperationsClass(){
        return documentElement.getFirstChildWithName(mobileConfElement).getFirstChildWithName(new QName("MDM")).getFirstChildWithName(new QName("OperationsClass")).getText();
    }

    public boolean isSampleDevicesEnabled(){
        return Boolean.valueOf(documentElement.getFirstChildWithName(mobileConfElement).getFirstChildWithName(new QName("MDM")).getFirstChildWithName(new QName("EnableSampleDevices")).getText());
    }

    public String getAppDownloadHost(){
        return documentElement.getFirstChildWithName(mobileConfElement).getFirstChildWithName(new QName("MDM")).getFirstChildWithName(new QName("AppDownloadURLHost")).getText();
    }




}
