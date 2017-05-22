/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.impl.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class to file operations such as create, delete, zip etc..
 */
public class FileUtil {

    private static Log log = LogFactory.getLog(FileUtil.class);
    private Path userPortalThemeTempFolder;
    private List<String> fileList = new ArrayList<>();

    /**
     * Delete temporary created directory.
     *
     * @param dirPath
     * @throws IOException
     */
    public void deleteDirectory(String dirPath) throws IOException {
        File directory = new File(dirPath);
        FileUtils.deleteDirectory(directory);
    }

    /**
     * Copy files.
     *
     * @param sourcePath
     * @param destinationPath
     * @throws IOException
     */
    public void copyFiles(String sourcePath, String destinationPath) throws IOException {
        File source = new File(sourcePath);
        File destination = new File(destinationPath);
        FileUtils.copyDirectory(source, destination);
    }

    /**
     * Zip files in a given directory.
     *
     * @param srcFolder
     * @param destinationZipFile
     * @throws IOException
     */
    public void zipFiles(Path srcFolder, String destinationZipFile) throws IOException {
        this.userPortalThemeTempFolder = srcFolder;
        generateFileList(srcFolder.toFile());
        createZip(destinationZipFile);
    }

    /**
     * Generate the zip file.
     *
     * @param destinationZipFile
     * @throws IOException
     */
    public void createZip(String destinationZipFile) throws IOException {

        byte[] buffer = new byte[1024];

        FileOutputStream fos = new FileOutputStream(destinationZipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        if (log.isDebugEnabled()) {
            String msg = String.format("User portal theme temp zip file path: %s", destinationZipFile);
            log.debug(msg);
        }

        for (String file : fileList) {

            if (log.isDebugEnabled()) {
                String msg = String.format("File Added to the zip: %s", file);
                log.debug(msg);
            }
            ZipEntry ze = new ZipEntry(file);
            zos.putNextEntry(ze);

            FileInputStream in =
                    new FileInputStream(userPortalThemeTempFolder.toString() + File.separator + file);

            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            in.close();
        }
        zos.closeEntry();
        zos.close();
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     *
     * @param srcFolder file or directory
     */
    public void generateFileList(File srcFolder) {

        //add file only
        if (srcFolder.isFile()) {
            fileList.add(generateZipEntry(srcFolder.getAbsoluteFile().toString()));
        }

        if (srcFolder.isDirectory()) {
            String[] subNote = srcFolder.list();
            for (String filename : subNote) {
                generateFileList(new File(srcFolder, filename));
            }
        }
    }

    /**
     * Format the file path for zip.
     *
     * @param file
     * @return
     */
    private String generateZipEntry(String file) {
        return file.substring(userPortalThemeTempFolder.toString().length() + 1, file.length());
    }

}
