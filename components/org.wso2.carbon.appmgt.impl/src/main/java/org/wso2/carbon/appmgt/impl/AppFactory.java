/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.impl;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.social.core.SocialActivityException;
import org.wso2.carbon.social.core.service.SocialActivityService;

/**
 * Parent class for the factories for app types.
 */
public abstract class AppFactory {

    private static final Log log = LogFactory.getLog(AppFactory.class);

    public App createApp(GenericArtifact artifact, Registry registry) throws AppManagementException{

        App app = doCreateApp(artifact, registry);
        setRating(app);

        return app;
    }

    /**
     *
     * Creates an App from the given registry artifact.
     *
     * @param artifact
     * @param registry
     * @return
     * @throws AppManagementException
     */
    protected abstract App doCreateApp(GenericArtifact artifact, Registry registry) throws AppManagementException;

    private void setRating(App app) throws AppManagementException {

        try {
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            SocialActivityService socialActivityService = (SocialActivityService) carbonContext.getOSGiService(org.wso2.carbon.social.core.service.SocialActivityService.class, null);
            JsonObject rating = socialActivityService.getRating(app.getType() + ":" + app.getUUID());

            if(rating != null && rating.get("rating") != null){
                app.setRating(rating.get("rating").getAsFloat());
            }

        } catch (SocialActivityException e) {
            String errorMessage = String.format("Can't get the rating for the app '%s:%s'", app.getType(), app.getUUID());
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        }

    }

}
