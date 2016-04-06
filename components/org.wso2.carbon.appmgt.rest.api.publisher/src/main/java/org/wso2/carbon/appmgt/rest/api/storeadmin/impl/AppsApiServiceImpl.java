package org.wso2.carbon.appmgt.rest.api.storeadmin.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.APPLifecycleActions;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.rest.api.storeadmin.ApiResponseMessage;
import org.wso2.carbon.appmgt.rest.api.storeadmin.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class AppsApiServiceImpl extends AppsApiService {

    private static final Log log = LogFactory.getLog(AppsApiService.class);

    @Override
    public Response appsMobileBinariesPost(InputStream fileInputStream, Attachment fileDetail, String ifMatch,
                                           String ifUnmodifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeGet(String appType, String query, Integer limit, Integer offset, String accept,
                                   String ifNoneMatch) {
        List<WebApp> allMatchedApis;
        AppListDTO appListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        try {
            //handle type
            if (!appType.equalsIgnoreCase(AppMConstants.APP_TYPE) &&
                    !appType.equalsIgnoreCase(AppMConstants.MOBILE_ASSET_TYPE)) {
                String errorMessage = "Invalid Asset Type : " + appType;
                return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
            }

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //if query parameter is not specified, This will search by name
            String searchType = AppMConstants.API_NAME;
            String searchContent = "";
            if (!StringUtils.isBlank(query)) {
                String[] querySplit = query.split(":");
                if (querySplit.length == 2 && StringUtils.isNotBlank(querySplit[0]) && StringUtils
                        .isNotBlank(querySplit[1])) {
                    searchType = querySplit[0];
                    searchContent = querySplit[1];
                } else if (querySplit.length == 1) {
                    searchContent = query;
                } else {
                    RestApiUtil.handleBadRequest("Provided query parameter '" + query + "' is invalid", log);
                }
            }

            //We should send null as the provider, Otherwise searchAPIs will return all APIs of the provider
            // instead of looking at type and query
            allMatchedApis = apiProvider.searchAppsWithOptionalType(searchContent, searchType, null, appType);
            if (allMatchedApis.isEmpty()) {
                String errorMessage = "No result found.";
                return RestApiUtil.buildNotFoundException(errorMessage, null).getResponse();
            }
            appListDTO = APPMappingUtil.fromAPIListToDTO(allMatchedApis, offset, limit);
            APPMappingUtil.setPaginationParams(appListDTO, query, offset, limit, allMatchedApis.size());
            return Response.ok().entity(appListDTO).build();
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving Apps";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response appsAppTypePost(String appType, AppDTO body, String contentType, String ifModifiedSince) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response appsAppTypeChangeLifecyclePost(String appType, String action, String appId, String ifMatch,
                                                   String ifUnmodifiedSince) {
        try {
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            boolean isValidAction = false;

            for(APPLifecycleActions appLifecycleAction : APPLifecycleActions.values()){
                if(appLifecycleAction.getStatus().equalsIgnoreCase(action)){
                    isValidAction = true;
                    break;
                }
            }
            if(!isValidAction){
                RestApiUtil.handleBadRequest("Invalid action '" + action + "' performed on a " + appType
                        + " with UUID " + appId, log);
            }
            String[] allowedLifecycleActions = appProvider.getAllowedLifecycleActions(appId, appType);
            if (!ArrayUtils.contains(allowedLifecycleActions, action)) {
                RestApiUtil.handleBadRequest(
                        "Action '" + action + "' is not allowed to perform on " + appType + " with id: " + appId +
                                ". Allowed actions are " + Arrays.toString(allowedLifecycleActions), log);
            }
            appProvider.changeLifeCycleStatus(appType, appId, action);
            return Response.accepted().build();
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while changing lifcycle state of app with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdGet(String appType, String appId, String accept, String ifNoneMatch,
                                          String ifModifiedSince) {
        AppDTO apiToReturn;
        try {
            //WebApp webApp = APPMappingUtil.getAPIFromApiIdOrUUID(appId);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String searchContent = appId;
            String searchType = "id";
            List<WebApp> allMatchedApps = apiProvider.searchAppsWithOptionalType(searchContent, searchType, null,
                                                                                 appType);
            if (allMatchedApps.isEmpty()) {
                String errorMessage = "Could not find requested application.";
                RestApiUtil.buildNotFoundException(errorMessage, appId);
            }
            WebApp webApp = allMatchedApps.get(0);
            apiToReturn = APPMappingUtil.fromAPItoDTO(webApp);
            return Response.ok().entity(apiToReturn).build();
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while retrieving App : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdPut(String appType, String appId, AppDTO body, String contentType, String ifMatch,
                                          String ifUnmodifiedSince) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response appsAppTypeIdAppIdDelete(String appType, String appId, String ifMatch, String ifUnmodifiedSince) {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String searchContent = appId;
            String searchType = "id";
            List<WebApp> allMatchedApps = apiProvider.searchAppsWithOptionalType(searchContent, searchType, null,
                                                                                 appType);
            if (allMatchedApps.isEmpty()) {
                String errorMessage = "Could not find requested application.";
                RestApiUtil.buildNotFoundException(errorMessage, appId);
            }
            WebApp webApp = allMatchedApps.get(0);
            APIIdentifier apiIdentifier = webApp.getId();
            if (appType.equals(AppMConstants.APP_TYPE)) {
                if (webApp.isAdvertiseOnly()) {
                    removeArtifactOnly(webApp, username);
                } else {
                    apiProvider.deleteApp(webApp.getId(), webApp.getSsoProviderDetails());
                }
            } else if (appType.equals(AppMConstants.MOBILE_ASSET_TYPE)) {
                removeArtifactOnly(webApp, username);
            }
            return Response.ok().build();
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while deleting App : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (RegistryException e) {
            RestApiUtil.handleInternalServerError("Error while initializing registry", e, log);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error while initializing UserStore", e, log);
        }
        return null;
    }

    //remove artifact from registry
    private void removeArtifactOnly(WebApp webApp, String username)
            throws RegistryException, AppManagementException, UserStoreException {
        String tenantDomainName = MultitenantUtils.getTenantDomain(username);
        String tenantUserName = MultitenantUtils.getTenantAwareUsername(username);
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                tenantDomainName);
        Registry registry = ServiceReferenceHolder.getInstance().
                getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);

        GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry,
                                                                                   AppMConstants.MOBILE_ASSET_TYPE);
        artifactManager.removeGenericArtifact(webApp.getUUID());
    }
}
