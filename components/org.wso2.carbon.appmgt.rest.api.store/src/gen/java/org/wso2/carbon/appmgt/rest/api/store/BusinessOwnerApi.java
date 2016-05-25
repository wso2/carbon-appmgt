package org.wso2.carbon.appmgt.rest.api.store;


import io.swagger.annotations.ApiParam;
import org.wso2.carbon.appmgt.rest.api.store.dto.BusinessOwnerDTO;
import org.wso2.carbon.appmgt.rest.api.store.factories.BusinessOwnerApiServiceFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/businessowner")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(value = "/businessowner", description = "the businessowner API")
public class BusinessOwnerApi {

    private final BusinessOwnerApiService delegate = BusinessOwnerApiServiceFactory.getBusinessOwnerApi();

    @GET
    @Path("/{businessOwnerId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get a Business owners by Id", notes = "Get a Business owners by Id.",
                                         response = BusinessOwnerDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK."),

            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request.\nInvalid request or validation error."),

            @io.swagger.annotations.ApiResponse(code = 403,
                                                message = "Forbidden.\nThe request must be conditional but no " +
                                                        "condition has been specified."),

            @io.swagger.annotations.ApiResponse(code = 404,
                                                message = "Not Found.\nThe resource to be updated does not exist.")})

    public Response businessownerBusinessOwnerIdGet(
            @ApiParam(value = "Business Owner Id.", required = true) @PathParam("businessOwnerId")
            Integer businessOwnerId,
            @ApiParam(value = "Media types acceptable for the response. Default is JSON.", defaultValue = "JSON")
            @HeaderParam("Accept") String accept,
            @ApiParam(
                    value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant" +
                            " of the resourec.")
            @HeaderParam("If-None-Match") String ifNoneMatch) {
        return delegate.businessOwnerBusinessOwnerIdGet(businessOwnerId, accept, ifNoneMatch);
    }
}
