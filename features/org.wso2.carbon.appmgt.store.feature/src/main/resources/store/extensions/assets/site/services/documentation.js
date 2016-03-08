/*
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

/*
 Description: This module encapsulates the document retrieval logic for a given asset.
 Filename: documentation.js
 */
var serviceModule = (function () {

    var QUERY_PARAM_PROVIDER = 'provider';
    var QUERY_PARAM_ASSET = 'name';
    var QUERY_PARAM_VERSION = 'version';
    var DOC_TYPES = ['HowTo', 'Samples', 'PublicForum', 'SupportForum', 'APIMessageFormat', 'Other'];
    var MAP_KEY_TYPE = 'type';
    var MAP_KEY_HASCONTENT = 'hasContent';
    var MAP_KEY_ENTRIES = 'entries';
    var ENTRY_KEY_ISINLINE = 'isInline';
    var ENTRY_KEY_ISFILE = 'isFile';
    var ENTRY_KEY_ISURL = 'isUrl';

    var DEFAULT_KEY_HASCONTENT = false;
    var DEFAULT_DOC_TYPE = 'Other';

    var log = new Log('Documentation service');

    function DocumentationService() {
        this.instance = null;
        this.user = null;
    }

    DocumentationService.prototype.init = function (context, session) {
        this.instance = context.module('documentation');
    };

    DocumentationService.prototype.getAllDocumentation = function (assetProvider, assetName, assetVersion, ofUser) {
        var query = getQuery(assetProvider, assetName, assetVersion);
        var user = ofUser || this.user;  //If the user does not provide a user then use the default user from session.

        var documents = this.instance.getAllDocumentations(query, user);
        var documentsAvailability = documents.documentations.length;

        documents = sortDocumentsByType(documents);
        if (documentsAvailability > 0) {
            documents['availability'] = true;
        } else {
            documents['availability'] = false;
        }

        return documents;
    };

    var getQuery = function (assetProvider, assetName, assetVersion) {
        var query = {};
        query[QUERY_PARAM_PROVIDER] = assetProvider;
        query[QUERY_PARAM_ASSET] = assetName;
        query[QUERY_PARAM_VERSION] = assetVersion;
        return query;
    }

    var createDocumentMap = function () {
        var map = {};
        var key;

        for (var index in DOC_TYPES) {
            key = DOC_TYPES[index];
            map[key] = {};
            map[key][MAP_KEY_TYPE] = key;
            map[key][MAP_KEY_HASCONTENT] = DEFAULT_KEY_HASCONTENT;
            map[key][MAP_KEY_ENTRIES] = [];
        }
        return map;
    };

    var sortDocumentsByType = function (data) {
        var doc;
        var docMap = createDocumentMap();
        var documents = data.documentations;

        if (documents.length > 0) {
            for (var docIndex in documents) {
                doc = documents[docIndex];

                //Add meta data.
                doc = addMetaDataToDoc(doc);

                //Assign to an appropriate type in the document map.
                assignToDocMap(docMap, doc.type, doc);
            }

            docMap = checkIfContentPresent(docMap);
        }

        return docMap;
    };

    /*
     The function checks if a particular type of content is present.
     */
    var checkIfContentPresent = function (docMap) {
        var docType;

        for (var typeIndex in docMap) {
            docType = docMap[typeIndex];
            if (docType.entries.length > 0) {
                docType[MAP_KEY_HASCONTENT] = true;
            }
        }

        return docMap;
    };

    /*
     The function assigns the document to an appropriate type
     in the documentMap.
     */
    var assignToDocMap = function (docMap, type, doc) {
        var targetType = documentTypeMapping(type);

        if (!docMap.hasOwnProperty(targetType)) {
            targetType = DEFAULT_DOC_TYPE;
        }

        docMap[targetType].entries.push(doc);
    };

    /*
     The function sets a flag indicating the type of
     content in the document.
     */
    var addMetaDataToDoc = function (doc) {

        doc[ENTRY_KEY_ISFILE] = false;
        doc[ENTRY_KEY_ISINLINE] = false;
        doc[ENTRY_KEY_ISURL] = false;

        switch (doc.sourceType) {
            case 'INLINE':
                doc[ENTRY_KEY_ISINLINE] = true;
                break;
            case 'FILE':
                doc[ENTRY_KEY_ISFILE] = true;
                break;
            case 'URL':
                doc[ENTRY_KEY_ISURL] = true;
                break;
            default:
                break;
        }

        return doc;
    };

    var documentTypeMapping = function (type) {
        var trimmedType = type.trim();
        return trimmedType.replace(/\s/g, '');
    };

    return {
        DocumentationService: DocumentationService
    };
})();
