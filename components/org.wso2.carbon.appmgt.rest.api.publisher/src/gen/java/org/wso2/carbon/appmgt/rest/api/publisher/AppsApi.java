package org.wso2.carbon.appmgt.rest.api.publisher;

import io.swagger.annotations.ApiParam;


import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppInfoDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.factories.AppsApiServiceFactory;


import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;


@Path("/apps")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the apps API")

public class AppsApi {
    private final AppsApiService delegate = AppsApiServiceFactory.getAppsApi();

    @POST
    @Path("/{appType}/change-lifecycle")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Change App Status", notes = "Change the lifecycle of an App\n",
                                         response = void.class, tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nLifecycle changed successfully.\n",
                                                response = void.class),
            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error\n",
                                                response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nRequested App does not exist.\n",
                                                response = void.class),
            @io.swagger.annotations.ApiResponse(code = 412,
                                                message = "Precondition Failed. \nThe request has not been performed " +
                                                        "because one of the preconditions is not met.\n",
                                                response = void.class)})
    public Response appsAppTypeChangeLifecyclePost(
            @ApiParam(value = "App Type. Either webapp or mobileapp\n", required = true) @PathParam("appType")
            String appType,
            @ApiParam(
                    value = "The action to demote or promote the state of the App.\n\n\nSupported actions are [ " +
                            "**Initial,Created,In-Review,Approved,Rejected,Published,Unpublished,Deprecated,Retire " +
                            "**]\n",
                    required = true,
                    allowableValues = "Initial, Created, In-Review, Approved, Rejected, Published, Unpublished, " +
                            "Deprecated, Retire")
            @QueryParam("action") String action,
            @ApiParam(
                    value = "**appId ID** consisting of the **UUID** of the App. \nThe combination of the provider of" +
                            " the appId, name of the appId and the version is also accepted as a valid App ID" +
                            ".\nShould be formatted as **provider-name-version**.\n",
                    required = true) @QueryParam("appId") String appId,
            @ApiParam(value = "Validator for conditional requests; based on ETag.\n") @HeaderParam("If-Match")
            String ifMatch,
            @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n")
            @HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince,
            @Context SecurityContext securityContext)
            throws NotFoundException {
        return delegate.appsAppTypeChangeLifecyclePost(appType, action, appId, ifMatch, ifUnmodifiedSince,
                                                       securityContext);
    }

    @GET
    @Path("/{appType}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get all Apps",
                                         notes = "Get a list of available Apps qualifying under a given search " +
                                                 "condition.\n",
                                         response = AppListDTO.class, tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nList of qualifying Apps is returned.\n",
                                                response = AppListDTO.class),
            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error.\n",
                                                response = AppListDTO.class),
            @io.swagger.annotations.ApiResponse(code = 403,
                                                message = "Forbidden. \nThe request must be conditional but no " +
                                                        "condition has been specified.\n",
                                                response = AppListDTO.class),
            @io.swagger.annotations.ApiResponse(code = 404,
                                                message = "Not Found. \nThe resource to be updated does not exist.\n",
                                                response = AppListDTO.class)})
    public Response appsAppTypeGet(
            @ApiParam(value = "App Type. Either webapp or mobileapp\n", required = true) @PathParam("appType")
            String appType,
            @ApiParam(
                    value = "**Search condition**.\n\n\nYou can search in attributes by using an **\"attribute:\"** " +
                            "modifier.\n\n\nEg. \"provider:wso2\" will match an App if the provider of the App " +
                            "contains \"wso2\".\n\n\nSupported attribute modifiers are [*provider, app_name, " +
                            "app_version, app_id**]\n\n\nIf no advanced attribute modifier has been specified, search" +
                            " will match the\ngiven query string against App Name.\n")
            @QueryParam("query") String query,
            @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue = "25") @DefaultValue("25")
            @QueryParam("limit") Integer limit,
            @ApiParam(value = "Starting point within the complete list of items qualified.  \n", defaultValue = "0")
            @DefaultValue("0") @QueryParam("offset") Integer offset,
            @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n", defaultValue = "JSON")
            @HeaderParam("Accept") String accept,
            @ApiParam(
                    value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant" +
                            " of the resourec.\n")
            @HeaderParam("If-None-Match") String ifNoneMatch,
            @Context SecurityContext securityContext)
            throws NotFoundException {
        return delegate.appsAppTypeGet(appType, query, limit, offset, accept, ifNoneMatch, securityContext);
    }

    @DELETE
    @Path("/{appType}/id/{appId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Delete App", notes = "Delete an existing App\n",
                                         response = void.class, tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nResource successfully deleted.\n",
                                                response = void.class),
            @io.swagger.annotations.ApiResponse(code = 403,
                                                message = "Forbidden. \nThe request must be conditional but no " +
                                                        "condition has been specified.\n",
                                                response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404,
                                                message = "Not Found. \nResource to be deleted does not exist.\n",
                                                response = void.class),
            @io.swagger.annotations.ApiResponse(code = 412,
                                                message = "Precondition Failed. \nThe request has not been performed " +
                                                        "because one of the preconditions is not met.\n",
                                                response = void.class)})
    public Response appsAppTypeIdAppIdDelete(
            @ApiParam(value = "App Type. Either webapp or mobileapp\n", required = true) @PathParam("appType")
            String appType,
            @ApiParam(
                    value = "**APP ID** consisting of the **UUID** of the App. \nThe combination of the provider of " +
                            "the app, name of the appId and the version is also accepted as a valid App ID.\nShould " +
                            "be formatted as **provider-name-version**.\n",
                    required = true) @PathParam("appId") String appId,
            @ApiParam(value = "Validator for conditional requests; based on ETag.\n") @HeaderParam("If-Match")
            String ifMatch,
            @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n")
            @HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince,
            @Context SecurityContext securityContext)
            throws NotFoundException {
        return delegate.appsAppTypeIdAppIdDelete(appType, appId, ifMatch, ifUnmodifiedSince, securityContext);
    }

    @GET
    @Path("/{appType}/id/{appId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get app details", notes = "Get details of an app.\n",
                                         response = AppInfoDTO.class, tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nQualifying App is returned.\n",
                                                response = AppInfoDTO.class),
            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error.\n",
                                                response = AppInfoDTO.class),
            @io.swagger.annotations.ApiResponse(code = 403,
                                                message = "Forbidden. \nThe request must be conditional but no " +
                                                        "condition has been specified.\n",
                                                response = AppInfoDTO.class),
            @io.swagger.annotations.ApiResponse(code = 404,
                                                message = "Not Found. \nThe resource to be updated does not exist.\n",
                                                response = AppInfoDTO.class)})
    public Response appsAppTypeIdAppIdGet(
            @ApiParam(value = "App Type. Either webapp or mobileapp\n", required = true) @PathParam("appType")
            String appType,
            @ApiParam(
                    value = "**APP ID** consisting of the **UUID** of the App. \nThe combination of the provider of " +
                            "the app, name of the appId and the version is also accepted as a valid App ID.\nShould " +
                            "be formatted as **provider-name-version**.\n",
                    required = true) @PathParam("appId") String appId,
            @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n", defaultValue = "JSON")
            @HeaderParam("Accept") String accept,
            @ApiParam(
                    value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant" +
                            " of the resourec.\n")
            @HeaderParam("If-None-Match") String ifNoneMatch,
            @ApiParam(
                    value = "Validator for conditional requests; based on Last Modified header of the \nformerly " +
                            "retrieved variant of the resource.\n")
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @Context SecurityContext securityContext)
            throws NotFoundException {
        return delegate.appsAppTypeIdAppIdGet(appType, appId, accept, ifNoneMatch, ifModifiedSince, securityContext);
    }

    @PUT
    @Path("/{appType}/id/{appId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update an existing App", notes = "Update an existing App\n",
                                         response = AppDTO.class, tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                                                message = "OK. \nSuccessful response with updated App object\n",
                                                response = AppDTO.class),
            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error\n",
                                                response = AppDTO.class),
            @io.swagger.annotations.ApiResponse(code = 403,
                                                message = "Forbidden. \nThe request must be conditional but no " +
                                                        "condition has been specified.\n",
                                                response = AppDTO.class),
            @io.swagger.annotations.ApiResponse(code = 404,
                                                message = "Not Found. \nThe resource to be updated does not exist.\n",
                                                response = AppDTO.class),
            @io.swagger.annotations.ApiResponse(code = 412,
                                                message = "Precondition Failed. \nThe request has not been performed " +
                                                        "because one of the preconditions is not met.\n",
                                                response = AppDTO.class)})
    public Response appsAppTypeIdAppIdPut(
            @ApiParam(value = "App Type. Either webapp or mobileapp\n", required = true) @PathParam("appType")
            String appType,
            @ApiParam(
                    value = "**APP ID** consisting of the **UUID** of the App. \nThe combination of the provider of " +
                            "the app, name of the appId and the version is also accepted as a valid App ID.\nShould " +
                            "be formatted as **provider-name-version**.\n",
                    required = true) @PathParam("appId") String appId,
            @ApiParam(value = "App object that needs to be added\n", required = true) AppDTO body,
            @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n", required = true,
                      defaultValue = "JSON") @HeaderParam("Content-Type") String contentType,
            @ApiParam(value = "Validator for conditional requests; based on ETag.\n") @HeaderParam("If-Match")
            String ifMatch,
            @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n")
            @HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince,
            @Context SecurityContext securityContext)
            throws NotFoundException {
        return delegate.appsAppTypeIdAppIdPut(appType, appId, body, contentType, ifMatch, ifUnmodifiedSince,
                                              securityContext);
    }

    @POST
    @Path("/{appType}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Create a new App", notes = "Create a new App\n",
                                         response = AppDTO.class, tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201,
                                                message = "Created. \nSuccessful response with the newly created " +
                                                        "object as entity in the body. \nLocation header contains URL" +
                                                        " of newly created entity.\n",
                                                response = AppDTO.class),
            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error.\n",
                                                response = AppDTO.class),
            @io.swagger.annotations.ApiResponse(code = 415,
                                                message = "Unsupported Media Type. \nThe entity of the request was in a not supported format.\n",
                                                response = AppDTO.class)})
    public Response appsAppTypePost(
            @ApiParam(value = "App Type. Either webapp or mobileapp\n", required = true) @PathParam("appType")
            String appType,
            @ApiParam(value = "App object that needs to be added\n", required = true) AppDTO body,
            @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n", required = true,
                      defaultValue = "JSON") @HeaderParam("Content-Type") String contentType,
            @ApiParam(
                    value = "Validator for conditional requests; based on Last Modified header of the \nformerly retrieved variant of the resource.\n")
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @Context SecurityContext securityContext)
            throws NotFoundException {
        return delegate.appsAppTypePost(appType, body, contentType, ifModifiedSince, securityContext);
    }

    @POST
    @Path("/mobile/binaries")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Uploading binary files",
                                         notes = "Uploading .apk/.IPA binary files.\n", response = void.class,
                                         tags = {"MobileApps"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nLifecycle changed successfully.\n",
                                                response = void.class),
            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error\n",
                                                response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nRequested App does not exist.\n",
                                                response = void.class),
            @io.swagger.annotations.ApiResponse(code = 412,
                                                message = "Precondition Failed. \nThe request has not been performed because one of the preconditions is not met.\n",
                                                response = void.class)})
    public Response appsMobileBinariesPost(
            @ApiParam(value = "Upload .apk/.IPA file into App Publisher", required = true) byte[] body,
            @ApiParam(value = "Validator for conditional requests; based on ETag.\n") @HeaderParam("If-Match")
            String ifMatch,
            @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n")
            @HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince,
            @Context SecurityContext securityContext)
            throws NotFoundException {
        return delegate.appsMobileBinariesPost(body, ifMatch, ifUnmodifiedSince, securityContext);
    }
}
