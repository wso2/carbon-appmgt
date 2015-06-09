/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.appmgt.sample.deployer.appcontroller;

import org.apache.log4j.Logger;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;

/**
 * This class is use to deploy war file in server
 */
public class BackEndApplicationCreator {

    final static Logger log = Logger.getLogger(BackEndApplicationCreator.class.getName());
    private static String homePath = CarbonUtils.getCarbonHome();

    /**
     * This method is use deploy war file according to given name
     *
     * @param warFileName Name of the war file that need to be deploy
     * @throws AppManagementException Throws this when failed to deploy web application
     */
    public void copyFileUsingFileStreams(String warFileName) throws AppManagementException {
        File warFile = new File(homePath + "/samples/" + warFileName + ".war");
        log.info(warFile.getAbsolutePath());
        File outputFolder = new File(homePath + "/repository/deployment/server/webapps/" + warFile.getName());
        InputStream is = null;
        OutputStream os = null;

        try {
            is = new FileInputStream(warFile);
            os = new FileOutputStream(outputFolder);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            log.error("Error while deploying a " + warFileName + ".war", e);
            throw new AppManagementException("Error while deploying a " + warFileName + ".war", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                String erroMessge = "Error while deploying a " + warFileName + ".war";
                log.error(erroMessge, e);
                throw new AppManagementException(erroMessge, e);
            }

        }
        File webAppFolder = new File(homePath + "/repository/deployment/server/webapps/" + warFileName);
        while (!webAppFolder.exists()) {
        }
    }


}

