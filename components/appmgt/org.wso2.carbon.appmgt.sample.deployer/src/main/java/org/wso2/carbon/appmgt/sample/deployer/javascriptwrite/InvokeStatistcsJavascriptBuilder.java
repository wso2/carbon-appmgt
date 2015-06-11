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

import java.io.*;

/**
 * Write tracking id and url in to invokeStatistcs.js
 */
public class InvokeStatistcsJavascriptBuilder {
    final static Logger log = Logger.getLogger(InvokeStatistcsJavascriptBuilder.class.getName());
    private String trackingID;
    private String ipAddress;
    private String gatewayPort;

    /**
     * Creates a new InvokeStatistcsJavascriptBuilder object and build content for invokeStatistcs.js
     *
     * @param trackingID  trackingID of the web application
     * @param ipAddress   ipAddress of the user
     * @param gatewayPort gateway port of the server
     */
    public InvokeStatistcsJavascriptBuilder(String trackingID, String ipAddress, String gatewayPort) {
        this.trackingID = trackingID;
        this.ipAddress = ipAddress;
        this.gatewayPort = gatewayPort;
    }

    /**
     * This method is used to write a java script file with tracking id in web application
     *
     * @param filePath file pathe of the web application
     * @throws IOException          Throws this when failed to write java script file
     * @throws InterruptedException Throws this when thread failed to sleep
     */
    public void buildInvokeStaticsJavascriptFile(String filePath) throws IOException, InterruptedException {
        BufferedWriter output = null;
        try {
            File file = new File(filePath + "/invokeStatistcs.js");
            String[] lines = appendTrackingCode(file);
            StringBuilder stringBuilder = new StringBuilder();
            int i = 0;
            for (String line : lines) {
                stringBuilder.append(line);
                if (i != lines.length - 1) {
                    stringBuilder.append("\n");
                }
                i++;
            }
            output = new BufferedWriter(new FileWriter(file));
            output.write(stringBuilder.toString());
        } finally {
            if (output != null) {
                output.close();
            }
        }

    }

    /**
     * This method is used to update tracking id and url in invokeStatistcs.js
     *
     * @param file invokeStatistcs.js file
     * @throws IOException Throws this when failed to write java script file
     */
    private String[] appendTrackingCode(File file) throws IOException {
        FileReader reader = null;
        try {
            String content = null;
            reader = new FileReader(file);
            char[] chars = new char[(int) file.length()];
            reader.read(chars);
            content = new String(chars);
            String[] lines = content.split("\n");
            String trackingCodes = lines[1].split("=")[1];
            String trackingCode = trackingCodes.substring(0, (trackingCodes.length() - 2));
            trackingCode = trackingCode.concat("," + this.trackingID + "\";");
            lines[1] = lines[1].split("=")[0] + "=" + trackingCode;
            lines[3] = lines[3].split(":")[0] + ":" + "\"http://" + this.ipAddress + ":" + gatewayPort + "/statistics/\",";
            return lines;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}