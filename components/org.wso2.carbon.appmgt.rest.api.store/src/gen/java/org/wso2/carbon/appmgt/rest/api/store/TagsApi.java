package org.wso2.carbon.appmgt.rest.api.store;

import org.wso2.carbon.appmgt.rest.api.store.dto.*;
import org.wso2.carbon.appmgt.rest.api.store.TagsApiService;
import org.wso2.carbon.appmgt.rest.api.store.factories.TagsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.appmgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppIdListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/tags")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/tags", description = "the tags API")
public class TagsApi  {

   private final TagsApiService delegate = TagsApiServiceFactory.getTagsApi();

    @GET
    @Path("/{tagName}/apps/{appType}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get apps with given tag", notes = "Get app list with given tag for particular asset type.", response = AppIdListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nList of Apps Id's returned."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. \nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. \nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. \nThe resource to be updated does not exist.") })

    public Response tagsTagNameAppsAppTypeGet(@ApiParam(value = "Tag name.",required=true ) @PathParam("tagName") String tagName,
    @ApiParam(value = "App Type. Either webapp or mobileapp",required=true ) @PathParam("appType") String appType,
    @ApiParam(value = "Maximum size of resource array to return.", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON."  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec."  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the \nformerly retrieved variant of the resource."  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.tagsTagNameAppsAppTypeGet(tagName,appType,limit,offset,accept,ifNoneMatch,ifModifiedSince);
    }
}

