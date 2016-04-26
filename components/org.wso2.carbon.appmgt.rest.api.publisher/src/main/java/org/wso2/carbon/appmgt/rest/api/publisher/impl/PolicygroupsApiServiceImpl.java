package org.wso2.carbon.appmgt.rest.api.publisher.impl;

import org.wso2.carbon.appmgt.rest.api.publisher.PolicygroupsApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyGroupDTO;

import javax.ws.rs.core.Response;

public class PolicygroupsApiServiceImpl extends PolicygroupsApiService {

    @Override
    public Response policygroupsPost(PolicyGroupDTO body, String contentType, String ifModifiedSince) {
        return null;
    }

    @Override
    public Response policygroupsPolicyGroupIdPut(Integer policyGroupId, PolicyGroupDTO body, String contentType,
                                                 String ifMatch, String ifUnmodifiedSince) {
        return null;
    }
}
