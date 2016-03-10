/*
 *   Copyright (c) 2005-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.appmgt.mdm.wso2mdm;

import org.wso2.carbon.appmgt.mdm.wso2mdm.handlers.ExternalOperationHandler;
import org.wso2.carbon.appmgt.mdm.wso2mdm.handlers.InternalOperationHandler;
import java.util.HashMap;

/**
 * MDMOperationFactory class return the suitable OperationHandler Impl
 */
public class MDMOperationFactory {

    /**
     * returns the related Operation Handler implementation to the provided configuration.
     * @param configProperties
     * @return OperationHandler Instance
     */
    public static OperationHandler getOperationHandler(HashMap<String, String> configProperties){
        if (Boolean.parseBoolean(Constants.PROPERTY_IS_REMOTE)) {
            return new ExternalOperationHandler();
        } else {
            return new InternalOperationHandler();
        }
    }
}
