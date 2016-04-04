package org.wso2.carbon.appmgt.rest.api.store;

import io.swagger.annotations.ApiParam;
import org.wso2.carbon.appmgt.rest.api.store.dto.User;
import org.wso2.carbon.appmgt.rest.api.store.factories.UsersApiServiceFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/users")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the users API")
public class UsersApi  {
   private final UsersApiService delegate = UsersApiServiceFactory.getUsersApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Register new user", notes = "Register new user", response = void.class, tags={ "Apps",  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. \nUser successfully registered.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. \nInvalid request or validation error.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict. \nUser already exists.", response = void.class) })

    public Response usersPost(@ApiParam(value = "App object that needs to be added" ,required=true) User body,@ApiParam(value = "Media type of the entity in the body. Default is JSON." ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the \nformerly retrieved variant of the resource." )@HeaderParam("If-Modified-Since") String ifModifiedSince,@Context SecurityContext securityContext)
    throws org.wso2.carbon.appmgt.rest.api.store.NotFoundException {
        return delegate.usersPost(body,contentType,ifModifiedSince,securityContext);
    }
    @GET
    @Path("/{userName}/lookup")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get existence status of given username", notes = "Get existence status of given username.", response = void.class, tags={ "Apps" })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Username exists.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. \nInvalid request or validation error.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class) })

    public Response usersUserNameLookupGet(@ApiParam(value = "userName to be check existence.",required=true) @PathParam("userName") String userName,@ApiParam(value = "Media types acceptable for the response. Default is JSON." , defaultValue="JSON")@HeaderParam("Accept") String accept,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec." )@HeaderParam("If-None-Match") String ifNoneMatch,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the \nformerly retrieved variant of the resource." )@HeaderParam("If-Modified-Since") String ifModifiedSince,@Context SecurityContext securityContext)
    throws org.wso2.carbon.appmgt.rest.api.store.NotFoundException {
        return delegate.usersUserNameLookupGet(userName,accept,ifNoneMatch,ifModifiedSince,securityContext);
    }
}
