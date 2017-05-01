/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.gateway.token;

import com.google.gson.Gson;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.SAMLConstants;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.appmgt.impl.token.JWTSignatureAlgorithm;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents the JSON Web Token generator.
 * By default the following properties are encoded to each authenticated API request:
 * subscriber, applicationName, apiContext, version, tier, and endUserName
 * Additional properties can be encoded by engaging the ClaimsRetrieverImplClass callback-handler.
 * The JWT header and body are base64 encoded separately and concatenated with a dot.
 * Finally the token is signed using SHA256 with RSA algorithm.
 */
public abstract class AbstractJWTGenerator implements TokenGenerator {

    private static final Log log = LogFactory.getLog(AbstractJWTGenerator.class);

    protected static final String APP_GATEWAY_ID = "wso2.org/products/appm";

    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    private static final String NONE = "NONE";

    private static volatile long ttl = -1L;

    private static long DEFAULT_TTL = 15L;

    private ClaimsRetriever claimsRetriever;

    private String dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;

    private String signatureAlgorithm = SHA256_WITH_RSA;

    private boolean includeClaims = true;

    private boolean enableSigning = true;

    private boolean addClaimsSelectively = false;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private Map<Integer, Key> privateKeys = new HashMap<Integer, Key>();
    private Map<Integer, Certificate> publicCertificate = new HashMap<Integer, Certificate>();
    private Map<Integer, String> base64EncodedThumbPrintMap = new HashMap<Integer, String>();
    private Map<String, Integer> tenantMap = new ConcurrentHashMap<String, Integer>();

