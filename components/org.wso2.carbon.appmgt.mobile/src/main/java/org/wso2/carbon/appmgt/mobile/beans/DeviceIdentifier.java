package org.wso2.carbon.appmgt.mobile.beans;


import java.io.Serializable;

public class DeviceIdentifier implements Serializable {
	private String id;
	private String type;

	public DeviceIdentifier() {
	}

	public DeviceIdentifier(String id, String type) {
		this.id = id;
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String toString() {
		return "DeviceIdentifier{id=\'" + this.id + '\'' + ", type=\'" + this.type + '\'' + '}';
	}
}
