/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.appmgt.mobile.utils;

public class MobileApplicationException extends Exception {

	private String errorMessage;

	public MobileApplicationException(String msg, Exception e) {
		super(msg, e);
		setErrorMessage(msg);
	}

	public MobileApplicationException(String msg, Throwable cause) {
		super(msg, cause);
		setErrorMessage(msg);
	}

	public MobileApplicationException(String msg) {
		super(msg);
		setErrorMessage(msg);
	}

	public MobileApplicationException() {
		super();
	}

	public MobileApplicationException(Throwable cause) {
		super(cause);
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
