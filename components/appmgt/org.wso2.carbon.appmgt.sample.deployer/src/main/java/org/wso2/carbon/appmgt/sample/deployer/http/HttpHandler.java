package org.wso2.carbon.appmgt.sample.deployer.http;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.wso2.carbon.appmgt.sample.deployer.bean.MobileApplicationBean;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class HttpHandler {

    private final static String USER_AGENT = "Mozilla/5.0";

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                if (hostname.equals("localhost"))
                    return true;
                return false;
            }
        });
    }

    public static String getHtml(String url) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        InputStream content = entity.getContent();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(content));
        StringBuffer responseBuffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            responseBuffer.append(line);
        }
        return responseBuffer.toString();
    }

    public String doPostHttps(String backEnd, String payload, String your_session_id, String contentType)
            throws IOException {
        URL obj = new URL(backEnd);

        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        if (!your_session_id.equals("")) {
            con.setRequestProperty(
                    "Cookie", "JSESSIONID=" + your_session_id);
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
            if (your_session_id.equals("")) {
                String session_id = response.substring((response.lastIndexOf(":") + 3), (response.lastIndexOf("}") - 2));
                return session_id;
            } else if (your_session_id.equals("header")) {
                return con.getHeaderField("Set-Cookie");
            }

            return response.toString();
        }
        return null;
    }

    public String doPostHttp(String backEnd, String payload, String your_session_id, String contentType)
            throws IOException {
        URL obj = new URL(backEnd);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        if (!your_session_id.equals("") && !your_session_id.equals("none")) {
            con.setRequestProperty(
                    "Cookie", "JSESSIONID=" + your_session_id);
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
            if (your_session_id.equals("")) {
                String session_id = response.substring((response.lastIndexOf(":") + 3), (response.lastIndexOf("}") - 2));
                return session_id;
            } else if (your_session_id.equals("appmSamlSsoTokenId")) {
                return con.getHeaderField("Set-Cookie").split(";")[0].split("=")[1];
            } else if (your_session_id.equals("header")) {
                return con.getHeaderField("Set-Cookie").split("=")[1].split(";")[0];
            } else {
                return response.toString();
            }
        }
        return null;
    }

    public String doPut(String url, String cookie) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        StringBuilder result = new StringBuilder();
        HttpPut putRequest = new HttpPut(url);
        putRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
        putRequest.addHeader("Accept-Language", "en-US,en;q=0.5");
        putRequest.addHeader("Cookie", "JSESSIONID=" + cookie);
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
        return result.toString();
    }

    public String doPostMultiData(String url, String method, MobileApplicationBean mobileApplicationBean, String cookie)
            throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Cookie", "JSESSIONID=" + cookie);
        MultipartEntityBuilder reqEntity;
        if (method.equals("upload")) {
            reqEntity = MultipartEntityBuilder.create();
            FileBody fileBody = new FileBody(new File(mobileApplicationBean.getApkFile()));
            reqEntity.addPart("file", fileBody);
        } else {
            reqEntity = MultipartEntityBuilder.create();
            reqEntity.addPart("version", new StringBody(mobileApplicationBean.getVersion(),
                    ContentType.MULTIPART_FORM_DATA));
            reqEntity.addPart("provider", new StringBody(mobileApplicationBean.getMarkettype(),
                    ContentType.MULTIPART_FORM_DATA));
            reqEntity.addPart("markettype", new StringBody(mobileApplicationBean.getMarkettype(),
                    ContentType.MULTIPART_FORM_DATA));
            reqEntity.addPart("platform", new StringBody(mobileApplicationBean.getPlatform(),
                    ContentType.MULTIPART_FORM_DATA));
            reqEntity.addPart("name", new StringBody(mobileApplicationBean.getName(), ContentType.MULTIPART_FORM_DATA));
            reqEntity.addPart("description", new StringBody(mobileApplicationBean.getDescription(),
                    ContentType.MULTIPART_FORM_DATA));
            FileBody bannerImageFile = new FileBody(new File(mobileApplicationBean.getBannerFilePath()));
            reqEntity.addPart("bannerFile", bannerImageFile);
            FileBody iconImageFile = new FileBody(new File(mobileApplicationBean.getIconFile()));
            reqEntity.addPart("iconFile", iconImageFile);
            FileBody screenShot1 = new FileBody(new File(mobileApplicationBean.getScreenShot1File()));
            reqEntity.addPart("screenshot1File", screenShot1);
            FileBody screenShot2 = new FileBody(new File(mobileApplicationBean.getScreenShot2File()));
            reqEntity.addPart("screenshot2File", screenShot2);
            FileBody screenShot3 = new FileBody(new File(mobileApplicationBean.getScreenShot3File()));
            reqEntity.addPart("screenshot3File", screenShot3);
            reqEntity.addPart("addNewAssetButton", new StringBody("Submit", ContentType.MULTIPART_FORM_DATA));
            reqEntity.addPart("mobileapp", new StringBody(mobileApplicationBean.getMobileapp(),
                    ContentType.MULTIPART_FORM_DATA));
            reqEntity.addPart("sso_ssoProvider", new StringBody(mobileApplicationBean.getSso_ssoProvider(),
                    ContentType.MULTIPART_FORM_DATA));
            reqEntity.addPart("appmeta", new StringBody(mobileApplicationBean.getAppmeta(),
                    ContentType.MULTIPART_FORM_DATA));
        }
        final HttpEntity entity = reqEntity.build();
        httpPost.setEntity(entity);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpClient.execute(httpPost, responseHandler);
        if (!method.equals("upload")) {
            String id_part = responseBody.split(",")[2].split(":")[1];
            return id_part.substring(2, (id_part.length() - 2));
        }
        return responseBody;
    }

    public String doGet(String url, String trackingCode, String appmSamlSsoTokenId, String webAppURl) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        //add request header
        if (trackingCode.equals("")) {
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            con.setRequestProperty("Cookie", "JSESSIONID=" + appmSamlSsoTokenId);
        } else {
            con.setRequestProperty("Cookie", "appmSamlSsoTokenId=" + appmSamlSsoTokenId);
            con.setRequestProperty("trackingCode", trackingCode);
            con.setRequestProperty("Referer", webAppURl);
        }
        int responseCode = con.getResponseCode();
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
