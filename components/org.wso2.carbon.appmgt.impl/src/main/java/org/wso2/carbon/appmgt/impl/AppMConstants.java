/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl;

import javax.xml.namespace.QName;
import java.io.File;

/**
 * This class represents the constants that are used for APIManager implementation
 */
public final class AppMConstants {

    // Velocity logger name parameter in app-manager.xml
	public static final String VELOCITY_LOGGER = "VelocityLogger";

    //key value of the provider rxt
    public static final String PROVIDER_KEY = "provider";

    //key value of the APIImpl rxt
    public static final String API_KEY = "webapp";

    //governance registry appmgt root location
    public static final String APPMGT_REGISTRY_LOCATION = "/appmgt";

    //governance registry appmgt mobile apps root location
    public static final String APPMGT_MOBILE_REGISTRY_LOCATION = "/mobileapps";

    public static final String API_CONTEXT_ID = "api.context.id";
    //This is the resource name of API
    public static final String API_RESOURCE_NAME ="/webapp";

    //This is registry status property
    public static final String WEB_APP_LIFECYCLE_STATUS = "registry.lifecycle.WebAppLifeCycle.state";
    public static final String MOBILE_APP_LIFECYCLE_STATUS = "registry.lifecycle.MobileAppLifeCycle.state";
    public static final String MOBILE_LIFE_CYCLE = "MobileAppLifeCycle";
    public static final String WEBAPP_LIFE_CYCLE = "WebAppLifeCycle";
    public static final String REGISTRY_LC_NAME = "registry.LC.name";

    //Association between documentation and its content
    public static final String DOCUMENTATION_CONTENT_ASSOCIATION = "hasContent";

    public static final String DOCUMENTATION_KEY = "document";
    public static final String DOCUMENTATION_RESOURCE_MAP_DATA = "Data";
    public static final String DOCUMENTATION_RESOURCE_MAP_CONTENT_TYPE = "contentType";
    public static final String DOCUMENTATION_RESOURCE_MAP_NAME = "name";

    //association type between provider and APIImpl
    public static final String PROVIDER_ASSOCIATION = "provides";

    //association type between WebApp and Documentation
    public static final String DOCUMENTATION_ASSOCIATION = "document";

    //registry location of providers
    public static final String PROVIDERS_PATH = "/providers";

    //appm synapse configuration file resource location
    public static final String SYNAPSE_CONFIG_RESOURCES_PATH = "/repository/resources/appm-synapse-config/";

    public static final String APPMGT_APPLICATION_DATA_LOCATION = APPMGT_REGISTRY_LOCATION +"/applicationdata";

    public static final String OAUTH_SCOPE_ROLE_MAPPING_FILE = "oauth-scope-role-mapping.json";

    public static final String OAUTH_SCOPE_ROLE_MAPPING_PATH = APPMGT_APPLICATION_DATA_LOCATION + "/" +  OAUTH_SCOPE_ROLE_MAPPING_FILE;

    public static final String CUSTOM_PROPERTY_DEFINITIONS_PATH = "custom-property-definitions";

    public static final String TENANT_CONF_FILENAME = "appm-tenant-conf.xml";

    //registry location of WebApp
    public static final String API_LOCATION = APPMGT_APPLICATION_DATA_LOCATION + "/provider";

    public static final String API_TIER_LOCATION = APPMGT_APPLICATION_DATA_LOCATION + "/tiers.xml";

    public static final String API_IMAGE_LOCATION = APPMGT_APPLICATION_DATA_LOCATION + "/icons";

    //Workflow Config Location
    public static final String WORKFLOW_EXECUTOR_LOCATION = APPMGT_APPLICATION_DATA_LOCATION + "/workflow-extensions.xml";

    public static final String WORKFLOW_MEDIA_TYPE = "workflow-config";

    //Workflow Cachce implementation
    public static final String WORKFLOW_CACHE_NAME = "workflowCache";

    //registry location for consumer
    public static final String API_ROOT_LOCATION = APPMGT_APPLICATION_DATA_LOCATION + "/provider";

    //registry location for WebApp documentation
    public static final String API_DOC_LOCATION = APPMGT_APPLICATION_DATA_LOCATION + "/api-docs";

    //registry location for Custom sequences
    public static final String API_CUSTOM_SEQUENCE_LOCATION = APPMGT_REGISTRY_LOCATION +"/customsequences";

    public static final String API_CUSTOM_INSEQUENCE_LOCATION = API_CUSTOM_SEQUENCE_LOCATION +"/in/";

    public static final String API_CUSTOM_OUTSEQUENCE_LOCATION = API_CUSTOM_SEQUENCE_LOCATION +"/out/";

    //registry location for secure vault passwords
    public static final String API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION = "/repository/components/secure-vault";

   //registry location for wsdl files
    public static final String API_WSDL_RESOURCE_LOCATION = APPMGT_APPLICATION_DATA_LOCATION + "/wsdls/";

    public static final String API_DOC_RESOURCE_NAME = "api-doc.json";

    public static final String API_DEFINITION_DOC_NAME = "Swagger WebApp Definition";

    public static final String API_ICON_IMAGE = "icon";

    public static final String API_GLOBAL_VISIBILITY = "public";

    public static final String API_RESTRICTED_VISIBILITY = "restricted";

    public static final String API_CONTROLLED_VISIBILITY = "controlled";

    public static final String ACCESS_TOKEN_STORE_TABLE = "IDN_OAUTH2_ACCESS_TOKEN";

    public static final String SYNAPSE_NAMESPACE = "http://ws.apache.org/ns/synapse";

    public static final String WEBAPP_ASSET_TYPE = "webapp";

    public static final String SITE_ASSET_TYPE = "site";

    public static final String MOBILE_ASSET_TYPE = "mobileapp";

    public static final String ENABLED_TYPE_LIST = "EnabledAssetTypeList.";

    public static final String ENABLED_ASSET_TYPE = ENABLED_TYPE_LIST + "Type";

    public static final String RESOURCE_FOLDER_LOCATION = "repository"+ File.separator + "resources";

    public static final String APPLICATION_JSON_MEDIA_TYPE = "application/json";

    public static final int ASSET_CREATED_DATE_LENGTH = 20;

