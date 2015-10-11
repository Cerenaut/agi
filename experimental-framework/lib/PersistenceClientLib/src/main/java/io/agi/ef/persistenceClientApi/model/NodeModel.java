package io.agi.ef.persistenceClientApi.model;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;


@ApiModel(description = "")
public class NodeModel  {
  
  private String name = null;
  private String host = null;
  private Integer port = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("host")
  public String getHost() {
    return host;
  }
  public void setHost(String host) {
    this.host = host;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("port")
  public Integer getPort() {
    return port;
  }
  public void setPort(Integer port) {
    this.port = port;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class NodeModel {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  host: ").append(host).append("\n");
    sb.append("  port: ").append(port).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
