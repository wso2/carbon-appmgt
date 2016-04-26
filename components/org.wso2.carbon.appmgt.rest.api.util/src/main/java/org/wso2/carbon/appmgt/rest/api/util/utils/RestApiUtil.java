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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.AppMgtAuthorizationFailedException;
import org.wso2.carbon.appmgt.api.AppMgtResourceNotFoundException;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.util.dto.ErrorListItemDTO;
import org.wso2.carbon.appmgt.rest.api.util.exception.*;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.validation.ConstraintViolation;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RestApiUtil {

    private static final Log log = LogFactory.getLog(RestApiUtil.class);

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

    public static String getLoggedInUsername() {
        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    public static APIProvider getProvider(String username) throws AppManagementException {
        return APIManagerFactory.getInstance().getAPIProvider(username);
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
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param msg error message
     * @param log Log instance
     * @throws org.wso2.carbon.appmgt.rest.api.util.exception.BadRequestException
     */
    public static void handleConflictException(String msg, Log log) throws ConflictException {
        ConflictException conflictException = buildConflictException(msg);
        log.error(msg);
        throw conflictException;
    }

    /**
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param msg error message
     * @param log Log instance
     * @throws org.wso2.carbon.appmgt.rest.api.util.exception.BadRequestException
     */
    public static void handlePreconditionFailedRequest(String msg, Log log) throws BadRequestException {
        PreconditionFailedException preconditionFailedRequest = buildPreconditionFailedRequestException(msg);
        log.error(msg);
        throw preconditionFailedRequest;
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
     * Returns a new ConflictException
     *
     * @param description description of the exception
     * @return a new ConflictException with the specified details as a response DTO
     */
    public static ConflictException buildConflictException(String description) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_CONFLCIT_MESSAGE_DEFAULT, 409l, description);
        return new ConflictException(errorDTO);
    }


    /**
     * Returns a new BadRequestException
     *
     * @param description description of the exception
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static PreconditionFailedException buildPreconditionFailedRequestException(String description) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_PRECONDITION_FAILED_MESSAGE_DEFAULT, 412l, description);
        return new PreconditionFailedException(errorDTO);
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

    public static String getLoggedInUserTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }


    /**
     * Check if the specified throwable e is happened as the required resource cannot be found
     *
     * @param e throwable to check
     * @return true if the specified throwable e is happened as the required resource cannot be found, false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToResourceNotFound(Throwable e) {
        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause instanceof AppMgtResourceNotFoundException
                || rootCause instanceof ResourceNotFoundException;
    }

    /**
     * Attempts to find the actual cause of the throwable 'e'
     *
     * @param e throwable
     * @return the root cause of 'e' if the root cause exists, otherwise returns 'e' itself
     */
    private static Throwable getPossibleErrorCause(Throwable e) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        rootCause = rootCause == null ? e : rootCause;
        return rootCause;
    }

    /**
     * Check if the specified throwable e is due to an authorization failure
     *
     * @param e throwable to check
     * @return true if the specified throwable e is due to an authorization failure, false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToAuthorizationFailure(Throwable e) {
        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause instanceof AuthorizationFailedException
                || rootCause instanceof AppMgtAuthorizationFailedException;
    }

    /**
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param resource requested resource
     * @param id       id of resource
     * @param t        Throwable instance
     * @param log      Log instance
     * @throws org.wso2.carbon.appmgt.rest.api.util.exception.NotFoundException
     */
    public static void handleResourceNotFoundError(String resource, String id, Throwable t, Log log)
            throws NotFoundException {
        NotFoundException notFoundException = buildNotFoundException(resource, id);
        log.error(notFoundException.getMessage(), t);
        throw notFoundException;
    }

    /**
     * Returns a new NotFoundException
     *
     * @param resource Resource type
     * @param id       identifier of the resource
     * @return a new NotFoundException with the specified details as a response DTO
     */
    public static NotFoundException buildNotFoundException(String resource, String id) {
        String description;
        if (!StringUtils.isEmpty(id)) {
            description = "Requested " + resource + " with Id '" + id + "' not found";
        } else {
            description = "Requested " + resource + " not found";
        }
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_NOT_FOUND_MESSAGE_DEFAULT, 404l, description);
        return new NotFoundException(errorDTO);
    }

    /**
     * Check whether the specified apiId is of type UUID
     *
     * @param apiId api identifier
     * @return true if apiId is of type UUID, false otherwise
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isUUID(String apiId) {
        try {
            UUID.fromString(apiId);
            return true;
        } catch (IllegalArgumentException e) {
            if (log.isDebugEnabled()) {
                log.debug(apiId + " is not a valid UUID");
            }
            return false;
        }

    }

    /**
     * This method uploads a given file to specified location
     *
     * @param uploadedInputStream input stream of the file
     * @param newFileName         name of the file to be created
     * @param storageLocation     destination of the new file
     * @throws AppManagementException if the file transfer fails
     */
    public static void transferFile(InputStream uploadedInputStream, String newFileName, String storageLocation)
            throws AppManagementException {
        FileOutputStream outFileStream = null;

        try {
            outFileStream = new FileOutputStream(new File(storageLocation, newFileName));
            int read;
            byte[] bytes = new byte[1024];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outFileStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            String errorMessage = "Error in transferring file : " + newFileName + " into storage location : " +
                    storageLocation;
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(outFileStream);
        }
    }

    public static boolean isExistingUser(String username) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
        try {
            String tenantDomainName = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            UserRealm realm = realmService.getTenantUserRealm(tenantId);
            UserStoreManager manager = realm.getUserStoreManager();
            return manager.isExistingUser(username);
        } catch (UserStoreException e) {
            log.error(e);
            return false;
        }
    }

    public static boolean isExistingRole(String roleName) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
        try {
            String tenantDomainName = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            UserRealm realm = realmService.getTenantUserRealm(tenantId);
            UserStoreManager manager = realm.getUserStoreManager();
            return manager.isExistingRole(roleName);
        } catch (UserStoreException e) {
            log.error(e);
            return false;
        }
    }
}
