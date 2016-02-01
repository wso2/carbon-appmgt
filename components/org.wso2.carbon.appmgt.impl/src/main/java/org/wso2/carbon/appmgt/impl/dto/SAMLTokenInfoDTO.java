package org.wso2.carbon.appmgt.impl.dto;

import org.joda.time.DateTime;/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.Serializable;

public class SAMLTokenInfoDTO implements Serializable{

    private String encodedSamlToken;

    private DateTime notOnOrAfter;

    private String sessionIndex;

    public String getSessionIndex() {
        return sessionIndex;
    }

    public void setSessionIndex(String sessionIndex) {
        this.sessionIndex = sessionIndex;
    }


    public void setEncodedSamlToken(String encodedSamlToken) {
        this.encodedSamlToken = encodedSamlToken;
    }

    public String getEncodedSamlToken() {
        return encodedSamlToken;
    }

    public void setNotOnOrAfter(DateTime notOnOrAfter) {
        this.notOnOrAfter = notOnOrAfter;
    }

    public DateTime getNotOnOrAfter() {
        return notOnOrAfter;
    }

}
