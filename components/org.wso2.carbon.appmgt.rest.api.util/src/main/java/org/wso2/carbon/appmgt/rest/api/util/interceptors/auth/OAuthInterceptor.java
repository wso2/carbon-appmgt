/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appmgt.rest.api.util.interceptors.auth;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.appmgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A CXF interceptor which secures resources using OAuth.
 */
public class OAuthInterceptor extends AbstractPhaseInterceptor {

    private static final Log log = LogFactory.getLog(OAuthInterceptor.class);

    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
    private static final String OAUTH_TOKEN_TYPE_NAME_PATTERN_STRING = "Bearer\\s";
    private static final Pattern OAUTH_TOKEN_TYPE_NAME_PATTERN = Pattern.compile(OAUTH_TOKEN_TYPE_NAME_PATTERN_STRING);
    private static final String SUPER_TENANT_SUFFIX = "@" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

    public OAuthInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }
    public void handleMessage(Message inMessage) {

        String accessToken = getAccessToken(inMessage);

        if(accessToken == null){
            ErrorDTO errorDetail = new ErrorDTO((long)401, "Access token header is not available.");
            sendErrorResponse(errorDetail, inMessage);
            return;
        }

        OAuth2TokenValidationResponseDTO validationResponse = validateToken(accessToken);

        if(!validationResponse.isValid()){
            ErrorDTO errorDetail = new ErrorDTO((long)401, validationResponse.getErrorMsg());
            sendErrorResponse(errorDetail, inMessage);
            return;
        }

        try {
            if(hasValidScopes(inMessage, validationResponse)){
                setUserDetails(validationResponse.getAuthorizedUser());
            }else{
                ErrorDTO errorDetail = new ErrorDTO((long)403, "Not authorized to access the API resource.");
                sendErrorResponse(errorDetail, inMessage);
                return;
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMessage = "Can't set user details after authentication.";
            log.error(errorMessage, e);
            ErrorDTO errorDetail = new ErrorDTO((long)500, errorMessage);
            sendErrorResponse(errorDetail, inMessage);
        }
    }

    private boolean hasValidScopes(Message message, OAuth2TokenValidationResponseDTO validationResponse) {

        String[] scopes = validationResponse.getScope();

        // Get the swagger definition.
        String scope = getResourceScope(message);

        if(scope == null){
            return true;
        }else{
            return ArrayUtils.contains(scopes, scope);
        }


    }

    private String getResourceScope(Message message) {

        String basePath = (String) message.get(Message.BASE_PATH);
        String verb = (String) message.get(Message.HTTP_REQUEST_METHOD);

        String apiName = null;

        String matchedURITemplate = getMatchedURITemplate(message);

        JSONObject apiDefinition = getAPIDefinition(basePath);

        JSONObject matchedResourceTemplate = (JSONObject) ((JSONObject) apiDefinition.get("paths")).get(matchedURITemplate);

        JSONObject matchedResource = (JSONObject) matchedResourceTemplate.get(verb.toLowerCase());

        return (String) matchedResource.get("x-scope");

    }

    private String getMatchedURITemplate(Message message) {

        Method resourceMethod = (Method) message.get("org.apache.cxf.resource.method");

        String classResourcePath = resourceMethod.getDeclaringClass().getAnnotation(Path.class).value();
        String methodResourcePath = resourceMethod.getAnnotation(Path.class).value();

        if("/".equals(classResourcePath)){
            return methodResourcePath;
        }else{
            return classResourcePath + methodResourcePath;
        }

    }

    private JSONObject getAPIDefinition(String basePath) {

        String apiDefinitionName = null;

        if(basePath.contains("api/appm/publisher")){
            apiDefinitionName = "publisher-api.json";
        }else if(basePath.contains("api/appm/store")){
            apiDefinitionName = "store-api.json";
        }else if(basePath.contains("api/appm/storeadmin")){
            apiDefinitionName = "storeadmin-api.json";
        }


        if(apiDefinitionName != null){
            try {

                String apiDefinition = IOUtils.toString(OAuthInterceptor.class.getResourceAsStream("/" + apiDefinitionName), "UTF-8");

                if(apiDefinition != null){
                    JSONParser parser = new JSONParser();
                    JSONObject parsedAPIDefinition = (JSONObject) parser.parse(apiDefinition);
                    return parsedAPIDefinition;
                }

            } catch (IOException e) {
                log.error(String.format("Can't read the API definition '%s'", apiDefinitionName));
            } catch (ParseException e) {
                log.error(String.format("Can't read the API definition '%s'", apiDefinitionName));
            }
        }


        return null;
    }

    private void setUserDetails(String username) throws org.wso2.carbon.user.api.UserStoreException {

        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            if (username.endsWith(SUPER_TENANT_SUFFIX)) {
                username = username.substring(0, username.length() - SUPER_TENANT_SUFFIX.length());
            }
        }

        int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        carbonContext.setTenantDomain(tenantDomain);
        carbonContext.setTenantId(tenantId);
        carbonContext.setUsername(username);

    }

    private OAuth2TokenValidationResponseDTO validateToken(String accessToken) {

        OAuth2TokenValidationService oAuth2TokenValidationService = new OAuth2TokenValidationService();
        OAuth2TokenValidationRequestDTO requestDTO = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO.OAuth2AccessToken token = requestDTO.new OAuth2AccessToken();

        token.setIdentifier(accessToken);
        token.setTokenType("bearer");
        requestDTO.setAccessToken(token);

        //TODO: If these values are not set, validation will fail giving an NPE. Need to see why that happens
        OAuth2TokenValidationRequestDTO.TokenValidationContextParam contextParam = requestDTO.new
                TokenValidationContextParam();
        contextParam.setKey("dummy");
        contextParam.setValue("dummy");

        OAuth2TokenValidationRequestDTO.TokenValidationContextParam[] contextParams =
                new OAuth2TokenValidationRequestDTO.TokenValidationContextParam[1];
        contextParams[0] = contextParam;
        requestDTO.setContext(contextParams);

        OAuth2ClientApplicationDTO clientApplicationDTO = oAuth2TokenValidationService.findOAuthConsumerIfTokenIsValid
                (requestDTO);
        OAuth2TokenValidationResponseDTO responseDTO = clientApplicationDTO.getAccessTokenValidationResponse();

        return responseDTO;
    }

    private String getAccessToken(Message message) {

        List<String> authHeaders = (List<String>) ((TreeMap) (message.get(Message.PROTOCOL_HEADERS))).get(HTTP_HEADER_AUTHORIZATION);

        if(authHeaders == null){
            return null;
        }

        // Get the first header.
        String authHeader= authHeaders.get(0);

        Matcher matcher = OAUTH_TOKEN_TYPE_NAME_PATTERN.matcher(authHeader);
        String accessToken = null;
        if (matcher.find()) {
            accessToken = authHeader.substring(matcher.end());
        }

        return accessToken;

    }

    private void sendErrorResponse(ErrorDTO errorDetail, Message inMessage) {

        Response response = Response
                .status(Response.Status.fromStatusCode(errorDetail.getCode().intValue()))
                .entity(errorDetail)
                .build();

        inMessage.getExchange().put(Response.class, response);
    }

}