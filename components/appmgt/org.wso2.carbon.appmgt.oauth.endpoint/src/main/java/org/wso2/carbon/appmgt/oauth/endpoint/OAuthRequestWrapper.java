package org.wso2.carbon.appmgt.oauth.endpoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.MultivaluedMap;

public class OAuthRequestWrapper extends HttpServletRequestWrapper {
    private MultivaluedMap<String, String> form;

    public OAuthRequestWrapper(HttpServletRequest request, MultivaluedMap<String, String> form) {
        super(request);
        this.form = form;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null) {
            value = form.getFirst(name);
        }
        return value;
    }
}