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

    private static enum State {

        JUST_STARTED, NORMAL, EAGER, END
    }

    private final int patternLength;
    private final int patternOutBound;
    private final int urlLength;
    private final int urlOutBound;
    private final String pattern;
    private final String url;

    private static final char MATCH_ALL = '*';
    private static final char MATCH_ONE = '?';

    private int patternPrefix;
    private int patternSuffix;
    private State state;
    private boolean matched = false;

    public UrlPatternMatcher(String pattern, String url) {

        if (pattern == null || url == null) {
            throw new IllegalArgumentException(
                    "Pattern and String must not be null");
        }

        this.pattern = pattern;
        this.url = url;
        patternLength = pattern.length();
        urlLength = url.length();
        if (patternLength == 0 || urlLength == 0) {
            throw new IllegalArgumentException(
                    "Pattern and String must have at least one character");
        }
        patternOutBound = patternLength - 1;
        urlOutBound = urlLength - 1;
        patternPrefix = 0;
        patternSuffix = 0;
        state = State.JUST_STARTED;

    }

    public UrlPatternMatcher(String pattern, String url, int patternPrefix, int patternSuffix) {

        this(pattern, url);
        this.patternPrefix = patternPrefix;
        this.patternSuffix = patternSuffix;
    }

    private void calcState() {
        if (state == State.END) {
            return;
        }

        if (!psafe() || !ssafe()) {
            state = State.END;
        } else if (getPatternCharAt() == MATCH_ALL) {
            if (!pnsafe()) {
                state = State.END;
                matched = true;
            } else {
                state = State.EAGER;
            }
        } else {
            state = State.NORMAL;
        }
    }

    private void eat() {

        if (state == State.END) {
            return;
        }

        matched = false;

        if (state == State.EAGER) {
            UrlPatternMatcher smo = new UrlPatternMatcher(pattern, url, patternPrefix + 1, patternSuffix + 1);
            if (smo.match()) {
                state = State.END;
                matched = true;
                return;
            }
            ips();
        } else if (state == State.NORMAL) {
            if (getPatternMatchedForChar()) {
                ips();
                ipp();
                matched = true;
            } else {
                state = State.END;
                matched = false;
            }
        }
    }

    private boolean getPatternMatchedForChar() {
        char pc = getPatternCharAt();
        return (pc == MATCH_ONE || pc == getUrlCharAt());
    }

    private boolean mn() {
        return (pn() == getUrlCharAt());
    }

    private char getPatternCharAt() {
        return pattern.charAt(patternPrefix);
    }

    private char pn() {
        return pattern.charAt(patternPrefix + 1);
    }

    private char getUrlCharAt() {
        return url.charAt(patternSuffix);
    }

    private boolean psafe() {
        return patternPrefix <= patternOutBound;
    }

    private boolean pnsafe() {
        return (patternPrefix + 1) <= patternOutBound;
    }

    private boolean ssafe() {
        return patternSuffix <= urlOutBound;
    }

    private void ipp() {
        patternPrefix++;
    }

    private void ips() {
        patternSuffix++;
    }

    public boolean match() {
        if (patternOutBound > urlOutBound) {
            return false;
        }
        while (state != State.END) {
            calcState();
            eat();
        }
        return matched;
    }

    public static boolean match(String p, String s) throws
            IllegalArgumentException {
        return new UrlPatternMatcher(p, s).match();
    }
}
