package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.BusinessOwner;
import org.wso2.carbon.appmgt.api.model.BusinessOwnerProperty;
import org.wso2.carbon.appmgt.rest.api.store.BusinessOwnerApiService;
import org.wso2.carbon.appmgt.rest.api.store.dto.BusinessOwnerDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.BusinessOwnerPropertiesDTO;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class BusinessOwnerApiServiceImpl extends BusinessOwnerApiService {
    private static final Log log = LogFactory.getLog(BusinessOwnerApiServiceImpl.class);

    @Override
    public Response businessOwnerBusinessOwnerIdGet(Integer businessOwnerId, String accept, String ifNoneMatch) {
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
            List<BusinessOwnerPropertiesDTO> businessOwnerPropertiesDTOList = null;
            if (businessOwnerPropertyList != null) {
                businessOwnerPropertiesDTOList = new ArrayList<>();
                for (BusinessOwnerProperty businessOwnerProperty : businessOwnerPropertyList) {
                    BusinessOwnerPropertiesDTO businessOwnerPropertiesDTO = new BusinessOwnerPropertiesDTO();
                    businessOwnerPropertiesDTO.setKey(businessOwnerProperty.getPropertyKey());
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
}
