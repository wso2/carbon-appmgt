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

package org.wso2.carbon.appmgt.sample.deployer.javascriptwrite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class InvokeStatistcsJavascriptBuilder {
    private String jsFunction;

    public InvokeStatistcsJavascriptBuilder(String trackingID, String ipAddress) {
        jsFunction = "function invokeStatistics(){\n" +
                "       var tracking_code = \"" + trackingID + "\";\n" +
                "        var request = $.ajax({\n" +
                "        url: \"http://" + ipAddress + ":8280/statistics/\",\n" +
                "        type: \"GET\",\n" +
                "        headers: {\n" +
                "            \"trackingCode\":tracking_code,\n" +
                "        }\n" +
                "     \n" +
                "    });\n" +
                "}";
    }

    public void buildInvokeStaticsJavascriptFile(String filePath) throws IOException {
        File file = new File(filePath + "/invokeStatistcs.js");
        if (file.exists()) {
            file.delete();
        }
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        output.write(jsFunction);
        output.close();
    }
}
