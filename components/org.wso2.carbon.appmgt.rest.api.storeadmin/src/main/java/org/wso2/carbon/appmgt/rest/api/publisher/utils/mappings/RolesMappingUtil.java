package org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings;


import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.RoleInfoDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.RoleListDTO;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RolesMappingUtil {
    /**
     * Converts a List object of APIs into a DTO
     *
     * @param roleList List of Devices
     * @param limit    maximum number of APIs returns
     * @param offset   starting index
     * @return APIListDTO object containing APIDTOs
     */
    public static RoleListDTO fromAPIListToDTO(List<RoleInfoDTO> roleList, int offset, int limit) {
        RoleListDTO roleListDTO = new RoleListDTO();
        List<RoleInfoDTO> roleInfoDTOs = roleListDTO.getList();
        if (roleInfoDTOs == null) {
            roleInfoDTOs = new ArrayList<>();
            roleListDTO.setList(roleInfoDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < roleList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= roleList.size() - 1 ? offset + limit - 1 : roleList.size() - 1;
        for (int i = start; i <= end; i++) {
            roleInfoDTOs.add(roleList.get(i));
        }
        roleListDTO.setCount(roleInfoDTOs.size());
        return roleListDTO;
    }


    /**
     * Sets pagination urls for a APIListDTO object given pagination parameters and url parameters
     *
     * @param deviceListDTO a DeviceListDTO object
     * @param limit         max number of objects returned
     * @param offset        starting index
     * @param size          max offset
     */
    public static void setPaginationParams(RoleListDTO deviceListDTO, int offset, int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                                                   paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                                               paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }

        deviceListDTO.setNext(paginatedNext);
        deviceListDTO.setPrevious(paginatedPrevious);
    }

    /**
     * Returns the paginated url for APIs API
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getAPIPaginatedURL(Integer offset, Integer limit) {
        String paginatedURL = RestApiConstants.ROLES_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

}
