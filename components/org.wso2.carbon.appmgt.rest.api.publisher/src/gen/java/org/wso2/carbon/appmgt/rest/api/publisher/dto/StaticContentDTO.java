package org.wso2.carbon.appmgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.validation.constraints.NotNull;


@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@ApiModel(description = "")
public class StaticContentDTO  {
  
  
  @NotNull
  private String path = null;

  
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

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class StaticContentDTO {\n");
    
    sb.append("  path: ").append(path).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
