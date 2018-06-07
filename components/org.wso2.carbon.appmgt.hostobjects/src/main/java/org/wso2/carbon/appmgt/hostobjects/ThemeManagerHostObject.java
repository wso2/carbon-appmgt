/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
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
package org.wso2.carbon.appmgt.hostobjects;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.json.simple.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.UserPortalTheme;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.utils.FileUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class has methods to add,remove custom themes.
 * <p/>
 * Default theme (user-portal/themes/<themeName>) will be overriden in user-portal/themes/<tenantDomain>/themes/custom.
 * <p/>
 * Only whitelisted files will be allowed to uploaded with custom theme. Whitelisted file extension are configured in
 * admin/conf/site.json
 */
public class ThemeManagerHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(ThemeManagerHostObject.class);
    public final static String USER_PORTAL_THEME_FILE_EXTENSION = ".zip";
    private static AppMDAO appMDAO = new AppMDAO();

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
     * Add the custom theme for given tenant.
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
        if (args == null || args.length != 5) {
            handleException("Invalid input parameters for addTenantTheme");
        }

        FileHostObject uploadFile = (FileHostObject) args[0];
        String tenantDomain = (String) args[1];
        NativeArray fileExtensions = (NativeArray) args[2];
        String themeName = (String) args[3];
        String description = (String) args[4];

        if (log.isDebugEnabled()) {
            String msg = String.format("Add Custom theme for tenant : %s", tenantDomain);
            log.debug(msg);
        }

        Set<String> whitelistedExt = new HashSet<String>();
        for (Object ext : fileExtensions) {
            whitelistedExt.add((String) ext);
        }
        //extract the zip file to user-portal directory
        deployCustomTheme(uploadFile, tenantDomain, whitelistedExt);
        addUserPortalThemeInfo(tenantDomain, themeName, description);

        return true;
    }

    /**
     * Download the custom theme for given tenant.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return theme file location
     * @throws AppManagementException
     */
    public static String jsFunction_downloadCustomTheme(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws AppManagementException {
        if (args == null || args.length != 1) {
            handleException("Invalid input parameters for downloadTenantTheme");
        }

        String tenantDomain = (String) args[0];

        if (log.isDebugEnabled()) {
            String msg = String.format("Download Custom theme for tenant : %s", tenantDomain);
            log.debug(msg);
        }

        return downloadCustomTheme(tenantDomain);
    }

    /**
     * Get the custom themes information deployed in the given tenant.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return theme information.
     * @throws AppManagementException
     */
    public static JSONObject jsFunction_getDeployedThemeInfo(Context cx, Scriptable thisObj, Object[] args,
                                                             Function funObj)
            throws AppManagementException {

        if (args == null || args.length != 1) {
            handleException(
                    "Invalid input parameters for getDeployedThemes.Expected parameters : tenantDomain");
        }
        String tenantDomain = (String) args[0];
        JSONObject themeInfo = new JSONObject();

        if (log.isDebugEnabled()) {
            String msg = String.format("Check deployed themes for tenant :%s", tenantDomain);
            log.debug(msg);
        }
        // check tenant theme dir exists
        Path path = getTenantThemePath(tenantDomain);
        if (!Files.exists(path)) {
            return themeInfo;
        }

        // check custom default theme
        path = getCustomThemePath(tenantDomain);
        UserPortalTheme userPortalTheme = getUserPortalTheme(tenantDomain);
        if (isThemeExists(path)) {
            themeInfo.put("themeName", userPortalTheme.getName());
            themeInfo.put("themeDescription", userPortalTheme.getDescription());
        }

        return themeInfo;
    }

    /**
     * Remove deployed custom theme for the given tenant.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return true if success
     * @throws AppManagementException
     */
    public static void jsFunction_deleteCustomThemeTempDirectory(Context cx, Scriptable thisObj, Object[] args,
                                                                 Function funObj)
            throws AppManagementException {
        if (args == null || args.length != 1) {
            handleException(
                    "Invalid input parameters for deleteCustomThemeTempDirectory. Expected parameters : tenantDomain");
        }
        String customThemeFilePath = (String) args[0];
        deleteCustomThemeTempDirectory(customThemeFilePath);
    }

    /**
     * Remove deployed custom theme for the given tenant.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return true if success
     * @throws AppManagementException
     */
    public static void jsFunction_deleteCustomTheme(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws AppManagementException {
        if (args == null || args.length != 1) {
            handleException(
                    "Invalid input parameters for deleteCustomTheme. Expected parameters : tenantDomain");
        }
        String tenantDomain = (String) args[0];
        deleteUserPortalThemeInfo(tenantDomain);
        deleteThemeDirectory(tenantDomain);
    }

    /**
     * Delete the theme directory in the file system based on given theme type.
     *
     * @param tenantDomain Tenant Domain
     * @return true if success.
     * @throws AppManagementException
     */
    private static void deleteThemeDirectory(String tenantDomain) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Delete custom theme of tenant: %s", tenantDomain);
            log.debug(msg);
        }
        // user-portal/themes/<tenantDomain>
        Path tenantThemePath = getTenantThemePath(tenantDomain);

        if (!Files.exists(tenantThemePath)) {
            //no tenant theme directory found
            String msg = String.format("Tenant theme directory could not be found : %s, while un-deploying the " +
                    "theme", tenantThemePath.toString());
            log.warn(msg);
        }

        if (log.isDebugEnabled()) {
            String msg = String.format("Deleting directory : %s", tenantThemePath.toString());
            log.debug(msg);
        }

        try {
            FileUtils.deleteDirectory(tenantThemePath.toFile());
        } catch (IOException e) {
            handleException("Could not delete directory :" + tenantThemePath.toString(), e);
        }
    }

    /**
     * Check given theme exists.
     *
     * @param path File Path
     * @return true if exists else false
     */
    private static boolean isThemeExists(Path path) {
        boolean isExists = Files.exists(path);
        if (log.isDebugEnabled()) {
            String msg = "Custom theme found status : " + isExists + " in path : " + path.toString();
            log.debug(msg);
        }
        return isExists;
    }

    /**
     * Deploy the uploaded custom theme to the correct custom theme directory. Files with unsupported extensions will be
     * omitted.
     *
     * @param themeFile      Theme File in zip format
     * @param tenantDomain   Tenant Domain
     * @param whitelistedExt Whitelisted file extensions
     * @throws AppManagementException
     */
    private static void deployCustomTheme(FileHostObject themeFile, String tenantDomain, Set<String> whitelistedExt) throws AppManagementException {

        if (log.isDebugEnabled()) {
            String msg = String.format("Deploy custom theme of type for tenant :%s", tenantDomain);
            log.debug(msg);
        }

        ZipInputStream zis = null;
        byte[] buffer = new byte[1024];

        //check user-portal theme directory exists

        Path themeDir = getUserPortalThemePath();
        if (!Files.exists(themeDir)) {
            String msg = "Could not found directory :" + themeDir.toString();
            handleException(msg);
        }

        Path themePath = getCustomThemePath(tenantDomain);
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
            if (!Files.exists(themePath)) {
                createDirectory(themePath);
            }

            if (log.isDebugEnabled()) {
                String msg = "Get zip file content and deploy";
                log.debug(msg);
            }
            //get the zip file content
            zis = new ZipInputStream(zipInputStream);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            String ext;

            while (ze != null) {
                String fileName = ze.getName();
                String intendedDir = themePath.toString();
                Path newFilePath = Paths.get(validateFilename(fileName, intendedDir));
                if (ze.isDirectory()) {
                    if (!Files.exists(newFilePath)) {
                        createDirectory(newFilePath);
                    }
                } else {
                    ext = FilenameUtils.getExtension(ze.getName());
                    if (whitelistedExt.contains(ext)) {
                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        Path parentDir = newFilePath.getParent();
                        if (!Files.exists(parentDir)) {
                            createDirectory(parentDir);
                        }
                        FileOutputStream fos = new FileOutputStream(newFilePath.toFile());

                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }

                        fos.close();
                    } else {
                        String msg = String.format(
                                "Unsupported file is uploaded with custom theme by tenant %1s. File : %2s ",
                                tenantDomain, ze.getName());
                        log.warn(msg);
                    }

                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            handleException("Failed to deploy custom theme", e);
        } finally {
            IOUtils.closeQuietly(zis);
            IOUtils.closeQuietly(zipInputStream);
        }
    }

    private static String validateFilename(String filename, String intendedDir)
            throws IOException {

        String appendedFilePath = Paths.get(intendedDir, filename).toString();
        File file = new File(appendedFilePath);
        String systemDependentFilePath = file.getCanonicalPath();

        File intendedDirectory = new File(intendedDir);
        String systemDependentIntendedDirectoryPath = intendedDirectory.getCanonicalPath();

        if (systemDependentFilePath.startsWith(systemDependentIntendedDirectoryPath)) {
            return systemDependentFilePath;
        } else {
            throw new IllegalStateException("File: " + filename + " is outside extraction target directory.");
        }
    }

    private static void createDirectory(Path directoryPath) throws AppManagementException {
        try {
            Files.createDirectories(directoryPath);
        } catch (FileAlreadyExistsException e) {
            handleException("Cannot create directory '" + directoryPath +
                                    "' as a file already exists in the same path.", e);
        } catch (IOException e) {
            handleException(
                    "An error occurred when creating directory '" + directoryPath + "'.", e);
        }
    }

    /**
     * Generate user portal theme temp zip file.
     * This will be created in the default temp directory for the downloadable purpose and it will be deleted after
     * the completion of download.
     *
     * @param tenantDomain
     * @return
     */
    public static String downloadCustomTheme(String tenantDomain) throws AppManagementException {
        String themeName = "";
        Path tempDirectory = null;

        try {
            tempDirectory = Files.createTempDirectory("IS_User_Portal_Theme_");
            tempDirectory.toFile().deleteOnExit();
            UserPortalTheme userPortalTheme = getUserPortalTheme(tenantDomain);
            themeName = userPortalTheme.getName();
            FileUtil downloadUtil = new FileUtil();

            downloadUtil.copyFiles(Paths.get(CarbonUtils.getCarbonHome(), getUserPortalThemePathPerTenant
                    (tenantDomain).toString()).toString(), Paths.get(tempDirectory.toString(), themeName).toString());

            downloadUtil.zipFiles(Paths.get(tempDirectory.toString(), themeName), Paths.get(tempDirectory
                    .toString(), themeName + USER_PORTAL_THEME_FILE_EXTENSION).toString());

            downloadUtil.deleteDirectory(Paths.get(tempDirectory.toString(), themeName).toString());
        } catch (IOException e) {
            handleException("Error occurred while creating theme zip file for the tenant: " + tenantDomain, e);
        }

        return Paths.get(tempDirectory.toString(), themeName + USER_PORTAL_THEME_FILE_EXTENSION).toString();
    }


    /**
     * Delete user portal theme temp directory
     *
     * @param customThemeFilePath
     * @return
     */
    public static void deleteCustomThemeTempDirectory(String customThemeFilePath) throws AppManagementException {

        File customThemeFile = new File(customThemeFilePath);
        String parentDir = customThemeFile.getParent();

        try {
            FileUtil downloadUtil = new FileUtil();
            downloadUtil.deleteDirectory(parentDir);
        } catch (IOException e) {
            handleException("Error occurred while deleting temp theme directory: " + parentDir, e);
        }
    }

    /**
     * Construct and return the default theme path of user-portal. [/repository/deployment/server/jaggeryapps/user-portal/themes]
     *
     * @return path
     */
    private static Path getUserPortalThemePath() {
        return Paths.get("repository", "deployment", "server", "jaggeryapps", "user-portal", "themes");
    }

    /**
     * Construct and return the custom theme path of tenant.
     * [/repository/deployment/server/jaggeryapps/user-portal/themes/<tenantDomain>]
     *
     * @return path
     */
    private static Path getTenantThemePath(String tenantDomain) {
        return getUserPortalThemePath().resolve(tenantDomain);
    }

    /**
     * Construct and return the custom tenant theme path.
     *
     * @param tenantDomain Tenant Domain
     * @return path
     */
    private static Path getCustomThemePath(String tenantDomain) {
        Path path = getTenantThemePath(tenantDomain);
        return path.resolve(Paths.get("themes", "custom"));
    }

    /**
     * Get user portal custom theme path for given tenant.
     *
     * @param tenantDomain
     * @return user portal custom theme path
     */
    public static Path getUserPortalThemePathPerTenant(String tenantDomain) {
        return Paths.get("repository", "deployment", "server", "jaggeryapps", "user-portal", "themes", tenantDomain,
                "themes", "custom");
    }

    /**
     * Add user-portal theme information for given tenant.
     *
     * @param tenantDomain Tenant Domain
     * @param name         Theme name
     * @param description  Theme description
     * @throws AppManagementException
     */
    public static void addUserPortalThemeInfo(String tenantDomain, String name, String description) throws AppManagementException {
        appMDAO.addUserPortalTheme(tenantDomain, name, description);
    }

    /**
     * Remove user-portal theme information for given tenant.
     *
     * @param tenantDomain Tenant Domain
     * @throws AppManagementException
     */
    public static void deleteUserPortalThemeInfo(String tenantDomain) throws AppManagementException {
        appMDAO.deleteUserPortalTheme(tenantDomain);
    }

    /**
     * Get user-portal theme for a given tenant.
     *
     * @param tenantDomain
     * @return user-portal theme description
     * @throws AppManagementException
     */
    public static UserPortalTheme getUserPortalTheme(String tenantDomain) throws AppManagementException {
        return appMDAO.getUserPortalTheme(tenantDomain);
    }

}
