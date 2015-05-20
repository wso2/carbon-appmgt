/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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



package org.wso2.carbon.appmgt.impl.utils;

public class UrlPatternMatcher {
	
	private static final String REGEX_SPECIAL_CHARACTERS = "([\\\\\\.\\[\\{\\(\\)\\+\\?\\^\\$\\|])";
	private static final String REGEXP_SPECIAL_CHARACCTERS_ESCAPE_PATTERN = "\\\\Q$1\\\\E";

	public static boolean match(String pattern, String url){
		
		// The format of a URL pattern is abc/cde/*/a.jsp
		// * denotes any character. So we need to convert the pattern to a property regular expression.
		
		// Escape all the regular expression special characters other than '*'
		String regexEscapedPattern = pattern.replaceAll(REGEX_SPECIAL_CHARACTERS, REGEXP_SPECIAL_CHARACCTERS_ESCAPE_PATTERN);
		
		// Replace the any (*) character with the regular expression version (.*) of it.
		String regexString = regexEscapedPattern.replace("*", ".*");

		return url.matches(regexString);
    }
}
