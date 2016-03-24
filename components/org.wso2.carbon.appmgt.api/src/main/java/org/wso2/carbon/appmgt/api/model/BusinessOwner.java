/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appmgt.api.model;

public class BusinessOwner {

    private int owner_id;
    private String owner_name;
    private String owner_mail;
    private String owner_desc;
    private String owner_site;
    private String keys;
    private String values;


    public BusinessOwner() {

    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public void setOwner_id(int owner_id) {
        this.owner_id = owner_id;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public void setOwner_mail(String owner_mail) {
        this.owner_mail = owner_mail;
    }

    public void setOwner_site(String owner_site) {
        this.owner_site = owner_site;
    }

    public void setOwner_desc(String owner_desc) {
        this.owner_desc = owner_desc;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public String getOwner_mail() {
        return owner_mail;
    }

    public String getOwner_desc() {
        return owner_desc;
    }

    public String getOwner_site() {
        return owner_site;
    }

    public String getKeys() {
        return keys;
    }

    public String getValues() {
        return values;
    }
}
