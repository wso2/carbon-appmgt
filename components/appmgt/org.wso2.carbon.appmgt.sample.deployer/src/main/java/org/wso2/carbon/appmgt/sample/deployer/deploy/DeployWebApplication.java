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

package org.wso2.carbon.appmgt.sample.deployer.deploy;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
/**
 * This class is use to deploy war file in server
 * */
public class DeployWebApplication {

    final static Logger log = Logger.getLogger(DeployWebApplication.class.getName());
    private static String homePath = CarbonUtils.getCarbonHome();

    /**
     * This method is use deploy war file according to given name
     *
     * @param warFileName
     *            - Name of the war file that need to be deploy
     *
     * @throws AppManagementException
     *             - Throws this when failed to deploy web application
     */
    public void copyFileUsingFileStreams(String warFileName) throws AppManagementException {
        homePath = CarbonUtils.getCarbonHome();
        File souceFile = new File(homePath + "/samples/" + warFileName);
        log.info(souceFile.getAbsolutePath());
        File destinantionFile = new File(homePath + "/repository/deployment/server/webapps/" + warFileName);
        try {
            FileUtils.copyFile(souceFile,
                    destinantionFile);
        } catch (IOException e) {
            log.error("Error while deploying a "+warFileName, e);
            throw  new AppManagementException("Error while deploying a "+warFileName, e);
        }
    }
}