    // Those constance are used in WebApp artifact.
    public static final String API_OVERVIEW_NAME = "overview_name";
    public static final String API_OVERVIEW_DISPLAY_NAME = "overview_displayName";
    public static final String API_OVERVIEW_VERSION = "overview_version";
    public static final String API_OVERVIEW_CONTEXT = "overview_context";
    public static final String API_OVERVIEW_DESCRIPTION = "overview_description";
    public static final String API_OVERVIEW_ENDPOINT_URL = "overview_webAppUrl";//ES converts names to Camel case
    public static final String API_OVERVIEW_LOGOUT_URL = "overview_logoutUrl";
    public static final String API_OVERVIEW_SANDBOX_URL = "overview_sandboxUrl";
    public static final String API_OVERVIEW_WSDL = "overview_wsdl";
    public static final String API_OVERVIEW_WADL = "overview_wadl";
    public static final String API_OVERVIEW_PROVIDER = "overview_provider";
    public static final String API_OVERVIEW_THUMBNAIL_URL="overview_thumbnail";
    public static final String API_OVERVIEW_STATUS="overview_status";
    public static final String API_OVERVIEW_TIER="overview_tier";
    public static final String API_OVERVIEW_IS_LATEST ="overview_isLatest";
    public static final String API_URI_TEMPLATES ="uriTemplates_entry";
    public static final String API_OVERVIEW_TEC_OWNER ="overview_technicalOwner";
    public static final String API_OVERVIEW_TEC_OWNER_EMAIL ="overview_technicalOwnerEmail";
    public static final String API_OVERVIEW_BUSS_OWNER ="overview_businessOwner";
    public static final String API_OVERVIEW_BUSS_OWNER_EMAIL ="overview_businessOwnerEmail";
    public static final String API_OVERVIEW_VISIBILITY ="overview_visibility";
    public static final String API_OVERVIEW_VISIBLE_ROLES ="overview_visibleRoles";
    public static final String API_OVERVIEW_VISIBLE_TENANTS ="overview_visibleTenants";
    public static final String API_STATUS = "STATUS";
    public static final String API_URI_PATTERN ="URITemplate_urlPattern";
    public static final String API_URI_HTTP_METHOD ="URITemplate_httpVerb";
    public static final String API_URI_AUTH_TYPE ="URITemplate_authType";
    public static final String API_URI_ALLOW_ANONYMOUS ="URITemplate_allowAnonymous";
    public static final String API_OVERVIEW_ENDPOINT_SECURED = "overview_endpointSecured";
    public static final String API_OVERVIEW_ENDPOINT_USERNAME = "overview_endpointUsername";
    public static final String API_OVERVIEW_ENDPOINT_PASSWORD = "overview_endpointPpassword";
    public static final String API_OVERVIEW_TRANSPORTS = "overview_transports";
    public static final String API_OVERVIEW_INSEQUENCE = "overview_inSequence";
    public static final String API_OVERVIEW_OUTSEQUENCE = "overview_outSequence";
    public static final String API_OVERVIEW_ALLOW_ANONYMOUS = "overview_allowAnonymous";
    public static final String API_OVERVIEW_SKIP_GATEWAY ="overview_skipGateway";

    public static final String API_OVERVIEW_RESPONSE_CACHING = "overview_responseCaching";
    public static final String API_OVERVIEW_CACHE_TIMEOUT = "overview_cacheTimeout";

    public static final String API_OVERVIEW_REDIRECT_URL = "overview_redirectUrl";
    public static final String API_OVERVIEW_OWNER = "overview_appOwner";
    public static final String API_OVERVIEW_TENANT = "overview_appTenant";
    public static final String API_OVERVIEW_ADVERTISE_ONLY = "overview_advertiseOnly";
    public static final String API_OVERVIEW_ADVERTISED_APP_UUID = "overview_advertisedAppUuid";
    public static final String API_OVERVIEW_ENDPOINT_CONFIG = "overview_endpointConfig";

    public static final String API_OVERVIEW_SUBSCRIPTION_AVAILABILITY = "overview_subscriptionAvailability";
    public static final String API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS = "overview_tenants";

    public static final String API_OVERVIEW_CREATED_TIME= "overview_createdtime";

    //
    public static final String API_OVERVIEW_ENABLE_SSO = "overview_enableSso";
    public static final String API_OVERVIEW_IDP_PROVIDER_URL = "overview_idpProviderUrl";
    public static final String API_OVERVIEW_SAML2_SSO_ISSUER = "overview_saml2SsoIssuer";
    public static final String APP_OVERVIEW_TREAT_AS_A_SITE = "overview_treatAsASite";
    public static final String APP_OVERVIEW_MAKE_AS_DEFAULT_VERSION = "overview_makeAsDefaultVersion";
    public static final String APP_OVERVIEW_OLD_VERSION = "overview_oldVersion";

    public static final String APP_SSO_SSO_PROVIDER = "sso_ssoProvider";
    public static final String APP_SSO_SINGLE_SIGN_ON = "sso_singleSignOn";
    public static final String APP_SSO_SAML2_SSO_ISSUER = "sso_saml2SsoIssuer";

    public static final String APP_URITEMPLATE_POLICYGROUP_IDS = "uriTemplate_policyGroupIds";
    public static final String APP_URITEMPLATE_URLPATTERN = "uriTemplate_urlPattern";
    public static final String APP_URITEMPLATE_HTTPVERB = "uriTemplate_httpVerb";

    public static final String APP_IMAGES_THUMBNAIL = "images_thumbnail";
    public static final String APP_IMAGES_BANNER = "images_banner";
    public static final String APP_TRACKING_CODE = "overview_trackingCode";
    public static final String APP_OVERVIEW_ACS_URL = "overview_acsUrl";

    public static final String MOBILE_APP_OVERVIEW_NAME = "overview_name";
    public static final String MOBILE_APP_OVERVIEW_PROVIDER = "overview_provider";
    public static final String MOBILE_APP_OVERVIEW_VERSION = "overview_version";
    public static final String MOBILE_APP_OVERVIEW_URL = "overview_url";
    public static final String MOBILE_APP_OVERVIEW_PACKAGE_NAME = "overview_packagename";
    public static final String MOBILE_APP_OVERVIEW_BUNDLE_VERSION = "overview_bundleversion";
    public static final String MOBILE_APP_OVERVIEW_CATEGORY = "overview_category";
    public static final String MOBILE_APP_OVERVIEW_TYPE = "overview_type";
    public static final String MOBILE_APP_OVERVIEW_RECENT_CHANGES = "overview_recentchanges";
    public static final String MOBILE_APP_OVERVIEW_PLATFORM = "overview_platform";
    public static final String MOBILE_APP_OVERVIEW_APP_ID = "overview_appid";
    public static final String MOBILE_APP_IMAGES_SCREENSHOTS = "images_screenshots";
    public static final String MOBILE_APP_IMAGES_THUMBNAIL= "images_thumbnail";
    public static final String MOBILE_APP_TYPE_PUBLIC= "public";

