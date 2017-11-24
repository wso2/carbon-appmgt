/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.rest.api.publisher.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.rest.api.publisher.JavapoliciesApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.JavaPolicyDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.JavaPolicyListDTO;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

public class JavapoliciesApiServiceImpl extends JavapoliciesApiService {
    private static final Log log = LogFactory.getLog(JavapoliciesApiServiceImpl.class);


    @Override
    public Response javapoliciesGet(Boolean isGlobal, String accept, String ifNoneMatch, String ifModifiedSince) {
        JavaPolicyListDTO javaPolicyListDTO = new JavaPolicyListDTO();
        List<JavaPolicyDTO> javaPolicyDTOList = new ArrayList<>();
        try {
            //set as true when null
            if (isGlobal == null) {
                isGlobal = true;
            }
            JSONArray availableJavaPolicies = AppMDAO.getAvailableJavaPolicyList(null, isGlobal);
            for (int i = 0; i < availableJavaPolicies.size(); i++) {
                JSONObject javaPolicy = (JSONObject) availableJavaPolicies.get(i);
                JavaPolicyDTO javaPolicyDTO = new JavaPolicyDTO();
                javaPolicyDTO.setDescription(javaPolicy.get("description").toString());
                javaPolicyDTO.setDisplayName(javaPolicy.get("displayName").toString());
                javaPolicyDTO.setId(Integer.parseInt(javaPolicy.get("javaPolicyId").toString()));
                javaPolicyDTO.setDisplayOrder(Integer.parseInt(javaPolicy.get("displayOrder").toString()));
                javaPolicyDTOList.add(javaPolicyDTO);

            }
            javaPolicyListDTO.setPolicyList(javaPolicyDTOList);
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving Java policy details";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(javaPolicyListDTO).build();
    }
}
