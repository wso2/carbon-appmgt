package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;



@ApiModel(description = "")
public class AppListDTO  {
  
  
  
  private Integer count = null;
  
  
  private String next = null;
  
  
  private String previous = null;


  private List<AppDTO> appList = new ArrayList<AppDTO>();


  private List<AppInfoDTO> appInfoList = new ArrayList<AppInfoDTO>();

  
  /**
   * Number of App returned.
   **/
  @ApiModelProperty(value = "Number of App returned.")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  
  /**
   * Link to the next subset of resources qualified.\nEmpty if no more resources are to be returned.
   **/
  @ApiModelProperty(value = "Link to the next subset of resources qualified.\nEmpty if no more resources are to be returned.")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }

  
  /**
   * Link to the previous subset of resources qualified.\nEmpty if current subset is the first subset returned.
   **/
  @ApiModelProperty(value = "Link to the previous subset of resources qualified.\nEmpty if current subset is the first subset returned.")
  @JsonProperty("previous")
  public String getPrevious() {
    return previous;
  }
  public void setPrevious(String previous) {
    this.previous = previous;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("appInfoList")
  public List<AppInfoDTO> getAppInfoList() {
    return appInfoList;
  }
  public void setAppInfoList(List<AppInfoDTO> appInfoList) {
    this.appInfoList = appInfoList;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("appList")
  public List<AppDTO> getAppList() {
    return appList;
  }
  public void setAppList(List<AppDTO> appList) {
    this.appList = appList;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  next: ").append(next).append("\n");
    sb.append("  previous: ").append(previous).append("\n");
    sb.append("  appList: ").append(appList).append("\n");
    sb.append("  appInfoList: ").append(appInfoList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
