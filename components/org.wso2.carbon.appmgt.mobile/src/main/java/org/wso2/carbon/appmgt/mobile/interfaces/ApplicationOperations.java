/*
 *
 *   Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.mobile.interfaces;

import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationAction;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationDevice;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.utils.MobileApplicationException;

import java.util.List;

/**
 * Interface for MDM Operations which connects with MDM components
 *
 */
public interface ApplicationOperations {

    /**
     *
     * This used to perform an action ex: install, uninstall, update apps on devices
     *
     * @param applicationOperationAction holds the information needs to perform an action on mdm
     * @return An ID which is a reference to the operation being performed.
     * @throws MobileApplicationException
     */
    String performAction(ApplicationOperationAction applicationOperationAction) throws MobileApplicationException;

	/**
     *  This used to get the device list from mdm
     *
     * @param applicationOperationDevice holds the information needs to retrieve device list
     * @return Devices list
     * @throws MobileApplicationException
     */
    List<Device> getDevices(ApplicationOperationDevice applicationOperationDevice) throws MobileApplicationException;

}
