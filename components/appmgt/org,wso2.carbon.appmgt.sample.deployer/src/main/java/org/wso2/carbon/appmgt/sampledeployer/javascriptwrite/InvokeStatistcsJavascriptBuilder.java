package org.wso2.carbon.appmgt.sampledeployer.javascriptwrite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ushan on 3/13/15.
 */
public class InvokeStatistcsJavascriptBuilder {
    private String jsFunction;
    public  InvokeStatistcsJavascriptBuilder(String trackingID,String ipAddress){
        jsFunction = "function invokeStatistics(){\n" +
                "       var tracking_code = \""+trackingID+"\";\n" +
                "        var request = $.ajax({\n" +
                "        url: \"http://"+ipAddress+":8280/statistics/\",\n" +
                "        type: \"GET\",\n" +
                "        headers: {\n" +
                "            \"trackingCode\":tracking_code,\n" +
                "        }\n" +
                "     \n" +
                "    });\n" +
                "}";
    }

    public void buildInvokeStaticsJavascriptFile(String filePath) throws IOException {
        File file = new File(filePath+"/invokeStatistcs.js");
        if(file.exists()){
            file.delete();
        }
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        output.write(jsFunction);
        output.close();
    }
}
