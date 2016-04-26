package org.wso2.carbon.appmgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;



@ApiModel(description = "")
public class DeviceInfoDTO  {
  
  
  
  private String id = null;
  
  
  private String platform = null;
  
  
  private String model = null;
  
  
  private String platformVersion = null;
  
  
  private String name = null;
  
  
  private String image = null;
  
  
  private String type = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("platform")
  public String getPlatform() {
    return platform;
  }
  public void setPlatform(String platform) {
    this.platform = platform;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("model")
  public String getModel() {
    return model;
  }
  public void setModel(String model) {
    this.model = model;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("platformVersion")
  public String getPlatformVersion() {
    return platformVersion;
  }
  public void setPlatformVersion(String platformVersion) {
    this.platformVersion = platformVersion;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("image")
  public String getImage() {
    return image;
  }
  public void setImage(String image) {
    this.image = image;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeviceInfoDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  platform: ").append(platform).append("\n");
    sb.append("  model: ").append(model).append("\n");
    sb.append("  platformVersion: ").append(platformVersion).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  image: ").append(image).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
