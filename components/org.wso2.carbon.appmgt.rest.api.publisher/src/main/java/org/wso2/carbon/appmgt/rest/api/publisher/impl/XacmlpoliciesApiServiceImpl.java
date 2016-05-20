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
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyPartial;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.rest.api.publisher.XacmlpoliciesApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialInfoDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings.XacmlMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class XacmlpoliciesApiServiceImpl extends XacmlpoliciesApiService {

    private static final Log log = LogFactory.getLog(XacmlpoliciesApiServiceImpl.class);


    @Override
    public Response xacmlpoliciesGet(Integer limit, Integer offset, String accept, String ifNoneMatch) {
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        PolicyPartialListDTO policyPartialListDTO = new PolicyPartialListDTO();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            //get all available xacml policies list
            List<EntitlementPolicyPartial> policyPartialList = apiProvider.getSharedPolicyPartialsList();

            if (policyPartialList.size() == 0) {
                return RestApiUtil.buildNotFoundException("XACML policies", null).getResponse();
            }

            List<PolicyPartialInfoDTO> allMatchedPolicies = new ArrayList<>();
            for (EntitlementPolicyPartial entitlementPolicyPartial : policyPartialList) {
                PolicyPartialInfoDTO policyPartialInfoDTO = new PolicyPartialInfoDTO();
                policyPartialInfoDTO.setPolicyPartialId(entitlementPolicyPartial.getPolicyPartialId());
                policyPartialInfoDTO.setPolicyPartialName(entitlementPolicyPartial.getPolicyPartialName());
                allMatchedPolicies.add(policyPartialInfoDTO);
            }

            //set list
            policyPartialListDTO = XacmlMappingUtil.fromAPIListToDTO(allMatchedPolicies, offset, limit);
            //set pagination
            XacmlMappingUtil.setPaginationParams(policyPartialListDTO, offset, limit, allMatchedPolicies.size());

            if (policyPartialListDTO.getCount() == 0) {
                return RestApiUtil.buildNotFoundException("XACML policies", null).getResponse();
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving XACML policy details";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(policyPartialListDTO).build();
    }

    @Override
    public Response xacmlpoliciesPolicyPartialIdAppsAppTypeGet(Integer policyPartialId, String appType, Integer limit,
                                                               Integer offset, String accept, String ifNoneMatch) {
        //Currently supports only webapps.
        if (!AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
            RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
        }
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        AppListDTO appListDTO = new AppListDTO();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            //get all associated apps for given policy partial id
            List<APIIdentifier> apiIdentifiers = apiProvider.getAssociatedApps(policyPartialId);
            if (apiIdentifiers.size() == 0) {
                return RestApiUtil.buildNotFoundException("Apps", null).getResponse();
            }

            List<App> allMatchedApps = new ArrayList<>();
            for (APIIdentifier identifier : apiIdentifiers) {
                WebApp webApp = apiProvider.getAPI(identifier);
                webApp.setType(appType);
                allMatchedApps.add(webApp);
            }
            //set App list
            appListDTO = APPMappingUtil.fromAPIListToDTO(allMatchedApps, offset, limit);
            if (appListDTO == null) {
                String errorMessage = "No result found.";
                return RestApiUtil.buildNotFoundException(errorMessage, null).getResponse();
            }

            //set pagination
            APPMappingUtil.setPaginationParams(appListDTO, "", offset, limit, allMatchedApps.size());

            if (appListDTO.getCount() == 0) {
                return RestApiUtil.buildNotFoundException("Apps", null).getResponse();
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving App details";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(appListDTO).build();
    }


}
