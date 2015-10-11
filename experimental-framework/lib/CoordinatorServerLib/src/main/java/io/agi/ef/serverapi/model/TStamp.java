package io.agi.ef.serverapi.model;

import java.math.BigDecimal;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;


@ApiModel(description = "")
public class TStamp  {
  
  private BigDecimal timeId = null;

  
  /**
   * Timestamp of the current step of the server.
   **/
  @ApiModelProperty(value = "Timestamp of the current step of the server.")
  @JsonProperty("timeId")
  public BigDecimal getTimeId() {
    return timeId;
  }
  public void setTimeId(BigDecimal timeId) {
    this.timeId = timeId;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TStamp {\n");
    
    sb.append("  timeId: ").append(timeId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
