package io.agi.ef.serverapi.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-02-01T23:41:44.379+11:00")
public class TStamp   {
  
  private BigDecimal timeId = null;

  
  /**
   * Timestamp of the current step of the server.
   **/
  
  @ApiModelProperty(value = "Timestamp of the current step of the server.")
  @JsonProperty("time_id")
  public BigDecimal getTimeId() {
    return timeId;
  }
  public void setTimeId(BigDecimal timeId) {
    this.timeId = timeId;
  }

  

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TStamp tStamp = (TStamp) o;
    return Objects.equals(timeId, tStamp.timeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TStamp {\n");
    
    sb.append("    timeId: ").append(toIndentedString(timeId)).append("\n");
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

