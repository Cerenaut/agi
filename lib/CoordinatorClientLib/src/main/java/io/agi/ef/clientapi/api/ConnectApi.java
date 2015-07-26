package io.agi.ef.clientapi.api;

import io.agi.ef.clientapi.ApiException;
import io.agi.ef.clientapi.ApiClient;
import io.agi.ef.clientapi.Configuration;

import io.agi.ef.clientapi.model.*;

import java.util.*;


import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import javax.ws.rs.core.MediaType;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

public class ConnectApi {
  private ApiClient apiClient;

  public ConnectApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ConnectApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  
  /**
   * Connect to Agent
   * Send request to connect this Agent server as specified url base path.\n
   * @param baseurl ID of agent to connect to
   * @return void
   */
  public void connectAgentBaseurlGet (String baseurl) throws ApiException {
    Object postBody = null;
    
    // verify the required parameter 'baseurl' is set
    if (baseurl == null) {
       throw new ApiException(400, "Missing the required parameter 'baseurl' when calling connectAgentBaseurlGet");
    }
    

    // create path and map variables
    String path = "/connect/agent/{baseurl}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "baseurl" + "\\}", apiClient.escapeString(baseurl.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, String> formParams = new HashMap<String, String>();

    

    

    final String[] accepts = {
      
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    if(contentType.startsWith("multipart/form-data")) {
      boolean hasFields = false;
      FormDataMultiPart mp = new FormDataMultiPart();
      
      if(hasFields)
        postBody = mp;
    }
    else {
      
    }

    try {
      String[] authNames = new String[] {  };
      String response = apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames);
      if(response != null){
        return ;
      }
      else {
        return ;
      }
    } catch (ApiException ex) {
      throw ex;
    }
  }
  
}
