/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.appmgt.impl.token;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.appmgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents the JSON Web Token generator.
 * By default the following properties are encoded to each authenticated WebApp request:
 * subscriber, applicationName, apiContext, version, tier, and endUserName
 * Additional properties can be encoded by engaging the ClaimsRetrieverImplClass callback-handler.
 * The JWT header and body are base64 encoded separately and concatenated with a dot.
 * Finally the token is signed using SHA256 with RSA algorithm.
 */
public class JWTGenerator {

    private static final Log log = LogFactory.getLog(JWTGenerator.class);

    private static final String API_GATEWAY_ID = "wso2.org/products/am";

    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    private static final String NONE = "NONE";

    private static volatile long ttl = -1L;

    private ClaimsRetriever claimsRetriever;

    private String dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;

    private String signatureAlgorithm = SHA256_WITH_RSA;

    private static final String SIGNATURE_ALGORITHM = "APIConsumerAuthentication.SignatureAlgorithm";

    private boolean includeClaims = true;

    private boolean enableSigning = true;

    private boolean saml2Enabled = true;

    private boolean addClaimsSelectively = false;

    private static ConcurrentHashMap<Integer, Key> privateKeys = new ConcurrentHashMap<Integer, Key>();
    private static ConcurrentHashMap<Integer, Certificate> publicCerts = new ConcurrentHashMap<Integer, Certificate>();

    //constructor for testing purposes
    public JWTGenerator(boolean includeClaims, boolean enableSigning) {
        this.includeClaims = includeClaims;
        this.enableSigning = enableSigning;
        signatureAlgorithm = NONE;
    }

    /**
     * Reads the ClaimsRetrieverImplClass from app-manager.xml ->
     * APIConsumerAuthentication -> ClaimsRetrieverImplClass.
     *
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public JWTGenerator() {

        String claimsRetrieverImplClass =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                        getAPIManagerConfiguration().getFirstProperty(ClaimsRetriever.CLAIMS_RETRIEVER_IMPL_CLASS);
        dialectURI = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(ClaimsRetriever.CONSUMER_DIALECT_URI);
        if (dialectURI == null) {
            dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;
        }
        if (claimsRetrieverImplClass != null) {
            try {
                claimsRetriever = (ClaimsRetriever) Class.forName(claimsRetrieverImplClass).newInstance();
                claimsRetriever.init();
            } catch (ClassNotFoundException e) {
                log.error("Cannot find class: " + claimsRetrieverImplClass, e);
            } catch (InstantiationException e) {
                log.error("Error instantiating " + claimsRetrieverImplClass);
            } catch (IllegalAccessException e) {
                log.error("Illegal access to " + claimsRetrieverImplClass);
            } catch (AppManagementException e) {
                log.error("Error while initializing " + claimsRetrieverImplClass);
            }
        }

        signatureAlgorithm = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(SIGNATURE_ALGORITHM);
        if (signatureAlgorithm == null || !(signatureAlgorithm.equals(NONE) || signatureAlgorithm.equals(SHA256_WITH_RSA))) {
            signatureAlgorithm = SHA256_WITH_RSA;
        }

        addClaimsSelectively = Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(AppMConstants.API_CONSUMER_AUTHENTICATION_ADD_CLAIMS_SELECTIVELY));
    }

  /**
     * Method that generates the JWT.
     *
     * @param keyValidationInfoDTO
     * @param apiContext
     * @param version
     * @param includeEndUserName
     * @return signed JWT token
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public String generateToken(APIKeyValidationInfoDTO keyValidationInfoDTO, String apiContext,
                     String version, boolean includeEndUserName) throws AppManagementException {

        //generating expiring timestamp
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long expireIn = currentTime + 1000 * 60 * getTTL();

        String jwtBody;
        String dialect;
        if(claimsRetriever != null){
            //jwtBody = JWT_INITIAL_BODY.replaceAll("\\[0\\]", claimsRetriever.getDialectURI(endUserName));
            dialect = claimsRetriever.getDialectURI(keyValidationInfoDTO.getEndUserName());
        }else{
            //jwtBody = JWT_INITIAL_BODY.replaceAll("\\[0\\]", dialectURI);
            dialect = dialectURI;
        }

        String subscriber = keyValidationInfoDTO.getSubscriber();
        String applicationName = keyValidationInfoDTO.getApplicationName();
        String applicationId = keyValidationInfoDTO.getApplicationId();
        String tier = keyValidationInfoDTO.getTier();
        String endUserName = includeEndUserName ? keyValidationInfoDTO.getEndUserName() : null;
        String keyType = keyValidationInfoDTO.getType();
        String userType = keyValidationInfoDTO.getUserType();
        String applicationTier = keyValidationInfoDTO.getApplicationTier();
        String enduserTenantId = includeEndUserName ? String.valueOf(getTenantId(endUserName)) : null;


        //Sample JWT body
        //{"iss":"wso2.org/products/am","exp":1349267862304,"http://wso2.org/claims/subscriber":"nirodhasub",
        // "http://wso2.org/claims/applicationname":"App1","http://wso2.org/claims/apicontext":"/echo",
        // "http://wso2.org/claims/version":"1.2.0","http://wso2.org/claims/tier":"Gold",
        // "http://wso2.org/claims/enduser":"null"}

        StringBuilder jwtBuilder = new StringBuilder();
        jwtBuilder.append("{");
        jwtBuilder.append("\"iss\":\"");
        jwtBuilder.append(API_GATEWAY_ID);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"exp\":");
        jwtBuilder.append(String.valueOf(expireIn));
        jwtBuilder.append(",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/subscriber\":\"");
        jwtBuilder.append(subscriber);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/applicationid\":\"");
        jwtBuilder.append(applicationId);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/applicationname\":\"");
        jwtBuilder.append(applicationName);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/applicationtier\":\"");
        jwtBuilder.append(applicationTier);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/apicontext\":\"");
        jwtBuilder.append(apiContext);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/version\":\"");
        jwtBuilder.append(version);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/tier\":\"");
        jwtBuilder.append(tier);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/keytype\":\"");
        jwtBuilder.append(keyType);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/usertype\":\"");
        jwtBuilder.append(userType);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/enduser\":\"");
        jwtBuilder.append(endUserName);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/enduserTenantId\":\"");
        jwtBuilder.append(enduserTenantId);
        jwtBuilder.append("\"");

        if(claimsRetriever != null){
            SortedMap<String,String> claimValues = claimsRetriever.getClaims(endUserName);
            Iterator<String> it = new TreeSet(claimValues.keySet()).iterator();
            while(it.hasNext()){
                String claimURI = it.next();
                jwtBuilder.append(", \"");
                jwtBuilder.append(claimURI);
                jwtBuilder.append("\":\"");
                jwtBuilder.append(claimValues.get(claimURI));
                jwtBuilder.append("\"");
            }
        }

        jwtBuilder.append("}");
        jwtBody = jwtBuilder.toString();

      String jwtHeader = null;

      //if signature algo==NONE, header without cert
      if(signatureAlgorithm.equals(NONE)){
          jwtHeader = "{\"typ\":\"JWT\"}";
      } else if (signatureAlgorithm.equals(SHA256_WITH_RSA)){
          jwtHeader = addCertToHeader(endUserName);
      }

      /*//add cert thumbprint to header
      String headerWithCertThumb = addCertToHeader(endUserName);*/

