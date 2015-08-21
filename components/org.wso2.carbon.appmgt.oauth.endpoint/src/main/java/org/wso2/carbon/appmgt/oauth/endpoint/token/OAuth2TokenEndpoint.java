package org.wso2.carbon.appmgt.oauth.endpoint.token;


import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.as.response.OAuthASResponse.OAuthTokenResponseBuilder;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.gateway.dto.Token;
import org.wso2.carbon.appmgt.oauth.endpoint.OAuthRequestWrapper;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth2.model.CarbonOAuthTokenRequest;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.OAuthClientException;
import org.wso2.carbon.appmgt.oauth.endpoint.util.EndpointUtil;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/token")
public class OAuth2TokenEndpoint {

    private static Log log = LogFactory.getLog(OAuth2TokenEndpoint.class);

    @POST
    @Path("/")
    @Consumes("application/x-www-form-urlencoded")
    public Response issueAccessToken(@Context HttpServletRequest request,
            MultivaluedMap<String, String> paramMap) throws  OAuthSystemException
    {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                    .getThreadLocalCarbonContext();
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            HttpServletRequestWrapper httpRequest = new OAuthRequestWrapper(request, paramMap);

            // extract the basic auth credentials if present in the request and use for
            // authentication.
            if (request.getHeader(OAuthConstants.HTTP_REQ_HEADER_AUTHZ) != null) {

                try {
                    String[] clientCredentials = EndpointUtil
                            .extractCredentialsFromAuthzHeader(request.getHeader(OAuthConstants.HTTP_REQ_HEADER_AUTHZ));

                    // The client MUST NOT use more than one authentication method in each request
                    if (paramMap.containsKey(OAuth.OAUTH_CLIENT_ID)
                            && paramMap.containsKey(OAuth.OAUTH_CLIENT_SECRET)) {
                        return handleBasicAuthFailure();
                    }

                    // add the credentials available in Authorization header to the parameter map
                    paramMap.add(OAuth.OAUTH_CLIENT_ID, clientCredentials[0]);
                    paramMap.add(OAuth.OAUTH_CLIENT_SECRET, clientCredentials[1]);

                } catch (OAuthClientException e) {
                    // malformed credential string is considered as an auth failure.
                    return handleBasicAuthFailure();
                }
            }

            try {
                //Allow only one grant_type parameter in request
                if (paramMap.get("grant_type").get(0).equals("SAML2")) {
                    paramMap.remove("grant_type");
                    paramMap.add("grant_type", "urn:ietf:params:oauth:grant-type:saml2-bearer");
                    paramMap.add("assertion", "dummy");
                }
                CarbonOAuthTokenRequest oauthRequest = new CarbonOAuthTokenRequest(httpRequest);
                // exchange the access token for the authorization grant.
                Token oauth2AccessTokenResp = getAccessToken(oauthRequest);

                OAuthTokenResponseBuilder oAuthRespBuilder = OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setAccessToken(oauth2AccessTokenResp.getAccessToken())
                        .setRefreshToken(oauth2AccessTokenResp.getRefreshToken())
                        .setExpiresIn(Long.toString(oauth2AccessTokenResp.getExpiresIn()))
                        .setTokenType("bearer");

                OAuthResponse response = oAuthRespBuilder.buildJSONMessage();

                //only access token need to be included in response
                response.setBody(oauth2AccessTokenResp.getAccessToken());

                ResponseBuilder respBuilder = Response
                        .status(response.getResponseStatus())
                        .header(OAuthConstants.HTTP_RESP_HEADER_CACHE_CONTROL,
                                OAuthConstants.HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE)
                        .header(OAuthConstants.HTTP_RESP_HEADER_PRAGMA,
                                OAuthConstants.HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE);

                return respBuilder.entity(response.getBody()).build();

            } catch (OAuthProblemException e) {
                log.debug(e.getError());
                OAuthResponse res = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                        .buildJSONMessage();
                return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
            } catch (OAuthClientException e) {
                OAuthResponse response = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                        .setError(OAuth2ErrorCodes.SERVER_ERROR)
                        .setErrorDescription(e.getMessage()).buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody())
                        .build();
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    private Response handleBasicAuthFailure() throws OAuthSystemException {
        OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                .setError(OAuth2ErrorCodes.INVALID_CLIENT)
                .setErrorDescription("Client Authentication failed.").buildJSONMessage();
        return Response.status(response.getResponseStatus())
                .header(OAuthConstants.HTTP_RESP_HEADER_AUTHENTICATE, EndpointUtil.getRealmInfo())
                .entity(response.getBody()).build();
    }

    private Token getAccessToken(CarbonOAuthTokenRequest oauthRequest)
            throws OAuthClientException {

        OAuth2AccessTokenReqDTO tokenReqDTO = new OAuth2AccessTokenReqDTO();
        String grantType = oauthRequest.getGrantType();
        tokenReqDTO.setGrantType(grantType);
        tokenReqDTO.setClientId(oauthRequest.getClientId());
        tokenReqDTO.setClientSecret(oauthRequest.getClientSecret());
        tokenReqDTO.setCallbackURI(oauthRequest.getRedirectURI());
        tokenReqDTO.setScope(oauthRequest.getScopes().toArray(new String[oauthRequest.getScopes().size()]));
        // Check the grant type and set the corresponding parameters
        if(GrantType.AUTHORIZATION_CODE.toString().equals(grantType)){
            tokenReqDTO.setAuthorizationCode(oauthRequest.getCode());
        } else if(GrantType.PASSWORD.toString().equals(grantType)){
            tokenReqDTO.setResourceOwnerUsername(oauthRequest.getUsername().toLowerCase());
            tokenReqDTO.setResourceOwnerPassword(oauthRequest.getPassword());
        } else if (GrantType.REFRESH_TOKEN.toString().equals(grantType)){
            tokenReqDTO.setRefreshToken(oauthRequest.getRefreshToken());
        } else if (org.wso2.carbon.identity.oauth.common.GrantType.SAML20_BEARER.toString().equals(grantType)){
            tokenReqDTO.setAssertion(oauthRequest.getAssertion());
            //tokenReqDTO.setIdp(oauthRequest.getIdP());
            tokenReqDTO.setTenantDomain(oauthRequest.getTenantDomain());
        } else { //handle web-app calling to app-m token endpoint
            tokenReqDTO.setGrantType(org.wso2.carbon.identity.oauth.common.GrantType.SAML20_BEARER.toString());
        }

        return EndpointUtil.getOAuth2Service().issueAccessToken(tokenReqDTO);
    }
}