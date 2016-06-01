package org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialInfoDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialListDTO;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XacmlMappingUtil {
    /**
     * Converts a List object of APIs into a DTO
     *
     * @param policyList List of XACML Policies
     * @param limit      maximum number of APIs returns
     * @param offset     starting index
     * @return APIListDTO object containing APIDTOs
     */
    public static PolicyPartialListDTO fromAPIListToDTO(List<PolicyPartialInfoDTO> policyList, int offset, int limit) {
        PolicyPartialListDTO policyPartialListDTO = new PolicyPartialListDTO();
        List<PolicyPartialInfoDTO> policyPartialInfoDTO = policyPartialListDTO.getPolicyList();
        if (policyPartialInfoDTO == null) {
            policyPartialInfoDTO = new ArrayList<>();
            policyPartialListDTO.setPolicyList(policyPartialInfoDTO);
        }

        //add the required range of objects to be returned
        int start = offset < policyList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= policyList.size() - 1 ? offset + limit - 1 : policyList.size() - 1;
        for (int i = start; i <= end; i++) {
            policyPartialInfoDTO.add(policyList.get(i));
        }
        policyPartialListDTO.setCount(policyPartialInfoDTO.size());
        return policyPartialListDTO;
    }

    /**
     * Sets pagination urls for a PolicyPartialListDTO object given pagination parameters and url parameters
     *
     * @param policyPartialListDTO a PolicyPartialListDTO object
     * @param limit                max number of objects returned
     * @param offset               starting index
     * @param size                 max offset
     */
    public static void setPaginationParams(PolicyPartialListDTO policyPartialListDTO, int offset, int limit, int size) {

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

        policyPartialListDTO.setNext(paginatedNext);
        policyPartialListDTO.setPrevious(paginatedPrevious);
    }

    /**
     * Returns the paginated url for APIs API
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getAPIPaginatedURL(Integer offset, Integer limit) {
        String paginatedURL = RestApiConstants.XACML_POLICIES_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

}
