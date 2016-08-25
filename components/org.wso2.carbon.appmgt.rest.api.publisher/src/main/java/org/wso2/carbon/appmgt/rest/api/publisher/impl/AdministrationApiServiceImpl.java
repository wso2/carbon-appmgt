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
import org.wso2.carbon.appmgt.api.model.BusinessOwner;
import org.wso2.carbon.appmgt.api.model.BusinessOwnerProperty;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyPartial;
import org.wso2.carbon.appmgt.rest.api.publisher.AdministrationApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.BusinessOwnerDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.BusinessOwnerListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.BusinessOwnerPropertiesDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialDTO;
import org.wso2.carbon.appmgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.appmgt.rest.api.util.validation.BeanValidator;
import org.wso2.carbon.utils.xml.StringUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class AdministrationApiServiceImpl extends AdministrationApiService {
    private static final Log log = LogFactory.getLog(AdministrationApiServiceImpl.class);
    BeanValidator beanValidator;

    @Override
    public Response administrationBusinessownerGet(String accept, String ifNoneMatch) {
        BusinessOwnerListDTO businessOwnerListDTO = new BusinessOwnerListDTO();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            //get business owner related to id.
            List<BusinessOwnerDTO> businessOwnerDTOList = new ArrayList<>();
            List<BusinessOwner> businessOwners = apiProvider.getBusinessOwners();
            List<BusinessOwnerProperty> businessOwnerPropertyList;
            if (businessOwners.isEmpty()) {
                return RestApiUtil.buildNotFoundException("Business Owners", null).getResponse();
            }

            for (BusinessOwner businessOwner : businessOwners) {
                BusinessOwnerDTO businessOwnerDTO = new BusinessOwnerDTO();
                businessOwnerDTO.setId(businessOwner.getBusinessOwnerId());
                businessOwnerDTO.setName(businessOwner.getBusinessOwnerName());
                businessOwnerDTO.setEmail(businessOwner.getBusinessOwnerEmail());
                businessOwnerDTO.setDescription(businessOwner.getBusinessOwnerDescription());
                businessOwnerDTO.setSite(businessOwner.getBusinessOwnerSite());
                businessOwnerPropertyList = businessOwner.getBusinessOwnerPropertiesList();
                List<BusinessOwnerPropertiesDTO> businessOwnerPropertiesDTOList = new ArrayList<>();
                //save custom properties of the owner.
                for (BusinessOwnerProperty businessOwnerProperty : businessOwnerPropertyList) {
                    BusinessOwnerPropertiesDTO businessOwnerPropertiesDTO = new BusinessOwnerPropertiesDTO();
                    businessOwnerPropertiesDTO.setKey(businessOwnerProperty.getPropertyId());
                    businessOwnerPropertiesDTO.setValue(businessOwnerProperty.getPropertyValue());
                    businessOwnerPropertiesDTO.setIsVisible(businessOwnerProperty.isShowingInStore());
                    businessOwnerPropertiesDTOList.add(businessOwnerPropertiesDTO);
                }
                businessOwnerDTO.setProperties(businessOwnerPropertiesDTOList);
                businessOwnerDTOList.add(businessOwnerDTO);
            }
            businessOwnerListDTO.setBusinessOwnerList(businessOwnerDTOList);
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving business owners.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(businessOwnerListDTO).build();
    }

    @Override
    public Response administrationBusinessownerPost(BusinessOwnerDTO body, String contentType, String ifModifiedSince) {
        beanValidator = new BeanValidator();
        beanValidator.validate(body);
        BusinessOwnerDTO businessOwnerDTO = new BusinessOwnerDTO();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String ownerName = body.getName();
            String ownerEmail = body.getEmail();
            String ownerDescription = body.getDescription();
            String ownerSite = body.getSite();
            if (StringUtils.isEmpty(ownerName)) {
                RestApiUtil.handleBadRequest("Business owner name cannot be null or empty.", log);
            }
            if (StringUtils.isEmpty(ownerEmail)) {
                RestApiUtil.handleBadRequest("Business owner email cannot be null or empty.", log);
            }
            ownerName = ownerName.trim();
            ownerEmail = ownerEmail.trim();
            int businessOwnerId = apiProvider.getBusinessOwnerId(ownerName, ownerEmail);
            if (businessOwnerId != -1) {
                String message =  "A duplicate business owner already exists with the owner name :  " + ownerName +
                        " and owner email " + ownerEmail;
                RestApiUtil.handleConflictException(message, log);
            }
            BusinessOwner businessOwner = new BusinessOwner();
            businessOwner.setBusinessOwnerName(ownerName);
            businessOwner.setBusinessOwnerEmail(ownerEmail);
            businessOwner.setBusinessOwnerDescription(ownerDescription);
            businessOwner.setBusinessOwnerSite(ownerSite);
            List<BusinessOwnerPropertiesDTO> businessOwnerPropertiesDTOList = body.getProperties();
            List<BusinessOwnerProperty> businessOwnerPropertyList = new ArrayList<>();
            for (BusinessOwnerPropertiesDTO businessOwnerPropertiesDTO : businessOwnerPropertiesDTOList) {
                BusinessOwnerProperty businessOwnerProperty = new BusinessOwnerProperty();
                String propertyId = businessOwnerPropertiesDTO.getKey();
                String propertyValue = businessOwnerPropertiesDTO.getValue();
                Boolean isVisible = businessOwnerPropertiesDTO.getIsVisible();
                if (StringUtils.isEmpty(propertyId) || StringUtils.isEmpty(propertyValue) || isVisible == null) {
                    RestApiUtil.handleBadRequest("Business owner properties cannot be empty or null.", log);
                }
                businessOwnerProperty.setPropertyId(propertyId.trim());
                businessOwnerProperty.setPropertyValue(propertyValue.trim());
                businessOwnerProperty.setShowingInStore(isVisible);
                businessOwnerPropertyList.add(businessOwnerProperty);
            }
            businessOwner.setBusinessOwnerPropertiesList(businessOwnerPropertyList);

            //save business owner.
            int ownerId = apiProvider.saveBusinessOwner(businessOwner);

            businessOwnerDTO.setId(ownerId);
            // remove properties from the response. need business owner id only.
            businessOwnerDTO.setProperties(null);
        } catch (AppManagementException e) {
            String errorMessage = "Error while saving Business Owner.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(businessOwnerDTO).build();
    }

    @Override
    public Response administrationBusinessownerBusinessOwnerIdGet(Integer businessOwnerId, String accept,
                                                                  String ifNoneMatch) {
        BusinessOwnerDTO businessOwnerDTO = new BusinessOwnerDTO();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            //get policy details related to id
            BusinessOwner businessOwner = apiProvider.getBusinessOwner(businessOwnerId);
            if (businessOwner == null) {
                return RestApiUtil.buildNotFoundException("Business Owner ", businessOwnerId.toString()).getResponse();
            }
            businessOwnerDTO.setName(businessOwner.getBusinessOwnerName());
            businessOwnerDTO.setEmail(businessOwner.getBusinessOwnerEmail());
            businessOwnerDTO.setDescription(businessOwner.getBusinessOwnerDescription());
            businessOwnerDTO.setSite(businessOwner.getBusinessOwnerSite());
            businessOwnerDTO.setId(businessOwner.getBusinessOwnerId());
            List<BusinessOwnerProperty> businessOwnerPropertyList = businessOwner.getBusinessOwnerPropertiesList();
            List<BusinessOwnerPropertiesDTO> businessOwnerPropertiesDTOList =null;
            if (businessOwnerPropertyList != null) {
                businessOwnerPropertiesDTOList = new ArrayList<>();
                for (BusinessOwnerProperty businessOwnerProperty : businessOwnerPropertyList) {
                    BusinessOwnerPropertiesDTO businessOwnerPropertiesDTO = new BusinessOwnerPropertiesDTO();
                    businessOwnerPropertiesDTO.setKey(businessOwnerProperty.getPropertyId());
                    businessOwnerPropertiesDTO.setValue(businessOwnerProperty.getPropertyValue());
                    businessOwnerPropertiesDTO.setIsVisible(businessOwnerProperty.isShowingInStore());
                    businessOwnerPropertiesDTOList.add(businessOwnerPropertiesDTO);
                }
            }
            businessOwnerDTO.setProperties(businessOwnerPropertiesDTOList);
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving details of business owner Business owner Id : " +
                    businessOwnerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(businessOwnerDTO).build();
    }

    @Override
    public Response administrationBusinessownerBusinessOwnerIdPut(Integer businessOwnerId, BusinessOwnerDTO body,
                                                                  String contentType, String ifMatch,
                                                                  String ifUnmodifiedSince) {
        beanValidator = new BeanValidator();
        beanValidator.validate(body);
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String ownerName = body.getName();
            String ownerEmail = body.getEmail();
            String ownerDescription = body.getDescription();
            String ownerSite = body.getSite();
            if (StringUtils.isEmpty(ownerName)) {
                RestApiUtil.handleBadRequest("Business owner name cannot be null or empty.", log);
            }
            if (StringUtils.isEmpty(ownerEmail)) {
                RestApiUtil.handleBadRequest("Business owner email cannot be null or empty.", log);
            }
            ownerName = ownerName.trim();
            ownerEmail = ownerEmail.trim();
            int existingOwnerId = apiProvider.getBusinessOwnerId(ownerName, ownerEmail);
            if (!((existingOwnerId == businessOwnerId) || (existingOwnerId == -1))) {
                String message =  "A duplicate business owner already exists with the owner name :  " + ownerName +
                        " and owner email " + ownerEmail;
                RestApiUtil.handleConflictException(message, log);
            }
            BusinessOwner businessOwner = new BusinessOwner();
            businessOwner.setBusinessOwnerId(businessOwnerId);
            businessOwner.setBusinessOwnerName(ownerName);
            businessOwner.setBusinessOwnerEmail(ownerEmail);
            businessOwner.setBusinessOwnerDescription(ownerDescription);
            businessOwner.setBusinessOwnerSite(ownerSite);
            List<BusinessOwnerPropertiesDTO> businessOwnerPropertiesDTOList = body.getProperties();

            List<BusinessOwnerProperty> businessOwnerPropertyList = new ArrayList<>();
            for (BusinessOwnerPropertiesDTO businessOwnerPropertiesDTO : businessOwnerPropertiesDTOList) {
                String propertyId = businessOwnerPropertiesDTO.getKey();
                String propertyValue = businessOwnerPropertiesDTO.getValue();
                Boolean isVisible = businessOwnerPropertiesDTO.getIsVisible();
                if (StringUtils.isEmpty(propertyId) || StringUtils.isEmpty(propertyValue) || isVisible == null) {
                    RestApiUtil.handleBadRequest("Business owner properties cannot be null or empty.", log);
                }

                BusinessOwnerProperty businessOwnerProperty = new BusinessOwnerProperty();
                businessOwnerProperty.setPropertyId(propertyId);
                businessOwnerProperty.setPropertyValue(propertyValue);
                businessOwnerProperty.setShowingInStore(isVisible);
                businessOwnerPropertyList.add(businessOwnerProperty);
            }
            businessOwner.setBusinessOwnerPropertiesList(businessOwnerPropertyList);
            boolean response = apiProvider.updateBusinessOwner(businessOwner);
            if (!response) {
               RestApiUtil.handleResourceNotFoundError("business owner", String.valueOf(businessOwnerId), log );
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error while updating Business owner for business owner Id " + businessOwnerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().build();
    }

    @Override
    public Response administrationBusinessownerBusinessOwnerIdDelete(Integer businessOwnerId, String ifMatch,
                                                                     String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            // check whether business owner is exist or not.
            BusinessOwner businessOwner = apiProvider.getBusinessOwner(businessOwnerId);
            if (businessOwner != null) {
                //delete the business owner.
                boolean isDeleted = apiProvider.deleteBusinessOwner(businessOwnerId.toString());
                if (!isDeleted) {
                    RestApiUtil.handlePreconditionFailedRequest("Business Owner is assigned to one or more apps. Please remove "
                                                                        + "it from them before deleting." , log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError("business owner", String.valueOf(businessOwnerId), log );
            }

        } catch (AppManagementException e) {
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getMessage(), 500l, e.getCause().getMessage());
            throw new InternalServerErrorException(errorDTO);
        }
        return Response.ok().build();
    }


    @Override
    public Response administrationXacmlpoliciesPost(PolicyPartialDTO body, String contentType, String ifModifiedSince) {
        beanValidator = new BeanValidator();
        beanValidator.validate(body);
        PolicyPartialDTO policyPartialDTO = new PolicyPartialDTO();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String currentUser = RestApiUtil.getLoggedInUsername();
            if (body.getPolicyPartialName().trim().isEmpty()) {
                RestApiUtil.handleBadRequest("XACML Policy Name cannot be empty", log);
            }
            if (body.getPolicyPartial().trim().isEmpty()) {
                RestApiUtil.handleBadRequest("XACML Policy Content cannot be empty", log);
            }
            //save policy
            int policyPartialId = apiProvider.saveEntitlementPolicyPartial(body.getPolicyPartialName(),
                                                                           body.getPolicyPartial(),
                                                                           body.getIsSharedPartial(), currentUser,
                                                                           body.getPolicyPartialDesc());
            policyPartialDTO.setPolicyPartialId(policyPartialId);
        } catch (AppManagementException e) {
            String errorMessage = "Error while saving XACML policy";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(policyPartialDTO).build();
    }

    @Override
    public Response administrationXacmlpoliciesValidatePost(PolicyPartialDTO body, String contentType,
                                                            String ifModifiedSince) {
        beanValidator = new BeanValidator();
        beanValidator.validate(body);
        try {
            if (body.getPolicyPartial().trim().isEmpty()) {
                RestApiUtil.handleBadRequest("XACML Policy Content cannot be empty", log);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Boolean isValid = apiProvider.validateEntitlementPolicyPartial(body.getPolicyPartial()).isValid();
            if (!isValid) {
                RestApiUtil.handleBadRequest("XACML Policy Content is not valid", log);
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error while validating XACML policy content ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().build();
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
        beanValidator = new BeanValidator();
        beanValidator.validate(body);
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String currentUser = RestApiUtil.getLoggedInUsername();
            if (body.getPolicyPartial().trim().isEmpty()) {
                RestApiUtil.handleBadRequest("XACML Policy Content cannot be empty", log);
            }
            EntitlementPolicyPartial entitlementPolicyPartial = apiProvider.getPolicyPartial(policyPartialId);
            if (entitlementPolicyPartial == null) {
                return RestApiUtil.buildNotFoundException("XACML Policy Partial", policyPartialId.toString())
                        .getResponse();
            }

            //todo: provide appropriate auth cookie
            //update policy
            String authorizedAdminCookie = null;
            apiProvider.updateEntitlementPolicyPartial(policyPartialId, body.getPolicyPartial(), currentUser,
                                                       body.getIsSharedPartial(), body.getPolicyPartialDesc(),
                                                       authorizedAdminCookie);
        } catch (AppManagementException e) {
            String errorMessage = "Error while updating XACML policy";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().build();
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
