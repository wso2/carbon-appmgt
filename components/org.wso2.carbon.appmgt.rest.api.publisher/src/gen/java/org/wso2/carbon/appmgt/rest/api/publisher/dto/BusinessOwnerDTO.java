package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.BusinessOwnerPropertiesDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class BusinessOwnerDTO  {
  
  
  
  private Integer id = null;
  
  
  private String name = null;
  
  
  private String description = null;
  
  
  private String email = null;
  
  
  private String site = null;
  
  
  private List<BusinessOwnerPropertiesDTO> properties = new ArrayList<BusinessOwnerPropertiesDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
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
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("site")
  public String getSite() {
    return site;
  }
  public void setSite(String site) {
    this.site = site;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("properties")
  public List<BusinessOwnerPropertiesDTO> getProperties() {
    return properties;
  }
  public void setProperties(List<BusinessOwnerPropertiesDTO> properties) {
    this.properties = properties;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class BusinessOwnerDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  email: ").append(email).append("\n");
    sb.append("  site: ").append(site).append("\n");
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