    /**
     * Reads the ClaimsRetrieverImplClass from app-manager.xml ->
     * AppConsumerAuthConfiguration -> ClaimsRetrieverImplClass.
     *
     * @throws AppManagementException
     */
    public AbstractJWTGenerator() {

        String claimsRetrieverImplClass = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(ClaimsRetriever.CLAIMS_RETRIEVER_IMPL_CLASS);
        dialectURI = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(ClaimsRetriever.CONSUMER_DIALECT_URI);
        if (dialectURI == null) {
            dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;
        }
        if (claimsRetrieverImplClass != null) {
            try {
                //TODO: Remove Class.forName
                claimsRetriever = (ClaimsRetriever) Class.forName(claimsRetrieverImplClass).newInstance();
                claimsRetriever.init();
            } catch (ClassNotFoundException e) {
                log.error("Cannot find class: " + claimsRetrieverImplClass, e);
            } catch (InstantiationException e) {
                log.error("Error instantiating " + claimsRetrieverImplClass, e);
            } catch (IllegalAccessException e) {
                log.error("Illegal access to " + claimsRetrieverImplClass, e);
            } catch (AppManagementException e) {
                log.error("Error while initializing " + claimsRetrieverImplClass, e);
            }
        }

        signatureAlgorithm = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(AppMConstants.SIGNATURE_ALGORITHM);
        if (signatureAlgorithm == null || !(signatureAlgorithm.equals(NONE) || signatureAlgorithm.equals(
                SHA256_WITH_RSA))) {
            signatureAlgorithm = SHA256_WITH_RSA;
        }

        addClaimsSelectively = Boolean.parseBoolean(ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.API_CONSUMER_AUTHENTICATION_ADD_CLAIMS_SELECTIVELY));
    }

    /**
     * Method that generates the JWT token from SAML2 response.
     *
     * @param saml2Assertions
     * @param webApp
     * @param messageContext
     * @return jwt token
     * @throws AppManagementException
     */
    public String generateToken(Map<String, Object> saml2Assertions, WebApp webApp,
                                MessageContext messageContext) throws AppManagementException {

        String endUserName = (String) saml2Assertions.get(SAMLConstants.SAML2_ASSERTION_SUBJECT);
        String jwtHeader = buildHeader(endUserName);

        String jwtBody = buildBody(saml2Assertions);

        String base64UrlEncodedHeader = Base64.encodeBase64URLSafeString(jwtHeader.getBytes(StandardCharsets.UTF_8));
        String base64UrlEncodedBody = Base64.encodeBase64URLSafeString(jwtBody.getBytes(StandardCharsets.UTF_8));

        if (signatureAlgorithm.equals(SHA256_WITH_RSA)) {
            String assertion = base64UrlEncodedHeader + "." + base64UrlEncodedBody;
            /* Get the assertion signed */
            byte[] signedAssertion = signJWT(assertion, endUserName);

            if (log.isDebugEnabled()) {
                log.debug("signed assertion value : " + new String(signedAssertion, StandardCharsets.UTF_8));
            }
            String base64UrlEncodedAssertion = Base64.encodeBase64URLSafeString(signedAssertion);
            return base64UrlEncodedHeader + "." + base64UrlEncodedBody + "." + base64UrlEncodedAssertion;
        } else {
            return base64UrlEncodedHeader + "." + base64UrlEncodedBody + ".";
        }
    }

    public String buildHeader(String endUserName) throws AppManagementException {
        //TODO: https://wso2.org/jira/browse/APPM-1060
        StringBuilder jwtHeaderBuilder = new StringBuilder();
        jwtHeaderBuilder.append("{\"typ\":\"JWT\",");
        jwtHeaderBuilder.append("\"alg\":\"");

        if (NONE.equals(signatureAlgorithm)) {
            jwtHeaderBuilder.append(JWTSignatureAlgorithm.NONE.getJwsCompliantCode());
            jwtHeaderBuilder.append("\"");
        } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            jwtHeaderBuilder.append(JWTSignatureAlgorithm.SHA256_WITH_RSA.getJwsCompliantCode());
            jwtHeaderBuilder.append("\",");
            jwtHeaderBuilder.append(addThumbPrintToHeader(endUserName));
        }

        jwtHeaderBuilder.append("}");
        return jwtHeaderBuilder.toString();
    }

    public String buildBody(Map<String, Object> saml2Assertions) throws AppManagementException {
        StringBuilder jwtBuilder = new StringBuilder();
        /* Populate claims from SAML Assertion if "AddClaimsSelectively" property is set to true,
         else add all claims values available in user profile */
        if (addClaimsSelectively) {
            Map<String, Object> standardClaims = populateStandardClaims(saml2Assertions);
            if (standardClaims != null) {
                jwtBuilder.append(buildJWTBody(standardClaims));
            }
        } else {
            Map<String, Object> customClaims = populateCustomClaims(saml2Assertions);
            if (customClaims != null) {
                jwtBuilder.append(buildJWTBody(customClaims));
            }
        }
        return jwtBuilder.toString();
    }

    private String buildJWTBody(Map<String, Object> claims) {
        StringBuilder jwtBuilder = new StringBuilder();
        jwtBuilder.append("{");
        if (claims != null) {
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                /* These values should be numbers. */
                if ("exp".equals(key) || "nbf".equals(key) || "iat".equals(key)) {
                    jwtBuilder.append("\"").append(key).append("\":").append(value).append(",");
                } else {
                    //value can be either String or List
                    if (value instanceof List) {
                        String assertionValue = new Gson().toJson(value);
                        jwtBuilder.append("\"").append(key).append("\":").append(assertionValue).append(",");
                    } else {
                        jwtBuilder.append("\"").append(key).append("\":\"").append(value.toString()).append("\",");
                    }
                }
            }
        }

        if (jwtBuilder.length() > 1) {
            jwtBuilder.delete(jwtBuilder.length() - 1, jwtBuilder.length());
        }

        jwtBuilder.append("}");
        return jwtBuilder.toString();
    }

    public abstract Map<String, Object> populateStandardClaims(Map<String, Object> saml2Assertions)
            throws AppManagementException;

    public abstract Map<String, Object> populateCustomClaims(Map<String, Object> saml2Assertions)
            throws AppManagementException;


    public ClaimsRetriever getClaimsRetriever() {
        return claimsRetriever;
    }

    /**
     * Helper method to sign the JWT
     *
     * @param assertion
     * @param endUserName
     * @return signed assertion
     * @throws AppManagementException
     */
    private byte[] signJWT(String assertion, String endUserName) throws AppManagementException {
        int tenantId = getTenantId(endUserName);
        try {
            Key privateKey = getPrivateKey(endUserName, tenantId);
            if (privateKey == null) {
                throw new AppManagementException("Private key is null for tenant " + tenantId);
            }
            /* Initialize signature with private key and algorithm */
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign((PrivateKey) privateKey);

            /* Update signature with data to be signed */
            byte[] dataInBytes = assertion.getBytes(StandardCharsets.UTF_8);
            signature.update(dataInBytes);

            /* Sign the assertion and return the signature */
            byte[] signedInfo = signature.sign();
            return signedInfo;
        } catch (NoSuchAlgorithmException e) {
            String error = "Signature algorithm " + signatureAlgorithm + " not found.";
            log.error(error, e);
            throw new AppManagementException(error, e);
        } catch (InvalidKeyException e) {
            String error = "Invalid private key provided for the signature for tenant " +  tenantId;
            log.error(error, e);
            throw new AppManagementException(error, e);
        } catch (SignatureException e) {
            String error = "Error in signature algorithm " + signatureAlgorithm;
            log.error(error, e);
            throw new AppManagementException(error, e);
        } catch (AppManagementException e) {
            String error = "Error in obtaining tenant's " + tenantId + " private key";
            log.error(error, e);
            throw new AppManagementException(error, e);
        }
    }

    /**
     * Helper method to get private key for specific tenant.
     *
     * @param endUserName
     * @param tenantId
     * @return private key
     * @throws AppManagementException
     */
    private Key getPrivateKey(String endUserName, int tenantId) throws AppManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomain(endUserName);
        try {
            Key privateKey = privateKeys.get(tenantId);
            if (privateKey == null) {
                KeyStoreManager tenantKSM = getKeyStoreManager(tenantId);

                if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    /* Derive key store name */
                    String keyStoreName = tenantDomain.trim().replace(".", "-");
                    String jksName = keyStoreName + ".jks";
                    /* Obtain private key */
                    privateKey = tenantKSM.getPrivateKey(jksName, tenantDomain);
                } else {
                    try {
                        privateKey = tenantKSM.getDefaultPrivateKey();
                    } catch (Exception e) {
                        String error = "Error while obtaining private key for super tenant";
                        log.error(error, e);
                        throw new AppManagementException(error, e);
                    }
                }
                if (privateKey != null) {
                    privateKeys.put(tenantId, privateKey);
                }
            }
            return privateKey;
        } catch (AppManagementException e) {
            String error = "Error in obtaining tenant's " + tenantId + " private key";
            log.error(error, e);
            throw new AppManagementException(error, e);
        }
    }

    private KeyStoreManager getKeyStoreManager(int tenantId) throws AppManagementException {
        try {
            AppManagerUtil.loadTenantRegistry(tenantId);
            return KeyStoreManager.getInstance(tenantId);
        } catch (AppManagementException e) {
            String error = "Error in obtaining  key store manager for tenant " + tenantId;
            log.error(error, e);
            throw new AppManagementException(error, e);
        }
    }

    /**
     * Helper method to add public certificate to JWT_HEADER to signature verification.
     *
     * @param endUserName
     * @return jwt header as a string
     * @throws AppManagementException
     */
    private String addThumbPrintToHeader(String endUserName) throws AppManagementException {
        int tenantId = getTenantId(endUserName);
        try {
            StringBuilder jwtHeader = new StringBuilder();
            String base64EncodedThumbPrint = getBase64EncodedThumbPrint(endUserName, tenantId);
            if (base64EncodedThumbPrint == null) {
                log.error("Base64 encoded thumb print is null for tenant : " + tenantId);
            }
            jwtHeader.append("\"x5t\":\"");
            jwtHeader.append(base64EncodedThumbPrint);
            jwtHeader.append("\"");
            return jwtHeader.toString();
        } catch (AppManagementException e) {
            String error = "Error in adding tenant's " + tenantId + " public certificate";
            throw new AppManagementException(error, e);
        }
    }

    /**
     * Helper method to get base 64 encoded thumb print for specific tenant.
     *
     * @param endUserName
     * @param tenantId
     * @return base 64 encoded thumb print
     * @throws AppManagementException
     */
    private String getBase64EncodedThumbPrint(String endUserName, int tenantId) throws AppManagementException {
        try {
            String base64EncodedThumbPrint =  base64EncodedThumbPrintMap.get(tenantId);
            if (base64EncodedThumbPrint == null) {
                //TODO: https://wso2.org/jira/browse/APPM-1061
                Certificate publicCert = getPublicCertificate(endUserName, tenantId);
                if (publicCert == null) {
                    throw new AppManagementException("Public certificate is null for tenant " + tenantId);
                }
                MessageDigest digestValue = MessageDigest.getInstance("SHA-1");
                byte[] der = publicCert.getEncoded();
                digestValue.update(der);
                byte[] digestInBytes = digestValue.digest();

                String publicCertThumbprint = bytesToHex(digestInBytes);
                base64EncodedThumbPrint = Base64Utils.encode(publicCertThumbprint.getBytes(StandardCharsets.UTF_8));
                if (base64EncodedThumbPrint != null) {
                    base64EncodedThumbPrintMap.put(tenantId, base64EncodedThumbPrint);
                }
            }
            return base64EncodedThumbPrint;
        } catch (CertificateEncodingException e) {
            String error = "Error in generating public certificate thumbprint for tenant " + tenantId;
            throw new AppManagementException(error, e);
        } catch (NoSuchAlgorithmException e) {
            String error = "Signature algorithm " + signatureAlgorithm + " not found.";
            throw new AppManagementException(error, e);
        }
    }

    /**
     * Helper method to get public certificate for specific tenant.
     *
     * @param endUserName
     * @param tenantId
     * @return public certificate
     * @throws AppManagementException
     */
    private Certificate getPublicCertificate(String endUserName, int tenantId) throws AppManagementException {
        String tenantDomain = MultitenantUtils.getTenantDomain(endUserName);
        try {
            Certificate publicCert = publicCertificate.get(tenantId);

            if (publicCert == null) {
                /* Get tenant's key store manager */
                KeyStoreManager tenantKSM = getKeyStoreManager(tenantId);
                KeyStore keyStore = null;
                if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    /* Derive key store name */
                    String ksName = tenantDomain.trim().replace(".", "-");
                    String jksName = ksName + ".jks";
                    keyStore = tenantKSM.getKeyStore(jksName);
                    publicCert = keyStore.getCertificate(tenantDomain);
                } else {
                    publicCert = tenantKSM.getDefaultPrimaryCertificate();
                }
                if (publicCert != null) {
                    publicCertificate.put(tenantId, publicCert);
                }
            }
            return publicCert;
        } catch (KeyStoreException e) {
            String error = "Error in obtaining tenant's " + tenantId + " keystore";
            throw new AppManagementException(error, e);
        } catch (CertificateEncodingException e) {
            String error = "Error in generating public certificate thumbprint for tenant " + tenantId;
            throw new AppManagementException(error, e);
        } catch (NoSuchAlgorithmException e) {
            String error = "Signature algorithm " + signatureAlgorithm + " not found.";
            throw new AppManagementException(error, e);
        } catch (Exception e) {
            String error = "Error in obtaining tenant's " + tenantId + " keystore";
            throw new AppManagementException(error, e);
        }
    }

    protected long getTTL() {
        if (ttl != -1) {
            return ttl;
        }

        synchronized (AbstractJWTGenerator.class) {
            if (ttl != -1) {
                return ttl;
            }
            AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String ttlValue = config.getFirstProperty(AppMConstants.API_KEY_SECURITY_CONTEXT_TTL);
            if (ttlValue != null) {
                ttl = Long.parseLong(ttlValue);
            } else {
                ttl = DEFAULT_TTL;
            }
            return ttl;
        }
    }

    /**
     * Helper method to get tenantId from userName
     *
     * @param userName
     * @return tenantId
     * @throws AppManagementException
     */
    protected int getTenantId(String userName) throws AppManagementException {
        int tenantId;
        if (tenantMap.containsKey(userName)) {
            tenantId = tenantMap.get(userName);
        } else {
            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

            if (realmService == null) {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            } else {
                try {
                    tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                } catch (UserStoreException e) {
                    String error = "Error in obtaining tenantId from Domain " + tenantDomain;
                    log.error(error, e);
                    throw new AppManagementException(error, e);
                }
            }
            tenantMap.put(userName, tenantId);
        }
        return tenantId;
    }

    /**
     * Helper method to hexify a byte array.
     *
     * @param bytes
     * @return hexadecimal representation
     */
    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}