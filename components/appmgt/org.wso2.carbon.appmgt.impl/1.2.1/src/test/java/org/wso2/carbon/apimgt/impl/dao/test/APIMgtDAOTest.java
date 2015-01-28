/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.impl.dao.test;

import junit.framework.TestCase;
import org.wso2.carbon.appmgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationServiceImpl;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.appmgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.appmgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.appmgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.Date;
import java.util.Set;

public class APIMgtDAOTest extends TestCase {

	AppMDAO appMDAO;

	@Override
	protected void setUp() throws Exception {
		String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
		AppManagerConfiguration config = new AppManagerConfiguration();
		config.load(dbConfigPath);
		ServiceReferenceHolder.getInstance()
		                      .setAPIManagerConfigurationService(new AppManagerConfigurationServiceImpl(
		                                                                                                config));
		APIMgtDBUtil.initialize();
		appMDAO = new AppMDAO();
		IdentityTenantUtil.setRealmService(new TestRealmService());
		String identityConfigPath = System.getProperty("IdentityConfigurationPath");
		IdentityConfigParser.getInstance(identityConfigPath);
	}

	public void testGetSubscribersOfProvider() throws Exception {
		Set<Subscriber> subscribers = appMDAO.getSubscribersOfProvider("SUMEDHA");
		assertNotNull(subscribers);
		assertTrue(subscribers.size() > 0);
	}

	public void testGetSubscribedAPIsOfUser() throws Exception {
		APIInfoDTO[] apis = appMDAO.getSubscribedAPIsOfUser("SUMEDHA");
		assertNotNull(apis);
		assertTrue(apis.length > 1);
	}

	public void testGetSubscribedUsersForAPI() throws Exception {
		APIInfoDTO apiInfoDTO = new APIInfoDTO();
		apiInfoDTO.setApiName("API1");
		apiInfoDTO.setProviderId("SUMEDHA");
		apiInfoDTO.setVersion("V1.0.0");
		APIKeyInfoDTO[] apiKeyInfoDTO = appMDAO.getSubscribedUsersForAPI(apiInfoDTO);
		assertNotNull(apiKeyInfoDTO);
		assertTrue(apiKeyInfoDTO.length > 1);
	}

	public void testGetSubscriber() throws Exception {
		Subscriber subscriber = appMDAO.getSubscriber("SUMEDHA");
		assertNotNull(subscriber);
		assertNotNull(subscriber.getName());
		assertNotNull(subscriber.getId());
	}

	public void testIsSubscribed() throws Exception {
		APIIdentifier apiIdentifier = new APIIdentifier("SUMEDHA", "API1", "V1.0.0");
		boolean isSubscribed = appMDAO.isSubscribed(apiIdentifier, "SUMEDHA");
		assertTrue(isSubscribed);

		apiIdentifier = new APIIdentifier("P1", "API2", "V1.0.0");
		isSubscribed = appMDAO.isSubscribed(apiIdentifier, "UDAYANGA");
		assertFalse(isSubscribed);
	}

	public void testGetAllAPIUsageByProvider() throws Exception {
		UserApplicationAPIUsage[] userApplicationAPIUsages =
		                                                     appMDAO.getAllAPIUsageByProvider("SUMEDHA");
		assertNotNull(userApplicationAPIUsages);

	}

	public void testGetSubscribedAPIs() throws Exception {
		Subscriber subscriber = new Subscriber("SUMEDHA");
		subscriber.setDescription("Subscriber description");
		Set<SubscribedAPI> subscribedAPIs = appMDAO.getSubscribedAPIs(subscriber);
		assertNotNull(subscribedAPIs);
	}

	public void testAddApplication() throws Exception {
		Subscriber subscriber = new Subscriber("SUMEDHA");
		subscriber.setDescription("Subscriber description");

		Application application = new Application("APPLICATION999", subscriber);
		Application application1 = new Application("APPLICATION998", subscriber);

		appMDAO.addApplication(application, "SUMEDHA");
		appMDAO.addApplication(application1, "SUMEDHA");

		Application[] applications = appMDAO.getApplications(subscriber);
		assertNotNull(applications);
		assertTrue(applications.length > 0);
		for (int a = 0; a < applications.length; a++) {
			assertTrue(applications[a].getId() > 0);
			assertNotNull(applications[a].getName());
		}
	}

	public void testAddApplication2() throws Exception {
		Application application = new Application("APPLICATION1000", null);
		appMDAO.addApplication(application, "SUMEDHA");
		Application[] applications = appMDAO.getApplications(null);
		assertNull(applications);

		Subscriber subscriber = new Subscriber("NEWUSER");
		applications = appMDAO.getApplications(subscriber);
		assertNull(applications);

		subscriber = new Subscriber("SUMEDHA");
		applications = appMDAO.getApplications(subscriber);
		assertNotNull(applications);
	}

	public void checkSubscribersEqual(Subscriber lhs, Subscriber rhs) throws Exception {
		assertEquals(lhs.getId(), rhs.getId());
		assertEquals(lhs.getEmail(), rhs.getEmail());
		assertEquals(lhs.getName(), rhs.getName());
		assertEquals(lhs.getSubscribedDate().getTime(), rhs.getSubscribedDate().getTime());
		assertEquals(lhs.getTenantId(), rhs.getTenantId());
	}

	public void testAddGetSubscriber() throws Exception {
		Subscriber subscriber1 = new Subscriber("LA_F");
		subscriber1.setEmail("laf@wso2.com");
		subscriber1.setSubscribedDate(new Date());
		subscriber1.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
		appMDAO.addSubscriber(subscriber1);
		assertTrue(subscriber1.getId() > 0);
		Subscriber subscriber2 = appMDAO.getSubscriber(subscriber1.getId());
		this.checkSubscribersEqual(subscriber1, subscriber2);
	}

