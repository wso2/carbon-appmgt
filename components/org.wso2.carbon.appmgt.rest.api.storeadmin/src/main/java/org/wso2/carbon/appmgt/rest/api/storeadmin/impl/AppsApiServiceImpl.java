package org.wso2.carbon.appmgt.rest.api.storeadmin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.rest.api.storeadmin.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.ErrorListItemDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.InstallDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;


public class AppsApiServiceImpl extends AppsApiService {
    private static final Log log = LogFactory.getLog(AppsApiServiceImpl.class);

    @Override
    public Response appsDownloadPost(String contentType, InstallDTO install) {
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
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomainName = MultitenantUtils.getTenantDomain(username);
            String tenantUserName = MultitenantUtils.getTenantAwareUsername(username);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                    tenantUserName, tenantId);

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry) registry, "mobileapp");

            Iterator<String> typeIdIterator = typeIds.iterator();
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode(Long.valueOf(Response.Status.OK.getStatusCode()));
            errorDTO.setDescription("App Un-installation details.");

            while (typeIdIterator.hasNext()) {
                String typeId = typeIdIterator.next();

                ErrorListItemDTO errorListItemDTO = new ErrorListItemDTO();
                GenericArtifact artifact = artifactManager.getGenericArtifact(appId);
                String parameterContext =
                        "[user: " + tenantUserName + ", " + type + ": " + type + ", app: " + appId + "]";


                if (artifact != null) {
                    try {
                        if ("role".equals(type)) {
                            UserStoreManager userStoreManager =
                                    ((UserRegistry) registry).getUserRealm().getUserStoreManager();
                            String[] users = userStoreManager.getUserListOfRole(typeId);
                            for (String userId : users) {
                                APPMappingUtil.subscribeApp(registry, userId, appId);
                                APPMappingUtil.showAppVisibilityToUser(artifact.getPath(), userId, "ALLOW");
                            }
                        } else if ("user".equals(type)) {
                            APPMappingUtil.subscribeApp(registry, typeId, appId);
                            APPMappingUtil.showAppVisibilityToUser(artifact.getPath(), typeId, "ALLOW");
                        }
                    } catch (org.wso2.carbon.registry.api.RegistryException e) {
                        errorListItemDTO.setCode(String.valueOf(Response.Status.PRECONDITION_FAILED.getStatusCode()));
                        errorListItemDTO.setMessage("User have not Subscribed. " + parameterContext);
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        errorListItemDTO.setCode(String.valueOf(Response.Status.PRECONDITION_FAILED.getStatusCode()));
                        errorListItemDTO.setMessage("Error while updating visibility of App." + parameterContext);
                    }
                } else {
                    errorListItemDTO.setCode(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
                    errorListItemDTO.setMessage("App Not Found. " + parameterContext);
                }

                if (errorListItemDTO.getCode() == null) {
                    errorListItemDTO.setCode(String.valueOf(Response.Status.ACCEPTED.getStatusCode()));
                    errorListItemDTO.setMessage(parameterContext);
                }

            }
            return Response.ok().entity(errorDTO).build();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while initializing UserStore.");
            RestApiUtil.buildInternalServerErrorException();
        } catch (RegistryException e) {
            log.error("Error while initializing Registry.");
            RestApiUtil.buildInternalServerErrorException();
        }

        return null;
    }

    @Override
    public Response appsUninstallationPost(String contentType, InstallDTO install) {
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
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomainName = MultitenantUtils.getTenantDomain(username);
            String tenantUserName = MultitenantUtils.getTenantAwareUsername(username);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                    tenantUserName, tenantId);

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry) registry, "mobileapp");

            Iterator<String> typeIdIterator = typeIds.iterator();
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode(Long.valueOf(Response.Status.OK.getStatusCode()));
            errorDTO.setDescription("App Un-installation details.");

            while (typeIdIterator.hasNext()) {
                String typeId = typeIdIterator.next();

                ErrorListItemDTO errorListItemDTO = new ErrorListItemDTO();
                GenericArtifact artifact = artifactManager.getGenericArtifact(appId);
                String parameterContext =
                        "[user: " + tenantUserName + ", " + type + ": " + type + ", app: " + appId + "]";

                if (artifact != null) {
                    try {
                        if ("role".equals(type)) {
                            UserStoreManager userStoreManager =
                                    ((UserRegistry) registry).getUserRealm().getUserStoreManager();
                            String[] users = userStoreManager.getUserListOfRole(typeId);
                            for (String userId : users) {
                                APPMappingUtil.unSubscribeApp(registry, userId, appId);
                                APPMappingUtil.showAppVisibilityToUser(artifact.getPath(), userId, "DENY");
                            }
                        } else if ("user".equals(type)) {
                            APPMappingUtil.unSubscribeApp(registry, typeId, appId);
                            APPMappingUtil.showAppVisibilityToUser(artifact.getPath(), typeId, "DENY");
                        }
                    } catch (org.wso2.carbon.registry.api.RegistryException e) {
                        errorListItemDTO.setCode(String.valueOf(Response.Status.PRECONDITION_FAILED.getStatusCode()));
                        errorListItemDTO.setMessage("User have not Subscribed. " + parameterContext);
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        errorListItemDTO.setCode(String.valueOf(Response.Status.PRECONDITION_FAILED.getStatusCode()));
                        errorListItemDTO.setMessage("Error while updating visibility of App." + parameterContext);
                    }
                } else {
                    errorListItemDTO.setCode(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
                    errorListItemDTO.setMessage("App Not Found. " + parameterContext);
                }

                if (errorListItemDTO.getCode() == null) {
                    errorListItemDTO.setCode(String.valueOf(Response.Status.ACCEPTED.getStatusCode()));
                    errorListItemDTO.setMessage(parameterContext);
                }

            }
            return Response.ok().entity(errorDTO).build();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while initializing UserStore.");
            RestApiUtil.buildInternalServerErrorException();
        } catch (RegistryException e) {
            log.error("Error while initializing Registry.");
            RestApiUtil.buildInternalServerErrorException();
        }

        return null;
    }


}
