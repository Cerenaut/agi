package io.agi.ef.clientapi.model;

import java.math.BigDecimal;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;


@ApiModel(description = "")
public class State  {
  
  private BigDecimal stateId = null;

  
  /**
   * The state.
   **/
  @ApiModelProperty(value = "The state.")
  @JsonProperty("state_id")
  public BigDecimal getStateId() {
    return stateId;
  }
  public void setStateId(BigDecimal stateId) {
    this.stateId = stateId;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class State {\n");
    
    sb.append("  stateId: ").append(stateId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
