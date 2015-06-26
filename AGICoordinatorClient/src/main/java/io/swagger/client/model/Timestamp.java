package io.swagger.client.model;

import java.math.BigDecimal;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;


@ApiModel(description = "")
public class Timestamp  {
  
  private BigDecimal productId = null;

  
  /**
   * Timestamp of the current step of the coordinator.
   **/
  @ApiModelProperty(value = "Timestamp of the current step of the coordinator.")
  @JsonProperty("product_id")
  public BigDecimal getProductId() {
    return productId;
  }
  public void setProductId(BigDecimal productId) {
    this.productId = productId;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Timestamp {\n");
    
    sb.append("  productId: ").append(productId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
