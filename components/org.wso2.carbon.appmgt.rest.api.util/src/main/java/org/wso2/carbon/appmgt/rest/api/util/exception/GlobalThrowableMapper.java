/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.rest.api.util.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.wso2.carbon.appmgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.EOFException;

public class GlobalThrowableMapper implements ExceptionMapper<Throwable> {

    private static final Log log = LogFactory.getLog(GlobalThrowableMapper.class);

    private ErrorDTO e500 = new ErrorDTO();

    GlobalThrowableMapper() {
        e500.setCode((long) 500);
        e500.setMessage("Internal server error");
        e500.setMoreInfo("");
        e500.setDescription("The server encountered an internal error. Please contact administrator.");
    }

    @Override
    public Response toResponse(Throwable e) {

        String errorMessage = "Error Occurred";

        if (e instanceof ClientErrorException) {
            errorMessage = "Client error";
            logError("Resource not found", e);
            return ((ClientErrorException) e).getResponse();
        }

        if (e instanceof NotFoundException) {
            errorMessage = "Resource not found";
            logError("Resource not found", e);
            return ((NotFoundException) e).getResponse();
        }

        if (e instanceof PreconditionFailedException) {
            errorMessage = "Precondition failed";
            logError("Precondition failed", e);
            return ((PreconditionFailedException) e).getResponse();
        }

        if (e instanceof BadRequestException) {
            errorMessage = "Bad request";
            logError("Bad request", e);
            return ((BadRequestException) e).getResponse();
        }

        if (e instanceof ConstraintViolationException) {
            errorMessage = "Constraint violation";
            logError("Constraint violation", e);
            return ((ConstraintViolationException) e).getResponse();
        }

        if (e instanceof ForbiddenException) {
            errorMessage = "Resource forbiddenn";
            logError("Resource forbidden", e);
            return ((ForbiddenException) e).getResponse();
        }

        if (e instanceof ConflictException) {
            errorMessage = "Conflict";
            logError("Conflict", e);
            return ((ConflictException) e).getResponse();
        }

        if (e instanceof MethodNotAllowedException) {
            errorMessage = "Method not allowed";
            logError("Method not allowed", e);
            return ((MethodNotAllowedException) e).getResponse();
        }

        if (e instanceof InternalServerErrorException) {
            errorMessage = "The server encountered an internal error : " + e.getMessage();
            logError(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json")
                    .entity(e500).build();
        }

        if (e instanceof JsonParseException) {
            errorMessage = "Malformed request body.";
            logError(errorMessage, e);
            //noinspection ThrowableResultOfMethodCallIgnored
            return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
        }

        if (e instanceof JsonMappingException) {
            if (e instanceof UnrecognizedPropertyException) {
                UnrecognizedPropertyException unrecognizedPropertyException = (UnrecognizedPropertyException) e;
                String unrecognizedProperty = unrecognizedPropertyException.getUnrecognizedPropertyName();
                errorMessage = "Unrecognized property '" + unrecognizedProperty + "'";
                logError(errorMessage, e);
                //noinspection ThrowableResultOfMethodCallIgnored
                return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
            } else {
                errorMessage = "One or more request body parameters contain disallowed values.";
                logError(errorMessage, e);
                //noinspection ThrowableResultOfMethodCallIgnored
                return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
            }
        }

        if (e instanceof AuthenticationException) {
            ErrorDTO errorDetail = new ErrorDTO();
            errorDetail.setCode((long) 401);
            errorDetail.setMoreInfo("");
            errorDetail.setMessage("");
            errorDetail.setDescription(e.getMessage());
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(errorDetail)
                    .build();
        }

        //This occurs when received an empty body in an occasion where the body is mandatory
        if (e instanceof EOFException) {
            errorMessage = "Request payload cannot be empty.";
            logError(errorMessage, e);
            //noinspection ThrowableResultOfMethodCallIgnored
            return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
        }

        //unknown exception log and return
        errorMessage = "An Unknown exception has been captured by global exception mapper.";
        logError(errorMessage, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json")
                .entity(e500).build();
    }


    private void logError(String errorMessage, Throwable e) {
        if (log.isDebugEnabled()) {
            log.error(errorMessage, e);
        } else {
            log.error(errorMessage);
        }
    }
}
