package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-03-31T05:35:13.991Z")
public class AppListDTO {
  
  private Integer count = null;
  private String next = null;
  private String previous = null;
  private List<AppInfoDTO> list = new ArrayList<>();

  
  /**
   * Number of App returned.
   **/
  public AppListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of App returned.")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  
  /**
   * Link to the next subset of resources qualified. \nEmpty if no more resources are to be returned.
   **/
  public AppListDTO next(String next) {
    this.next = next;
    return this;
  }

  
  @ApiModelProperty(example = "/app?limit=1&amp;offset=2&amp;query=", value = "Link to the next subset of resources qualified. \nEmpty if no more resources are to be returned.")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }

  
  /**
   * Link to the previous subset of resources qualified. \nEmpty if current subset is the first subset returned.
   **/
  public AppListDTO previous(String previous) {
    this.previous = previous;
    return this;
  }

  
  @ApiModelProperty(example = "/app?limit=1&amp;offset=0&amp;query=", value = "Link to the previous subset of resources qualified. \nEmpty if current subset is the first subset returned.")
  @JsonProperty("previous")
  public String getPrevious() {
    return previous;
  }
  public void setPrevious(String previous) {
    this.previous = previous;
  }

  
  /**
   **/
  public AppListDTO list(List<AppInfoDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<AppInfoDTO> getList() {
    return list;
  }
  public void setList(List<AppInfoDTO> list) {
    this.list = list;
  }

  

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AppListDTO appListDTO = (AppListDTO) o;
    return Objects.equals(count, appListDTO.count) &&
        Objects.equals(next, appListDTO.next) &&
        Objects.equals(previous, appListDTO.previous) &&
        Objects.equals(list, appListDTO.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, next, previous, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppList {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

