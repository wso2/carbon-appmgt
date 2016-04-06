package org.wso2.carbon.appmgt.rest.api.storeadmin.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class BinaryDTO  {
  
  
  
  private String binaryId = null;
  
  @NotNull
  private String name = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("binaryId")
  public String getBinaryId() {
    return binaryId;
  }
  public void setBinaryId(String binaryId) {
    this.binaryId = binaryId;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class BinaryDTO {\n");
    
    sb.append("  binaryId: ").append(binaryId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
