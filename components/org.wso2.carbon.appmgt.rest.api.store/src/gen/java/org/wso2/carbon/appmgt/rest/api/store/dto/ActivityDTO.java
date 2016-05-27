package org.wso2.carbon.appmgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "")
public class ActivityDTO {


    private String activityId = null;


    private String code = null;

    public enum TypeEnum {
        CONFIG, MESSAGE, INFO, COMMAND, PROFILE, POLICY,
    }


    private TypeEnum type = null;


    private String createdTimeStamp = null;


    /**
     * Activity Id return for the operation.
     **/
    @ApiModelProperty(value = "Activity Id return for the operation.")
    @JsonProperty("activityId")
    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }


    /**
     * Operation code
     **/
    @ApiModelProperty(value = "Operation code")
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    /**
     * Type of the operation.
     **/
    @ApiModelProperty(value = "Type of the operation.")
    @JsonProperty("type")
    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }


    /**
     * Time of the operation creation.
     **/
    @ApiModelProperty(value = "Time of the operation creation.")
    @JsonProperty("createdTimeStamp")
    public String getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(String createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ActivityDTO {\n");

        sb.append("  activityId: ").append(activityId).append("\n");
        sb.append("  code: ").append(code).append("\n");
        sb.append("  type: ").append(type).append("\n");
        sb.append("  createdTimeStamp: ").append(createdTimeStamp).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
