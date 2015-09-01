/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.appmgt.usage.publisher.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationService;
import org.wso2.carbon.appmgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.appmgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.appmgt.impl.service.APIMGTSampleService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="api.mgt.usage.component" immediate="true"
 * @scr.reference name="user.realm.service"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="org.wso2.apimgt.impl.services"
 * interface="org.wso2.carbon.appmgt.impl.service.APIMGTSampleService" cardinality="1..1" policy="dynamic"
 * bind="setAPIMGTSampleService" unbind="unsetAPIMGTSampleService"
 */
public class UsageComponent {

    private static final Log log = LogFactory.getLog(UsageComponent.class);

    private static Map<String, List> responseTimeMap;

    private static RealmService realmService;

    private static APIMGTConfigReaderService apimgtConfigReaderService;

    private static AppManagerConfigurationService amConfigService;
    private static CarbonTomcatService carbonTomcatService;
    private static ConfigurationContextService configContextService;

    protected void activate(ComponentContext ctx) {
        try {
            DataPublisherUtil.setEnabledMetering(Boolean.parseBoolean(ServerConfiguration.getInstance().getFirstProperty("EnableMetering")));
            responseTimeMap = new ConcurrentHashMap<String, List>();
            log.debug("WebApp Management Usage Publisher bundle is activated ");
        } catch (Throwable e) {
            log.error("WebApp Management Usage Publisher bundle ", e);
        }
    }

	protected void setRealmService(RealmService realmService) {
		if (realmService != null && log.isDebugEnabled()) {
			log.debug("Realm service initialized");
		}
		UsageComponent.realmService = realmService;
	}

	protected void unsetRealmService(RealmService realmService) {
		UsageComponent.realmService = null;
	}
	
	public static RealmService getRealmService() {
		return realmService;
	}

	protected void deactivate(ComponentContext ctx) {

    }

    /**
     * Fetch the data publisher which has been registered under the tenant domain.
     * @param tenantDomain - The tenant domain under which the data publisher is registered
     * @return - Instance of the LoadBalancingDataPublisher which was registered. Null if not registered.
     */
    public static LoadBalancingDataPublisher getDataPublisher(String tenantDomain){
        if(APPManagerConfigurationServiceComponent.getDataPublisherMap().containsKey(tenantDomain)){
            return APPManagerConfigurationServiceComponent.getDataPublisherMap().get(tenantDomain);
        }
        return null;
    }

    /**
     * Adds a LoadBalancingDataPublisher to the data publisher map.
     * @param tenantDomain - The tenant domain under which the data publisher will be registered.
     * @param dataPublisher - Instance of the LoadBalancingDataPublisher
     * @throws DataPublisherAlreadyExistsException - If a data publisher has already been registered under the
     * tenant domain
     */
    public static void addDataPublisher(String tenantDomain, LoadBalancingDataPublisher dataPublisher)
            throws DataPublisherAlreadyExistsException {
        if(APPManagerConfigurationServiceComponent.getDataPublisherMap().containsKey(tenantDomain)){
            throw new DataPublisherAlreadyExistsException("A DataPublisher has already been created for the tenant " +
                    tenantDomain);
        }

        APPManagerConfigurationServiceComponent.getDataPublisherMap().put(tenantDomain, dataPublisher);
    }


    public static List<Long[]> getResponseTime(String key){
           if(responseTimeMap.containsKey(key)){
               return responseTimeMap.get(key);

           }
           return null;
    }

    public static void addResponseTime(String key,long time){
        List<Long[]> list = getResponseTime(key);
        Long[] serviceTimeArray = new Long[2];
        serviceTimeArray[0] = time;
        serviceTimeArray[1] =  System.currentTimeMillis();
        if(list != null){

            list.add(serviceTimeArray);
            responseTimeMap.put(key,list);
        }
        else{
             list = new ArrayList<Long[]>();
             list.add(serviceTimeArray);
            responseTimeMap.put(key,list);
        }

    }

    public static void deleteResponseTime(String key,long time){
        String context =  key.split("/")[0];
        for (Map.Entry<String, List> entry : responseTimeMap.entrySet()) {
            if (entry.getKey() == key) {
                List<Long[]> timeList = entry.getValue();
                for (int x = 0; x < timeList.size(); x++) {
                    Long [] timaArray =   timeList.get(x);

                    if(timaArray[1] < time) {
                        timeList.remove(x);
                    }
                }
            } else if(entry.getKey().contains(context)){
                   responseTimeMap.remove(entry.getKey());
            }
        }
    }

    protected void setAPIMGTSampleService(APIMGTSampleService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Application mgt service initialized.");
        }
    }

    protected void unsetAPIMGTSampleService(APIMGTSampleService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Application mgt service destroyed.");
        }
    }

    public static CarbonTomcatService getCarbonTomcatService() {
        return carbonTomcatService;
    }

    public static void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        UsageComponent.carbonTomcatService = carbonTomcatService;
    }
    public static void setConfigContextService(ConfigurationContextService configContext) {
        UsageComponent.configContextService = configContext;
    }

    public static void unsetConfigContextService(ConfigurationContextService configContext) {
        UsageComponent.carbonTomcatService = null;
    }

    protected void setAPIManagerConfigurationService(AppManagerConfigurationService service) {
        log.debug("WebApp manager configuration service bound to the WebApp usage handler");
        amConfigService = service;
    }

}
