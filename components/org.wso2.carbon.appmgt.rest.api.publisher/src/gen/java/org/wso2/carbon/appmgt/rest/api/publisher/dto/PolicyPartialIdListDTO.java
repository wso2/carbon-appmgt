package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.Object;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class PolicyPartialIdListDTO  {
  
  
  
  private Object xacmlPolicyIds = null;

  
  /**
   * List of XACML policy partial Id's
   **/
  @ApiModelProperty(value = "List of XACML policy partial Id's")
  @JsonProperty("xacmlPolicyIds")
  public Object getXacmlPolicyIds() {
    return xacmlPolicyIds;
  }
  public void setXacmlPolicyIds(Object xacmlPolicyIds) {
    this.xacmlPolicyIds = xacmlPolicyIds;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyPartialIdListDTO {\n");
    
    sb.append("  xacmlPolicyIds: ").append(xacmlPolicyIds).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
