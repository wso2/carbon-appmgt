package org.wso2.carbon.appmgt.rest.api.publisher;

import io.swagger.annotations.ApiParam;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.BusinessOwnerDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.BusinessOwnerListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.factories.AdministrationApiServiceFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/administration")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/administration", description = "the administration API")
public class AdministrationApi  {

   private final AdministrationApiService delegate = AdministrationApiServiceFactory.getAdministrationApi();

    @GET
    @Path("/businessowner")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Business owners", notes = "Get a list of available Apps qualifying under a given search condition.", response = BusinessOwnerListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of qualifying Business Owner List is returned."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.") })

    public Response administrationBusinessownerGet(@ApiParam(value = "Media types acceptable for the response. Default is JSON."  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.administrationBusinessownerGet(accept,ifNoneMatch);
    }
    @POST
    @Path("/businessowner")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new Business Owner", notes = "Create a new Business Owner", response = BusinessOwnerDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media Type.\nThe entity of the request was in a not supported format.") })

    public Response administrationBusinessownerPost(@ApiParam(value = "BusinessOwner object that needs to be added" ,required=true ) BusinessOwnerDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON." ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource."  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.administrationBusinessownerPost(body,contentType,ifModifiedSince);
    }
    @GET
    @Path("/businessowner/{businessOwnerId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a Business owners by Id", notes = "Get a Business owners by Id.", response = BusinessOwnerDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.") })

    public Response administrationBusinessownerBusinessOwnerIdGet(@ApiParam(value = "Business Owner Id.",required=true ) @PathParam("businessOwnerId") Integer businessOwnerId,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON."  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.administrationBusinessownerBusinessOwnerIdGet(businessOwnerId,accept,ifNoneMatch);
    }
    @PUT
    @Path("/businessowner/{businessOwnerId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing Business Owner", notes = "Update an existing Business Owner.", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.") })

    public Response administrationBusinessownerBusinessOwnerIdPut(@ApiParam(value = "Business Owner Id.",required=true ) @PathParam("businessOwnerId") Integer businessOwnerId,
    @ApiParam(value = "BusinessOwner object that needs to be added" ,required=true ) BusinessOwnerDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON." ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.administrationBusinessownerBusinessOwnerIdPut(businessOwnerId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/businessowner/{businessOwnerId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete Business Owner", notes = "Delete an existing Business Owner.", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.") })

    public Response administrationBusinessownerBusinessOwnerIdDelete(@ApiParam(value = "Business Owner Id.",required=true ) @PathParam("businessOwnerId") Integer businessOwnerId,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.administrationBusinessownerBusinessOwnerIdDelete(businessOwnerId,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/xacmlpolicies/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new Policy Partial", notes = "Create a new Policy Partial.", response = PolicyPartialDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media Type.\nThe entity of the request was in a not supported format.") })

    public Response administrationXacmlpoliciesPost(@ApiParam(value = "PolicyPartial object that needs to be added" ,required=true ) PolicyPartialDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON." ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource."  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.administrationXacmlpoliciesPost(body,contentType,ifModifiedSince);
    }
    @POST
    @Path("/xacmlpolicies/validate")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Validate Policy content", notes = "Create a new App", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media Type.\nThe entity of the request was in a not supported format.") })

    public Response administrationXacmlpoliciesValidatePost(@ApiParam(value = "PolicyPartial object that needs to be added" ,required=true ) PolicyPartialDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON." ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource."  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.administrationXacmlpoliciesValidatePost(body,contentType,ifModifiedSince);
    }
    @GET
    @Path("/xacmlpolicies/{policyPartialId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get policy partial details", notes = "Get policy partial.", response = PolicyPartialDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nQualifying App is returned."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.") })

    public Response administrationXacmlpoliciesPolicyPartialIdGet(@ApiParam(value = "policy partial id",required=true ) @PathParam("policyPartialId") Integer policyPartialId,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON."  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec."  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource."  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.administrationXacmlpoliciesPolicyPartialIdGet(policyPartialId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/xacmlpolicies/{policyPartialId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing policy partial", notes = "Update an existing policy partial", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.") })

    public Response administrationXacmlpoliciesPolicyPartialIdPut(@ApiParam(value = "policy partial id",required=true ) @PathParam("policyPartialId") Integer policyPartialId,
    @ApiParam(value = "App object that needs to be added" ,required=true ) PolicyPartialDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON." ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.administrationXacmlpoliciesPolicyPartialIdPut(policyPartialId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/xacmlpolicies/{policyPartialId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete policy partial", notes = "Delete an existing policy partial", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.") })

    public Response administrationXacmlpoliciesPolicyPartialIdDelete(@ApiParam(value = "policy partial id",required=true ) @PathParam("policyPartialId") Integer policyPartialId,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.administrationXacmlpoliciesPolicyPartialIdDelete(policyPartialId,ifMatch,ifUnmodifiedSince);
    }
}