	public void testUpdateGetSubscriber() throws Exception {
		Subscriber subscriber1 = new Subscriber("LA_F2");
		subscriber1.setEmail("laf@wso2.com");
		subscriber1.setSubscribedDate(new Date());
		subscriber1.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
		appMDAO.addSubscriber(subscriber1);
		assertTrue(subscriber1.getId() > 0);
		subscriber1.setEmail("laf2@wso2.com");
		subscriber1.setSubscribedDate(new Date());
		subscriber1.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
		appMDAO.updateSubscriber(subscriber1);
		Subscriber subscriber2 = appMDAO.getSubscriber(subscriber1.getId());
		this.checkSubscribersEqual(subscriber1, subscriber2);
	}

//	public void testLifeCycleEvents() throws Exception {
//		APIIdentifier apiId = new APIIdentifier("hiranya", "WSO2Earth", "1.0.0");
//		WebApp api = new WebApp(apiId);
//		api.setContext("/wso2earth");
//		appMDAO.addWebApp(api);
//
//		List<LifeCycleEvent> events = appMDAO.getLifeCycleEvents(apiId);
//		assertEquals(1, events.size());
//		LifeCycleEvent event = events.get(0);
//		assertEquals(apiId, event.getApi());
//		assertNull(event.getOldStatus());
//		assertEquals(APIStatus.CREATED, event.getNewStatus());
//		assertEquals("hiranya", event.getUserId());
//
//		appMDAO.recordAPILifeCycleEvent(apiId, APIStatus.CREATED, APIStatus.PUBLISHED, "admin");
//		appMDAO.recordAPILifeCycleEvent(apiId, APIStatus.PUBLISHED, APIStatus.DEPRECATED, "admin");
//		events = appMDAO.getLifeCycleEvents(apiId);
//		assertEquals(3, events.size());
//	}

//	public void testKeyForwardCompatibility() throws Exception {
//		Set<APIIdentifier> apiSet = appMDAO.getAPIByConsumerKey("SSDCHEJJ-AWUIS-232");
//		assertEquals(1, apiSet.size());
//		for (APIIdentifier apiId : apiSet) {
//			assertEquals("SUMEDHA", apiId.getProviderName());
//			assertEquals("API1", apiId.getApiName());
//			assertEquals("V1.0.0", apiId.getVersion());
//		}
//
//		WebApp api = new WebApp(new APIIdentifier("SUMEDHA", "API1", "V2.0.0"));
//		api.setContext("/context1");
//		appMDAO.addWebApp(api);
//		appMDAO.makeKeysForwardCompatible("SUMEDHA", "API1", "V1.0.0", "V2.0.0", "/context1");
//		apiSet = appMDAO.getAPIByConsumerKey("SSDCHEJJ-AWUIS-232");
//		assertEquals(2, apiSet.size());
//		for (APIIdentifier apiId : apiSet) {
//			assertEquals("SUMEDHA", apiId.getProviderName());
//			assertEquals("API1", apiId.getApiName());
//			assertTrue("V1.0.0".equals(apiId.getVersion()) || "V2.0.0".equals(apiId.getVersion()));
//		}
//
//		apiSet = appMDAO.getAPIByConsumerKey("p1q2r3s4");
//		assertEquals(2, apiSet.size());
//		for (APIIdentifier apiId : apiSet) {
//			assertEquals("SUMEDHA", apiId.getProviderName());
//			assertEquals("API1", apiId.getApiName());
//			assertTrue("V1.0.0".equals(apiId.getVersion()) || "V2.0.0".equals(apiId.getVersion()));
//		}
//
//		apiSet = appMDAO.getAPIByConsumerKey("a1b2c3d4");
//		assertEquals(1, apiSet.size());
//		for (APIIdentifier apiId : apiSet) {
//			assertEquals("PRABATH", apiId.getProviderName());
//			assertEquals("API2", apiId.getApiName());
//			assertEquals("V1.0.0", apiId.getVersion());
//		}
//	}

//	public void testUnsubscribe() throws Exception {
//		Subscriber subscriber = new Subscriber("THILINA");
//		Set<SubscribedAPI> subscriptions = appMDAO.getSubscribedAPIs(subscriber);
//		assertEquals(1, subscriptions.size());
//		SubscribedAPI sub = subscriptions.toArray(new SubscribedAPI[subscriptions.size()])[0];
//		appMDAO.removeSubscription(sub.getApiId(), sub.getApplication().getId());
//
//		subscriptions = appMDAO.getSubscribedAPIs(subscriber);
//		assertTrue(subscriptions.isEmpty());
//	}

//	public void testIsAccessTokenExists() throws Exception {
//		boolean exist = appMDAO.isAccessTokenExists(testRegisterApplicationAccessToken()[0]);
//		assertEquals(true, exist);
//	}

//	public void testUpdateRefreshedApplicationAccessToken() throws Exception {
//		String newTok = UUID.randomUUID().toString();
//		long validityTime = 5000;
//
//		appMDAO.updateRefreshedApplicationAccessToken("PRODUCTION", newTok, validityTime);
//		String key1 = appMDAO.getAccessKeyForApplication("PRABATH", "APPLICATION3", "PRODUCTION");
//		assertNotNull(key1);
//
//		appMDAO.updateRefreshedApplicationAccessToken("PRODUCTION", newTok, validityTime);
//		String key2 = appMDAO.getAccessKeyForApplication("PRABATH", "APPLICATION4", "SANDBOX");
//		assertNotNull(key1);
//		assertTrue(!key1.equals(key2));
//	}

}
