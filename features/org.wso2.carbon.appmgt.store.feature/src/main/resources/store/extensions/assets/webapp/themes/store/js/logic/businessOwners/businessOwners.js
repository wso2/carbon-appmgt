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
function merge(businessOwner){
    var keys = businessOwner.keys;
    var values = businessOwner.values;

    if(keys != null || keys !="") {
        var keySet = keys.split("/");
        var valueSet = values.split("/");
        }
    var length = keySet.length +4;
    var owner = new Array();
    
    owner.push({
                      name : "Name",
                      value : businessOwner.owner_name
                  });
    owner.push({
                      name : "Email",
                      value : businessOwner.owner_email
                  });
    owner.push({
                      name : "Website",
                      value : businessOwner.owner_site
                  });
    owner.push({
                      name : "Description",
                      value : businessOwner.owner_desc
                  });

    for(var i = 5; i< length; i++){

        owner.push({
                          name : keySet[i-4],
                          value : valueSet[i-4]
                      });
    }
    return owner;
}