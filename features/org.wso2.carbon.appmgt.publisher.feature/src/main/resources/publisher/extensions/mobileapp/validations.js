/**
 * @license
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Indicates different validation statues.
 * @enum {number}
 */
var ValidationStatus = {
    VALID: 1,
    INVALID: {
        EMPTY: -1,
        FORMAT: -2,
        LENGTH: {
            MAX: -3,
            MIN: -4
        },
        DATA_TYPE: -5,
        VALUE: -6
    },
    UNKNOWN: 0
};

/**
 * Input fields for a mobile app.
 * @enum {string}
 */
var Field = {
    OVERVIEW_PROVIDER: 'overview.provider',
    OVERVIEW_PLATFORM: 'overview.platform',
    OVERVIEW_TYPE: 'overview.type',
    OVERVIEW_PACKAGE_NAME: 'overview.packagename',
    OVERVIEW_APP_ID: 'overview.appid',
    OVERVIEW_PREVIOUS_VERSION_APP_ID: 'overview.previousVersionAppId',
    OVERVIEW_URL: 'overview.url',
    OVERVIEW_FILE: 'overview.file',
    OVERVIEW_NAME: 'overview.name',
    OVERVIEW_DISPLAY_NAME: 'overview.displayName',
    OVERVIEW_VERSION: 'overview.version',
    OVERVIEW_BUNDLE_VERSION: 'overview.bundleversion',
    OVERVIEW_DESCRIPTION: 'overview.description',
    OVERVIEW_RECENT_CHANGES: 'overview.recentchanges',
    OVERVIEW_STATUS: 'overview.status',
    OVERVIEW_CATEGORY: 'overview.category',
    OVERVIEW_CREATED_TIME: 'overview.createdtime',
    IMAGES_BANNER: 'images.banner',
    IMAGES_OLD_BANNER: 'images.oldbanner',
    IMAGES_SCREENSHOTS: 'images.screenshots',
    IMAGES_SCREENSHOT_0: 'images.screenshot0',
    IMAGES_OLD_SCREENSHOT_0: 'images.oldscreenshot0',
    IMAGES_SCREENSHOT_1: 'images.screenshot1',
    IMAGES_OLD_SCREENSHOT_1: 'images.oldscreenshot1',
    IMAGES_SCREENSHOT_2: 'images.screenshot2',
    IMAGES_OLD_SCREENSHOT_2: 'images.oldscreenshot2',
    IMAGES_THUMBNAIL: 'images.thumbnail',
    IMAGES_OLD_THUMBNAIL: 'images.oldthumbnail'
};

/**
 * This class is capable of validating any field of a mobile app.
 * @param configurations {Object} configurations for this validator
 * @constructor
 */
