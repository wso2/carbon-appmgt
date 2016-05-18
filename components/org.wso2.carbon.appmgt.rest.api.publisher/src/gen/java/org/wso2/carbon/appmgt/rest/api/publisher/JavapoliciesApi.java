package org.wso2.carbon.appmgt.rest.api.publisher;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;
import org.wso2.carbon.appmgt.rest.api.publisher.JavapoliciesApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.factories.JavapoliciesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.JavaPolicyListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/javapolicies")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/javapolicies", description = "the javapolicies API")
public class JavapoliciesApi  {

   private final JavapoliciesApiService delegate = JavapoliciesApiServiceFactory.getJavapoliciesApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get available java policies", notes = "Get available java policies.", response = JavaPolicyListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nQualifying App is returned."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.") })

    public Response javapoliciesGet(@ApiParam(value = "Either global policy or not. Default value is 'true'.", defaultValue="true") @QueryParam("isGlobal") Boolean isGlobal,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON."  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec."  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource."  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.javapoliciesGet(isGlobal,accept,ifNoneMatch,ifModifiedSince);
    }
}

