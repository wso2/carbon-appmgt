package org.wso2.carbon.appmgt.rest.api.publisher;

import io.swagger.annotations.ApiParam;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppInfoDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.factories.AppsApiServiceFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/apps")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the apps API")
public class AppsApi {
    private final AppsApiService delegate = AppsApiServiceFactory.getAppsApi();

    @DELETE
    @Path("/{appId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Delete App", notes = "Delete an existing App", response = void.class,
                                         tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nResource successfully deleted.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 403,
                                                message = "Forbidden. \nThe request must be conditional but no " +
                                                        "condition has been specified.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404,
                                                message = "Not Found. \nResource to be deleted does not exist.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 412,
                                                message = "Precondition Failed. \nThe request has not been performed " +
                                                        "because one of the preconditions is not met.",
                                                response = void.class)})

    public Response appsAppIdDelete(
            @ApiParam(
                    value = "**APP ID** consisting of the **UUID** of the App. \nThe combination of the provider of " +
                            "the app, name of the appId and the version is also accepted as a valid App ID.\nShould " +
                            "be formatted as **provider-name-version**.",
                    required = true) @PathParam("appId") String appId,
            @ApiParam(value = "Validator for conditional requests; based on ETag.") @HeaderParam("If-Match")
            String ifMatch,
            @ApiParam(value = "Validator for conditional requests; based on Last Modified header.")
            @HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince, @Context SecurityContext securityContext)
            throws org.wso2.carbon.appmgt.rest.api.publisher.NotFoundException {
        return delegate.appsAppIdDelete(appId, ifMatch, ifUnmodifiedSince, securityContext);
    }

    @GET
    @Path("/{appId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get app details", notes = "Get details of an app.",
                                         response = AppInfoDTO.class, tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nQualifying App is returned.",
                                                response = AppInfoDTO.class),

            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error.",
                                                response = AppInfoDTO.class),

            @io.swagger.annotations.ApiResponse(code = 403,
                                                message = "Forbidden. \nThe request must be conditional but no " +
                                                        "condition has been specified.",
                                                response = AppInfoDTO.class),

            @io.swagger.annotations.ApiResponse(code = 404,
                                                message = "Not Found. \nThe resource to be updated does not exist.",
                                                response = AppInfoDTO.class)})

    public Response appsAppIdGet(
            @ApiParam(
                    value = "**APP ID** consisting of the **UUID** of the App. \nThe combination of the provider of " +
                            "the app, name of the appId and the version is also accepted as a valid App ID.\nShould " +
                            "be formatted as **provider-name-version**.",
                    required = true) @PathParam("appId") String appId,
            @ApiParam(value = "Media types acceptable for the response. Default is JSON.", defaultValue = "JSON")
            @HeaderParam("Accept") String accept,
            @ApiParam(
                    value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant" +
                            " of the resourec.")
            @HeaderParam("If-None-Match") String ifNoneMatch,
            @ApiParam(
                    value = "Validator for conditional requests; based on Last Modified header of the \nformerly " +
                            "retrieved variant of the resource.")
            @HeaderParam("If-Modified-Since") String ifModifiedSince, @Context SecurityContext securityContext)
            throws org.wso2.carbon.appmgt.rest.api.publisher.NotFoundException {
        return delegate.appsAppIdGet(appId, accept, ifNoneMatch, ifModifiedSince, securityContext);
    }

    @PUT
    @Path("/{appId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update an existing App", notes = "Update an existing App",
                                         response = AppDTO.class, tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                                                message = "OK. \nSuccessful response with updated App object",
                                                response = AppDTO.class),

            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error",
                                                response = AppDTO.class),

            @io.swagger.annotations.ApiResponse(code = 403,
                                                message = "Forbidden. \nThe request must be conditional but no " +
                                                        "condition has been specified.",
                                                response = AppDTO.class),

            @io.swagger.annotations.ApiResponse(code = 404,
                                                message = "Not Found. \nThe resource to be updated does not exist.",
                                                response = AppDTO.class),

            @io.swagger.annotations.ApiResponse(code = 412,
                                                message = "Precondition Failed. \nThe request has not been performed " +
                                                        "because one of the preconditions is not met.",
                                                response = AppDTO.class)})

    public Response appsAppIdPut(
            @ApiParam(
                    value = "**APP ID** consisting of the **UUID** of the App. \nThe combination of the provider of " +
                            "the app, name of the appId and the version is also accepted as a valid App ID.\nShould " +
                            "be formatted as **provider-name-version**.",
                    required = true) @PathParam("appId") String appId,
            @ApiParam(value = "App object that needs to be added", required = true) AppDTO body,
            @ApiParam(value = "Media type of the entity in the body. Default is JSON.", required = true,
                      defaultValue = "JSON") @HeaderParam("Content-Type") String contentType,
            @ApiParam(value = "Validator for conditional requests; based on ETag.") @HeaderParam("If-Match")
            String ifMatch,
            @ApiParam(value = "Validator for conditional requests; based on Last Modified header.")
            @HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince, @Context SecurityContext securityContext)
            throws org.wso2.carbon.appmgt.rest.api.publisher.NotFoundException {
        return delegate.appsAppIdPut(appId, body, contentType, ifMatch, ifUnmodifiedSince, securityContext);
    }

    @POST
    @Path("/change-lifecycle")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Change App Status", notes = "Change the lifecycle of an App",
                                         response = void.class, tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nLifecycle changed successfully.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nRequested App does not exist.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 412,
                                                message = "Precondition Failed. \nThe request has not been performed " +
                                                        "because one of the preconditions is not met.",
                                                response = void.class)})

    public Response appsChangeLifecyclePost(@ApiParam(
            value = "The action to demote or promote the state of the App.\n\n\nSupported actions are [ **Initial," +
                    "Created,In-Review,Approved,Rejected,Published,Unpublished,Deprecated,Retire **]",
            required = true,
            allowableValues = "Initial, Created, In-Review, Approved, Rejected, Published, Unpublished, Deprecated, " +
                    "Retire")
                                            @QueryParam("action") String action
            , @ApiParam(
            value = "**appId ID** consisting of the **UUID** of the App. \nThe combination of the provider of the " +
                    "appId, name of the appId and the version is also accepted as a valid App ID.\nShould be " +
                    "formatted as **provider-name-version**.",
            required = true) @QueryParam("appId") String appId
            ,
                                            @ApiParam(value = "Validator for conditional requests; based on ETag.")
                                            @HeaderParam("If-Match") String ifMatch,
                                            @ApiParam(
                                                    value = "Validator for conditional requests; based on Last " +
                                                            "Modified header.")
                                            @HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince,
                                            @Context SecurityContext securityContext)
            throws org.wso2.carbon.appmgt.rest.api.publisher.NotFoundException {
        return delegate.appsChangeLifecyclePost(action, appId, ifMatch, ifUnmodifiedSince, securityContext);
    }

    @GET

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get all Apps",
                                         notes = "Get a list of available Apps qualifying under a given search " +
                                                 "condition.",
                                         response = AppListDTO.class, tags = {"Apps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nList of qualifying Apps is returned.",
                                                response = AppListDTO.class),

            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error.",
                                                response = AppListDTO.class),

            @io.swagger.annotations.ApiResponse(code = 403,
                                                message = "Forbidden. \nThe request must be conditional but no " +
                                                        "condition has been specified.",
                                                response = AppListDTO.class),

            @io.swagger.annotations.ApiResponse(code = 404,
                                                message = "Not Found. \nThe resource to be updated does not exist.",
                                                response = AppListDTO.class)})

    public Response appsGet(@ApiParam(
            value = "**Search condition**.\n\n\nYou can search in attributes by using an **\"attribute:\"** modifier" +
                    ".\n\n\nEg. \"provider:wso2\" will match an App if the provider of the App contains \"wso2\"" +
                    ".\n\n\nSupported attribute modifiers are [*provider, app_name, app_version, app_id**]\n\n\nIf no" +
                    " advanced attribute modifier has been specified, search will match the\ngiven query string " +
                    "against App Name.")
                            @QueryParam("query") String query
            , @ApiParam(value = "Maximum size of resource array to return.", defaultValue = "25") @DefaultValue("25")
                            @QueryParam("limit") Integer limit
            , @ApiParam(value = "Starting point within the complete list of items qualified.", defaultValue = "0")
                            @DefaultValue("0") @QueryParam("offset") Integer offset
            ,
                            @ApiParam(value = "Media types acceptable for the response. Default is JSON.",
                                      defaultValue = "JSON") @HeaderParam("Accept") String accept,
                            @ApiParam(
                                    value = "Validator for conditional requests; based on the ETag of the formerly " +
                                            "retrieved\nvariant of the resourec.")
                            @HeaderParam("If-None-Match") String ifNoneMatch, @Context
                            SecurityContext securityContext)
            throws org.wso2.carbon.appmgt.rest.api.publisher.NotFoundException {
        return delegate.appsGet(query, limit, offset, accept, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/mobile/binaries")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Uploading binary files", notes = "Uploading .apk/.IPA binary files.",
                                         response = void.class, tags = {"MobileApps",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nLifecycle changed successfully.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nRequested App does not exist.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 412,
                                                message = "Precondition Failed. \nThe request has not been performed " +
                                                        "because one of the preconditions is not met.",
                                                response = void.class)})

    public Response appsMobileBinariesPost(
            @ApiParam(value = "Upload .apk/.IPA file into App Publisher", required = true) byte[] body,
            @ApiParam(value = "Validator for conditional requests; based on ETag.") @HeaderParam("If-Match")
            String ifMatch,
            @ApiParam(value = "Validator for conditional requests; based on Last Modified header.")
            @HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince, @Context SecurityContext securityContext)
            throws org.wso2.carbon.appmgt.rest.api.publisher.NotFoundException {
        return delegate.appsMobileBinariesPost(body, ifMatch, ifUnmodifiedSince, securityContext);
    }

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Create a new App", notes = "Create a new App",
                                         response = AppDTO.class,
                                         tags = {"Apps"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201,
                                                message = "Created. \nSuccessful response with the newly created " +
                                                        "object as entity in the body. \nLocation header contains URL" +
                                                        " of newly created entity.",
                                                response = AppDTO.class),

            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. \nInvalid request or validation error.",
                                                response = AppDTO.class),

            @io.swagger.annotations.ApiResponse(code = 415,
                                                message = "Unsupported Media Type. \nThe entity of the request was in" +
                                                        " a not supported format.",
                                                response = AppDTO.class)})

    public Response appsPost(
            @ApiParam(value = "Media type of the entity in the body. Default is JSON.", required = true,
                      defaultValue = "JSON") @HeaderParam("Content-Type") String contentType,
            @ApiParam(
                    value = "Validator for conditional requests; based on Last Modified header of the \nformerly " +
                            "retrieved variant of the resource.")
            @HeaderParam("If-Modified-Since") String ifModifiedSince, @Context SecurityContext securityContext)
            throws org.wso2.carbon.appmgt.rest.api.publisher.NotFoundException {
        return delegate.appsPost(contentType, ifModifiedSince, securityContext);
    }
}
