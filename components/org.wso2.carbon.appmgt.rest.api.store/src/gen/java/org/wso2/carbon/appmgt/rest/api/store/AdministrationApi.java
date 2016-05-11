package org.wso2.carbon.appmgt.rest.api.store;

import org.wso2.carbon.appmgt.rest.api.store.dto.*;
import org.wso2.carbon.appmgt.rest.api.store.AdministrationApiService;
import org.wso2.carbon.appmgt.rest.api.store.factories.AdministrationApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.appmgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.AdminInstallDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.RoleIdListDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.UserIdListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/administration")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/administration", description = "the administration API")
public class AdministrationApi  {

   private final AdministrationApiService delegate = AdministrationApiServiceFactory.getAdministrationApi();

    @POST
    @Path("/apps/download")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Enterprise Download/Install App", notes = "Enterprise Download an new App", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. \nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. \nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nResource to be deleted does not exist.") })

    public Response administrationAppsDownloadPost(@ApiParam(value = "Media type of the entity in the body. Default is JSON." ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = ""  ) AdminInstallDTO install)
    {
    return delegate.administrationAppsDownloadPost(contentType,install);
    }
    @POST
    @Path("/apps/uninstallation")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Enterprise Uninstall App", notes = "Enterprise Uninstall an existing App", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. \nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. \nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nResource to be deleted does not exist.") })

    public Response administrationAppsUninstallationPost(@ApiParam(value = "Media type of the entity in the body. Default is JSON." ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = ""  ) AdminInstallDTO install)
    {
    return delegate.administrationAppsUninstallationPost(contentType,install);
    }
    @GET
    @Path("/roles")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Roles", notes = "Get a list of available Roles.", response = RoleIdListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nList of qualifying Roles are returned."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. \nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. \nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nThe resource to be updated does not exist.") })

    public Response administrationRolesGet(@ApiParam(value = "Maximum size of resource array to return.", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON."  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.administrationRolesGet(limit,offset,accept,ifNoneMatch);
    }
    @GET
    @Path("/users")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Users", notes = "Get a list of available Users.", response = UserIdListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nList of Users are returned."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. \nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. \nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nThe resource to be updated does not exist.") })

    public Response administrationUsersGet(@ApiParam(value = "Maximum size of resource array to return.", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON."  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.administrationUsersGet(limit,offset,accept,ifNoneMatch);
    }
}

