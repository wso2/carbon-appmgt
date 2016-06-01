package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.PListInfoDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class PListDTO  {
  
  
  
  private List<PListInfoDTO> pListAssets = new ArrayList<PListInfoDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("pListAssets")
  public List<PListInfoDTO> getPListAssets() {
    return pListAssets;
  }
  public void setPListAssets(List<PListInfoDTO> pListAssets) {
    this.pListAssets = pListAssets;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PListDTO {\n");
    
    sb.append("  pListAssets: ").append(pListAssets).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
