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
import org.wso2.carbon.appmgt.api.*;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
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
import org.wso2.uri.template.URITemplate;
import org.wso2.uri.template.URITemplateException;

import javax.validation.ConstraintViolation;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestApiUtil {

    private static final Log log = LogFactory.getLog(RestApiUtil.class);
    private static Dictionary<org.wso2.uri.template.URITemplate, List<String>> uriToHttpMethodsMap;

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


    public static APIConsumer getConsumer(String subscriberName) throws AppManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
    }

    /**
     * Returns an APIConsumer which is corresponding to the current logged in user taken from the carbon context
     *
     * @return an APIConsumer which is corresponding to the current logged in user
     * @throws AppManagementException
     */
    public static APIConsumer getLoggedInUserConsumer() throws AppManagementException {
        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        return APIManagerFactory.getInstance().getAPIConsumer(loggedInUser);
    }

    /**
     * Url validator, Allow any url with https and http.
     * Allow any url without fully qualified domain
     *
     * @param url Url as string
     * @return boolean type stating validated or not
     */
    public static boolean isURL(String url) {

        Pattern pattern = Pattern.compile("^(http|https)://(.)+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();

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
    public static void handleForbiddenRequest(String msg, Log log) throws ForbiddenException {
        ForbiddenException forbiddenException = buildForbiddenException(msg);
        log.error(msg);
        throw forbiddenException;
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
     * Returns a new ConflictException
     *
     * @param description description of the exception
     * @return a new ConflictException with the specified details as a response DTO
     */
    public static ForbiddenException buildForbiddenException(String description) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT, 403l, description);
        return new ForbiddenException(errorDTO);
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
     * Returns the paginated url for APIs API
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getAPIPaginatedURL(Integer offset, Integer limit) {
        String paginatedURL = RestApiConstants.APIS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /**
     * Returns the paginated url for APIs API
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getAppRatingPaginatedURL(Integer offset, Integer limit) {
        String paginatedURL = RestApiConstants.APP_RATE_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
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
     * Logs the error, builds a internalServerErrorException with specified details and throws it
     *
     * @param msg error message
     * @param log Log instance
     * @throws org.wso2.carbon.appmgt.rest.api.util.exception.InternalServerErrorException
     */
    public static void handleInternalServerError(String msg, Log log)
            throws InternalServerErrorException {
        InternalServerErrorException internalServerErrorException = buildInternalServerErrorException();
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
     * Check if the specified throwable e is happened as the required resource cannot be found
     *
     * @param e throwable to check
     * @return true if the specified throwable e is happened as the required resource cannot be found, false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToResourceAlreadyExisting(Throwable e) {
        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause instanceof AppMgtResourceAlreadyExistsException;
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
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param resource requested resource
     * @param id       id of resource
     * @param log      Log instance
     * @throws org.wso2.carbon.appmgt.rest.api.util.exception.NotFoundException
     */
    public static void handleResourceNotFoundError(String resource, String id, Log log)
            throws NotFoundException {
        NotFoundException notFoundException = buildNotFoundException(resource, id);
        throw notFoundException;
    }

    /**
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param description error description
     * @param t        Throwable instance
     * @param log      Log instance
     * @throws org.wso2.carbon.appmgt.rest.api.util.exception.NotFoundException
     */
    public static void handleAuthorizationFailedError(String description, Throwable t, Log log)
            throws ForbiddenException{
        ForbiddenException forbiddenException = buildAuthorizationFailedException(description);
        log.error(forbiddenException.getMessage(), t);
        throw forbiddenException;
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
     * Returns a new ForbiddenException
     *
     * @param description  Error description
     * @return a new NotFoundException with the specified details as a response DTO
     */
    public static ForbiddenException buildAuthorizationFailedException(String description) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 403l, description);
        return new ForbiddenException(errorDTO);
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
            outFileStream = new FileOutputStream(new File(AppManagerUtil.resolvePath(storageLocation, newFileName)));
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

    public static File readFileFromStorage(String fileName) throws AppManagementException {
        File storageFile = null;
        AppManagerConfiguration appManagerConfiguration = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        storageFile = new File(AppManagerUtil.resolvePath(
                appManagerConfiguration.getFirstProperty(AppMConstants.BINARY_FILE_STORAGE_ABSOLUTE_LOCATION), fileName));
        return storageFile;
    }

    public static boolean isValidFileName(String fileName){
        boolean isValid = true;
        if(fileName == null || StringUtils.isEmpty(fileName) || (fileName.indexOf('\u0000') > 0)){
            isValid = false;
        }
        return isValid;
    }

    public static String readFileContentType(String filePath) throws AppManagementException{
        String fileContentType = null;
        try {
         fileContentType = Files.probeContentType(Paths.get(filePath));
        } catch (IOException e) {
            throw new AppManagementException("Error occurred while reading file details from file "+filePath);
        }
        return fileContentType;
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

    public static boolean isSubscriptionEnable() {
        AppManagerConfiguration appManagerConfiguration = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        Boolean selfSubscriptionStatus = Boolean.valueOf(appManagerConfiguration.getFirstProperty(
                AppMConstants.ENABLE_SELF_SUBSCRIPTION));
        Boolean enterpriseSubscriptionStatus = Boolean.valueOf(appManagerConfiguration.getFirstProperty(
                AppMConstants.ENABLE_ENTERPRISE_SUBSCRIPTION));

        boolean isSubscriptionEnable = (selfSubscriptionStatus || enterpriseSubscriptionStatus);
        return isSubscriptionEnable;
    }

    /**
     *
     * Returns the seach terms of the given query string.
     *
     * @param query
     * @return
     */
    public static Map<String, String> getSearchTerms(String query) {

        Map<String, String> searchTerms = new HashMap<String, String>();

        if(query != null && !query.isEmpty()){

            String[] termTokens = query.split(",");

            String[] termElements = null;
            for(String termToken : termTokens){
                termElements = termToken.split(":");
                searchTerms.put(termElements[0], termElements[1]);
            }
        }

        return searchTerms;
    }

    /**
     * Returns the white-listed URIs and associated HTTP methods for REST API. If not already read before, reads
     * app-manager.xml configuration, store the results in a static reference and returns the results.
     * Otherwise returns previously stored the static reference object.
     *
     * @return A Dictionary with the white-listed URIs and the associated HTTP methods
     * @throws AppManagementException
     */
    public static Dictionary<URITemplate, List<String>> getWhiteListedURIsToMethodsMap()
            throws AppManagementException {

        if (uriToHttpMethodsMap == null) {
            uriToHttpMethodsMap = getWhiteListedURIsToMethodsMapFromConfig();
        }
        return uriToHttpMethodsMap;
    }

    /**
     * Returns the white-listed URIs and associated HTTP methods for REST API by reading api-manager.xml configuration
     *
     * @return A Dictionary with the white-listed URIs and the associated HTTP methods
     * @throws AppManagementException
     */
    private static Dictionary<org.wso2.uri.template.URITemplate, List<String>> getWhiteListedURIsToMethodsMapFromConfig()
            throws AppManagementException {
        Hashtable<org.wso2.uri.template.URITemplate, List<String>> uriToMethodsMap = new Hashtable<>();
        AppManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        List<String> uriList = apiManagerConfiguration
                .getProperty(AppMConstants.APPM_RESTAPI_WHITELISTED_URI_URI);
        List<String> methodsList = apiManagerConfiguration
                .getProperty(AppMConstants.APPM_RESTAPI_WHITELISTED_URI_HTTPMethods);

        if (uriList != null && methodsList != null) {
            if (uriList.size() != methodsList.size()) {
                String errorMsg = "Provided White-listed URIs for REST API are invalid."
                        + " Every 'WhiteListedURI' should include 'URI' and 'HTTPMethods' elements";
                log.error(errorMsg);
                return new Hashtable<>();
            }

            for (int i = 0; i < uriList.size(); i++) {
                String uri = uriList.get(i);
                try {
                    org.wso2.uri.template.URITemplate uriTemplate = new org.wso2.uri.template.URITemplate(uri);
                    String methodsForUri = methodsList.get(i);
                    List<String> methodListForUri = Arrays.asList(methodsForUri.split(","));
                    uriToMethodsMap.put(uriTemplate, methodListForUri);
                } catch (URITemplateException e) {
                    String msg = "Error in parsing uri " + uri + " when retrieving white-listed URIs for REST API";
                    log.error(msg, e);
                    throw new AppManagementException(msg, e);
                }
            }
        }
        return uriToMethodsMap;
    }

    /**
     * Get Store REST API context path
     * @return context path of store REST APIs
     */
    public static String getStoreRESTAPIContextPath(){
        AppManagerConfiguration appManagerConfiguration = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        return appManagerConfiguration.getFirstProperty(
                AppMConstants.APPM_RESTAPI_STORE_API_CONTEXT_PATH);
    }
}
