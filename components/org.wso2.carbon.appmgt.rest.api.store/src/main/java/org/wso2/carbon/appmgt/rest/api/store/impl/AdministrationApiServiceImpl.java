package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.mobile.store.Operations;
import org.wso2.carbon.appmgt.mobile.utils.MobileApplicationException;
import org.wso2.carbon.appmgt.rest.api.store.AdministrationApiService;
import org.wso2.carbon.appmgt.rest.api.store.dto.AdminInstallDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.ErrorListItemDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.RoleIdListDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.UserIdListDTO;
import org.wso2.carbon.appmgt.rest.api.util.exception.NotFoundException;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.appmgt.rest.api.util.validation.BeanValidator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AdministrationApiServiceImpl extends AdministrationApiService {
    private static final Log log = LogFactory.getLog(AdministrationApiServiceImpl.class);
    BeanValidator beanValidator;

    @Override
    public Response administrationAppsDownloadPost(String contentType, AdminInstallDTO install) {
        beanValidator = new BeanValidator();
        beanValidator.validate(install);

        if (install.getAppId() == null) {
            String errorMessage = "Apps not found in payload.";
            return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
        }

        //Type : either role wise or user wise
        String type;
        if (install.getType().equalsIgnoreCase("role") || install.getType().equalsIgnoreCase("user")) {
            type = install.getType();
        } else {
            String errorMessage = "Type cannot be found in payload.";
            return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
        }

        //Id list related to type (eg: if Roles, role ids)
        JSONArray typeIds = (JSONArray) new JSONValue().parse(JSONArray.toJSONString((List) install.getTypeIds()));

        try {
            String appId = install.getAppId();
            //check app validity
            Map<String, String> searchTerms = new HashMap<String, String>();
            searchTerms.put("id", appId);
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            List<App> result = appProvider.searchApps(AppMConstants.MOBILE_ASSET_TYPE, searchTerms);
            if (result.isEmpty()) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }

            String loggedUserName = RestApiUtil.getLoggedInUsername();
            String tenantDomainName = MultitenantUtils.getTenantDomain(loggedUserName);
            String tenantUserName = MultitenantUtils.getTenantAwareUsername(loggedUserName);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                    tenantUserName, tenantId);

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry) registry, "mobileapp");

            Iterator<String> typeIdIterator = typeIds.iterator();
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode(Long.valueOf(Response.Status.OK.getStatusCode()));
            errorDTO.setDescription("App(s) download status details.");
            errorDTO.setMoreInfo("user: " + tenantUserName + ", type of operation (user/role wise)" + ": " + type);
            List<ErrorListItemDTO> errorListItemDTOs = new ArrayList<>();

            while (typeIdIterator.hasNext()) {
                String typeId = typeIdIterator.next();

                ErrorListItemDTO errorListItemDTO = new ErrorListItemDTO();
                GenericArtifact artifact = artifactManager.getGenericArtifact(appId);
                String parameterContext =
                        "[appId: " + appId + "]";


                if (artifact != null) {
                    try {
                        Operations mobileOperation = new Operations();
                        String action = "install";
                        String[] parameters = null;

                        JSONObject user = new JSONObject();
                        user.put("username", tenantUserName);
                        user.put("tenantDomain", tenantDomainName);
                        user.put("tenantId", tenantId);
                        appProvider.subscribeMobileApp(loggedUserName, appId);

                        if ("role".equalsIgnoreCase(type)) {
                            PrivilegedCarbonContext carbonContext =
                                    PrivilegedCarbonContext.getThreadLocalCarbonContext();
                            RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class,
                                                                                                    null);
                            UserRealm realm = realmService.getTenantUserRealm(tenantId);
                            UserStoreManager manager = realm.getUserStoreManager();
                            String[] userNames;
                            userNames = manager.getUserListOfRole(typeId);
                            if (userNames == null) {
                                return RestApiUtil.buildNotFoundException("Users not found", null).getResponse();
                            }

                            for (int i = 0; i < userNames.length; i++) {
                                parameters = new String[1];
                                parameters[0] = userNames[i];
                                mobileOperation.performAction(user.toString(), action, tenantId, appId,
                                                              install.getType(),
                                                              parameters);
                            }
                        } else if ("user".equalsIgnoreCase(type)) {
                            parameters = new String[1];
                            parameters[0] = typeId;
                            mobileOperation.performAction(user.toString(), action, tenantId, appId,
                                                          install.getType(),
                                                          parameters);

                        } else {
                            RestApiUtil.handleBadRequest("Invalid installation type.", log);
                        }
                    } catch (NotFoundException e) {
                        errorListItemDTO.setCode(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
                        errorListItemDTO.setMessage(type + ": " + typeId + " Not Found");
                    } catch (MobileApplicationException e) {
                        errorListItemDTO.setCode(String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
                        errorListItemDTO.setMessage("Error while installing App on Mobile device.");
                    }
                } else {
                    errorListItemDTO.setCode(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
                    errorListItemDTO.setMessage("App Not Found. " + parameterContext);
                }

                if (errorListItemDTO.getCode() == null) {
                    errorListItemDTO.setCode(String.valueOf(Response.Status.ACCEPTED.getStatusCode()));
                    errorListItemDTO.setMessage(Response.Status.ACCEPTED + " " + parameterContext);
                }
                errorListItemDTOs.add(errorListItemDTO);
            }
            errorDTO.setError(errorListItemDTOs);
            return Response.status(Response.Status.ACCEPTED).entity(errorDTO).build();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while initializing UserStore.");
            RestApiUtil.buildInternalServerErrorException();
        } catch (RegistryException e) {
            log.error("Error while initializing Registry.");
            RestApiUtil.buildInternalServerErrorException();
        } catch (JSONException e) {
            RestApiUtil.handleInternalServerError("Json casting Error occurred while installing", e, log);
        } catch (AppManagementException e) {
            log.error("Error while Downloading the App.");
            RestApiUtil.buildInternalServerErrorException();
        }

        return null;
    }


    @Override
    public Response administrationAppsUninstallationPost(String contentType, AdminInstallDTO install) {
        beanValidator = new BeanValidator();
        beanValidator.validate(install);

        if (install.getAppId() == null) {
            String errorMessage = "Apps not found in payload.";
            return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
        }

        //Type : either role wise or user wise
        String type;
        if (install.getType().equalsIgnoreCase("role") || install.getType().equalsIgnoreCase("user")) {
            type = install.getType();
        } else {
            String errorMessage = "Type cannot be found in payload.";
            return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
        }

        //Id list related to type (eg: if Roles, role ids)
        JSONArray typeIds = (JSONArray) new JSONValue().parse(JSONArray.toJSONString((List) install.getTypeIds()));

        try {
            String appId = install.getAppId();
            //check app validity
            Map<String, String> searchTerms = new HashMap<String, String>();
            searchTerms.put("id", appId);
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            List<App> result = appProvider.searchApps(AppMConstants.MOBILE_ASSET_TYPE, searchTerms);
            if (result.isEmpty()) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }

            String loggedUserName = RestApiUtil.getLoggedInUsername();
            String tenantDomainName = MultitenantUtils.getTenantDomain(loggedUserName);
            String tenantUserName = MultitenantUtils.getTenantAwareUsername(loggedUserName);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                    tenantUserName, tenantId);

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry) registry, "mobileapp");

            Iterator<String> typeIdIterator = typeIds.iterator();
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode(Long.valueOf(Response.Status.OK.getStatusCode()));
            errorDTO.setDescription("App(s) download status details.");
            errorDTO.setMoreInfo("user: " + tenantUserName + ", type of operation (user/role wise)" + ": " + type);
            List<ErrorListItemDTO> errorListItemDTOs = new ArrayList<>();

            while (typeIdIterator.hasNext()) {
                String typeId = typeIdIterator.next();

                ErrorListItemDTO errorListItemDTO = new ErrorListItemDTO();
                GenericArtifact artifact = artifactManager.getGenericArtifact(appId);
                String parameterContext =
                        "[appId: " + appId + "]";


                if (artifact != null) {
                    try {
                        Operations mobileOperation = new Operations();
                        String action = "uninstall";
                        String[] parameters = null;

                        JSONObject user = new JSONObject();
                        user.put("username", tenantUserName);
                        user.put("tenantDomain", tenantDomainName);
                        user.put("tenantId", tenantId);
                        appProvider.subscribeMobileApp(loggedUserName, appId);

                        if ("role".equalsIgnoreCase(type)) {
                            PrivilegedCarbonContext carbonContext =
                                    PrivilegedCarbonContext.getThreadLocalCarbonContext();
                            RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class,
                                                                                                    null);
                            UserRealm realm = realmService.getTenantUserRealm(tenantId);
                            UserStoreManager manager = realm.getUserStoreManager();
                            String[] userNames;
                            userNames = manager.getUserListOfRole(typeId);
                            if (userNames == null) {
                                return RestApiUtil.buildNotFoundException("Users not found", null).getResponse();
                            }

                            for (int i = 0; i < userNames.length; i++) {
                                parameters = new String[1];
                                parameters[0] = userNames[i];
                                mobileOperation.performAction(user.toString(), action, tenantId, appId,
                                                              install.getType(),
                                                              parameters);
                            }
                        } else if ("user".equalsIgnoreCase(type)) {
                            parameters = new String[1];
                            parameters[0] = typeId;
                            mobileOperation.performAction(user.toString(), action, tenantId, appId,
                                                          install.getType(),
                                                          parameters);

                        } else {
                            RestApiUtil.handleBadRequest("Invalid installation type.", log);
                        }
                    } catch (NotFoundException e) {
                        errorListItemDTO.setCode(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
                        errorListItemDTO.setMessage(type + ": " + typeId + " Not Found");
                    } catch (MobileApplicationException e) {
                        errorListItemDTO.setCode(String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
                        errorListItemDTO.setMessage("Error while installing App on Mobile device.");
                    }
                } else {
                    errorListItemDTO.setCode(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
                    errorListItemDTO.setMessage("App Not Found. " + parameterContext);
                }

                if (errorListItemDTO.getCode() == null) {
                    errorListItemDTO.setCode(String.valueOf(Response.Status.ACCEPTED.getStatusCode()));
                    errorListItemDTO.setMessage(Response.Status.ACCEPTED + " " + parameterContext);
                }
                errorListItemDTOs.add(errorListItemDTO);
            }
            errorDTO.setError(errorListItemDTOs);
            return Response.status(Response.Status.ACCEPTED).entity(errorDTO).build();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while initializing UserStore.");
            RestApiUtil.buildInternalServerErrorException();
        } catch (RegistryException e) {
            log.error("Error while initializing Registry.");
            RestApiUtil.buildInternalServerErrorException();
        } catch (JSONException e) {
            RestApiUtil.handleInternalServerError("Json casting Error occurred while installing", e, log);
        } catch (AppManagementException e) {
            log.error("Error while Downloading the App.");
            RestApiUtil.buildInternalServerErrorException();
        }

        return null;
    }

    @Override
    public Response administrationRolesGet(Integer limit, Integer offset, String accept, String ifNoneMatch) {
        RoleIdListDTO roleListDTO = new RoleIdListDTO();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
        String[] roleNames = null;

        try {
            String tenantDomainName = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            UserRealm realm = realmService.getTenantUserRealm(tenantId);
            UserStoreManager manager = realm.getUserStoreManager();
            roleNames = manager.getRoleNames();
            if (roleNames == null) {
                return RestApiUtil.buildNotFoundException("Roles not found", null).getResponse();
            }
        } catch (UserStoreException e) {
            return RestApiUtil.buildInternalServerErrorException().getResponse();
        }

        JSONArray roleNamesArr = new JSONArray();
        for (int i = 0; i < roleNames.length; i++) {
            String roleName = roleNames[i];
            //exclude internal roles
            if (roleName.indexOf("Internal/") <= -1) {
                roleNamesArr.add(roleName);
            }
        }
        roleListDTO.setRoleIds(roleNamesArr);
        return Response.ok().entity(roleListDTO).build();
    }

    @Override
    public Response administrationUsersGet(Integer limit, Integer offset, String accept, String ifNoneMatch) {
        UserIdListDTO userListDTO = new UserIdListDTO();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
        String[] userNames = null;

        try {
            String tenantDomainName = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            UserRealm realm = realmService.getTenantUserRealm(tenantId);
            UserStoreManager manager = realm.getUserStoreManager();
            userNames = manager.listUsers("", -1);
            if (userNames == null) {
                return RestApiUtil.buildNotFoundException("Users not found", null).getResponse();
            }
        } catch (UserStoreException e) {
            return RestApiUtil.buildInternalServerErrorException().getResponse();
        }

        JSONArray userNamesArr = new JSONArray();
        for (int i = 0; i < userNames.length; i++) {
            userNamesArr.add(userNames[i]);
        }
        userListDTO.setUserIds(userNamesArr);
        return Response.ok().entity(userListDTO).build();
    }
}
