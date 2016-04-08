package org.wso2.carbon.appmgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class AppAppmetaDTO  {
  
  
  
  private String weburl = null;
  
  
  private String _package = null;
  
  
  private String path = null;
  
  
  private String version = null;

  
  /**
   * Web application urk
   **/
  @ApiModelProperty(value = "Web application urk")
  @JsonProperty("weburl")
  public String getWeburl() {
    return weburl;
  }
  public void setWeburl(String weburl) {
    this.weburl = weburl;
  }

  
  /**
   * The package name of the application binary
   **/
  @ApiModelProperty(value = "The package name of the application binary")
  @JsonProperty("_package")
  public String getPackage() {
    return _package;
  }
  public void setPackage(String _package) {
    this._package = _package;
  }

  
  /**
   * Application binary file API
   **/
  @ApiModelProperty(value = "Application binary file API")
  @JsonProperty("path")
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }

  
  /**
   * The version of the application binary
   **/
  @ApiModelProperty(value = "The version of the application binary")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppAppmetaDTO {\n");
    
    sb.append("  weburl: ").append(weburl).append("\n");
    sb.append("  _package: ").append(_package).append("\n");
    sb.append("  path: ").append(path).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