function Validator(configurations) {

    /**
     * Constants
     * @enum {number}
     */
    var Constant = {
        MAX_LENGTH_OF_NAME: 30,
        MAX_LENGTH_OF_DISPLAY_NAME: 30,
        MAX_LENGTH_OF_DESCRIPTION: 1000,
        MAX_LENGTH_OF_RECENT_CHANGES: 700,
        SCREENSHOTS_MIN_COUNT: 1
    };

    /**
     * Fields in a HTML form.
     * @type {Field[]}
     */
    var formValidatingFields = [
        Field.OVERVIEW_PLATFORM,
        Field.OVERVIEW_TYPE,
        Field.OVERVIEW_PACKAGE_NAME,
        Field.OVERVIEW_APP_ID,
        Field.OVERVIEW_URL,
        Field.OVERVIEW_NAME,
        Field.OVERVIEW_DISPLAY_NAME,
        Field.OVERVIEW_VERSION,
        Field.OVERVIEW_DESCRIPTION,
        Field.OVERVIEW_RECENT_CHANGES,
        Field.IMAGES_BANNER,
        Field.IMAGES_SCREENSHOT_0,
        Field.IMAGES_SCREENSHOT_1,
        Field.IMAGES_SCREENSHOT_2,
        Field.IMAGES_THUMBNAIL
    ];

    /**
     * Fields in a mobile app model.
     * @type {Field[]}
     */
    var modelValidatingFields = [
        Field.OVERVIEW_PLATFORM,
        Field.OVERVIEW_TYPE,
        Field.OVERVIEW_PACKAGE_NAME,
        Field.OVERVIEW_APP_ID,
        Field.OVERVIEW_URL,
        Field.OVERVIEW_NAME,
        Field.OVERVIEW_DISPLAY_NAME,
        Field.OVERVIEW_VERSION,
        Field.OVERVIEW_DESCRIPTION,
        Field.OVERVIEW_RECENT_CHANGES,
        Field.IMAGES_BANNER,
        Field.IMAGES_SCREENSHOTS,
        Field.IMAGES_THUMBNAIL
    ];

    this.configurations = configurations;

    /**
     * Validates data from a HTML form submission.
     * @param formName {string} name of the HTML form
     * @param formData {Object.<Field, string>} form submission data to be validated
     * @param isPartialValidation {boolean} whether the validation is done for some or all fields
     * @returns {{status: ValidationStatus, validationResults: Object.<Field, string>}} validation
     *     result with the status and messages
     */
    this.validateForm = function (formName, formData, isPartialValidation) {
        var result = {status: ValidationStatus.VALID, validationResults: {}};
        for (var i = 0; i < formValidatingFields.length; i++) {
            var fieldName = formValidatingFields[i];
            var fieldValue = formData[fieldName];
            if (((typeof fieldValue) == 'undefined') && isPartialValidation) {
                // this is a partial form validation, just ignore empty fields
                continue;
            }
            var validationResult = this.validateFormField(fieldName, fieldValue, formData);
            if (validationResult.status != ValidationStatus.VALID) {
                result.status = ValidationStatus.INVALID;
                result.validationResults[fieldName] = validationResult.message;
            }
        }
        return result;
    };

    /**
     * Validates mobile app model.
     * @param mobileAppModel {Object} mobile app model to be validated
     * @param isPartialValidation {boolean} whether the validation is done for some or all fields
     * @returns {{status: ValidationStatus, validationResults: Object.<Field, string>}} validation
     *     result with the status and messages
     */
    this.validateModel = function (mobileAppModel, isPartialValidation) {
        var result = {status: ValidationStatus.VALID, validationResults: {}};
        for (var i = 0; i < modelValidatingFields.length; i++) {
            var fieldName = modelValidatingFields[i];
            var fieldValue = mobileAppModel.get(fieldName).value;
            if (((typeof fieldValue) == 'undefined') && isPartialValidation) {
                // this is a partial form validation, just ignore empty fields
                continue;
            }
            var validationResult = this.validateModelField(fieldName, mobileAppModel);
            if (validationResult.status != ValidationStatus.VALID) {
                result.status = ValidationStatus.INVALID;
                result.validationResults[fieldName] = validationResult.message;
            }
        }
        return result;
    };

    /**
     * Validates the specified field.
     * @param fieldName {Field} field to be validated
     * @param fieldValue {string} value of the specified field
     * @param dependentValues {Object.<Field, string>} other fields and their values which are
     *     required for the validation of the specified field
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateFormField = function (fieldName, fieldValue, dependentValues) {
        switch (fieldName) {
            case Field.OVERVIEW_PLATFORM:
                return this.validatePlatform(fieldValue);
            case Field.OVERVIEW_TYPE:
                return this.validateType(fieldValue, dependentValues);
            case Field.OVERVIEW_PACKAGE_NAME:
                return this.validateAndroidPackageName(fieldValue, dependentValues);
            case Field.OVERVIEW_APP_ID:
                return this.validateIosAppIdentifier(fieldValue, dependentValues);
            case Field.OVERVIEW_URL:
                return this.validateWebAppURL(fieldValue, dependentValues);
            case Field.OVERVIEW_NAME:
                return this.validateName(fieldValue);
            case Field.OVERVIEW_DISPLAY_NAME:
                return this.validateDisplayName(fieldValue);
            case Field.OVERVIEW_VERSION:
                return this.validateVersion(fieldValue);
            case Field.OVERVIEW_DESCRIPTION:
                return this.validateDescription(fieldValue);
            case Field.OVERVIEW_RECENT_CHANGES:
                return this.validateRecentChanges(fieldValue);
            case Field.IMAGES_BANNER:
                return this.validateBannerImage(fieldValue);
            case Field.IMAGES_SCREENSHOT_0:
            case Field.IMAGES_SCREENSHOT_1:
            case Field.IMAGES_SCREENSHOT_2:
                var screenshots = [];
                screenshots.push(dependentValues[Field.IMAGES_SCREENSHOT_0]);
                screenshots.push(dependentValues[Field.IMAGES_SCREENSHOT_1]);
                screenshots.push(dependentValues[Field.IMAGES_SCREENSHOT_2]);
                return this.validateScreenshotsImages(screenshots);
            case Field.IMAGES_THUMBNAIL:
                return this.validateThumbnailImage(fieldValue);
            default:
                return {status: ValidationStatus.VALID, message: ""};
        }
    };

    /**
     * Validates the specified field from a mobile app model.
     * @param fieldName {Field} field to be validated
     * @param mobileAppModel {Object} mobile app model
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateModelField = function (fieldName, mobileAppModel) {
        var fieldValue = mobileAppModel.get(fieldName).value;
        switch (fieldName) {
            case Field.OVERVIEW_PLATFORM:
                return this.validatePlatform(fieldValue);
            case Field.OVERVIEW_TYPE:
                var dependentValues = {};
                dependentValues[Field.OVERVIEW_PLATFORM] =
                    mobileAppModel.get(Field.OVERVIEW_PLATFORM).value;
                return this.validateType(fieldValue, dependentValues);
            case Field.OVERVIEW_PACKAGE_NAME:
                var dependentValues = {};
                dependentValues[Field.OVERVIEW_PLATFORM] =
                    mobileAppModel.get(Field.OVERVIEW_PLATFORM).value;
                dependentValues[Field.OVERVIEW_TYPE] =
                    mobileAppModel.get(Field.OVERVIEW_TYPE).value;
                return this.validateAndroidPackageName(fieldValue, dependentValues);
            case Field.OVERVIEW_APP_ID:
                var dependentValues = {};
                dependentValues[Field.OVERVIEW_PLATFORM] =
                    mobileAppModel.get(Field.OVERVIEW_PLATFORM).value;
                dependentValues[Field.OVERVIEW_TYPE] =
                    mobileAppModel.get(Field.OVERVIEW_TYPE).value;
                return this.validateIosAppIdentifier(fieldValue, dependentValues);
            case Field.OVERVIEW_URL:
                var dependentValues = {};
                dependentValues[Field.OVERVIEW_PLATFORM] =
                    mobileAppModel.get(Field.OVERVIEW_PLATFORM).value;
                return this.validateWebAppURL(fieldValue, dependentValues);
            case Field.OVERVIEW_NAME:
                return this.validateName(fieldValue);
            case Field.OVERVIEW_DISPLAY_NAME:
                return this.validateDisplayName(fieldValue);
            case Field.OVERVIEW_VERSION:
                return this.validateVersion(fieldValue);
            case Field.OVERVIEW_DESCRIPTION:
                return this.validateDescription(fieldValue);
            case Field.OVERVIEW_RECENT_CHANGES:
                return this.validateRecentChanges(fieldValue);
            case Field.IMAGES_BANNER:
                return this.validateBannerImage(fieldValue);
            case Field.IMAGES_SCREENSHOTS:
                return this.validateScreenshotsImages(fieldValue.split(","));
            case Field.IMAGES_THUMBNAIL:
                return this.validateThumbnailImage(fieldValue);
            default:
                return {status: ValidationStatus.VALID, message: ""};
        }
    };

    /**
     * Validates the 'platform' field of a mobile app.
     * @param platform {string} value of the 'platform' field
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validatePlatform = function (platform) {
        var result = {status: null, message: ""};
        if (!platform) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Platform cannot be empty. Please select a suitable platform.";
            return result;
        }
        switch (platform) {
            case 'android':
            case 'ios':
            case 'webapp':
                result.status = ValidationStatus.VALID;
                return result;
            default :
                result.status = ValidationStatus.INVALID.VALUE;
                result.message = "Value entered as platform is invalid. "
                                 + "Please select a valid platform.";
                return result;
        }
    };

    /**
     * Validates the 'store type' field of a mobile app. Validity of the 'store type' field depends
     * on the value of {@link Field.OVERVIEW_PLATFORM} field.
     * @param type {string} value of the 'store type' field
     * @param dependentValues {Object.<Field, string>} other fields and their values which are
     *     required for this validation
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateType = function (type, dependentValues) {
        var result = {status: null, message: ""};
        if (dependentValues[Field.OVERVIEW_PLATFORM] == 'webapp') {
            // type doesn't matter
            result.status = ValidationStatus.VALID;
            return result;
        }

        if (!type) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Store type cannot be empty. Please select a suitable store type.";
            return result;
        }
        switch (type) {
            case 'enterprise':
            case 'public':
                result.status = ValidationStatus.VALID;
                return result;
            default :
                result.status = ValidationStatus.INVALID.VALUE;
                result.message = "Value entered as store type is invalid. "
                                 + "Please select a valid store type.";
                return result;
        }
    };

    /**
     * Validates the 'package name' field of a mobile app. Validity of the 'package name' field
     * depends on the values of {@link Field.OVERVIEW_PLATFORM} and {@link Field.OVERVIEW_TYPE}
     * fields.
     * @param packageName {string} value of the 'package name' field
     * @param dependentValues {Object.<Field, string>} other fields and their values which are
     *     required for this validation
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateAndroidPackageName = function (packageName, dependentValues) {
        var result = {status: null, message: ""};
        var platform = dependentValues[Field.OVERVIEW_PLATFORM];
        var type = dependentValues[Field.OVERVIEW_TYPE];
        if ((platform == 'android' && type == 'enterprise') || (platform == 'ios')
            || (platform == 'webapp')) {
            // package name doesn't matter
            result.status = ValidationStatus.VALID;
            return result;
        }

        if (!packageName) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Android package name cannot be empty. "
                             + "Please enter a suitable Android package name.";
            return result;
        }
        var URL = 'https://play.google.com/store/apps/details?id=';
        try {
            var httpResponse = get(URL + packageName);
            if (httpResponse.xhr.status != 200) {
                result.status = ValidationStatus.INVALID.VALUE;
                result.message = "Value entered as Android package name is invalid. "
                                 + "Please enter a valid Android package name.";
                return result;
            }
        } catch (e) {
            log.error("Android package name validation operation failed. "
                      + "Cannot send HTTP GET request to '" + URL + packageName + "'.");
            log.error(e);
            result.status = ValidationStatus.UNKNOWN;
            result.message = "An error occurred. Cannot validate Android package name.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    /**
     * Validates the 'app identifier' field of a mobile app. Validity of the 'app identifier' field
     * depends on the values of {@link Field.OVERVIEW_PLATFORM} and {@link Field.OVERVIEW_TYPE}
     * fields.
     * @param appIdentifier {string} value of the 'app identifier' field
     * @param dependentValues {Object.<Field, string>} other fields and their values which are
     *     required for this validation
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateIosAppIdentifier = function (appIdentifier, dependentValues) {
        var result = {status: null, message: ""};
        var platform = dependentValues[Field.OVERVIEW_PLATFORM];
        var type = dependentValues[Field.OVERVIEW_TYPE];
        if ((platform == 'android') || (platform == 'webapp')
            || (platform == 'ios' && type == 'enterprise')) {
            // iOS app identifier doesn't matter
            result.status = ValidationStatus.VALID;
            return result;
        }

        if (!appIdentifier) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "iOS app identifier cannot be empty. "
                             + "Please enter a suitable iOS app identifier.";
            return result;
        }
        if (isNaN(parseInt(appIdentifier))) {
            result.status = ValidationStatus.INVALID.DATA_TYPE;
            result.message = "iOS app identifier should be an integer. "
                             + "Please enter a valid iOS app identifier.";
            return result;
        }
        var URL = 'https://itunes.apple.com/lookup?id=';
        try {
            var httpResponse = get(URL + appIdentifier);
            if (httpResponse.xhr.status != 200) {
                result.status = ValidationStatus.UNKNOWN;
                result.message = "iOS app identifier validation operation failed. "
                                 + "iTunes server responded with HTTP status "
                                 + httpResponse.xhr.status + ".";
                return result;
            }
            var responseData = JSON.parse(httpResponse.data);
            if (responseData.resultCount != 1) {
                result.status = ValidationStatus.INVALID.VALUE;
                result.message = "Value entered as iOS app identifier is invalid. "
                                 + "Please enter a valid iOS app identifier.";
                return result;
            }
        } catch (e) {
            log.error("iOS app identifier validation operation failed. "
                      + "Cannot send HTTP GET request to '" + URL + appIdentifier + "'.");
            log.error(e);
            result.status = ValidationStatus.UNKNOWN;
            result.message = "An error occurred. Cannot validate iOS app identifier.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    /**
     * Validates the 'url' field of a mobile app. Validity of the 'url' field depends on the values
     * of {@link Field.OVERVIEW_PLATFORM} and {@link Field.OVERVIEW_TYPE} fields.
     * @param url {string} value of the 'url' field
     * @param dependentValues {Object.<Field, string>} other fields and their values which are
     *     required for this validation
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateWebAppURL = function (url, dependentValues) {
        var result = {status: null, message: ""};
        if (dependentValues[Field.OVERVIEW_PLATFORM] != 'webapp') {
            // web app URL doesn't matter
            result.status = ValidationStatus.VALID;
            return result;
        }

        if (!url) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Web app URL cannot be empty. Please enter a suitable web app URL.";
            return result;
        }
        var REGEX = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
        if (!REGEX.test(url)) {
            result.status = ValidationStatus.INVALID.FORMAT;
            result.message = "Value entered as web app URL is invalid. "
                             + "Please enter a valid web app URL.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    /**
     * Validates the 'name' field of a mobile app.
     * @param name {string} value of the 'name' field
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateName = function (name) {
        var result = {status: null, message: ""};
        if (!name) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Name cannot be empty. Please enter a suitable name.";
            return result;
        }
        if (name.length > Constant.MAX_LENGTH_OF_NAME) {
            result.status = ValidationStatus.INVALID.LENGTH.MAX;
            result.message = "Name cannot contain more than " + Constant.MAX_LENGTH_OF_NAME
                             + " characters.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    /**
     * Validates the 'display name' field of a mobile app.
     * @param displayName {string} value of the 'display name' field
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateDisplayName = function (displayName) {
        var result = {status: null, message: ""};
        if (!displayName) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Display name cannot be empty. Please enter a suitable display name.";
            return result;
        }
        if (displayName.length > Constant.MAX_LENGTH_OF_DISPLAY_NAME) {
            result.status = ValidationStatus.INVALID.LENGTH.MAX;
            result.message = "Display name cannot contain more than "
                             + Constant.MAX_LENGTH_OF_DISPLAY_NAME + " characters.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    /**
     * Validates the 'version' field of a mobile app.
     * @param version {string} value of the 'version' field
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateVersion = function (version) {
        var result = {status: null, message: ""};
        if (!version) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Version name cannot be empty. Please enter a suitable version.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    /**
     * Validates the 'description' field of a mobile app.
     * @param description {string} value of the 'description' field
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateDescription = function (description) {
        var result = {status: null, message: ""};
        if (!description) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Description cannot be empty. Please enter a suitable description.";
            return result;
        }
        if (description.length > Constant.MAX_LENGTH_OF_DESCRIPTION) {
            result.status = ValidationStatus.INVALID.LENGTH.MAX;
            result.message = "Description cannot contain more than "
                             + Constant.MAX_LENGTH_OF_DESCRIPTION + " characters.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    /**
     * Validates the 'recent changes' field of a mobile app.
     * @param recentChanges {string} value of the 'recent changes' field
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateRecentChanges = function (recentChanges) {
        var result = {status: null, message: ""};
        if (recentChanges && (recentChanges.length > Constant.MAX_LENGTH_OF_RECENT_CHANGES)) {
            result.status = ValidationStatus.INVALID.LENGTH.MAX;
            result.message = "Recent changes cannot contain more than "
                             + Constant.MAX_LENGTH_OF_RECENT_CHANGES + " characters.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    /**
     * Validates the 'banner' field of a mobile app.
     * @param banner {string} value of the 'banner' field
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateBannerImage = function (banner) {
        var result = {status: null, message: ""};
        if (!banner) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Banner image cannot be empty. Please select a suitable banner image.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    /**
     * Validates the 'screenshots' field of a mobile app.
     * @param screenshots {string[]} value of the 'screenshots' field
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateScreenshotsImages = function (screenshots) {
        var result = {status: null, message: ""};
        if (!Array.isArray(screenshots)) {
            result.status = ValidationStatus.INVALID.DATA_TYPE;
            result.message = "Screenshots should be an array.";
            return result;
        }
        var minScreenshotsCount = Constant.SCREENSHOTS_MIN_COUNT;
        if (screenshots.length < minScreenshotsCount) {
            result.status = ValidationStatus.INVALID.FORMAT;
            result.message = "Screenshots should contain at least" + minScreenshotsCount + " images.";
            return result;
        }
        var screenshotsCount = 0;
        for (var i = 0; i < screenshots.length; i++) {
            if (screenshots[i]) {
                screenshotsCount++;
            }
        }
        if (screenshotsCount < minScreenshotsCount) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Screenshots cannot be empty. "
                             + "Please select at least one screenshot image for this app.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    /**
     * Validates the 'thumbnail' field of a mobile app.
     * @param thumbnail {string} value of the 'thumbnail' field
     * @returns {{status: ValidationStatus, message: string}} validation result with the status and
     *     the message
     */
    this.validateThumbnailImage = function (thumbnail) {
        var result = {status: null, message: ""};
        if (!thumbnail) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Icon image cannot be empty. Please select a suitable icon image.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };
}
