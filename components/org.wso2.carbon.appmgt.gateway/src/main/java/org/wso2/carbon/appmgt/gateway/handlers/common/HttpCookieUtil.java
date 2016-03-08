/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.gateway.handlers.common;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class to manipulate HTTP cookies in request and response
 *
 */
public class HttpCookieUtil {

    private static final long MAX_AGE_UNSPECIFIED = -1;

    /**
     * Parses the set cookie header.
     * "Set-Cookie"
     *
     * There can be multiple "Set-Cookie" headers, each describing only a single cookie.
     *
     * @param cookieString
     * @return The parsed HTTP Cookie or null if there is no such cookie.
     */
    public static HttpCookie parseSetCookieHeader(String cookieString) {
        if (cookieString != null) {
            List<HttpCookie> cookies = HttpCookie.parse(cookieString);
            if (cookies.size() > 0) {
                return cookies.get(0);
            }
        }
        return null;
    }

    /*
     *  Converts the HttpCookie into the header string in the format conforming to "Set-Cookie"
     */
    public static String formatSetCookieHeader(HttpCookie cookie) {
        StringBuilder sb = new StringBuilder();

        sb.append(cookie.getName()).append("=").append(cookie.getValue());
        if (cookie.getPath() != null)
            sb.append("; Path=").append(cookie.getPath());
        if (cookie.getDomain() != null)
            sb.append("; Domain=").append(cookie.getDomain());

        if (cookie.getMaxAge() != MAX_AGE_UNSPECIFIED) {
            sb.append("; Max-Age=").append(cookie.getMaxAge());
        }
        if (cookie.getSecure())
            sb.append("; Secure");
        if (cookie.isHttpOnly())
            sb.append("; HttpOnly");

        return sb.toString();
    }

    /**
     * Formats the cookies to the header format suitable for HTTP request.
     *
     * @param axis2MC Axis2 Message Context.
     * @param cookies List of HTTP Cookies.
     * @return Cookie String in header. null if the cookies list is empty.
     */
    public static String formatRequestCookieHeader(org.apache.axis2.context.MessageContext axis2MC,
            List<HttpCookie> cookies) {

        if (cookies == null || cookies.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cookies.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            HttpCookie httpCookie = cookies.get(i);
            sb.append(httpCookie.getName()).append("=").append(httpCookie.getValue());
        }
        return sb.toString();
    }

    /**
     * Writes the "Set-Cookie" headers into the response.
     *
     * @param axis2MC Axis2 Message Context
     * @param setCookies List of HTTP Cookies
     */
    public static void writeSetCookieHeaders(org.apache.axis2.context.MessageContext axis2MC,
            List<HttpCookie> setCookies) {
        Map<String, Object> headers = (Map<String, Object>) axis2MC
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Map<String, Object> excessHeaders = (Map<String, Object>) axis2MC
                .getProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS);

        excessHeaders.remove(HTTPConstants.HEADER_SET_COOKIE);
        if (setCookies.size() > 0) {
            headers.put(HTTPConstants.HEADER_SET_COOKIE, HttpCookieUtil.formatSetCookieHeader((setCookies.get(0))));
            if (setCookies.size() > 1) {
                for (int i = 1; i < setCookies.size(); i++) {
                    excessHeaders.put(HTTPConstants.HEADER_SET_COOKIE,
                            HttpCookieUtil.formatSetCookieHeader((setCookies.get(i))));
                }
            }
        }
    }

    /**
     * Returns all the avialable HTTP "Set-Cookie" heades in the Axis2 Message Context.
     *
     * @param axis2MC Axis2 Message Context
     * @return list of HTTPCookie in both Transport header and Excess Headers.
     */
    public static List<HttpCookie> getAllHttpSetCookies(org.apache.axis2.context.MessageContext axis2MC) {
        List<HttpCookie> allSetCookies = new ArrayList<>();
        Map<String, Object> excessHeaders = (Map<String, Object>) axis2MC
                .getProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS);
        if (excessHeaders != null) {
            List<String> excessCookies = (List<String>) excessHeaders.get(HTTPConstants.HEADER_SET_COOKIE);
            if (excessCookies != null) {
                for (String cookieString : excessCookies) {
                    HttpCookie cookie = parseSetCookieHeader(cookieString);
                    if (cookie != null) {
                        allSetCookies.add(cookie);
                    }
                }
            }
        }

        Map<String, Object> headers = (Map<String, Object>) axis2MC
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String cookieString = (String) headers.get(HTTPConstants.HEADER_SET_COOKIE);
        if (cookieString != null) {
            HttpCookie cookie = parseSetCookieHeader(cookieString);
            if (cookie != null) {
                allSetCookies.add(cookie);
            }
        }
        return allSetCookies;
    }

    /**
     * Parses the cookie string to list of cookies
     * @param cookieString
     * @return
     */
    public static List<HttpCookie> parseRequestCookie(String cookieString) {
        if(cookieString == null || cookieString.isEmpty()) {
            return Collections.emptyList();
        }
        List<HttpCookie> result = new ArrayList<>();
        String[] parts = cookieString.split(";");
        for(String part: parts) {
            String[] nameValue = part.split("=");
            if(nameValue.length ==2) {
                HttpCookie httpCookie = new HttpCookie(nameValue[0].trim(), nameValue[1].trim());
                result.add(httpCookie);
            }
        }

        return result;
    }
}
