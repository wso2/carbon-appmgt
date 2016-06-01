package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.PListAssetsDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class PListInfoDTO  {
  
  
  
  private List<PListAssetsDTO> pListAssets = new ArrayList<PListAssetsDTO>();
  
  
  private String bundleIdentifier = null;
  
  
  private String bundleVersion = null;
  
  
  private String kind = null;
  
  
  private String title = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("pListAssets")
  public List<PListAssetsDTO> getPListAssets() {
    return pListAssets;
  }
  public void setPListAssets(List<PListAssetsDTO> pListAssets) {
    this.pListAssets = pListAssets;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("bundleIdentifier")
  public String getBundleIdentifier() {
    return bundleIdentifier;
  }
  public void setBundleIdentifier(String bundleIdentifier) {
    this.bundleIdentifier = bundleIdentifier;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("bundleVersion")
  public String getBundleVersion() {
    return bundleVersion;
  }
  public void setBundleVersion(String bundleVersion) {
    this.bundleVersion = bundleVersion;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("kind")
  public String getKind() {
    return kind;
  }
  public void setKind(String kind) {
    this.kind = kind;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PListInfoDTO {\n");
    
    sb.append("  pListAssets: ").append(pListAssets).append("\n");
    sb.append("  bundleIdentifier: ").append(bundleIdentifier).append("\n");
    sb.append("  bundleVersion: ").append(bundleVersion).append("\n");
    sb.append("  kind: ").append(kind).append("\n");
    sb.append("  title: ").append(title).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
