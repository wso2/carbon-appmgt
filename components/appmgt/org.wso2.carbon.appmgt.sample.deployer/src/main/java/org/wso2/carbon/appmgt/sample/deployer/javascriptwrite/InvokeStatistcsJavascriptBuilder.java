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

package org.wso2.carbon.appmgt.sample.deployer.javascriptwrite;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class InvokeStatistcsJavascriptBuilder {
    private String jsFunction;
    final static Logger log = Logger.getLogger(InvokeStatistcsJavascriptBuilder.class.getName());

    /**
     * Creates a new InvokeStatistcsJavascriptBuilder object and build content for invokeStatistcs.js
     *
     * @param trackingID
     *            - trackingID of the web application
     *
     * @param ipAddress
     *            - ipAddress of the user
     *
     * @param gatewayPort
     *            - gateway port of the server
     *
     */
    public InvokeStatistcsJavascriptBuilder(String trackingID, String ipAddress,String gatewayPort) {
        jsFunction = "function invokeStatistics(){\n" +
                "       var tracking_code = \"" + trackingID + "\";\n" +
                "        var request = $.ajax({\n" +
                "        url: \"http://" + ipAddress + ":"+gatewayPort+"/statistics/\",\n" +
                "        type: \"GET\",\n" +
                "        headers: {\n" +
                "            \"trackingCode\":tracking_code,\n" +
                "        }\n" +
                "     \n" +
                "    });\n" +
                "}";
    }

    /**
     * This method is used to write a java script file with tracking id in web application
     *
     * @param filePath
     *          - file pathe of the web application
     *
     * @throws IOException
     *             - Throws this when failed to write java script file
     *
     * @throws InterruptedException
     *             - Throws this when thread failed to sleep
     */
    public void buildInvokeStaticsJavascriptFile(String filePath) throws IOException, InterruptedException {
        File file = new File(filePath + "/invokeStatistcs.js");
        if (file.exists()) {
            file.delete();
        }
        Thread.sleep(5000);
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        output.write(jsFunction);
        output.close();
    }
}
