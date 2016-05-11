package org.wso2.carbon.appmgt.rest.api.store;

import org.wso2.carbon.appmgt.rest.api.store.dto.*;
import org.wso2.carbon.appmgt.rest.api.store.DevicesApiService;
import org.wso2.carbon.appmgt.rest.api.store.factories.DevicesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.appmgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.DeviceListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/devices")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/devices", description = "the devices API")
public class DevicesApi  {

   private final DevicesApiService delegate = DevicesApiServiceFactory.getDevicesApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Devices", notes = "Get a list of available Devices qualifying under a given search condition.", response = DeviceListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nList of qualifying Devices are returned."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. \nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. \nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nThe resource to be updated does not exist.") })

    public Response devicesGet(@ApiParam(value = "**Search condition**.\n\nYou can search in attributes by using an **\"attribute:\"** modifier.\n\nEg. \"type:mobile\" will match an App if the provider of the App contains \"wso2\".\n\nSupported attribute modifiers are [*provider, app_name, app_version, app_id**]\n\nIf no advanced attribute modifier has been specified, search will match the\ngiven query string against App Name.") @QueryParam("query") String query,
    @ApiParam(value = "Maximum size of resource array to return.", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON."  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.devicesGet(query,limit,offset,accept,ifNoneMatch);
    }
}

