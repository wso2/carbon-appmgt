/*
 *
 *   Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.appmgt.mdm.restconnector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.mdm.restconnector.beans.RemoteServer;
import org.wso2.carbon.appmgt.mdm.restconnector.utils.RestUtils;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationAction;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationDevice;
import org.wso2.carbon.appmgt.mobile.beans.DeviceIdentifier;
import org.wso2.carbon.appmgt.mobile.interfaces.ApplicationOperations;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.mdm.Property;
import org.wso2.carbon.appmgt.mobile.utils.MobileApplicationException;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ApplicationOperationsImpl implements ApplicationOperations {

	private static final Log log = LogFactory.getLog(ApplicationOperationsImpl.class);

	private static final RemoteServer remoteServer = new RemoteServer();

	/**
	 * @param applicationOperationAction holds the information needs to perform an action on mdm.
	 */
	@Override public String performAction(ApplicationOperationAction applicationOperationAction)
			throws MobileApplicationException {
		if (remoteServer.isEmpty()) {
			setRemoteServer(this.remoteServer);
		}

		HashMap<String, String> configProperties = applicationOperationAction.getConfigParams();

		JSONObject requestObj = new JSONObject();
		String type = applicationOperationAction.getType();
		String[] params = applicationOperationAction.getParams();
		int tenantId = applicationOperationAction.getTenantId();
		PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

		if (Constants.USER.equals(type)) {
			List<String> users = new ArrayList<>(Arrays.asList(params));
			JSONArray devicesOfUsers = getDevicesOfTypes(users, Constants.USERS);
			requestObj.put(Constants.DEVICE_IDENTIFIERS, getDeviceIdsFromDevices(devicesOfUsers));

		} else if (Constants.ROLE.equals(type)) {
			List<String> roles =  new ArrayList<>(Arrays.asList(params));
			JSONArray devicesOfRoles = getDevicesOfTypes(roles, Constants.ROLES);
			requestObj.put(Constants.DEVICE_IDENTIFIERS, getDeviceIdsFromDevices(devicesOfRoles));

		} else {
			JSONArray deviceIdentifiers = new JSONArray();
			for (String param : params) {
				if (isValidJSON(param)) {
					JSONParser parser = new JSONParser();
					try {
						JSONObject parsedObj = (JSONObject) parser.parse(param);
						deviceIdentifiers.add(parsedObj);
					} catch (ParseException e) {
						log.error("Device Identifier is not valid json object.", e);
						throw new MobileApplicationException(e);
					}

				}
			}
			requestObj.put(Constants.DEVICE_IDENTIFIERS, deviceIdentifiers);
		}

		JSONObject requestApp = new JSONObject();
		App app = applicationOperationAction.getApp();

		Method[] methods = app.getClass().getMethods();

		for (Method method : methods) {

			if (method.isAnnotationPresent(Property.class)) {
				try {
					Object value = method.invoke(app);
					if (value != null) {
						requestApp.put(method.getAnnotation(Property.class).name(), value);
					}
				} catch (IllegalAccessException e) {
					String errorMessage = "Illegal Action";
					log.error(errorMessage, e);
					throw new MobileApplicationException(e);
				} catch (InvocationTargetException e) {
					String errorMessage = "Target invocation failed";
					log.error(errorMessage, e);
					throw new MobileApplicationException(e);
				}
			}

		}

		if (Constants.IOSConstants.IOS.equals(requestApp.get(Constants.PLATFORM))) {

			JSONObject iosProperties = new JSONObject();
			if (Constants.IOSConstants.ENTERPRISE
					.equals(requestApp.get(Constants.IOSConstants.TYPE))) {
				iosProperties.put(Constants.IOSConstants.IS_REMOVE_APP, true);
				iosProperties.put(Constants.IOSConstants.IS_PREVENT_BACKUP, true);
			} else if (Constants.IOSConstants.PUBLIC
					.equals(requestApp.get(Constants.IOSConstants.TYPE))) {
				iosProperties.put(Constants.IOSConstants.I_TUNES_ID, Integer.parseInt(
						requestApp.get(Constants.IOSConstants.IDENTIFIER).toString()));
				iosProperties.put(Constants.IOSConstants.IS_REMOVE_APP, true);
				iosProperties.put(Constants.IOSConstants.IS_PREVENT_BACKUP, true);
			} else if (Constants.IOSConstants.WEBAPP
					.equals(requestApp.get(Constants.IOSConstants.TYPE))) {
				iosProperties.put(Constants.IOSConstants.LABEL,
				                  requestApp.get(Constants.IOSConstants.TYPE));
				iosProperties.put(Constants.IOSConstants.IS_REMOVE_APP, true);
			}
			requestApp.put(Constants.PROPERTIES, iosProperties);

		} else if (Constants.WebAppConstants.WEBAPP.equals(requestApp.get(Constants.PLATFORM))) {

			JSONObject webappProperties = new JSONObject();
			webappProperties.put(Constants.WebAppConstants.LABEL,
			                     requestApp.get(Constants.WebAppConstants.NAME));
			webappProperties.put(Constants.WebAppConstants.IS_REMOVE_APP, true);
			requestApp.put(Constants.PROPERTIES, webappProperties);
		}

		//make type to uppercase
		requestApp.put(Constants.TYPE, requestApp.get(Constants.TYPE).toString().toUpperCase());
		requestObj.put(Constants.APPLICATION, requestApp);

		StringEntity requestEntity = null;

		if (log.isDebugEnabled()) {
			log.debug("Request Payload for MDM: " + requestObj.toJSONString());
		}

		try {
			requestEntity = new StringEntity(requestObj.toJSONString(), "UTF-8");
			requestEntity.setContentType(Constants.RestConstants.APPLICATION_JSON);
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			throw new MobileApplicationException(e);
		}

		String requestURL = configProperties.get(Constants.PROPERTY_SERVER_URL);

		String actionURL;
		if (Constants.INSTALL.equals(applicationOperationAction.getAction())) {
			actionURL = String.format(Constants.API_INSTALL_APP);
		} else if (Constants.UPDATE.equals(applicationOperationAction.getAction())){
			actionURL = String.format(Constants.API_UPDATE_APP);
		} else {
			actionURL = String.format(Constants.API_UNINSTALL_APP);
		}

		HttpClient httpClient = AppManagerUtil.getHttpClient(requestURL + actionURL);
		HttpPost postMethod = new HttpPost(requestURL + actionURL);
		postMethod.setEntity(requestEntity);
		String action = applicationOperationAction.getAction();
		if (RestUtils.executeMethod(remoteServer, httpClient, postMethod) != null) {
			if (log.isDebugEnabled()) {
				log.debug(action + " operation performed successfully on " + type + " " +
				          params.toString());
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug(action + " operation unsuccessful.");
			}
		}

		return null;

	}

	/**
	 * @param applicationOperationDevice holds the information needs to retrieve device list.
	 * @return List of devices
	 */
	@Override public List<Device> getDevices(ApplicationOperationDevice applicationOperationDevice)
			throws MobileApplicationException {
		if (remoteServer.isEmpty()) {
			setRemoteServer(this.remoteServer);
		}
		int tenantId = applicationOperationDevice.getTenantId();
		PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
		String[] params = applicationOperationDevice.getParams();
		List<NameValuePair> nameValuePairs = new ArrayList<>();
		String platform = applicationOperationDevice.getPlatform();
		String platformVersion = applicationOperationDevice.getPlatformVersion();
		if(platform != null) {
			nameValuePairs.add(new BasicNameValuePair(Constants.PLATFORM, platform));
		}

		if(platformVersion != null) {
			nameValuePairs.add(new BasicNameValuePair(Constants.PLATFORM_VERSION, platform));
		}
		String queryString = URLEncodedUtils.format(nameValuePairs, "utf-8");

		return getDevicesOfUser(params[0], queryString);

	}

	private boolean isValidJSON(String json) {
		JSONParser parser = new JSONParser();
		try {
			parser.parse(json);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}

	private JSONArray getDeviceIdsFromDevices(JSONArray devices) {
		JSONArray deviceIdentifiers = new JSONArray();
		Iterator<JSONObject> iterator = devices.iterator();
		while (iterator.hasNext()) {
			JSONObject deviceObj = iterator.next();
			JSONObject obj = new JSONObject();
			obj.put(Constants.ID, deviceObj.get(Constants.DEVICE_IDENTIFIER).toString());
			obj.put(Constants.TYPE, deviceObj.get(Constants.TYPE).toString());
			deviceIdentifiers.add(obj);
		}
		return deviceIdentifiers;
	}

	private JSONArray getDevices(String requestURL, HttpGet getMethod)
			throws MobileApplicationException {

		JSONArray jsonArray = null;
		HttpClient httpClient =  AppManagerUtil.getHttpClient(requestURL);

		if (getMethod == null) {
			getMethod = new HttpGet(requestURL);
		}

		getMethod.addHeader(Constants.RestConstants.ACCEPT,
		                    Constants.RestConstants.APPLICATION_JSON);

		String responseString = RestUtils.executeMethod(remoteServer, httpClient, getMethod);
		if (responseString != null ) {
			jsonArray = (JSONArray) new JSONValue().parse(responseString);
			if (jsonArray != null) {
				if (log.isDebugEnabled()) {
					log.debug("Devices received from MDM: " + jsonArray.toJSONString());
				}
			}
		} else {
			log.error("Getting devices from MDM API failed");
		}

		if (jsonArray == null) {
			jsonArray = (JSONArray) new JSONValue().parse("[]");
		}

		return jsonArray;
	}

	private List<Device> convertJSONToDevices(JSONArray jsonArray) {
		List<Device> devices = new ArrayList<>();

		Iterator<JSONObject> iterator = jsonArray.iterator();
		while (iterator.hasNext()) {
			JSONObject deviceObj = iterator.next();

			Device device = new Device();
			DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
			deviceIdentifier.setId(deviceObj.get(Constants.DEVICE_IDENTIFIER).toString());
			deviceIdentifier.setType(deviceObj.get(Constants.TYPE).toString());
			device.setDeviceIdentifier(deviceIdentifier);
			device.setName(deviceObj.get(Constants.NAME).toString());
			device.setModel(deviceObj.get(Constants.NAME).toString());
			device.setType(Constants.MOBILE_DEVICE);
			String imgUrl;
			if (Constants.ANDROID.equalsIgnoreCase((deviceObj.get(Constants.TYPE).toString()))) {
				imgUrl = String.format(getActiveMDMProperties().get(Constants.IMAGE_URL),
				                       Constants.NEXUS);
			} else if (Constants.IOSConstants.IOS
					.equalsIgnoreCase((deviceObj.get(Constants.TYPE).toString()))) {
				imgUrl = String.format(getActiveMDMProperties().get(Constants.IMAGE_URL),
				                       Constants.IPHONE);
			} else {
				imgUrl = String.format(getActiveMDMProperties().get(Constants.IMAGE_URL),
				                       Constants.NONE);
			}
			device.setImage(imgUrl);
			device.setPlatform(deviceObj.get(Constants.TYPE).toString());
			devices.add(device);

		}

		return devices;
	}

	/**
	 * Will return device list for user list or role list
	 *
	 * @param types Type list which for devices to be retrieved. ex: user list or role list
	 * @param typeName Type name which for devices to be retrieved ex: user or role
	 * @return Device list which retrieved for user set or role set
	 * @throws MobileApplicationException
	 */
	private JSONArray getDevicesOfTypes(List<String> types, String typeName)
			throws MobileApplicationException {
		List<NameValuePair> nameValuePairs = new ArrayList<>();
		for (String type : types) {
			nameValuePairs.add(new BasicNameValuePair(Constants.TYPES, type));
		}
		String deviceListAPI = String.format(Constants.API_DEVICE_LIST_OF_TYPES, typeName);
		String requestURL =
				getActiveMDMProperties().get(Constants.PROPERTY_SERVER_URL) + deviceListAPI;

		HttpGet getMethod = new HttpGet(requestURL + "?" + URLEncodedUtils.format(nameValuePairs, "utf-8"));
		return this.getDevices(requestURL, getMethod);

	}

	private List<Device> getDevicesOfUser(String user, String queryString)
			throws MobileApplicationException {
		String deviceListAPI = String.format(Constants.API_DEVICE_LIST_OF_USER, user);
		String requestURL =
				getActiveMDMProperties().get(Constants.PROPERTY_SERVER_URL) + deviceListAPI;
		HttpGet getMethod = new HttpGet(requestURL + "?" + queryString);
		return convertJSONToDevices(this.getDevices(requestURL, getMethod));
	}

	private HashMap<String, String> getActiveMDMProperties() {
		MobileConfigurations configurations = MobileConfigurations.getInstance();
		return configurations.getActiveMDMProperties();
	}

	public void setRemoteServer(RemoteServer remoteServer) {
		HashMap<String, String> configProperties = getActiveMDMProperties();
		remoteServer.setTokenApiURL(configProperties.get(Constants.PROPERTY_TOKEN_API_URL));
		remoteServer.setClientKey(configProperties.get(Constants.PROPERTY_CLIENT_KEY));
		remoteServer.setClientSecret(configProperties.get(Constants.PROPERTY_CLIENT_SECRET));
		remoteServer.setAuthUser(configProperties.get(Constants.PROPERTY_AUTH_USER));
		remoteServer.setAuthPass(configProperties.get(Constants.PROPERTY_AUTH_PASS));
	}

	public RemoteServer getRemoteServer() {
		return remoteServer;
	}

}
