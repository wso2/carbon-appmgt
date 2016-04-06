package org.wso2.carbon.appmgt.rest.api.storeadmin;


public class NotFoundException extends ApiException {
	private int code;
	public NotFoundException (int code, String msg) {
		super(code, msg);
		this.code = code;
	}
}
