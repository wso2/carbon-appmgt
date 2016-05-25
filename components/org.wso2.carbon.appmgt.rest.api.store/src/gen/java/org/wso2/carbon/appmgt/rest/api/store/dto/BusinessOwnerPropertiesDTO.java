package org.wso2.carbon.appmgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "")
public class BusinessOwnerPropertiesDTO {
  
  
  
  private String key = null;
  
  
  private String value = null;
  
  
  private Boolean isVisible = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("key")
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("value")
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("isVisible")
  public Boolean getIsVisible() {
    return isVisible;
  }
  public void setIsVisible(Boolean isVisible) {
    this.isVisible = isVisible;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class BusinessOwnerPropertiesDTO {\n");
    
    sb.append("  key: ").append(key).append("\n");
    sb.append("  value: ").append(value).append("\n");
    sb.append("  isVisible: ").append(isVisible).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
