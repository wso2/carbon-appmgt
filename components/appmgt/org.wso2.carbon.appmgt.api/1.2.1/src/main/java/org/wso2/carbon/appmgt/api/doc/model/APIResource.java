package org.wso2.carbon.appmgt.api.doc.model;

import java.util.List;

public class APIResource {
	
	private String path;
	
	private String description;
	
	private List<Operation> operations;
	
	public APIResource(String path, String description, List<Operation> ops) {
		this.path = path;
		this.description = description;
		this.operations = ops;
	}

}


