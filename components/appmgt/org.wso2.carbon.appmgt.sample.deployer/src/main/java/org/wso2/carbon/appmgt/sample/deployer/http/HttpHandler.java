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

package org.wso2.carbon.appmgt.sample.deployer.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is use as a http client
 */
public class HttpHandler {

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                if (hostname.equals("localhost"))
                    return true;
                return false;
            }
        });
    }

    /**
     * This method is use get a html file for given url
     *
     * @param url Web page url
     * @return response
     * @throws java.io.IOException Throws this when failed to retrieve web page
     */
    public static String getHtml(String url) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        InputStream content = entity.getContent();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(content));
        StringBuffer responseBuffer = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            responseBuffer.append(line);
        }
        if (in != null) {
            in.close();
        }
        return responseBuffer.toString();
    }

    /**
     * This method is used to do a https post request
     *
     * @param url         request url
     * @param payload     Content of the post request
     * @param sessionId   sessionId for authentication
     * @param contentType content type of the post request
     * @return response
     * @throws java.io.IOException - Throws this when failed to fulfill a https post request
     */
    public String doPostHttps(String url, String payload, String sessionId, String contentType)
            throws IOException {
        URL obj = new URL(url);

        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        //add reuqest header
        con.setRequestMethod("POST");
        if (!sessionId.equals("")) {
            con.setRequestProperty(
                    "Cookie", "JSESSIONID=" + sessionId);
        }
        if (!contentType.equals("")) {
            con.setRequestProperty("Content-Type", contentType);
        }
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(payload);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            if (sessionId.equals("")) {
                String session_id = response.substring((response.lastIndexOf(":") + 3), (response.lastIndexOf("}") - 2));
                return session_id;
            } else if (sessionId.equals("header")) {
                return con.getHeaderField("Set-Cookie");
            }

            return response.toString();
        }
        return null;
    }

    /**
     * This method is used to do a http post request
     *
     * @param url         request url
     * @param payload     Content of the post request
     * @param sessionId   sessionId for authentication
     * @param contentType content type of the post request
     * @return response
     * @throws java.io.IOException - Throws this when failed to fulfill a http post request
     */
    public String doPostHttp(String url, String payload, String sessionId, String contentType)
            throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        //add reuqest header
        con.setRequestMethod("POST");
        //con.setRequestProperty("User-Agent", USER_AGENT);
        if (!sessionId.equals("") && !sessionId.equals("none")) {
            con.setRequestProperty(
                    "Cookie", "JSESSIONID=" + sessionId);
        }
        con.setRequestProperty("Content-Type", contentType);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(payload);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            if (sessionId.equals("")) {
                String session_id = response.substring((response.lastIndexOf(":") + 3), (response.lastIndexOf("}") - 2));
                return session_id;
            } else if (sessionId.equals("appmSamlSsoTokenId")) {
                return con.getHeaderField("Set-Cookie").split(";")[0].split("=")[1];
            } else if (sessionId.equals("header")) {
                return con.getHeaderField("Set-Cookie").split("=")[1].split(";")[0];
            } else {
                return response.toString();
            }
        }
        return null;
    }

    /**
     * This method is used to do a http put request
     *
     * @param url       request url
     * @param sessionId sessionId for authentication
     * @return response
     * @throws java.io.IOException - Throws this when failed to fulfill a http put request
     */
    public String doPut(String url, String sessionId) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        StringBuilder result = new StringBuilder();
        HttpPut putRequest = new HttpPut(url);
        putRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
        putRequest.addHeader("Accept-Language", "en-US,en;q=0.5");
        putRequest.addHeader("Cookie", "JSESSIONID=" + sessionId);
        putRequest.addHeader("Accept-Encoding", "gzip, deflate");
        HttpResponse response = httpClient.execute(putRequest);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatusLine().getStatusCode());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (response.getEntity().getContent())));
        String output;
        while ((output = br.readLine()) != null) {
            result.append(output);
        }
        if (br != null) {
            br.close();
        }
        return result.toString();
    }

    /**
     * This method is used to do a http get request
     *
     * @param url                request url
     * @param trackingCode       tracking code of the web application
     * @param appmSamlSsoTokenId appmSamlSsoTokenId id of the web application
     * @param refer              web page url
     * @return response
     * @throws java.io.IOException Throws this when failed to fulfill a http get request
     */
    public String doGet(String url, String trackingCode, String appmSamlSsoTokenId, String refer) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        //add request header
        if (trackingCode.equals("")) {
            con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            con.setRequestProperty("Cookie", "JSESSIONID=" + appmSamlSsoTokenId);
        } else {
            con.setRequestProperty("Cookie", "appmSamlSsoTokenId=" + appmSamlSsoTokenId);
            con.setRequestProperty("trackingCode", trackingCode);
            con.setRequestProperty("Referer", refer);
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}
