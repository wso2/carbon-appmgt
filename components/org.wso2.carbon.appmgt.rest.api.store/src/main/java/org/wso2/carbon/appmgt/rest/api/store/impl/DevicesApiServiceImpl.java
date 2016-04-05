package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.mobile.store.Devices;
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

        List<DeviceInfoDTO> allMatcheddevices = new ArrayList<>();
        DeviceListDTO appListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;


        Devices devices = new Devices();

        String username = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        int tenantId = 1;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error while initializing UserStore", e, log);
        }

        String[] users = {username};

        JSONObject userObj = new JSONObject();
        userObj.put("username", username);
        userObj.put("tenantDomain", tenantDomain);
        userObj.put("tenantId", tenantId);


        String deviceList = devices.getDevicesList(userObj.toJSONString(), tenantId, "user", users);
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

            allMatcheddevices.add(deviceInfoDTO);
        }


        if (allMatcheddevices.isEmpty()) {
            String errorMessage = "No result found.";
            return RestApiUtil.buildNotFoundException(errorMessage, null).getResponse();
        }

        appListDTO = DeviceMappingUtil.fromAPIListToDTO(allMatcheddevices, offset, limit);
        DeviceMappingUtil.setPaginationParams(appListDTO, query, offset, limit, allMatcheddevices.size());
        return Response.ok().entity(appListDTO).build();
    }
}
