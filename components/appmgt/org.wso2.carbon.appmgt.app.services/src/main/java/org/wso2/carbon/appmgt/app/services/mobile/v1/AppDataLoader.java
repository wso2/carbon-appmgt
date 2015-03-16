package org.wso2.carbon.appmgt.app.services.mobile.v1;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

public class AppDataLoader {

    public static App load(App app, GenericArtifact artifact){


        try {
            app.setId(artifact.getId());
            app.setName(artifact.getAttribute("overview_name"));
            app.setPlatform(artifact.getAttribute("overview_platform"));
            app.setVersion(artifact.getAttribute("overview_version"));
            app.setType(artifact.getAttribute("overview_type"));
            app.setIconImage(artifact.getAttribute("overview_thumbnail"));

            if("Enterprise".equals(artifact.getAttribute("overview_type"))){
                app.setType("enterprise");
            }else if ("Market".equals(artifact.getAttribute("overview_type"))){
                app.setType("public");
            }else if ("Web App".equals(artifact.getAttribute("overview_type"))){
                app.setType("webapp");
                app.setIdentifier(artifact.getAttribute("overview_url"));
            }

            if("android".equals(artifact.getAttribute("overview_platform"))){
                app.setPackageName(artifact.getAttribute("overview_packagename"));
                app.setIdentifier(artifact.getAttribute("overview_packagename"));
            }else  if("ios".equals(artifact.getAttribute("overview_platform"))){
                app.setPackageName(artifact.getAttribute("overview_packagename"));
                app.setAppIdentifier(artifact.getAttribute("overview_appid"));
                app.setIdentifier(artifact.getAttribute("overview_appid"));
            }
        } catch (GovernanceException e) {
            e.printStackTrace();
        }finally {
            return app;
        }

    }
}
