package org.wso2.carbon.appmgt.rest.api.publisher.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.validation.constraints.NotNull;


@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@ApiModel(description = "")
public class BinaryDTO  {
  
  
  @NotNull
  private String path = null;
  
  
  private String _package = null;
  
  
  private String version = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("path")
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("_package")
  public String getPackage() {
    return _package;
  }
  public void setPackage(String _package) {
    this._package = _package;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
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
    sb.append("class BinaryDTO {\n");
    
    sb.append("  path: ").append(path).append("\n");
    sb.append("  _package: ").append(_package).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
