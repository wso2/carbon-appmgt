package org.wso2.carbon.appmgt.sampledeployer.appm;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException;
import org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceStub;
import org.wso2.carbon.claim.mgt.stub.dto.ClaimDTO;
import org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO;
import java.rmi.RemoteException;

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

public class ClaimManagementServiceClient {

    private ClaimManagementServiceStub claimManagementServiceStub;

    public ClaimManagementServiceClient(String cookie, String url) throws AxisFault {
        String serviceURL = url + "/services/ClaimManagementService";
        claimManagementServiceStub = new ClaimManagementServiceStub(serviceURL);
        ServiceClient svcClient = claimManagementServiceStub._getServiceClient();
        Options option;
        option = svcClient.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void addClaim(String description, String claimURI, boolean isRequired) throws RemoteException,
            ClaimManagementServiceException {
        ClaimDTO claimDTO = new ClaimDTO();
        claimDTO.setDialectURI("http://wso2.org/claims");
        claimDTO.setClaimUri(claimURI);
        claimDTO.setDisplayTag(description);
        claimDTO.setDescription(description);
        claimDTO.setSupportedByDefault(true);
        claimDTO.setReadOnly(false);
        claimDTO.setRequired(isRequired);
        claimDTO.setDisplayOrder(0);
        ClaimMappingDTO claimMappingDTO = new ClaimMappingDTO();
        claimMappingDTO.setClaim(claimDTO);
        claimMappingDTO.setMappedAttribute(description);
        claimManagementServiceStub.addNewClaimMapping(claimMappingDTO);
    }


}
