package org.wso2.carbon.appmgt.mobile.utils;

/**
 * Created by Lakshman on 3/29/16.
 */
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
