package org.wso2.carbon.appmgt.rest.api.publisher.impl;

import org.wso2.carbon.appmgt.rest.api.publisher.AdministrationApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialDTO;

import javax.ws.rs.core.Response;

public class AdministrationApiServiceImpl extends AdministrationApiService {

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
        return null;
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
        return null;
    }
}
