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

import java.util.LinkedList;
import java.util.Queue;


public class NamedMatchList<T> {

    private static class NamedPattern<T> {

        private final T identifier;
        private final String pattern;

        public NamedPattern(T name, String pattern) {
            this.identifier = name;
            this.pattern = pattern;
        }

        public T getIdentifier() {
            return identifier;
        }


        public String getPattern() {
            return pattern;
        }

    }

    private final Queue<NamedPattern<T>> queue;

    public NamedMatchList() {
        queue = new LinkedList<NamedPattern<T>>();
    }

    /**
     * add a pattern to list
     * @param identifier
     * @param pattern
     */
    public void add(T identifier, String pattern) {
        queue.add(new NamedPattern(identifier, pattern));
    }

    /**
     * match and return identifier;
     * @param s string to consider
     * @return identifier of pattern if matched else null;
     */
    public T match(String s) {
        for (NamedPattern<T> m : queue) {
            if (UrlPatternMatcher.match(m.getPattern(), s)) {
                return m.getIdentifier();
            }
        }
        return null;
    }
}
