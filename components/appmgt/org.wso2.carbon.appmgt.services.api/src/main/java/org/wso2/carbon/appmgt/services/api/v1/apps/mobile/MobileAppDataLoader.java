package org.wso2.carbon.appmgt.services.api.v1.apps.mobile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import java.io.File;

public class MobileAppDataLoader {

    private static final Log log = LogFactory.getLog(MobileAppDataLoader.class);

    public static MobileApp load(MobileApp mobileApp, GenericArtifact artifact, int tenantId, boolean showLocationInfo){


        try {
            mobileApp.setId(artifact.getId());
            mobileApp.setName(artifact.getAttribute("overview_name"));
            mobileApp.setPlatform(artifact.getAttribute("overview_platform"));
            mobileApp.setVersion(artifact.getAttribute("overview_version"));
            mobileApp.setType(artifact.getAttribute("overview_type"));
            mobileApp.setIconImage(HostResolver.getHostWithHTTP() + artifact.getAttribute("images_thumbnail"));

            if("Enterprise".equals(artifact.getAttribute("overview_type"))){
                mobileApp.setType("enterprise");
                if(showLocationInfo){
                    if("android".equals(artifact.getAttribute("overview_platform"))){
                        mobileApp.setLocation(HostResolver.getHost(MobileConfigurations.getInstance().getMDMConfigs()
                                .get(MobileConfigurations.APP_DOWNLOAD_URL_HOST)) + artifact.getAttribute("overview_url"));
                    }else  if("ios".equals(artifact.getAttribute("overview_platform"))){
                        String fileName = new File(artifact.getAttribute("overview_url")).getName();
                        mobileApp.setLocation(HostResolver.getHost(MobileConfigurations.getInstance().getMDMConfigs()
                                .get(MobileConfigurations.APP_DOWNLOAD_URL_HOST)) + "/" + MobileConfigurations.getInstance().getInstance()
                                .getMDMConfigs().get(MobileConfigurations.IOS_PLIST_PATH) + "/" + tenantId + "/"  + fileName);
                    }
                }
            }else if ("public".equals(artifact.getAttribute("overview_type"))){
                mobileApp.setType("public");
            }else if ("webapp".equals(artifact.getAttribute("overview_type"))){
                mobileApp.setType("webapp");
                mobileApp.setIdentifier(artifact.getAttribute("overview_url"));
                if(showLocationInfo){
                    mobileApp.setLocation(artifact.getAttribute("overview_url"));
                }
            }



            if("android".equals(artifact.getAttribute("overview_platform"))){
                mobileApp.setPackageName(artifact.getAttribute("overview_packagename"));
                mobileApp.setIdentifier(artifact.getAttribute("overview_packagename"));
            }else  if("ios".equals(artifact.getAttribute("overview_platform"))){
                mobileApp.setPackageName(artifact.getAttribute("overview_packagename"));
                mobileApp.setAppIdentifier(artifact.getAttribute("overview_appid"));
                mobileApp.setIdentifier(artifact.getAttribute("overview_packagename"));
                mobileApp.setBundleVersion(artifact.getAttribute("overview_bundleversion"));
            }
        } catch (GovernanceException e) {
            String errorMessage = "GovernanceException occurred";
            if(log.isDebugEnabled()){
                log.error(errorMessage, e);
            }else{
                log.error(errorMessage);
            }
        }finally {
            return mobileApp;
        }

    }
}
