var ValidationStatus = Object.freeze({
    VALID: 1,
    INVALID: Object.freeze({
        EMPTY: -1,
        FORMAT: -2,
        LENGTH: Object.freeze({
            MAX: -3,
            MIN: -4
        }),
        DATA_TYPE: -5,
        VALUE: -6
    }),
    UNKNOWN: 0
});

var Field = Object.freeze({
    OVERVIEW_PLATFORM: 'overview_platform',
    OVERVIEW_TYPE: 'overview_type',
    OVERVIEW_PACKAGE_NAME: 'overview_packagename',
    OVERVIEW_URL: 'overview_url',
    OVERVIEW_NAME: 'overview_name',
    OVERVIEW_DISPLAY_NAME: 'overview_displayName',
    OVERVIEW_VERSION: 'overview_version',
    OVERVIEW_DESCRIPTION: 'overview_description',
    OVERVIEW_RECENT_CHANGES: 'overview_recentchanges',
    IMAGES_BANNER: 'images_banner',
    IMAGES_SCREENSHOTS: 'images_screenshots',
    IMAGES_THUMBNAIL: 'images_thumbnail'
});

function getInstance(configurations) {

    var Constant = Object.freeze({
        MAX_LENGTH_OF_NAME: 30,
        MAX_LENGTH_OF_DISPLAY_NAME: 30,
        MAX_LENGTH_OF_DESCRIPTION: 1000,
        MAX_LENGTH_OF_RECENT_CHANGES: 700,
        SCREENSHOTS_COUNT: 3
    });

    function Validator(configurations) {
        this.configurations = configurations;
    }

    Validator.prototype.validateFormData = function (formName, formData) {
        var result = {valid: true, messages: {}};
        var fields = Field;
        for (var fieldName in fields) {
            if (!fields.hasOwnProperty(fieldName)) {
                // this is not a property of the 'Field' object,
                // might be a property inherited from Object class
                continue;
            }
            var fieldValue = formData[fieldName];
            if (fieldValue == 'undefined') {
                // this might be a partial form validation, just ignore
                continue;
            }
            var validationResult = this.validateField(fieldName, fieldValue, formData);
            if (validationResult.status != ValidationStatus.VALID) {
                result.valid = false;
                result.messages[fieldName] = validationResult.message;
            }
        }
        return result;
    };

    Validator.prototype.validateField = function (fieldName, fieldValue, optionalValues) {

        function valueAdded(rv) {
            rv.valid = (rv.status == ValidationStatus.VALID);
            return rv;
        }

        switch (fieldName) {
            case Field.OVERVIEW_PLATFORM:
                return valueAdded(this.validatePlatform(fieldValue));
            case Field.OVERVIEW_TYPE:
                var platform = optionalValues[Field.OVERVIEW_PLATFORM];
                var optionalValidation = this.validatePlatform(platform);
                if (optionalValidation != ValidationStatus.VALID) {
                    return valueAdded(optionalValidation);
                }
                if (platform == 'webapp') {
                    return valueAdded({status: ValidationStatus.VALID, message: ""});
                }
                return valueAdded(this.validateType(fieldValue)); // android, ios
            case Field.OVERVIEW_PACKAGE_NAME:
                var type = optionalValues[Field.OVERVIEW_TYPE];
                var optionalValidation = this.validateType(type);
                if (optionalValidation != ValidationStatus.VALID) {
                    return valueAdded(optionalValidation);
                }
                if (type == 'enterprise') {
                    return valueAdded({status: ValidationStatus.VALID, message: ""});
                } else {
                    // public
                    var platform = optionalValues[Field.OVERVIEW_PLATFORM];
                    var optionalValidation = this.validatePlatform(platform);
                    if (optionalValidation != ValidationStatus.VALID) {
                        return valueAdded(optionalValidation);
                    }
                    switch (platform) {
                        case 'android':
                            return valueAdded(this.validateAndroidPackageName(fieldValue));
                        case 'ios':
                            return valueAdded(this.validateIosAppIdentifier(fieldValue));
                        case 'webapp':
                            return valueAdded({status: ValidationStatus.VALID, message: ""});
                    }
                }
                break;
            case Field.OVERVIEW_URL:
                var platform = optionalValues[Field.OVERVIEW_PLATFORM];
                var optionalValidation = this.validatePlatform(platform);
                if (optionalValidation != ValidationStatus.VALID) {
                    return valueAdded(optionalValidation);
                }
                if (platform == 'webapp') {
                    return valueAdded(this.validateURL(fieldValue));
                }
                return valueAdded({status: ValidationStatus.VALID, message: ""}); // android, ios
            case Field.OVERVIEW_NAME:
                return valueAdded(this.validateName(fieldValue));
            case Field.OVERVIEW_DISPLAY_NAME:
                return valueAdded(this.validateDisplayName(fieldValue));
            case Field.OVERVIEW_VERSION:
                return valueAdded(this.validateVersion(fieldValue));
            case Field.OVERVIEW_DESCRIPTION:
                return valueAdded(this.validateDescription(fieldValue));
            case Field.OVERVIEW_RECENT_CHANGES:
                return valueAdded(this.validateRecentChanges(fieldValue));
            case Field.IMAGES_BANNER:
                return valueAdded(this.validateBannerImage(fieldValue));
            case Field.IMAGES_SCREENSHOTS:
                return valueAdded(this.validateScreenshotsImages(fieldValue));
            case Field.IMAGES_THUMBNAIL:
                return valueAdded(this.validateThumbnailImage(fieldValue));
            default:
                return {
                    valid: false,
                    status: ValidationStatus.UNKNOWN,
                    message: "Unknown field '" + fieldName + "'."
                };
        }
    };

    Validator.prototype.validatePlatform = function (platform) {
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

    Validator.prototype.validateType = function (type) {
        var result = {status: null, message: ""};
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

    Validator.prototype.validateAndroidPackageName = function (packageName) {
        var result = {status: null, message: ""};
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

    Validator.prototype.validateIosAppIdentifier = function (appIdentifier) {
        var result = {status: null, message: ""};
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

    Validator.prototype.validateURL = function (url) {
        var result = {status: null, message: ""};
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

    Validator.prototype.validateName = function (name) {
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

    Validator.prototype.validateDisplayName = function (displayName) {
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

    Validator.prototype.validateVersion = function (version) {
        var result = {status: null, message: ""};
        if (!version) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Version name cannot be empty. Please enter a suitable version.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    Validator.prototype.validateDescription = function (description) {
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

    Validator.prototype.validateRecentChanges = function (recentChanges) {
        var result = {status: null, message: ""};
        if (recentChanges.length > Constant.MAX_LENGTH_OF_RECENT_CHANGES) {
            result.status = ValidationStatus.INVALID.LENGTH.MAX;
            result.message = "Recent changes cannot contain more than "
                             + Constant.MAX_LENGTH_OF_RECENT_CHANGES + " characters.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    Validator.prototype.validateBannerImage = function (banner) {
        var result = {status: null, message: ""};
        if (!banner) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Banner image cannot be empty. Please select a suitable banner image.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    Validator.prototype.validateScreenshotsImages = function (screenshots) {
        var result = {status: null, message: ""};
        if (!isArray(screenshots)) {
            result.status = ValidationStatus.INVALID.DATA_TYPE;
            result.message = "Screenshots should be an array.";
            return result;
        }
        var screenshotsCount = Constant.SCREENSHOTS_COUNT;
        if (screenshots.length != screenshotsCount) {
            result.status = ValidationStatus.INVALID.FORMAT;
            result.message = "Screenshots should contain " + screenshotsCount + " images.";
            return result;
        }
        var emptyScreenshotsCount = 0;
        for (var i = 0; i < screenshotsCount; i++) {
            if (!screenshots[i]) {
                emptyScreenshotsCount++;
            }
        }
        if (emptyScreenshotsCount == screenshotsCount) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Screenshots cannot be empty. "
                             + "Please select at least one screenshot image for this app.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    Validator.prototype.validateThumbnailImage = function (thumbnail) {
        var result = {status: null, message: ""};
        if (!thumbnail) {
            result.status = ValidationStatus.INVALID.EMPTY;
            result.message = "Icon image cannot be empty. Please select a suitable icon image.";
            return result;
        }
        result.status = ValidationStatus.VALID;
        return result;
    };

    return new Validator(configurations);
}
