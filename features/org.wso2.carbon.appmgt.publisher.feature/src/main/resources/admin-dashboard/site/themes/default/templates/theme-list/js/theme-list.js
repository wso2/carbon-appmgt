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

$(document).on("click", ".theme-delete-button", function () {
    var themeType = $(this).data("theme-type");
    var theme = $(this).data("theme");
    var context = jagg.site.context;
    var isConfirmed = confirm("Are you sure you want to delete the theme type \" " + theme + "\"?");
    if (isConfirmed) {
        $.ajax({
                   async: false,
                   url: context + '/apis/customthemes/' + themeType,
                   type: 'DELETE',
                   success: function (response) {
                       location.reload();
                   },
                   error: function (response) {
                   }
               });

    }
});