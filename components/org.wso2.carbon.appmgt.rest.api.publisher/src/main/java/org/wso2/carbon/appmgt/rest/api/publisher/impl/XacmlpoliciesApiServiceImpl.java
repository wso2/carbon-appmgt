package org.wso2.carbon.appmgt.rest.api.publisher.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyPartial;
import org.wso2.carbon.appmgt.rest.api.publisher.XacmlpoliciesApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialInfoDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialListDTO;
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
            List<EntitlementPolicyPartial> policyPartialList = apiProvider.getSharedPolicyPartialsList();

            if (policyPartialList.size() == 0) {
                String errorMessage = "XACML policies";
                return RestApiUtil.buildNotFoundException(errorMessage, null).getResponse();
            }

            List<PolicyPartialInfoDTO> allMatchedPolicies = new ArrayList<>();
            for (EntitlementPolicyPartial entitlementPolicyPartial : policyPartialList) {
                PolicyPartialInfoDTO policyPartialInfoDTO = new PolicyPartialInfoDTO();
                policyPartialInfoDTO.setPolicyPartialId(entitlementPolicyPartial.getPolicyPartialId());
                policyPartialInfoDTO.setPolicyPartialName(entitlementPolicyPartial.getPolicyPartialName());
                allMatchedPolicies.add(policyPartialInfoDTO);
            }

            policyPartialListDTO = XacmlMappingUtil.fromAPIListToDTO(allMatchedPolicies, offset, limit);
            XacmlMappingUtil.setPaginationParams(policyPartialListDTO, offset, limit, allMatchedPolicies.size());
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving Provider details";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(policyPartialListDTO).build();
    }

    @Override
    public Response xacmlpoliciesPolicyPartialIdAppsGet(Integer policyPartialId, Integer limit, Integer offset,
                                                        String accept, String ifNoneMatch) {
        return null;
    }
}
