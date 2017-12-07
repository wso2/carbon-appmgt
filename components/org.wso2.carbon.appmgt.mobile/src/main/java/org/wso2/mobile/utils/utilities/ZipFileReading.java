/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.mobile.utils.utilities;

import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.NSDictionary;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileReading {
	
	//ios CF Bundle keys
	public static final String IPA_BUNDLE_VERSION_KEY = "CFBundleVersion";
	public static final String IPA_BUNDLE_NAME_KEY = "CFBundleName";
	public static final String IPA_BUNDLE_IDENTIFIER_KEY = "CFBundleIdentifier";
	
	//Android attributes
	public static final String APK_VERSION_KEY = "versionName";
	public static final String APK_PACKAGE_KEY = "package";
	
    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public String readAndroidManifestFile(String filePath) {
        String xml = "";
        try {
            ApkFile apkFile = new ApkFile(new File(filePath));
            ApkMeta apkMeta = apkFile.getApkMeta();
            ;
            JSONObject obj = new JSONObject();
            obj.put("version", apkMeta.getVersionName());
            obj.put("package", apkMeta.getPackageName());
            xml = obj.toJSONString();
        } catch (IOException e) {
            xml = "Exception occurred " + e;
        }
        return xml;
    }

    public String readiOSManifestFile(String filePath, String name) {
        String plist = "";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            File file = new File(filePath);
            ZipInputStream stream = new ZipInputStream(
                    new FileInputStream(file));
            try {
                ZipEntry entry;
                while ((entry = stream.getNextEntry()) != null) {
                   	if (entry.getName().matches("^(Payload/)(.)+(.app/Info.plist)$")) {
                        InputStream is = stream;

                        int nRead;
                        byte[] data = new byte[16384];

                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }

                        buffer.flush();
                        break;
                    }
                }
                NSDictionary rootDict = (NSDictionary) BinaryPropertyListParser
                        .parse(buffer.toByteArray());
                JSONObject obj = new JSONObject();
                obj.put("version", rootDict.objectForKey(IPA_BUNDLE_VERSION_KEY).toString());
                obj.put("name", rootDict.objectForKey(IPA_BUNDLE_NAME_KEY).toString());
                obj.put("package",
                        rootDict.objectForKey(IPA_BUNDLE_IDENTIFIER_KEY).toString());
                plist = obj.toJSONString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            plist = "Exception occured " + e;
            e.printStackTrace();
        }
        return plist;
    }
}
