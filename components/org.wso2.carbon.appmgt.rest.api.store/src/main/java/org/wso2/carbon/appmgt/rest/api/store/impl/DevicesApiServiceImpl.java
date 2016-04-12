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

package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.mobile.store.Devices;
import org.wso2.carbon.appmgt.mobile.utils.MobileApplicationException;
import org.wso2.carbon.appmgt.rest.api.store.DevicesApiService;
import org.wso2.carbon.appmgt.rest.api.store.dto.DeviceInfoDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.DeviceListDTO;
import org.wso2.carbon.appmgt.rest.api.store.utils.mappings.DeviceMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class DevicesApiServiceImpl extends DevicesApiService {
    private static final Log log = LogFactory.getLog(DevicesApiServiceImpl.class);

    @Override
    public Response devicesGet(String query, Integer limit, Integer offset, String accept, String ifNoneMatch) {
        List<DeviceInfoDTO> allMatchedDevices = new ArrayList<>();
        DeviceListDTO deviceListDTO;
        Devices devices = new Devices();
        String username = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        int tenantId = -1;


        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;

        String searchType = AppMConstants.SEARCH_CONTENT_TYPE;
        String searchContent = "";
        if (!StringUtils.isBlank(query)) {
            String[] querySplit = query.split(":");
            if (querySplit.length == 2 && StringUtils.isNotBlank(querySplit[0]) && StringUtils
                    .isNotBlank(querySplit[1])) {
                searchType = querySplit[0];
                searchContent = querySplit[1];
            } else if (querySplit.length == 1) {
                searchContent = query;
            } else {
                RestApiUtil.handleBadRequest("Provided query parameter '" + query + "' is invalid", log);
            }
        }

        if (!searchContent.isEmpty()) {
            //currently it support only mobile apps. Query is given to support future enhancements
            if (!searchContent.equals("mobile")) {
                RestApiUtil.handleBadRequest("Provided query parameter value '" + searchContent + "' is not supported.",
                                             log);
            }
        }

        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error while initializing UserStore", e, log);
        }

        //building request parameters to required format
        String[] users = {username};
        JSONObject userObj = new JSONObject();
        userObj.put("username", username);
        userObj.put("tenantDomain", tenantDomain);
        userObj.put("tenantId", tenantId);

        String deviceList = null;
        try {
            deviceList = devices.getDevicesList(userObj.toJSONString(), tenantId, "user", users);
        } catch (MobileApplicationException e) {
            log.error("Error while retrieving devices. " + e.getMessage());
            return RestApiUtil.buildInternalServerErrorException().getResponse();
        }
        JSONArray deviceArr = (JSONArray) new JSONValue().parse(deviceList);


        for (int i = 0; i < deviceArr.size(); i++) {
            JSONObject jsonObject = (JSONObject) deviceArr.get(i);
            DeviceInfoDTO deviceInfoDTO = new DeviceInfoDTO();
            deviceInfoDTO.setId(jsonObject.get("id").toString());
            deviceInfoDTO.setImage(jsonObject.get("image").toString());
            deviceInfoDTO.setModel(jsonObject.get("model").toString());
            deviceInfoDTO.setName(jsonObject.get("name").toString());
            deviceInfoDTO.setPlatform(jsonObject.get("platform").toString());
            deviceInfoDTO.setPlatformVersion(jsonObject.get("platform_version").toString());
            deviceInfoDTO.setType(jsonObject.get("type").toString());
            allMatchedDevices.add(deviceInfoDTO);
        }

        if (allMatchedDevices.isEmpty()) {
            String errorMessage = "No result found.";
            return RestApiUtil.buildNotFoundException(errorMessage, null).getResponse();
        }

        deviceListDTO = DeviceMappingUtil.fromAPIListToDTO(allMatchedDevices, offset, limit);
        DeviceMappingUtil.setPaginationParams(deviceListDTO, query, offset, limit, allMatchedDevices.size());
        return Response.ok().entity(deviceListDTO).build();
    }

}
