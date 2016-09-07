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
package org.wso2.carbon.appmgt.hostobjects;


import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.appmgt.api.AppManagementException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

/**
 * This class has methods to add,remove custom themes.
 * <p/>
 * Default theme (store/themes/<themeName>) will be overriden in store/themes/<tenantDomain>/themes/custom.
 * <p/>
 * Extension level theme (store/extensions/assets/<assetType>/themes/<themeName>) will be overriden in
 * store/themes/<tenantDomain>/extensions/assets/<assetType>/themes/custom. Theme type is equal to asset type will be
 * same  extension level themes.
 * <p/>
 * Only whitelisted files will be allowed to uploaded with custom theme. Whitelisted file extension are configured in
 * admin-dasboard/site/config/site.json
 */
public class ThemeManagerHostObject extends ScriptableObject {
    private static final Log log = LogFactory.getLog(ThemeManagerHostObject.class);
    private static final String DEFAULT = "default";

    /**
     * Construct and return the default theme path of store.
     * [/repository/deployment/server/jaggeryapps/store/themes]
     *
     * @return path
     */
    private static String getStoreThemePath() {
        StringBuilder path = new StringBuilder("repository").append(File.separator).append("deployment").append(
                File.separator).append("server").append(File.separator).append("jaggeryapps").append(File.separator)
                .append("store").append(File.separator).append("themes");
        return path.toString();
    }

    /**
     * Construct and return the custom theme path of tenant.
     * [/repository/deployment/server/jaggeryapps/store/themes/<tenantDomain>]
     *
     * @return path
     */
    private static String getTenantThemePath(String tenant) {
        StringBuilder path = new StringBuilder(getStoreThemePath());
        path.append(File.separator).append(tenant);
        return path.toString();
    }

    /**
     * Construct and return the custom tenant theme extensions path.
     * [/repository/deployment/server/jaggeryapps/store/themes/<tenantDomain>/extensions/assets]
     *
     * @return path
     */
    private static String getExtThemePath(String tenantDomain) {
        StringBuilder path = new StringBuilder(getTenantThemePath(tenantDomain));
        path.append(File.separator).append("extensions").append(File.separator)
                .append("assets");
        return path.toString();
    }

    /**
     * Construct and return the custom tenant theme path.
     *
     * @param tenantDomain Tenant Domain
     * @param themeType    Theme Type (Default ,<assetType> e.g webapp)
     * @return path
     */
    private static String getCustomThemePath(String tenantDomain, String themeType) {
        StringBuilder path = new StringBuilder(getTenantThemePath(tenantDomain));
        if (DEFAULT.equals(themeType)) {
            path.append(File.separator).append("themes").append(File.separator)
                    .append("custom");
        } else {
            path.append(File.separator).append("extensions").append(File.separator)
                    .append("assets").append(File.separator).append(themeType).append(File.separator).append("themes")
                    .append(File.separator).append("custom");
        }
        return path.toString();
    }


