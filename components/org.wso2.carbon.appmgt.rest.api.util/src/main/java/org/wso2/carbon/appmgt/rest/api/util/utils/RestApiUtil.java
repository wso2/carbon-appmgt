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
 * /
 */

package org.wso2.carbon.appmgt.rest.api.util.utils;

import org.apache.commons.logging.Log;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.util.dto.ErrorListItemDTO;
import org.wso2.carbon.appmgt.rest.api.util.exception.BadRequestException;
import org.wso2.carbon.appmgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.context.CarbonContext;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestApiUtil {
    public static <T> ErrorDTO getConstraintViolationErrorDTO(Set<ConstraintViolation<T>> violations) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setDescription("Validation Error");
        errorDTO.setMessage("Bad Request");
        errorDTO.setCode(400l);
        errorDTO.setMoreInfo("");
        List<ErrorListItemDTO> errorListItemDTOs = new ArrayList<ErrorListItemDTO>();
        for (ConstraintViolation violation : violations) {
            ErrorListItemDTO errorListItemDTO = new ErrorListItemDTO();
            errorListItemDTO.setCode(400 + "_" + violation.getPropertyPath());
            errorListItemDTO.setMessage(violation.getPropertyPath() + ": " + violation.getMessage());
            errorListItemDTOs.add(errorListItemDTO);
        }
        errorDTO.setError(errorListItemDTOs);
        return errorDTO;
    }

    public static APIProvider getLoggedInUserProvider() throws AppManagementException {
        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        return APIManagerFactory.getInstance().getAPIProvider(loggedInUser);
    }

    /**
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param msg error message
     * @param log Log instance
     * @throws org.wso2.carbon.appmgt.rest.api.util.exception.BadRequestException
     */
    public static void handleBadRequest(String msg, Log log) throws BadRequestException {
        BadRequestException badRequestException = buildBadRequestException(msg);
        log.error(msg);
        throw badRequestException;
    }

    /**
     * Returns a new BadRequestException
     *
     * @param description description of the exception
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(String description) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400l, description);
        return new BadRequestException(errorDTO);
    }

    /**
     * Returns a generic errorDTO
     *
     * @param message specifies the error message
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(String message, Long code, String description) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMoreInfo("");
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        return errorDTO;
    }

    /**
     * Returns the paginated url for APIs API
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @param query  search query value
     * @return constructed paginated url
     */
    public static String getAPIPaginatedURL(Integer offset, Integer limit, String query) {
        String paginatedURL = RestApiConstants.APIS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.QUERY_PARAM, query);
        return paginatedURL;
    }

    /**
     * Returns the next/previous offset/limit parameters properly when current offset, limit and size parameters are
     * specified
     *
     * @param offset current starting index
     * @param limit  current max records
     * @param size   maximum index possible
     * @return the next/previous offset/limit parameters as a hash-map
     */
    public static Map<String, Integer> getPaginationParams(Integer offset, Integer limit, Integer size) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        if (offset >= size || offset < 0) {
            return result;
        }

        int start = offset;
        int end = offset + limit - 1;

        int nextStart = end + 1;
        if (nextStart < size) {
            result.put(RestApiConstants.PAGINATION_NEXT_OFFSET, nextStart);
            result.put(RestApiConstants.PAGINATION_NEXT_LIMIT, limit);
        }

        int previousEnd = start - 1;
        int previousStart = previousEnd - limit + 1;

        if (previousEnd >= 0) {
            if (previousStart < 0) {
                result.put(RestApiConstants.PAGINATION_PREVIOUS_OFFSET, 0);
                result.put(RestApiConstants.PAGINATION_PREVIOUS_LIMIT, limit);
            } else {
                result.put(RestApiConstants.PAGINATION_PREVIOUS_OFFSET, previousStart);
                result.put(RestApiConstants.PAGINATION_PREVIOUS_LIMIT, limit);
            }
        }
        return result;
    }


    /**
     * Logs the error, builds a internalServerErrorException with specified details and throws it
     *
     * @param msg error message
     * @param t   Throwable instance
     * @param log Log instance
     * @throws org.wso2.carbon.appmgt.rest.api.util.exception.InternalServerErrorException
     */
    public static void handleInternalServerError(String msg, Throwable t, Log log)
            throws InternalServerErrorException {
        InternalServerErrorException internalServerErrorException = buildInternalServerErrorException();
        log.error(msg, t);
        throw internalServerErrorException;
    }

    /**
     * Returns a new InternalServerErrorException
     *
     * @return a new InternalServerErrorException with default details as a response DTO
     */
    public static InternalServerErrorException buildInternalServerErrorException() {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT, 500l,
                                        RestApiConstants.STATUS_INTERNAL_SERVER_ERROR_DESCRIPTION_DEFAULT);
        return new InternalServerErrorException(errorDTO);
    }
}
