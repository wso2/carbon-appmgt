package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO to represent a custom property of an app.
 */

@ApiModel(description = "The DTO for a custom app property")
public class CustomPropertyDTO {


    private String name;


    private String value;


    /**
     * name
     **/
    @ApiModelProperty(value = "name")
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    /**
     * name
     **/
    @ApiModelProperty(value = "value")
    @JsonProperty("value")
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class CustomPropertyDTO {\n");

        sb.append("  name: ").append(name).append("\n");
        sb.append("  value: ").append(value).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

}
