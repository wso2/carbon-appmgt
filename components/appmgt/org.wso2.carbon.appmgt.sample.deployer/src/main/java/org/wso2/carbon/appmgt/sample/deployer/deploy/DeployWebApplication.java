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

package org.wso2.carbon.appmgt.sample.deployer.deploy;

import org.apache.log4j.Logger;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class is use to deploy war file in server
 * */
public class DeployWebApplication {

    final static Logger log = Logger.getLogger(DeployWebApplication.class.getName());
    private static String homePath = CarbonUtils.getCarbonHome();
    private static final int  BUFFER_SIZE = 4096;

    /**
     * This method is use deploy war file according to given name
     *
     * @param warFileName
     *            Name of the war file that need to be deploy
     *
     * @throws AppManagementException
     *            Throws this when failed to deploy web application
     */
    public void copyFileUsingFileStreams(String warFileName) throws AppManagementException {
        homePath = CarbonUtils.getCarbonHome();
        File warFile = new File(homePath + "/samples/" + warFileName+".war");
        log.info(warFile.getAbsolutePath());
        File outputFolder = new File(homePath + "/repository/deployment/server/webapps/" + warFileName);
        if(!outputFolder.exists()){
            try {
                ZipInputStream zin = new ZipInputStream(new FileInputStream(warFile));
                ZipEntry entry;
                String name, dir;
                while ((entry = zin.getNextEntry()) != null) {
                    name = entry.getName();
                    if (entry.isDirectory()) {
                        mkdirs(outputFolder, name);
                        continue;
                    }
                /* this part is necessary because file entry can come before
                  * directory entry where is file located
                  * i.e.:
                  *   /foo/foo.txt
                  *   /foo/
                */
                    dir = dirpart(name);
                    if (dir != null)
                        mkdirs(outputFolder, dir);
                    extractFile(zin, outputFolder, name);
                }
                zin.close();
            } catch (IOException e) {
                log.error("Error while deploying a "+warFileName+".war", e);
                throw  new AppManagementException("Error while deploying a "+warFileName+".war", e);
            }
        }

    }

    /**
     * This method is use extract war file in given location
     *
     * @param warFile
     *            Name of the war file that need to be deploy
     *
     * @param outdir
     *            tomcat server path
     *
     * @param outdir
     *            name of the war file extracting folder
     */
    private static void extractFile(ZipInputStream warFile, File outdir, String name) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outdir,name)));
        int count = -1;
        while ((count = warFile.read(buffer)) != -1)
            out.write(buffer, 0, count);
        out.close();
    }

    /**
     * This method is use create the path to extract a war file if the given path is not exist
     *
     * @param outdir
     *            Name of the war file that need to be deploy
     *
     * @param path
     *            tomcat server path
     *
     */
    private static void mkdirs(File outdir,String path)
    {
        File d = new File(outdir, path);
        if( !d.exists() )
            d.mkdirs();
    }

    /**
     * This method is use to get the folder name
     *
     * @param name
     *          folder name with file seperator
     */
    private static String dirpart(String name)
    {
        int s = name.lastIndexOf( File.separatorChar );
        return s == -1 ? null : name.substring( 0, s );
    }


}

