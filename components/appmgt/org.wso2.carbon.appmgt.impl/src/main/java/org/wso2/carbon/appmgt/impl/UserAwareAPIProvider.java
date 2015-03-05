/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.impl;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicy;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;

import java.util.List;

/**
 * User aware APIProvider implementation which ensures that the invoking user has the
 * necessary privileges to execute the operations. Users can use this class as an
 * entry point to accessing the core WebApp provider functionality. In order to ensure
 * proper initialization and cleanup of these objects, the constructors of the class
 * has been hidden. Users should use the APIManagerFactory class to obtain an instance
 * of this class. This implementation also allows anonymous access to some of the
 * available operations. However if the user attempts to execute a privileged operation
 * when the object had been created in the anonymous mode, an exception will be thrown.
 */
public class UserAwareAPIProvider extends APIProviderImpl {

    private String username;

    UserAwareAPIProvider(String username) throws AppManagementException {
        super(username);
        this.username = username;
    }

    @Override
    public void addWebApp(WebApp api) throws AppManagementException {
//        checkCreatePermission();
        super.addWebApp(api);
    }

    @Override
    public void createNewAPIVersion(WebApp api, String newVersion) throws DuplicateAPIException,
                                                                          AppManagementException {
//        checkCreatePermission();
        super.createNewAPIVersion(api, newVersion);
    }

    @Override
    public void updateAPI(WebApp api) throws AppManagementException {
//        checkCreatePermission();
        super.updateAPI(api);
    }

    @Override
    public void changeAPIStatus(WebApp api, APIStatus status, String userId,
                                boolean updateGatewayConfig) throws AppManagementException {
//        if(userId.contains("@")){
//            checkPublishPermissionTenantMode(userId);
//        }else {
//        if(APIStatus.PUBLISHED.equals(status)) {
//            checkPublishPermission();
//        }
//        }
        super.changeAPIStatus(api, status, userId, updateGatewayConfig);
    }

    @Override
    public void addDocumentation(APIIdentifier apiId,
                                 Documentation documentation) throws AppManagementException {
//        checkCreatePermission();
        super.addDocumentation(apiId, documentation);
    }

    @Override
    public void removeDocumentation(APIIdentifier apiId, String docName,
                                    String docType) throws AppManagementException {
//        checkCreatePermission();
        super.removeDocumentation(apiId, docName, docType);
    }

    @Override
    public void updateDocumentation(APIIdentifier apiId,
                                    Documentation documentation) throws AppManagementException {
//        checkCreatePermission();
        super.updateDocumentation(apiId, documentation);
    }

    @Override
    public void addDocumentationContent(APIIdentifier identifier, String documentationName,
                                        String text) throws AppManagementException {
//        checkCreatePermission();
        super.addDocumentationContent(identifier, documentationName, text);
    }

    @Override
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion) throws
                                                                            AppManagementException {
//        checkCreatePermission();
        super.copyAllDocumentation(apiId, toVersion);
    }

//    public void checkCreatePermission() throws AppManagementException {
//        AppManagerUtil.checkPermission(username, AppMConstants.Permissions.API_CREATE);
//    }
    
    public void checkManageTiersPermission() throws AppManagementException {
        AppManagerUtil.checkPermission(username, AppMConstants.Permissions.MANAGE_TIERS);
    }

//    public void checkPublishPermission() throws AppManagementException {
//        AppManagerUtil.checkPermission(username, AppMConstants.Permissions.API_PUBLISH);
//    }

    @Override
    public void generateEntitlementPolicies(APIIdentifier apiIdentifier) throws
                                                                         AppManagementException {
//        checkCreatePermission();
        super.generateEntitlementPolicies(apiIdentifier);
    }

    @Override
    public void updateEntitlementPolicies(List<EntitlementPolicy> policies) throws
                                                                            AppManagementException {
//        checkCreatePermission();
        super.updateEntitlementPolicies(policies);
    }

    public void checkPublishPermissionTenantMode(String userId) throws AppManagementException {
        AppManagerUtil.checkPermission(userId, AppMConstants.Permissions.WEB_APP_PUBLISH);
    }

    @Override
    public boolean deleteEntitlementPolicyPartial(int policyPartialId, String author) throws
                                                                                      AppManagementException {
//        checkCreatePermission();
        return super.deleteEntitlementPolicyPartial(policyPartialId, author);
    }

    @Override
    public boolean updateEntitlementPolicyPartial(int policyPartialId, String policyPartial,
                                                  String author, boolean isShared, String policyPartialDesc)
            throws AppManagementException {
//        checkCreatePermission();
        return super.updateEntitlementPolicyPartial(policyPartialId, policyPartial, author, isShared,
                policyPartialDesc);

    }

    @Override
    public int saveEntitlementPolicyPartial(String policyPartialName, String policyPartial, boolean isSharedPartial,
                                            String policyAuthor, String policyPartialDesc)
            throws AppManagementException {
//        checkCreatePermission();
        return super.saveEntitlementPolicyPartial(policyPartialName, policyPartial, isSharedPartial, policyAuthor,
                policyPartialDesc);
    }
}
