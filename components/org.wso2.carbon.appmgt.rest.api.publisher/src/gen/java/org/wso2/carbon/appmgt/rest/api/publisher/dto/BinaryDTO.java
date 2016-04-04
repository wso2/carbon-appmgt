package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;






public class BinaryDTO {
  
  private String binaryId = null;
  private String name = null;

  
  /**
   **/
  public BinaryDTO binaryId(String binaryId) {
    this.binaryId = binaryId;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("binaryId")
  public String getBinaryId() {
    return binaryId;
  }
  public void setBinaryId(String binaryId) {
    this.binaryId = binaryId;
  }

  
  /**
   **/
  public BinaryDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "anroidapp.apk", required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BinaryDTO binary = (BinaryDTO) o;
    return Objects.equals(binaryId, binary.binaryId) &&
        Objects.equals(name, binary.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(binaryId, name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Binary {\n");
    
    sb.append("    binaryId: ").append(toIndentedString(binaryId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