    //Those constance are used in Provider artifact.
    public static final String PROVIDER_OVERVIEW_NAME= "overview_name";
    public static final String PROVIDER_OVERVIEW_EMAIL = "overview_email";
    public static final String PROVIDER_OVERVIEW_DESCRIPTION = "overview_description";

    //tables columns for APPMGR_WEBAPP_SSO_CONFIG
    public static final String SSO_CONFIG_FIELD_API_ID = "APP_ID";
    public static final String SSO_CONFIG_FIELD_IDP_PROVIDER_URL = "IDP_PROVIDER_URL";
    public static final String SSO_CONFIG_FIELD_SAML2_SSO_ISSUER = "SAML2_SSO_ISSUER";

    //database columns for Subscriber
    public static final String SUBSCRIBER_FIELD_SUBSCRIBER_ID = "SUBSCRIBER_ID";
    public static final String SUBSCRIBER_FIELD_USER_ID = "USER_ID";
    public static final String SUBSCRIBER_FIELD_TENANT_ID = "TENANT_ID";
    public static final String SUBSCRIBER_FIELD_EMAIL_ADDRESS = "EMAIL_ADDRESS";
    public static final String SUBSCRIBER_FIELD_DATE_SUBSCRIBED = "DATE_SUBSCRIBED";

    //tables columns for subscription
    public static final String SUBSCRIPTION_FIELD_SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    public static final String SUBSCRIPTION_FIELD_TIER_ID = "TIER_ID";
    public static final String SUBSCRIPTION_FIELD_APP_ID = "APP_ID";
    public static final String SUBSCRIPTION_FIELD_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String SUBSCRIPTION_FIELD_LAST_ACCESS = "LAST_ACCESSED";
    public static final String SUBSCRIPTION_FIELD_SUB_STATUS = "SUB_STATUS";
    public static final String SUBSCRIPTION_FIELD_TYPE = "SUBSCRIPTION_TYPE";
    public static final String SUBSCRIPTION_FIELD_TRUSTED_IDP = "TRUSTED_IDP";

    public static final String SUBSCRIPTION_KEY_TYPE = "KEY_TYPE";
    public static final String SUBSCRIPTION_USER_TYPE = "USER_TYPE";
    public static final String ACCESS_TOKEN_USER_TYPE_APPLICATION = "APPLICATION";
    public static final String USER_TYPE_END_USER = "END_USER";
    public static final String FIELD_API_NAME = "APP_NAME";
    public static final String FIELD_CONSUMER_KEY = "CONSUMER_KEY";
    public static final String FIELD_API_PUBLISHER = "APP_PROVIDER";

    //table columns for APM_APPLICATION
    public static final String APPLICATION_ID = "APPLICATION_ID";
    public static final String APPLICATION_NAME = "NAME";
    public static final String APPLICATION_SUBSCRIBER_ID = "SUBSCRIBER_ID";
    public static final String APPLICATION_TIER = "APPLICATION_TIER";

    //IDENTITY OAUTH2 table
    public static final String IDENTITY_OAUTH2_FIELD_TOKEN_STATE="TOKEN_STATE";
    public static final String IDENTITY_OAUTH2_FIELD_AUTHORIZED_USER = "AUTHZ_USER";
    public static final String IDENTITY_OAUTH2_FIELD_TIME_CREATED = "TIME_CREATED";
    public static final String IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD = "VALIDITY_PERIOD";

    //documentation rxt

    public static final String DOC_NAME= "overview_name";
    public static final String DOC_SUMMARY = "overview_summary";
    public static final String DOC_TYPE = "overview_type";
    public static final String DOC_DIR = "documentation";
    public static final String INLINE_DOCUMENT_CONTENT_DIR = "contents";
    public static final String NO_CONTENT_UPDATE = "no_content_update";
    public static final String DOCUMENT_FILE_DIR = "files";
    public static final String DOC_API_BASE_PATH="overview_apiBasePath";
    public static final String DOC_SOURCE_URL = "overview_sourceURL";
    public static final String DOC_FILE_PATH = "overview_filePath";
    public static final String DOC_SOURCE_TYPE = "overview_sourceType";
    public static final String DOC_OTHER_TYPE_NAME = "overview_otherTypeName";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String CREATED = "CREATED";
    public static final String DEPRECATED = "DEPRECATED";
    public static final String DOCUMENTATION_INLINE_CONTENT_TYPE = "text/plain";
    public static final String APP_LIFE_CYCLE="AppLifeCycle";
    public static final String APP_LC_PUBLISHED = "Published";

    // REST API constants
    public static final String REST_API_SCOPES_CONFIG = "RESTAPIScopes";
    public static final String APPM_RESTAPI = "RESTAPI.";
    public static final String APPM_RESTAPI_PUBLISHER_API_CONTEXT_PATH = APPM_RESTAPI + "PublisherAPIContextPath";
    public static final String APPM_RESTAPI_STORE_API_CONTEXT_PATH = APPM_RESTAPI + "StoreAPIContextPath";
    public static final String APPM_RESTAPI_WHITELISTED_URI = APPM_RESTAPI + "WhiteListedURIs.WhiteListedURI.";
    public static final String APPM_RESTAPI_WHITELISTED_URI_URI = APPM_RESTAPI_WHITELISTED_URI + "URI";
    public static final String APPM_RESTAPI_WHITELISTED_URI_HTTPMethods = APPM_RESTAPI_WHITELISTED_URI + "HTTPMethods";

