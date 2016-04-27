/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyPartial;
import org.wso2.carbon.appmgt.rest.api.publisher.AdministrationApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialDTO;
import org.wso2.carbon.appmgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class AdministrationApiServiceImpl extends AdministrationApiService {
    private static final Log log = LogFactory.getLog(AdministrationApiServiceImpl.class);

    @Override
    public Response administrationPolicygroupsPolicyGroupIdAppsGet(Integer policyGroupId, String accept,
                                                                   String ifNoneMatch, String ifModifiedSince) {
        return null;
    }

    @Override
    public Response administrationXacmlpoliciesPost(PolicyPartialDTO body, String contentType, String ifModifiedSince) {
        return null;
    }

    @Override
    public Response administrationXacmlpoliciesValidatePost(PolicyPartialDTO body, String contentType,
                                                            String ifModifiedSince) {
        return null;
    }

    @Override
    public Response administrationXacmlpoliciesPolicyPartialIdGet(Integer policyPartialId, String accept,
                                                                  String ifNoneMatch, String ifModifiedSince) {
        PolicyPartialDTO policyPartialDTO = new PolicyPartialDTO();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            //get policy details related to id
            EntitlementPolicyPartial entitlementPolicyPartial = apiProvider.getPolicyPartial(policyPartialId);
            if (entitlementPolicyPartial == null) {
                return RestApiUtil.buildNotFoundException("XACML Policy Partial", policyPartialId.toString())
                        .getResponse();
            }
            policyPartialDTO.setPolicyPartialId(policyPartialId);
            policyPartialDTO.setPolicyPartialName(entitlementPolicyPartial.getPolicyPartialName());
            policyPartialDTO.setPolicyPartial(entitlementPolicyPartial.getPolicyPartialContent());
            policyPartialDTO.setPolicyPartialDesc(entitlementPolicyPartial.getDescription());
            policyPartialDTO.setIsSharedPartial(entitlementPolicyPartial.isShared());

        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving details of XACML Policy Partial Id : " + policyPartialId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(policyPartialDTO).build();
    }

    @Override
    public Response administrationXacmlpoliciesPolicyPartialIdPut(Integer policyPartialId, PolicyPartialDTO body,
                                                                  String contentType, String ifMatch,
                                                                  String ifUnmodifiedSince) {
        return null;
    }

    @Override
    public Response administrationXacmlpoliciesPolicyPartialIdDelete(Integer policyPartialId, String ifMatch,
                                                                     String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String currentUser = RestApiUtil.getLoggedInUsername();
            //check if policy id is valid
            if (apiProvider.getPolicyPartial(policyPartialId) == null) {
                return RestApiUtil.buildNotFoundException("XACML Policy", policyPartialId.toString()).getResponse();
            }
            //delete policy partail
            apiProvider.deleteEntitlementPolicyPartial(policyPartialId, currentUser);
        } catch (AppManagementException e) {
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getMessage(),
                                                        500l, e.getCause().getMessage());
            throw new InternalServerErrorException(errorDTO);
        }
        return Response.ok().build();
    }
}
