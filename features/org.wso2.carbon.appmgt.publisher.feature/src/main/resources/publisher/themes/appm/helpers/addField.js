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
var addField = function (data, owner_name) {

    var c = data.data.fields.length - 1;
    data.data.fields[c].name = "overview_ownerName";
    data.data.fields[c].label = "Business Owner";
    data.data.fields[c].isRequired = true;
    data.data.fields[c].isTextBox = true;
    data.data.fields[c].isTextArea = false;
    data.data.fields[c].isOptions = false;
    data.data.fields[c].isOptionsText = false;
    data.data.fields[c].isDate = false;
    data.data.fields[c].isReadOnly = false;
    data.data.fields[c].isEditable = true;
    data.data.fields[c].isFile = false;
    data.data.fields[c].isImageFile = false;
    data.data.fields[c].value = owner_name;
    data.data.fields[c].valueList = {};
    log.info(data.artifact.id);
    log.info(owner_name);

    return data;
};