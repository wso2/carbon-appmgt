package org.wso2.carbon.appmgt.rest.api.store;

import io.swagger.annotations.ApiParam;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppInfo;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppList;
import org.wso2.carbon.appmgt.rest.api.store.factories.AppsApiServiceFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/apps")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the apps API")
public class AppsApi  {
   private final AppsApiService delegate = AppsApiServiceFactory.getAppsApi();

    @GET
    @Path("/{appId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get app details", notes = "Get details of an app.", response = AppInfo.class, tags={ "Apps",  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nQualifying App is returned.", response = AppInfo.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. \nInvalid request or validation error.", response = AppInfo.class),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. \nThe request must be conditional but no condition has been specified.", response = AppInfo.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nThe resource to be updated does not exist.", response = AppInfo.class) })

    public Response appsAppIdGet(@ApiParam(value = "**APP ID** consisting of the **UUID** of the App. \nThe combination of the provider of the app, name of the appId and the version is also accepted as a valid App ID.\nShould be formatted as **provider-name-version**.",required=true) @PathParam("appId") String appId,@ApiParam(value = "Media types acceptable for the response. Default is JSON." , defaultValue="JSON")@HeaderParam("Accept") String accept,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec." )@HeaderParam("If-None-Match") String ifNoneMatch,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the \nformerly retrieved variant of the resource." )@HeaderParam("If-Modified-Since") String ifModifiedSince,@Context SecurityContext securityContext)
    throws org.wso2.carbon.appmgt.rest.api.store.NotFoundException {
        return delegate.appsAppIdGet(appId,accept,ifNoneMatch,ifModifiedSince,securityContext);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Apps", notes = "Get a list of available Apps qualifying under a given search condition.", response = AppList.class, tags={ "Apps" })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nList of qualifying Apps is returned.", response = AppList.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. \nInvalid request or validation error.", response = AppList.class),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. \nThe request must be conditional but no condition has been specified.", response = AppList.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nThe resource to be updated does not exist.", response = AppList.class) })

    public Response appsGet(@ApiParam(value = "**Search condition**.\n\n\nYou can search in attributes by using an **\"attribute:\"** modifier.\n\n\nEg. \"provider:wso2\" will match an App if the provider of the App contains \"wso2\".\n\n\nSupported attribute modifiers are [*provider, app_name, app_version, app_id**]\n\n\nIf no advanced attribute modifier has been specified, search will match the\ngiven query string against App Name.") @QueryParam("query") String query,@ApiParam(value = "Maximum size of resource array to return.", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,@ApiParam(value = "Starting point within the complete list of items qualified.", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,@ApiParam(value = "Media types acceptable for the response. Default is JSON." , defaultValue="JSON")@HeaderParam("Accept") String accept,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec." )@HeaderParam("If-None-Match") String ifNoneMatch,@Context SecurityContext securityContext)
    throws org.wso2.carbon.appmgt.rest.api.store.NotFoundException {
        return delegate.appsGet(query,limit,offset,accept,ifNoneMatch,securityContext);
    }
}
