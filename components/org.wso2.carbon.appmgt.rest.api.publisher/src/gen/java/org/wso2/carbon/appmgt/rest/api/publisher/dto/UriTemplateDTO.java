package org.wso2.carbon.appmgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class UriTemplateDTO  {
  
  
  
  private String urlPattern = null;
  
  
  private String httpVerb = null;
  
  
  private String policyGroupName = null;

  
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
