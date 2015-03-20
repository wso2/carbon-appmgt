package org.wso2.carbon.appmgt.services.v1.apps.mobile;

import org.wso2.carbon.appmgt.services.v1.apps.utils.HostResolver;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

public class MobileAppDataLoader {

    public static MobileApp load(MobileApp mobileApp, GenericArtifact artifact){


        try {
            mobileApp.setId(artifact.getId());
            mobileApp.setName(artifact.getAttribute("overview_name"));
            mobileApp.setPlatform(artifact.getAttribute("overview_platform"));
            mobileApp.setVersion(artifact.getAttribute("overview_version"));
            mobileApp.setType(artifact.getAttribute("overview_type"));
            mobileApp.setIconImage(HostResolver.getHostWithHTTP() + artifact.getAttribute("images_thumbnail"));

            if("Enterprise".equals(artifact.getAttribute("overview_type"))){
                mobileApp.setType("enterprise");
            }else if ("Market".equals(artifact.getAttribute("overview_type"))){
                mobileApp.setType("public");
            }else if ("Web App".equals(artifact.getAttribute("overview_type"))){
                mobileApp.setType("webapp");
                mobileApp.setIdentifier(artifact.getAttribute("overview_url"));
            }

            if("android".equals(artifact.getAttribute("overview_platform"))){
                mobileApp.setPackageName(artifact.getAttribute("overview_packagename"));
                mobileApp.setIdentifier(artifact.getAttribute("overview_packagename"));
            }else  if("ios".equals(artifact.getAttribute("overview_platform"))){
                mobileApp.setPackageName(artifact.getAttribute("overview_packagename"));
                mobileApp.setAppIdentifier(artifact.getAttribute("overview_appid"));
                mobileApp.setIdentifier(artifact.getAttribute("overview_appid"));
            }
        } catch (GovernanceException e) {
            e.printStackTrace();
        }finally {
            return mobileApp;
        }

    }
}