      String base64EncodedHeader = Base64Utils.encode(jwtHeader.getBytes());
      String base64EncodedBody = Base64Utils.encode(jwtBody.getBytes());
      if(signatureAlgorithm.equals(SHA256_WITH_RSA)){
          String assertion = base64EncodedHeader + "." + base64EncodedBody;

            //get the assertion signed
            byte[] signedAssertion = signJWT(assertion, endUserName);

            if (log.isDebugEnabled()) {
                log.debug("signed assertion value : " + new String(signedAssertion));
            }
            String base64EncodedAssertion = Base64Utils.encode(signedAssertion);

            return base64EncodedHeader + "." + base64EncodedBody + "." + base64EncodedAssertion;
        } else {
            return base64EncodedHeader + "." + base64EncodedBody + ".";
        }
    }


    /**
     * Method that generates the JWT token from SAML2 response
     * @param saml2Assertions
     * @param apiContext
     * @param version
     * @return
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public String generateToken(Map<String, Object> saml2Assertions, String apiContext, String version) throws
                                                                                                        AppManagementException {

        //generating expiring timestamp
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long expireIn = currentTime + 1000 * 60 * getTTL();

        String jwtBody;

        StringBuilder jwtBuilder = new StringBuilder();
        jwtBuilder.append("{");
        jwtBuilder.append("\"iss\":\"");
        jwtBuilder.append(API_GATEWAY_ID);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"exp\":");
        jwtBuilder.append(String.valueOf(expireIn));

        /* Populate claims from SAML Assertion if "AddClaimsSelectively" property is set to true,
         else add all claims values available in user profile */
        if (addClaimsSelectively) {
            if (saml2Assertions != null) {
                Iterator<String> it = new TreeSet(saml2Assertions.keySet()).iterator();
                while (it.hasNext()) {
                    String assertionAttribute = it.next();
                    jwtBuilder.append(",\"");
                    jwtBuilder.append(assertionAttribute);
                    jwtBuilder.append("\":\"");
                    jwtBuilder.append(saml2Assertions.get(assertionAttribute));
                    jwtBuilder.append("\"");
                }
            }
        } else {
            Map<String, String> customClaims = populateCustomClaims(saml2Assertions);
            if (customClaims != null) {
                Iterator<String> it = new TreeSet(customClaims.keySet()).iterator();
                while (it.hasNext()) {
                    String claimAttribute = it.next();
                    jwtBuilder.append(",\"");
                    jwtBuilder.append(claimAttribute);
                    jwtBuilder.append("\":\"");
                    jwtBuilder.append(customClaims.get(claimAttribute));
                    jwtBuilder.append("\"");
                }
            }
        }

        jwtBuilder.append("}");
        jwtBody = jwtBuilder.toString();

      String jwtHeader = null;
      String endUserName = (String) saml2Assertions.get("Subject");
      //if signature algo==NONE, header without cert
      if(signatureAlgorithm.equals(NONE)){
          jwtHeader = "{\"typ\":\"JWT\"}";
      } else if (signatureAlgorithm.equals(SHA256_WITH_RSA)){
          jwtHeader = addCertToHeader(endUserName);
      }

      /*//add cert thumbprint to header
      String headerWithCertThumb = addCertToHeader(endUserName);*/

      String base64EncodedHeader = Base64Utils.encode(jwtHeader.getBytes());
      String base64EncodedBody = Base64Utils.encode(jwtBody.getBytes());
      if(signatureAlgorithm.equals(SHA256_WITH_RSA)){
          String assertion = base64EncodedHeader + "." + base64EncodedBody;

            //get the assertion signed
            byte[] signedAssertion = signJWT(assertion, endUserName);

            if (log.isDebugEnabled()) {
                log.debug("signed assertion value : " + new String(signedAssertion));
            }
            String base64EncodedAssertion = Base64Utils.encode(signedAssertion);

            return base64EncodedHeader + "." + base64EncodedBody + "." + base64EncodedAssertion;
        } else {
            return base64EncodedHeader + "." + base64EncodedBody + ".";
        }
    }


    public Map<String, String> populateCustomClaims(Map<String, Object> saml2Assertions)
            throws AppManagementException {

        Map<String, String> claims = new HashMap<String, String> ();
        ClaimsRetriever claimsRetriever = getClaimsRetriever();
        if (claimsRetriever != null) {
            String userName = (String) saml2Assertions.get("Subject");
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String tenantAwareUserName = userName;

            try {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService()
                        .getTenantManager().getTenantId(tenantDomain);

                if (MultitenantConstants.SUPER_TENANT_ID == tenantId) {
                    tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(tenantAwareUserName);
                }

                claims.put("Subject", userName);
                claims.putAll(claimsRetriever.getClaims(tenantAwareUserName));

                return claims;
            } catch (UserStoreException e) {
                log.error("Error while getting tenant id to populate claims ", e);
                throw new AppManagementException("Error while getting tenant id to populate claims ", e);
            }
        }

        return null;
    }
    

    public ClaimsRetriever getClaimsRetriever() {
        return claimsRetriever;
    }

  /**
     * Helper method to sign the JWT
     *
     * @param assertion
     * @param endUserName
     * @return signed assertion
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    private byte[] signJWT(String assertion, String endUserName)
            throws AppManagementException {

        try {
            //get tenant domain
            String tenantDomain = MultitenantUtils.getTenantDomain(endUserName);
            //get tenantId
            int tenantId = getTenantId(endUserName);

            Key privateKey = null;

            if (!(privateKeys.containsKey(tenantId))) {
                AppManagerUtil.loadTenantRegistry(tenantId);
                //get tenant's key store manager
                KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

                if(!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)){
                    //derive key store name
                    String ksName = tenantDomain.trim().replace(".", "-");
                    String jksName = ksName + ".jks";
                    //obtain private key
                    //TODO: maintain a hash map with tenants' private keys after first initialization
                    privateKey = tenantKSM.getPrivateKey(jksName, tenantDomain);
                }else{
                    try{
                        privateKey = tenantKSM.getDefaultPrivateKey();
                    }catch (Exception e){
                        log.error("Error while obtaining private key for super tenant",e);
                    }
                }
                if (privateKey != null) {
                    privateKeys.put(tenantId, privateKey);
                }
            } else {
                privateKey = privateKeys.get(tenantId);
            }

            //initialize signature with private key and algorithm
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign((PrivateKey) privateKey);

            //update signature with data to be signed
            byte[] dataInBytes = assertion.getBytes();
            signature.update(dataInBytes);

            //sign the assertion and return the signature
            byte[] signedInfo = signature.sign();
            return signedInfo;

        } catch (NoSuchAlgorithmException e) {
            String error = "Signature algorithm not found.";
            //do not log
            throw new AppManagementException(error);
        } catch (InvalidKeyException e) {
            String error = "Invalid private key provided for the signature";
            //do not log
            throw new AppManagementException(error);
        } catch (SignatureException e) {
            String error = "Error in signature";
            //do not log
            throw new AppManagementException(error);
        } catch (AppManagementException e) {
            //do not log
            throw new AppManagementException(e.getMessage());
        }
    }

  /**
     * Helper method to add public certificate to JWT_HEADER to signature verification.
     *
     * @param endUserName
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    private String addCertToHeader(String endUserName) throws AppManagementException {

        try {
            //get tenant domain
            String tenantDomain = MultitenantUtils.getTenantDomain(endUserName);
            //get tenantId
            int tenantId = getTenantId(endUserName);
            Certificate publicCert = null;

            if (!(publicCerts.containsKey(tenantId))) {
                //get tenant's key store manager
                AppManagerUtil.loadTenantRegistry(tenantId);
                KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

                KeyStore keyStore = null;
                if(!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)){
                    //derive key store name
                    String ksName = tenantDomain.trim().replace(".", "-");
                    String jksName = ksName + ".jks";
                    keyStore = tenantKSM.getKeyStore(jksName);
                    publicCert = keyStore.getCertificate(tenantDomain);
                }else{
                    keyStore = tenantKSM.getPrimaryKeyStore();
                    publicCert = tenantKSM.getDefaultPrimaryCertificate();
                }
                if (publicCert != null) {
                    publicCerts.put(tenantId, publicCert);
                }
            } else {
                publicCert = publicCerts.get(tenantId);
            }

            //generate the SHA-1 thumbprint of the certificate
            //TODO: maintain a hashmap with tenants' pubkey thumbprints after first initialization
            MessageDigest digestValue = MessageDigest.getInstance("SHA-1");
            byte[] der = publicCert.getEncoded();
            digestValue.update(der);
            byte[] digestInBytes = digestValue.digest();

            String publicCertThumbprint = hexify(digestInBytes);
            String base64EncodedThumbPrint = Base64Utils.encode(publicCertThumbprint.getBytes());
            //String headerWithCertThumb = JWT_HEADER.replaceAll("\\[1\\]", base64EncodedThumbPrint);
            //headerWithCertThumb = headerWithCertThumb.replaceAll("\\[2\\]", signatureAlgorithm);
            //return headerWithCertThumb;

            StringBuilder jwtHeader = new StringBuilder();
            //Sample header
            //{"typ":"JWT", "alg":"SHA256withRSA", "x5t":"NmJmOGUxMzZlYjM2ZDRhNTZlYTA1YzdhZTRiOWE0NWI2M2JmOTc1ZA=="}
            //{"typ":"JWT", "alg":"[2]", "x5t":"[1]"}
            jwtHeader.append("{\"typ\":\"JWT\",");
            jwtHeader.append("\"alg\":\"");
            jwtHeader.append(signatureAlgorithm);
            jwtHeader.append("\",");

            jwtHeader.append("\"x5t\":\"");
            jwtHeader.append(base64EncodedThumbPrint);
            jwtHeader.append("\"");

            jwtHeader.append("}");
            return jwtHeader.toString();

        } catch (KeyStoreException e) {
            String error = "Error in obtaining tenant's keystore";
            throw new AppManagementException(error);
        } catch (CertificateEncodingException e) {
            String error = "Error in generating public cert thumbprint";
            throw new AppManagementException(error);
        } catch (NoSuchAlgorithmException e) {
            String error = "Error in generating public cert thumbprint";
            throw new AppManagementException(error);
        } catch (Exception e) {
            String error = "Error in obtaining tenant's keystore";
            throw new AppManagementException(error);
        }
    }

    private long getTTL() {
        if (ttl != -1) {
            return ttl;
        }

        synchronized (JWTGenerator.class) {
            if (ttl != -1) {
                return ttl;
            }
            AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String ttlValue = config.getFirstProperty(AppMConstants.API_KEY_SECURITY_CONTEXT_TTL);
            if (ttlValue != null) {
                ttl = Long.parseLong(ttlValue);
            } else {
                ttl = 15L;
            }
            return ttl;
        }
    }

  /**
     * Helper method to get tenantId from userName
     *
     * @param userName
     * @return tenantId
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    static int getTenantId(String userName) throws AppManagementException {
        //get tenant domain from user name
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

        if(realmService == null){
            return MultitenantConstants.SUPER_TENANT_ID;
        }

        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            return tenantId;
        } catch (UserStoreException e) {
            String error = "Error in obtaining tenantId from Domain";
            //do not log
            throw new AppManagementException(error);
        }
    }

  /**
     * Helper method to hexify a byte array.
     * TODO:need to verify the logic
     *
     * @param bytes
     * @return  hexadecimal representation
     */
    private String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuffer buf = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }

        return buf.toString();
    }

}