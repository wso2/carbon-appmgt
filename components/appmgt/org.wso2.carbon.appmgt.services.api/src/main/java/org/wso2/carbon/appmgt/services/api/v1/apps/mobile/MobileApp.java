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

package org.wso2.carbon.appmgt.services.api.v1.apps.mobile;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.wso2.carbon.appmgt.services.api.v1.apps.common.App;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MobileApp extends App {

    private String id;
    private String name;
    private String type;
    private String platform;
    private String version;
    private String identifier;
    private String iconImage;

    private String packageName;
    private String appIdentifier;
    private String location;


    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public String getIconImage() {
        return iconImage;
    }

    public void setIconImage(String iconImage) {
        this.iconImage = iconImage;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public String getAppIdentifier() {
        return appIdentifier;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public void setAppIdentifier(String appIdentifier) {
        this.appIdentifier = appIdentifier;
    }

    public String getLocation() {
        return location;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @XmlElement
    public void setLocation(String location) {
        this.location = location;
    }
}
