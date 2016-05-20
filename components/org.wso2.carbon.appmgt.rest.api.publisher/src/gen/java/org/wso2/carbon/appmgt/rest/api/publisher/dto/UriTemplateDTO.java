package org.wso2.carbon.appmgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class UriTemplateDTO  {


  private Integer id = null;


  private String urlPattern = null;
  
  
  private String httpVerb = null;
  
  
  private String policyGroupName = null;


  private Integer policyGroupId = null;


  /**
   * URI template Id
   **/
  @ApiModelProperty(value = "ID of the URI Template")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }


  /**
   * URL pattern of the URI
   **/
  @ApiModelProperty(value = "URL pattern of the URI")
  @JsonProperty("urlPattern")
  public String getUrlPattern() {
    return urlPattern;
  }
  public void setUrlPattern(String urlPattern) {
    this.urlPattern = urlPattern;
  }

  
  /**
   * Http verb of the uri pattern
   **/
  @ApiModelProperty(value = "Http verb of the uri pattern")
  @JsonProperty("httpVerb")
  public String getHttpVerb() {
    return httpVerb;
  }
  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }

  
  /**
   * The name of the uri template policy group
   **/
  @ApiModelProperty(value = "The name of the uri template policy group")
  @JsonProperty("policyGroupName")
  public String getPolicyGroupName() {
    return policyGroupName;
  }
  public void setPolicyGroupName(String policyGroupName) {
    this.policyGroupName = policyGroupName;
  }


  /**
   * Database ID of the associated policy group.
   **/
  @ApiModelProperty(value = "Database ID of the associated policy group.")
  @JsonProperty("getPolicyGroupId")
  public Integer getPolicyGroupId() {
    return policyGroupId;
  }
  public void setPolicyGroupId(Integer policyGroupId) {
    this.policyGroupId = policyGroupId;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class UriTemplateDTO {\n");
    
    sb.append("  urlPattern: ").append(urlPattern).append("\n");
    sb.append("  httpVerb: ").append(httpVerb).append("\n");
    sb.append("  policyGroupName: ").append(policyGroupName).append("\n");
    sb.append("}\n");
    return sb.toString();
  }

}