    public static class TokenStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String BLOCKED = "BLOCKED";
        public static final String REVOKED = "REVOKED";
        public static final String INACTIVE = "INACTIVE";
    }
    public static class SubscriptionStatus {
        public static final String BLOCKED = "BLOCKED";
        public static final String PROD_ONLY_BLOCKED = "PROD_ONLY_BLOCKED";
        public static final String UNBLOCKED = "UNBLOCKED";
        public static final String ON_HOLD = "ON_HOLD";
        public static final String REJECTED = "REJECTED";
    }

    public static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    public static final int TOP_TATE_MARGIN = 4;

    public static final class Permissions {
        //Login permission
        public static final String LOGIN = "/permission/admin/login";

        //Web App Permissions
        public static final String WEB_APP_CREATE = "/permission/admin/manage/webapp/create";
        public static final String WEB_APP_PUBLISH = "/permission/admin/manage/webapp/publish";
        public static final String WEB_APP_UPDATE = "/permission/admin/manage/webapp/update";
        public static final String WEB_APP_SUBSCRIBE = "/permission/admin/manage/webapp/subscribe";
        public static final String WEB_APP_DELETE = "/permission/admin/manage/webapp/delete";

        //Mobile App Permissions
        public static final String MOBILE_APP_CREATE = "/permission/admin/manage/mobileapp/create";
        public static final String MOBILE_APP_PUBLISH = "/permission/admin/manage/mobileapp/publish";
        public static final String MOBILE_APP_UPDATE = "/permission/admin/manage/mobileapp/update";
        public static final String MOBILE_APP_INSTALL = "/permission/admin/manage/mobileapp/subscribe";
        public static final String MOBILE_APP_DELETE = "/permission/admin/manage/mobileapp/delete";

        //Document Permissions
        public static final String DOCUMENT_ADD = "/permission/admin/manage/document/add";
        public static final String DOCUMENT_EDIT = "/permission/admin/manage/document/edit";
        public static final String DOCUMENT_DELETE = "/permission/admin/manage/document/delete";

        //Admin Dash Board Permissions
        public static final String APP_WORKFLOWADMIN = "/permission/admin/manage/appm/workflowadmin";
        public static final String MANAGE_TIERS = "/permission/admin/manage/appm/manage_tiers";
        public static final String VIEW_STATS = "/permission/admin/manage/appm/view_stats";

        // Service provider management permissions
        public static final String IDENTITY_APPLICATION_MANAGEMENT = "/permission/admin/manage/identity/applicationmgt";
        
        public static final String XACML_POLICY_ADD = "/permission/admin/configure/entitlement/policy/manage/add";
        public static final String XACML_POLICY_DELETE = "/permission/admin/configure/entitlement/policy/manage/delete";
        public static final String XACML_POLICY_EDIT = "/permission/admin/configure/entitlement/policy/manage/edit";
        public static final String XACML_POLICY_ENABLE = "/permission/admin/configure/entitlement/policy/manage/enable";
        public static final String XACML_POLICY_PUBLISH = "/permission/admin/configure/entitlement/policy/publish";
        public static final String XACML_POLICY_VIEW = "/permission/admin/configure/entitlement/policy/view";

    }

    public static final String SEARCH_CONTENT_NAME = "name";
    public static final String SEARCH_CONTENT_TYPE = "type";

    public static final String API_GATEWAY = "APIGateway.";
    public static final String API_GATEWAY_SERVER_URL = "ServerURL";
    public static final String API_GATEWAY_USERNAME = "Username";
    public static final String API_GATEWAY_PASSWORD = "Password";
    public static final String API_GATEWAY_KEY_CACHE_ENABLED = API_GATEWAY + "EnableGatewayKeyCache";
    public static final String API_GATEWAY_ENDPOINT = "GatewayEndpoint";
    public static final String API_GATEWAY_CLIENT_DOMAIN_HEADER = API_GATEWAY + "ClientDomainHeader";
    public static final String API_GATEWAY_TYPE = "GatewayType";
    public static final String API_GATEWAY_TYPE_SYNAPSE = "Synapse";

    public static final String API_KEY_MANAGER = "APIKeyManager.";
    public static final String API_KEY_MANAGER_URL = API_KEY_MANAGER + "ServerURL";
    public static final String API_KEY_MANAGER_TOKEN_ENDPOINT_NAME = API_KEY_MANAGER + "TokenEndPointName";
    public static final String API_KEY_MANAGER_USERNAME = API_KEY_MANAGER + "Username";
    public static final String API_KEY_MANAGER_PASSWORD = API_KEY_MANAGER + "Password";
    public static final String API_KEY_MANAGER_APPLICATION_ACCESS_TOKEN_VALIDATION_PERIOD = API_KEY_MANAGER + "ApplicationTokenDefaultValidityPeriod";
    public static final String API_KEY_MANGER_THRIFT_CLIENT_PORT = API_KEY_MANAGER + "ThriftClientPort";
    public static final String API_KEY_MANGER_THRIFT_SERVER_PORT = API_KEY_MANAGER + "ThriftServerPort";
    public static final String API_KEY_MANGER_THRIFT_SERVER_HOST = API_KEY_MANAGER + "ThriftServerHost";
    public static final String API_KEY_MANGER_CONNECTION_TIMEOUT = API_KEY_MANAGER + "ThriftClientConnectionTimeOut";
    public static final String API_KEY_MANAGER_THRIFT_SERVER_HOST = API_KEY_MANAGER + "ThriftServerHost";
    public static final String API_KEY_VALIDATOR_CLIENT_TYPE = API_KEY_MANAGER + "KeyValidatorClientType";
    public static final String API_KEY_VALIDATOR_WS_CLIENT = "WSClient";
    public static final String API_KEY_MANAGER_ENABLE_THRIFT_SERVER = API_KEY_MANAGER + "EnableThriftServer";
    public static final String API_KEY_VALIDATOR_THRIFT_CLIENT = "ThriftClient";
    public static final String API_KEY_SECURITY_CONTEXT_TTL = API_KEY_MANAGER + "SecurityContextTTL";
    public static final String API_KEY_MANAGER_ENABLE_JWT_CACHE = API_KEY_MANAGER + "EnableJWTCache";
    public static final String API_KEY_MANAGER_ENABLE_VALIDATION_INFO_CACHE = API_KEY_MANAGER + "EnableKeyMgtValidationInfoCache";
    public static final String API_KEY_MANAGER_REMOVE_USERNAME_TO_JWT_FOR_APP_TOKEN = API_KEY_MANAGER + "RemoveUserNameToJWTForApplicationToken";
    public static final String API_KEY_MANAGER_ENABLE_ASSERTIONS = API_KEY_MANAGER + "EnableAssertions.";
    public static final String API_KEY_MANAGER_ENABLE_ASSERTIONS_USERNAME = API_KEY_MANAGER_ENABLE_ASSERTIONS + "UserName";
    public static final String API_KEY_MANAGER_ENABLE_ACCESS_TOKEN_PARTITIONING = API_KEY_MANAGER + "AccessTokenPartitioning." + "EnableAccessTokenPartitioning";
    public static final String API_KEY_MANAGER_ACCESS_TOKEN_PARTITIONING_DOMAINS = API_KEY_MANAGER + "AccessTokenPartitioning." + "AccessTokenPartitioningDomains";
    public static final String API_KEY_MANAGER_ENCRYPT_TOKENS = API_KEY_MANAGER + "EncryptPersistedTokens";

    public static final String API_STORE = "APIStore.";
    public static final String API_STORE_DISPLAY_ALL_APIS = API_STORE + "DisplayAllAPIs";
    public static final String API_STORE_DISPLAY_MULTIPLE_VERSIONS = API_STORE + "DisplayMultipleVersions";
	public static final String API_STORE_DISPLAY_COMMENTS = API_STORE + "DisplayComments";
	public static final String API_STORE_DISPLAY_RATINGS = API_STORE + "DisplayRatings";
    public static final String API_STORE_TAG_CACHE_DURATION = API_STORE + "TagCacheDuration";

    public static final String WSO2_APP_STORE_TYPE = "wso2";

    public static final String EXTERNAL_APP_STORES = "ExternalAPPStores";
    public static final String LOGIN_CONFIGS = "LoginConfig";
    //public static final String EXTERNAL_API_STORES_STORE_URL = EXTERNAL_APP_STORES + ".StoreURL";
    public static final String EXTERNAL_APP_STORE = "ExternalAPPStore";
    public static final String EXTERNAL_APP_STORE_ID = "id";
    public static final String EXTERNAL_APP_STORE_TYPE = "type";
    public static final String EXTERNAL_APP_STORE_DISPLAY_NAME = "DisplayName";
    public static final String EXTERNAL_APP_STORE_ENDPOINT = "Endpoint";
    public static final String EXTERNAL_APP_STORE_USERNAME = "Username";
    public static final String EXTERNAL_APP_STORE_PASSWORD ="Password";

    public static final String STORE_CONFIGURATION = "APPStoreConfiguration.";
    public static final String STORE_DISPLAY_MULTIPLE_VERSIONS = STORE_CONFIGURATION + "DisplayMultipleVersions";

    public static final String AUTH_MANAGER = "AuthManager.";
    public static final String AUTH_MANAGER_URL = AUTH_MANAGER + "ServerURL";
    public static final String AUTH_MANAGER_USERNAME = AUTH_MANAGER + "Username";
    public static final String AUTH_MANAGER_PASSWORD = AUTH_MANAGER + "Password";

    public static final String SELF_SIGN_UP = "SelfSignUp.";
    public static final String SELF_SIGN_UP_ENABLED = SELF_SIGN_UP + "Enabled";
    public static final String SELF_SIGN_UP_ROLE = SELF_SIGN_UP + "SubscriberRoleName";
    public static final String SELF_SIGN_UP_CREATE_ROLE = SELF_SIGN_UP + "CreateSubscriberRole";

    public static final String MOBILE_APPS_CONFIGURATION = "MobileAppsConfiguration.";
    public static final String BINARY_FILE_STORAGE =  "BinaryFileStorage.";
    public static final String BINARY_FILE_STORAGE_ABSOLUTE_LOCATION = BINARY_FILE_STORAGE + "AbsoluteLocation";


    public static final String STATUS_OBSERVERS = "StatusObservers.";
    public static final String OBSERVER = STATUS_OBSERVERS + "Observer";

    public static final String CORS_CONFIGURATION = "CORSConfiguration.";
    public static final String CORS_CONFIGURATION_ENABLED = CORS_CONFIGURATION + "Enabled";
    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN = CORS_CONFIGURATION + "Access-Control-Allow-Origin";
    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS = CORS_CONFIGURATION + "Access-Control-Allow-Headers";
    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS = CORS_CONFIGURATION + "Access-Control-Allow-Methods";

    public static final String SSO_CONFIGURATION = "SSOConfiguration.";
    public static final String SSO_CONFIGURATION_RESPONSE_SIGNING_KEY_ALIAS = SSO_CONFIGURATION + "ResponseSigningKeyAlias";
    public static final String SSO_CONFIGURATION_ACS_URL_POSTFIX = SSO_CONFIGURATION + "ACSURLPostfix";
    public static final String SSO_CONFIGURATION_RESPONSE_VALIDITY_TIME_STAMP_SKEW = SSO_CONFIGURATION + "SAMLResponseValidityTimeStampSkew";
    public static final String SSO_CONFIGURATION_ENABLE_RESPONSE_SIGNING = SSO_CONFIGURATION + "EnableResponseSigning";
    public static final String SSO_CONFIGURATION_ENABLE_ASSERTION_SIGNING = SSO_CONFIGURATION + "EnableAssertionSigning";
    public static final String SSO_CONFIGURATION_VALIDATE_ASSERTION_EXPIRY = SSO_CONFIGURATION + "ValidateAssertionExpiry";
    public static final String SSO_CONFIGURATORS = SSO_CONFIGURATION + "Configurators.";
    public static final String SSO_CONFIGURATION_CREATE_SP_FOR_SKIP_GATEWAY_APPS = SSO_CONFIGURATION + "CreateServiceProviderForSkipGatewayApps";
    public static final String SSO_CONFIGURATOR = SSO_CONFIGURATORS + "Configurator.";
    public static final String SSO_CONFIGURATOR_NAME = SSO_CONFIGURATOR + "name";
    public static final String SSO_CONFIGURATOR_VERSION = SSO_CONFIGURATOR + "version";
    public static final String SSO_CONFIGURATION_IDENTITY_PROVIDER_URL = SSO_CONFIGURATION + "IdentityProviderUrl";
    public static final String SSO_CONFIGURATION_ENABLE_SSO_CONFIGURATION = SSO_CONFIGURATION + "EnableSamlSSOConfig";
    public static final String SSO_CONFIGURATION_IDENTITY_PROVIDER_MANAGER = SSO_CONFIGURATION + "IdPManager";
    public static final String SSO_CONFIGURATION_ENABLE_IDP_MERGING = SSO_CONFIGURATION + "EnableIdPMerge";
    public static final String SSO_CONFIGURATION_IDP_SERVICE_URL = SSO_CONFIGURATION + "IdPServiceUrl";
    public static final String SSO_CONFIGURATION_IDP_SERVICE_USER_NAME = SSO_CONFIGURATION + "UserName";
    public static final String SSO_CONFIGURATION_IDP_SERVICE_PWD = SSO_CONFIGURATION + "Password";

    public static final String APP_CONSUMER_AUTH_CONFIG = "AppConsumerAuthConfiguration.";
    public static final String GATEWAY_SESSION_TIMEOUT = APP_CONSUMER_AUTH_CONFIG + "SessionTimeout";
    public static final String API_CONSUMER_AUTHENTICATION_ADD_SAML_RESPONSE_HEADER_TO_OUT_MSG = APP_CONSUMER_AUTH_CONFIG + "AddSAMLResponseHeaderToOutMessage";
    public static final String API_CONSUMER_AUTHENTICATION_ADD_CLAIMS_SELECTIVELY = APP_CONSUMER_AUTH_CONFIG + "AddClaimsSelectively";
    public static final String TOKEN_GENERATOR_IMPL = APP_CONSUMER_AUTH_CONFIG + "TokenGeneratorImpl";
    public static final String ENABLE_JWT_GENERATION = APP_CONSUMER_AUTH_CONFIG + "EnableTokenGeneration";
    public static final String SIGNATURE_ALGORITHM = APP_CONSUMER_AUTH_CONFIG + "SignatureAlgorithm";

    public static final String SUBSCRIPTION_CONFIG = "SubscriptionConfiguration.";
    public static final String ENABLE_SELF_SUBSCRIPTION = SUBSCRIPTION_CONFIG + "EnableSelfSubscription";
    public static final String ENABLE_ENTERPRISE_SUBSCRIPTION = SUBSCRIPTION_CONFIG + "EnableEnterpriseSubscription";

    public static final String API_KEY_TYPE = "AM_KEY_TYPE";
    public static final String API_KEY_TYPE_PRODUCTION = "PRODUCTION";
    public static final String API_KEY_TYPE_SANDBOX = "SANDBOX";

    public static final String BILLING_AND_USAGE_CONFIGURATION = "EnableBillingAndUsage";

    public static final String DEFAULT_APPLICATION_NAME = "DefaultApplication";

    public static final QName POLICY_ELEMENT = new QName("http://schemas.xmlsoap.org/ws/2004/09/policy",
                      "Policy");
    public static final QName ASSERTION_ELEMENT = new QName("http://www.wso2.org/products/wso2commons/throttle",
            "MediatorThrottleAssertion");
    public static final QName THROTTLE_ID_ELEMENT = new QName("http://www.wso2.org/products/wso2commons/throttle",
            "ID");
    public static final QName THROTTLE_ID_DISPLAY_NAME_ELEMENT = new QName("http://www.wso2.org/products/wso2commons/throttle",
            "displayName");
    public static final String TIER_DESCRIPTION_PREFIX = "tier.desc.";

    public static final String TIER_MANAGEMENT = "TierManagement.";
    public static final String ENABLE_UNLIMITED_TIER = TIER_MANAGEMENT + "EnableUnlimitedTier";

    public static final String UNLIMITED_TIER = "Unlimited";
    public static final int UNLIMITED_TIER_REQUEST_PER_MINUTE = 10000;
    public static final String UNLIMITED_TIER_DESC = "Allows unlimited requests";

    public static final String UNAUTHENTICATED_TIER = "Unauthenticated";

    public static final int AM_CREATOR_APIMGT_EXECUTION_ID = 200;
    public static final int AM_CREATOR_GOVERNANCE_EXECUTION_ID = 201;
    public static final int AM_PUBLISHER_APIMGT_EXECUTION_ID = 202;
    public static final int AM_MOBILE_CREATOR_APIMGT_EXECUTION_ID = 203;
    public static final QName THROTTLE_CONTROL_ELEMENT = new QName("http://www.wso2.org/products/wso2commons/throttle",
                        "Control");
    public static final QName THROTTLE_MAXIMUM_COUNT_ELEMENT = new QName("http://www.wso2"
            +".org/products/wso2commons/throttle", "MaximumCount");
    public static final QName THROTTLE_UNIT_TIME_ELEMENT = new QName("http://www.wso2"
                                                                     + ".org/products/wso2commons/throttle", "UnitTime");
    public static final QName THROTTLE_ATTRIBUTES_ELEMENT = new QName("http://www.wso2"
                                                                      + ".org/products/wso2commons/throttle", "Attributes");
    public static final String THROTTLE_ATTRIBUTE_DISPLAY_NAME= "displayName";

    public static final String TIER_DESC_NOT_AVAILABLE = "Tire Description is not available";
    public static final int TIER_MAX_COUNT = 0;

    public static final String AUTH_TYPE_DEFAULT = "DEFAULT";
    public static final String AUTH_TYPE_NONE = "NONE";
    public static final String AUTH_TYPE_USER = "USER";
    public static final String AUTH_TYPE_APP = "APP";

    public static final String REMOTE_ADDR = "REMOTE_ADDR";

    public static final String TIER_PERMISSION_ALLOW = "allow";

    public static final String SUBSCRIPTION_TO_CURRENT_TENANT = "current_tenant";
    public static final String SUBSCRIPTION_TO_ALL_TENANTS = "all_tenants";
    public static final String SUBSCRIPTION_TO_SPECIFIC_TENANTS = "specific_tenants";

    //registry resource containing the self signup user config
    public static final String SELF_SIGN_UP_CONFIG_LOCATION = APPMGT_APPLICATION_DATA_LOCATION + "/sign-up-config.xml";
    public static final String SELF_SIGN_UP_CONFIG_MEDIA_TYPE =  "signup-config";

    //elements in the configuration file in the registry related to self signup
    public static final String SELF_SIGN_UP_REG_ROOT = "SelfSignUp";
    public static final String SELF_SIGN_UP_REG_DOMAIN_ELEM = "SignUpDomain";
    public static final String SELF_SIGN_UP_REG_ROLES_ELEM = "SignUpRoles";
    public static final String SELF_SIGN_UP_REG_ROLE_ELEM = "SignUpRole";
    public static final String SELF_SIGN_UP_REG_USERNAME = "AdminUserName";
    public static final String SELF_SIGN_UP_REG_PASSWORD = "AdminPassword";
    public static final String SELF_SIGN_UP_REG_ENABLED = "EnableSignup";
    public static final String SELF_SIGN_UP_REG_ROLE_NAME_ELEMENT = "RoleName";
    public static final String SELF_SIGN_UP_REG_ROLE_IS_EXTERNAL = "IsExternalRole";
    public static final String SELF_SIGN_UP_REG_ROLE_PERMISSIONS = "Permissions";

    public static final String APPM_DATA_SOURCES = "DataSources.";
    public static final String APPM_DATA_SOURCES_STORAGE = APPM_DATA_SOURCES + "Storage";

    //TODO: move this to a common place (& Enum) to be accessible by all components
    public static class KeyValidationStatus {
        public static final int API_AUTH_GENERAL_ERROR       = 900900;
        public static final int API_AUTH_INVALID_CREDENTIALS = 900901;
        public static final int API_AUTH_MISSING_CREDENTIALS = 900902;
        public static final int API_AUTH_ACCESS_TOKEN_EXPIRED = 900903;
        public static final int API_AUTH_ACCESS_TOKEN_INACTIVE = 900904;
        public static final int API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE = 900905;
        public static final int API_AUTH_INCORRECT_API_RESOURCE = 900906;
        public static final int API_BLOCKED = 900907;
        public static final int SUBSCRIPTION_INACTIVE = 900909;
    }

    public static final String EMAIL_DOMAIN_SEPARATOR = "@";

    public static final String EMAIL_DOMAIN_SEPARATOR_REPLACEMENT = "-AT-";
    public static final String SECONDERY_USER_STORE_SEPERATOR = ":";
    public static final String SECONDERY_USER_STORE_DEFAULT_SEPERATOR = "/";

    //WebApp caching related constants
    public static final String API_MANAGER_CACHE_MANAGER = "API_MANAGER_CACHE";
    public static final String APP_MANAGER_CACHE_MANAGER = "APP_MANAGER_CACHE";
    public static final String API_CONTEXT_CACHE_MANAGER = "API_CONTEXT_CACHE_MANAGER";
    public static final String RESOURCE_CACHE_NAME = "resourceCache";
    public static final String KEY_CACHE_NAME = "keyCache";
    public static final String JWT_CACHE_NAME = "jwtCache";
    public static final String AUTHENTICATED_IDP_CACHE_MANAGER = "APPMGT.GATEWAY.AUTHENTICATED_IDP_CACHE_MANAGER";
    public static final String AUTHENTICATED_IDP_CACHE = "APPMGT.GATEWAY.authenticatedIDPCache";
    public static final String API_CONTEXT_CACHE = "apiContextCache";
    public static final int API_CONTEXT_CACHE_EXPIRY_TIME_IN_DAYS = 3650 ;
    public static final String SAML2_SESSION_INDEX_CACHE = "saml2SessionIndexCache";
    public static final String SAML2_SESSION_INDEX_CACHE_MANAGER = "SAML2_SESSION_INDEX_CACHE_MANAGER";
    public static final String SAML2_SESSION_INDEX = "SAML2_SESSION_INDEX";
    public static final String SAML2_CONFIG_CACHE = "saml2ConfigCache";
    public static final String SAML2_RELAY_STATE_CACHE = "saml2RelayStateCache";
    public static final String SAML2_CONFIG_CACHE_MANAGER = "SAML2_CONFIG_CACHE_MANAGER";
    public static final String SAML2_RELAY_STATE_CACHE_MANAGER = "SAML2_RELAY_STATE_CACHE_MANAGER";
    public static final String SAML2_RELAY_STATE_CACHE_KEY = "SAML2RelayStateCacheKey";
    public static final String USAGE_CONFIG_CACHE = "usageConfigCache";
    public static final String USAGE_CONFIG_CACHE_MANAGER = "USAGE_CONFIG_CACHE_MANAGER";
    public static final String USER_ROLES_CACHE_MANAGER = "USER_ROLES_CACHE_MANAGER";
    public static final String USER_ROLES_CONFIG_CACHE = "userRolesConfigCache";
    public static final String USER_ROLES_CACHE_KEY = "userRolesCacheKey";
    public static final String APP_CONTEXT_VERSION_CACHE_MANAGER = "APP_CONTEXT_VERSION_CACHE_MANAGER";
    public static final String APP_CONTEXT_VERSION_CONFIG_CACHE = "APP_CONTEXT_VERSION_CONFIG_CACHE";
    public static final String APP_CONTEXT_VERSION_CACHE_KEY = "APP_CONTEXT_VERSION_CACHE_KEY";

    // Gateway caching constants
    public static final String GATEWAY_CACHE_MANAGER = "APPMGT.GATEWAY";
    public static final String GATEWAY_SESSION_CACHE = "appm.sessionCache";
    public static final String GATEWAY_SESSION_INDEX_MAPPING_CACHE = "appm.sessionIndexMappingCache";
    public static final String USAGE_CACHE_MANAGER = "APPMGT.USAGE";
    public static final String USAGE_CACHE = "appm.usageCache";


    public static final String GATEWAY_DEFAULT_VERSION_INDICATION_HEADER_NAME = "WSO2_APPM_INVOKED_WITHOUT_VERSION";


    public static final String SAML2_COOKIE = "appmanager_sso";
    public static final String SAML_SSO_TOKENID = "samlssoTokenId";
    public static final String APPM_SAML2_COOKIE = "APPMSESSIONID";
    public static final String APPM_SAML2_RESPONSE = "AppMgtSAML2Response";

    public static final String APPM_SAML_REQUEST = "SAMLRequest";
    public static final String APPM_IDP_URL = "IdpUrl";
    public static final String APPM_SAML_SEQUENCE = "saml2_sequence";

    //URI Authentication Schemes
    public static final String AUTH_NO_AUTHENTICATION = "None";
    public static final String AUTH_APPLICATION_LEVEL_TOKEN = "Application";
    public static final String AUTH_APPLICATION_USER_LEVEL_TOKEN = "Application_User";
    public static final String AUTH_APPLICATION_OR_USER_LEVEL_TOKEN = "Any";
    public static final String NO_MATCHING_AUTH_SCHEME = "noMatchedAuthScheme";

    public static final String EVERYONE_ROLE = "internal/everyone";
    public static final String CREATOR_ROLE = "internal/creator";
    public static final String PUBLISHER_ROLE = "internal/publisher";
    public static final String STORE_ADMIN_ROLE = "internal/store-admin";
    public static final String ANONYMOUS_ROLE = "system/wso2.anonymous.role";

    public static final String READ_ACTION = "2";
    public static final String WRITE_ACTION = "3";
    public static final String DELETE_ACTION = "4";
    public static final String PERMISSION_ENABLED = "1";
    public static final String PERMISSION_DISABLED = "0";

    public static final String API_ACTION="action";
    public static final String API_ADD_ACTION="addAPI";
    public static final String API_UPDATE_ACTION="updateAPI";
    public static final String API_CHANGE_STATUS_ACTION="updateStatus";
    public static final String API_REMOVE_ACTION="removeAPI";
    public static final String API_LOGIN_ACTION="login";
    public static final String API_LOGOUT_ACTION="logout";
    public static final String APISTORE_LOGIN_USERNAME="username";
    public static final String APISTORE_LOGIN_PASSWORD="password";
    public static final String APISTORE_LOGIN_URL="/site/blocks/user/login/ajax/login.jag";
    public static final String APISTORE_PUBLISH_URL="/site/blocks/life-cycles/ajax/life-cycles.jag";
    public static final String APISTORE_ADD_URL="/site/blocks/item-add/ajax/add.jag";
    public static final String APISTORE_DELETE_URL="/site/blocks/item-add/ajax/remove.jag";

    public static final String SWAGGER_VERSION = "1.1";

    public static class OperationParameter {
    	public static final String AUTH_PARAM_NAME = "Authorization";
    	public static final String AUTH_PARAM_DESCRIPTION = "Access Token";
    	public static final String AUTH_PARAM_TYPE = "header";
    	public static final String PAYLOAD_PARAM_NAME = "Payload";
    	public static final String PAYLOAD_PARAM_DESCRIPTION = "Request Payload";
    	public static final String QUERY_PARAM_NAME = "Query Parameters";
    	public static final String QUERY_PARAM_DESCRIPTION = "Request Query Parameters";
    	public static final String PAYLOAD_PARAM_TYPE = "body";
    }

    public static class CORSHeaders {
    	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

    	public static final String ACCESS_CONTROL_ALLOW_HEADERS_VALUE = "authorization,Access-Control-Allow-Origin,Content-Type";
    	public static final String ACCESS_CONTROL_ALLOW_METHODS_VALUE = "GET,POST,PUT,DELETE,OPTIONS";
    }

    public static final String EXTENSION_HANDLER_POSITION = "ExtensionHandlerPosition";

    public static final String GATEWAY_ENV_TYPE_HYBRID = "hybrid";
    public static final String GATEWAY_ENV_TYPE_PRODUCTION = "production";
    public static final String GATEWAY_ENV_TYPE_SANDBOX = "sandbox";
    public static final String GATEWAY_ACS_RELATIVE_URL = "acs";
    public static final String MESSAGE_CONTEXT_PROPERTY_GATEWAY_SKIP_SECURITY = "appm.gateway.skipSecurity";
    public static final String MESSAGE_CONTEXT_PROPERTY_MATCHED_URI_TEMPLATE = "appm.gateway.matchedURITemplate";
    public static final String MESSAGE_CONTEXT_PROPERTY_REDIRECTION_FRIENDLY_FULL_REQUEST_PATH = "appm.gateway.redirectionFriendlyFullRequestPath";
    public static final String MESSAGE_CONTEXT_PROPERTY_APP_ID = "appm.gateway.appID";

    public static final String API_RESPONSE_CACHE_ENABLED = "Enabled";
    public static final String API_RESPONSE_CACHE_DISABLED = "Disabled";
    public static final int API_RESPONSE_CACHE_TIMEOUT = 300;


    public static class ApplicationStatus {
       public static final String APPLICATION_CREATED = "CREATED";
       public static final String APPLICATION_APPROVED = "APPROVED";
       public static final String APPLICATION_REJECTED = "REJECTED";
       public static final String APPLICATION_ONHOLD = "ON_HOLD";
        public static final String APPLICATION_RETIRED = "RETIRED";

    }

    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";
    public static final String HOST = "Host";
    public static final String REFERER = "Referer";
    public static final String HTTP = "http://";
    public static final String URL_DELIMITER = "://";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTP_PROTOCOL = "http";

    // constants related to Statistics
    public static class Statistics {
        public static final int SUBSCRIPTION_LIMIT = 10;
        public static final String OTHER_APP = "[Other]";
    }
    //key  of the endpoint securevault
    public static final String API_SECUREVAULT_ENABLE = "EnableSecureVault";

    //Directory path and name of the mobile app resources
    public static final String MOBILE_APPS_DIRECTORY_PATH = "repository/resources";
    public static final String MOBILE_APPS_DIRECTORY_NAME = "mobileapps";

    //Mobile application binary file extensions
    public static final String MOBILE_APPS_ANDROID_EXT = "apk";
    public static final String MOBILE_APPS_IOS_EXT = ".ipa";

    //Mobile application platform types
    public static final String MOBILE_APPS_PLATFORM_ANDROID = "android";
    public static final String MOBILE_APPS_PLATFORM_IOS = "ios";
    public static final String MOBILE_APPS_PLATFORM_WEBAPP = "webapp";

    public static final String MOBILE_ONE_TIME_DOWNLOAD_API_PATH = "/apps/mobile/binaries/one-time";
    public static final String MOBILE_PLIST_API_PATH = "/apps/mobile/plist";


    public static final String APPM_SAML2_CACHE_HIT = "appmSamlCacheHit";
    public static final String CACHE_STAT_PUBLISHED = "cacheStatPublished";

    // These two synapse properties are used to in the entitlement handler.
    public static final String MATCHED_URL_PATTERN_PROERTY_NAME = "appm.matchedUrlPattern";
    public static final String MATCHED_APP_ID_PROERTY_NAME = "appm.matchedAppId";

    public static final String PAGE_LOAD_EVENT = "page-load";

    public static final int API_AUTH_FORBIDDEN = 900908;

    public static final String ANALYTICS = "Analytics.";
    public static final String APP_USAGE_DAS_UI_ACTIVITY_ENABLED = ANALYTICS
            + "UIActivityDASPublishEnabled";
    public static final String DATA_SOURCE_NAME = ANALYTICS + "DataSourceName";
    public static final String APP_STATISTIC_CLIENT_PROVIDER = ANALYTICS + "StatisticClientProvider";

    public static final String EXTERNAL_APP_STORES_LOCATION = APPMGT_APPLICATION_DATA_LOCATION
            + "/external-app-stores.xml";

    public static final String EXTERNAL_APP_STORES_STORE_URL = "StoreURL";
    public static final String EXTERNAL_APP_STORE_CLASS_NAME = "className";

    public static final String APP_ACTION ="action";
    public static final String APP_LOGIN_ACTION ="login";
    public static final String APP_LOGOUT_ACTION ="logout";
    public static final String APP_STORE_LOGIN_USERNAME ="username";
    public static final String APP_STORE_LOGIN_PASSWORD ="password";
    public static final String APP_STORE_AUTHENTICATE_URL ="/api/authenticate";
    public static final String APP_STORE_ADD_URL ="/api/asset/webapp";
    public static final String APP_STORE_ADD_TAGS_URL ="/api/tag/webapp/";
    public static final String APP_STORE_DELETE_URL ="/api/asset/webapp/";

    public static final String APP_STORE_STATE_CHANGE ="/api/lifecycle/";
    public static final String APP_TYPE ="webapp";
    public static final String STATE_SUMIT_FOR_REVIEW ="Submit%20for%20Review";
    public static final String STATE_APPROVE ="Approve";
    public static final String STATE_PUBLISH ="Publish";
    public static final String APP_STORE_GET_UUID_URL ="/api/asset/get/uuid/webapp/";

    public static final String IDP_AUTHENTICATED_COOKIE = "idp-authenticated-cookie";

    public  static class MediaType {
        public static final String WEB_APP = "application/vnd.wso2-webapp+xml";
        public static final String MOBILE_APP = "application/vnd.wso2-mobileapp+xml";
    }

    public static class LifecycleActions{
        public static final String SUBMIT_FOR_REVIEW = "Submit for Review";
        public static final String PUBLISH = "Publish";
        public static final String RE_PUBLISH = "Re-Publish";
        public static final String CREATE = "Create";
    }

    public static class MobileAppTypes{
        public static final String ENTERPRISE = "enterprise";
        public static final String PUBLIC = "public";
    }

    public static class claims {
        public static final String CLAIM_ROLES = "http://wso2.org/claims/role";
    }
}
