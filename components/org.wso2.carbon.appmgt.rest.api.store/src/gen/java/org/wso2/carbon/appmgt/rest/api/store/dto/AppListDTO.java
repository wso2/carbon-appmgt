package org.wso2.carbon.appmgt.rest.api.store.dto;

import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;


@ApiModel(description = "")
public class AppListDTO  {



  private Integer count = null;


  private String next = null;


  private String previous = null;


  private List<AppDTO> appList = new ArrayList<AppDTO>();


  private List<AppSummaryDTO> appSummaryList = new ArrayList<AppSummaryDTO>();


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
  @JsonProperty("appSummaryList")
  public List<AppSummaryDTO> getAppSummaryList() {
    return appSummaryList;
  }
  public void setAppSummaryList(List<AppSummaryDTO> appSummaryList) {
    this.appSummaryList = appSummaryList;
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
    sb.append("  appSummaryList: ").append(appSummaryList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
