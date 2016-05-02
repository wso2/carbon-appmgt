package org.wso2.carbon.appmgt.rest.api.store.dto;

import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class TagListDTO  {
  
  
  
  private List<String> tags = new ArrayList<String>();

  
  /**
   * List of Tags
   **/
  @ApiModelProperty(value = "List of Tags")
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TagListDTO {\n");
    
    sb.append("  tags: ").append(tags).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
