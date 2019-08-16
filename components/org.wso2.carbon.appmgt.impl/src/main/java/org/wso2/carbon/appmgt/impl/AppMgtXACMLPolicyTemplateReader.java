/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;

import java.io.*;

public class AppMgtXACMLPolicyTemplateReader  {

    private String configuration;

    private static final Log log = LogFactory.getLog(AppManagerConfiguration.class);


    boolean initialized;

    public void load(String filePath) throws AppManagementException {

        if (initialized) {
            return;
        }
        InputStream in = null;
        try {
            in = FileUtils.openInputStream(new File(filePath));
            configuration = fileToBuffer(in);
            initialized = true;
        } catch (IOException e) {
            throw new AppManagementException("I/O error while reading the WebApp manager " +
                    "configuration: " + filePath, e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public String getConfiguration() {
        return configuration;
    }

    public String fileToBuffer(InputStream is) throws IOException{
        StringBuffer stringBuffer = new StringBuffer();
        InputStreamReader inputStreamReader = null;


        try {
            inputStreamReader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        }

        return stringBuffer.toString();
    }



}