    @Override
    public String getClassName() {
        return "ThemeManager";
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException, AppManagementException {
        return new ThemeManagerHostObject();
    }

    private static void handleException(String msg) throws AppManagementException {
        log.error(msg);
        throw new AppManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws AppManagementException {
        log.error(msg, t);
        throw new AppManagementException(msg, t);
    }

    /**
     * Add the custom theme for given tenant based on the theme type(eg. default,webapp,site).
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return true if success
     * @throws AppManagementException
     */
    public static boolean jsFunction_addCustomTheme(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws AppManagementException {
        if (args == null || args.length != 4) {
            handleException("Invalid input parameters for addTenantTheme");
        }

        FileHostObject uploadFile = (FileHostObject) args[0];
        String tenant = (String) args[1];
        String themeType = (String) args[2];
        NativeArray fileExtensions = (NativeArray) args[3];

        if (log.isDebugEnabled()) {
            String msg = String.format("Add Custom theme : %1s for tenant : %2s", themeType, tenant);
            log.debug(msg);
        }

        Set<String> whitelistedExt = new HashSet<String>();
        for (Object ext : fileExtensions) {
            whitelistedExt.add((String) ext);
        }
        //extract the zip file to store directory
        deployCustomTheme(uploadFile, tenant, themeType, whitelistedExt);

        return true;
    }

    /**
     * Get the list of custom themes(e.g default,webapp,site ) deployed to given tenant.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return Custom Theme Types.
     * @throws AppManagementException
     */
    public static NativeObject jsFunction_getDeployedThemes(Context cx, Scriptable thisObj, Object[] args,
                                                            Function funObj)
            throws AppManagementException {

        if (args == null || args.length != 2) {
            handleException(
                    "Invalid input parameters for getDeployedThemes.Expected parameters : tenantDomain,themeTypes");
        }
        String tenantDomain = (String) args[0];
        NativeArray themeTypes = (NativeArray) args[1];
        NativeObject themes = new NativeObject();

        if (log.isDebugEnabled()) {
            String msg = String.format("Get deployed themes for tenant :%s", tenantDomain);
            log.debug(msg);
        }
        //check tenant theme dir exists
        String path = getTenantThemePath(tenantDomain);
        File dir = new File(path);
        if (!dir.exists()) {
            return themes;
        }

        for (Object themeType : themeTypes) {
            String theme = (String) themeType;
            path = getCustomThemePath(tenantDomain, (String) themeType);
            themes.put(theme, themes, isThemeExists(path));
        }
        return themes;
    }

    /**
     * Remove given deployed custom them for given tenant.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return true if success
     * @throws AppManagementException
     */
    public static boolean jsFunction_deleteCustomTheme(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws AppManagementException {
        if (args == null || args.length != 2) {
            handleException(
                    "Invalid input parameters for getDeployedThemes.Expected parameters : tenantDomain,themeType");
        }
        String tenantDomain = (String) args[0];
        String themeType = (String) args[1];
        return undeployTheme(tenantDomain, themeType);

    }

    /**
     * Delete the theme directory in the file system based on given theme type.
     *
     * @param tenantDomain Tenant Domain
     * @param themeType    Theme Type (Default ,<assetType> e.g webapp)
     * @return true if success.
     * @throws AppManagementException
     */
    private static boolean undeployTheme(String tenantDomain, String themeType) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Delete custom theme:%1s of tenant : %2s", themeType, tenantDomain);
            log.debug(msg);
        }
        // store/themes/<tenantDomain>
        String tenantThemePath = getTenantThemePath(tenantDomain);
        // store/themes/<tenantDomain>/extensions/assets
        String extAssetsPath = getExtThemePath(tenantDomain);
        // store/themes/<tenantDomain>/themes
        String defaultThemePath = tenantThemePath + File.separator + "themes";
        // store/themes/<tenantDomain>/extensions
        String extPath = tenantThemePath + File.separator + "extensions";

        File dir = new File(tenantThemePath);
        if (!dir.exists()) {
            //no tenant theme directory found
            if (log.isDebugEnabled()) {
                String msg = String.format("Tenant theme directory does not exist");
                log.debug(msg);
            }
            return true;
        }

        if (DEFAULT.equals(themeType)) {
            deleteDir(defaultThemePath);
            //if custom extension theme path does not exist
            //then  delete tenant theme dir
            dir = new File(extPath);
            if (!dir.exists()) {
                deleteDir(tenantThemePath);
            }
        } else {
            // store/themes/<tenantDomain>/extensions/assets/<assetType>
            String path = extAssetsPath + File.separator + themeType;
            deleteDir(path);
            //if there are no any custom asset themes
            // then delete the extensions dir
            dir = new File(extAssetsPath);
            if (dir.list().length == 0) {
                deleteDir(extPath);
                //if custom default theme is not exists
                //then delete tenand theme dir
                dir = new File(defaultThemePath);
                if (!dir.exists()) {
                    deleteDir(tenantThemePath);
                }
            }
        }
        return true;
    }

    /**
     * Delete a directory recursively in file system.
     *
     * @throws AppManagementException
     */
    private static void deleteDir(String path) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Delete directory : %s", path);
            log.debug(msg);
        }
        File dir = new File(path);
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            handleException("Could not delete directory :" + dir.getPath());
        }
    }

    /**
     * Check given theme exists.
     *
     * @param path File Path
     * @return true if exists else false
     */
    private static boolean isThemeExists(String path) {
        File dir = new File(path);
        if (log.isDebugEnabled()) {
            String msg = "Custom theme found status : " + dir.exists() + " in path : " + path;
            log.debug(msg);
        }
        return dir.exists();
    }

    /**
     * Deploy the uploaded custom theme to the correct custom theme directory. Files with unsupported extensions will be
     * omitted.
     *
     * @param themeFile      Theme File in zip format
     * @param tenant         Tenant Domain
     * @param themeType      Theme Type (Default ,<assetType> e.g webapp)
     * @param whitelistedExt Whitelisted file extensions
     * @throws AppManagementException
     */
    private static void deployCustomTheme(FileHostObject themeFile, String tenant, String themeType,
                                          Set<String> whitelistedExt) throws AppManagementException {

        if (log.isDebugEnabled()) {
            String msg = String.format("Deploy custom theme of type :%1s for tenant :%2s", themeType, tenant);
            log.debug(msg);
        }
        ZipInputStream zis = null;
        byte[] buffer = new byte[1024];

        //check store theme directory exists
        File themeDir = new File(getStoreThemePath());
        if (!themeDir.exists()) {
            String msg = "Could not found directory :" + themeDir;
            handleException(msg);
        }

        String themePath = null;
        themePath = getCustomThemePath(tenant, themeType);
        InputStream zipInputStream = null;
        try {
            zipInputStream = themeFile.getInputStream();
        } catch (ScriptException e) {
            handleException("Error occurred while deploying custom theme file", e);
        }

        try {
            if (log.isDebugEnabled()) {
                String msg = String.format("Create custom theme dir :%s", themePath);
                log.debug(msg);
            }
            //create output directory if it is not exists
            File dir = new File(themePath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    handleException("Unable to create tenant custom directory :" + dir);
                }
            }

            if (log.isDebugEnabled()) {
                String msg = "Get zip file content and deploy";
                log.debug(msg);
            }
            //get the zip file content
            zis = new ZipInputStream(zipInputStream);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            String ext = null;

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(dir + File.separator + fileName);
                if (ze.isDirectory()) {
                    if (!newFile.exists()) {
                        boolean status = newFile.mkdir();
                        if (!status) {
                            handleException("Could not create directory :" + newFile.getPath());
                        }
                    }
                } else {
                    ext = FilenameUtils.getExtension(ze.getName());
                    if (whitelistedExt.contains(ext)) {
                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        File parentDir = new File(newFile.getParent());
                        parentDir.mkdirs();
                        FileOutputStream fos = new FileOutputStream(newFile);

                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }

                        fos.close();
                    } else {
                        String msg = String.format(
                                "Unsupported file is uploaded with custom theme by tenant %1s.File : %2s ",
                                tenant, ze.getName());
                        log.warn(msg);
                    }

                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            handleException("Failed to deploy Custom theme", e);
        } finally {
            IOUtils.closeQuietly(zis);
            IOUtils.closeQuietly(zipInputStream);
        }
    }

}
