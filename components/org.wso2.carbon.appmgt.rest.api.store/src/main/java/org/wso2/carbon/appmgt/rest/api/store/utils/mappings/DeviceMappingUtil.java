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

package org.wso2.carbon.appmgt.rest.api.store.utils.mappings;


import org.wso2.carbon.appmgt.rest.api.store.dto.DeviceInfoDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.DeviceListDTO;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceMappingUtil {
    /**
     * Converts a List object of APIs into a DTO
     *
     * @param deviceList List of Devices
     * @param limit      maximum number of APIs returns
     * @param offset     starting index
     * @return APIListDTO object containing APIDTOs
     */
    public static DeviceListDTO fromAPIListToDTO(List<DeviceInfoDTO> deviceList, int offset, int limit) {
        DeviceListDTO deviceListDTO = new DeviceListDTO();
        List<DeviceInfoDTO> deviceInfoDTOs = deviceListDTO.getDeviceList();
        if (deviceInfoDTOs == null) {
            deviceInfoDTOs = new ArrayList<>();
            deviceListDTO.setDeviceList(deviceInfoDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < deviceList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= deviceList.size() - 1 ? offset + limit - 1 : deviceList.size() - 1;
        for (int i = start; i <= end; i++) {
            deviceInfoDTOs.add(deviceList.get(i));
        }
        deviceListDTO.setCount(deviceInfoDTOs.size());
        return deviceListDTO;
    }


    /**
     * Sets pagination urls for a APIListDTO object given pagination parameters and url parameters
     *
     * @param deviceListDTO a DeviceListDTO object
     * @param query         search condition
     * @param limit         max number of objects returned
     * @param offset        starting index
     * @param size          max offset
     */
    public static void setPaginationParams(DeviceListDTO deviceListDTO, String query, int offset, int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                                                   paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT),
                                                   query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                                               paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        deviceListDTO.setNext(paginatedNext);
        deviceListDTO.setPrevious(paginatedPrevious);
    }

    /**
     * Returns the paginated url for APIs API
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @param query  search query value
     * @return constructed paginated url
     */
    public static String getAPIPaginatedURL(Integer offset, Integer limit, String query) {
        String paginatedURL = RestApiConstants.DEVICES_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.QUERY_PARAM, query);
        return paginatedURL;
    }

}
