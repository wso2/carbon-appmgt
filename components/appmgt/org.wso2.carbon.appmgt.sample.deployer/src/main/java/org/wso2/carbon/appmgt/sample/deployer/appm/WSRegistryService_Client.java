/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.sample.deployer.appm;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.appmgt.sample.deployer.configuration.Configuration;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class WSRegistryService_Client {
    final static Logger log = Logger.getLogger(WSRegistryService_Client.class.getName());
    private static final String axis2Repo = CarbonUtils.getCarbonHome() + File.separator + "repository" +
            File.separator + "deployment" + File.separator + "client";
    private static final String axis2Conf =
            ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation");
    private Registry wsRegistryServiceClient;

    public WSRegistryService_Client(String backEndUrl) throws RegistryException, AxisFault {
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                axis2Repo, axis2Conf);
        wsRegistryServiceClient = new WSRegistryServiceClient(backEndUrl + "/services/",
                Configuration.getUserName(), Configuration.getPassword(), configContext);
    }

    public String getUUID(String path) throws RegistryException {
        Resource resource = wsRegistryServiceClient.get(path);
        return resource.getUUID();
    }
}

