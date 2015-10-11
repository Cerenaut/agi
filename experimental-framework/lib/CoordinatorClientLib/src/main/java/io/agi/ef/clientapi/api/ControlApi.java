package io.agi.ef.clientapi.api;

import io.agi.ef.clientapi.ApiException;
import io.agi.ef.clientapi.ApiClient;
import io.agi.ef.clientapi.Configuration;

import io.agi.ef.clientapi.model.*;

import java.util.*;

import io.agi.ef.clientapi.model.TStamp;
import io.agi.ef.clientapi.model.Error;

import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import javax.ws.rs.core.MediaType;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

public class ControlApi {
  private ApiClient apiClient;

  public ControlApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ControlApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  
  /**
   * Send command to a specific entity..
   * Send a control command signal to an entity. It can consist of Step, Stop, Start, Pause and Resume.\n
   * @param entityName The name of the entity to receive command.
   * @param command The command to send.
   * @return TStamp
   */
  public TStamp controlEntityEntityNameCommandCommandGet (String entityName, String command) throws ApiException {
    Object postBody = null;
    
    // verify the required parameter 'entityName' is set
    if (entityName == null) {
       throw new ApiException(400, "Missing the required parameter 'entityName' when calling controlEntityEntityNameCommandCommandGet");
    }
    
    // verify the required parameter 'command' is set
    if (command == null) {
       throw new ApiException(400, "Missing the required parameter 'command' when calling controlEntityEntityNameCommandCommandGet");
    }
    

    // create path and map variables
    String path = "/control/entity/{entityName}/command/{command}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "entityName" + "\\}", apiClient.escapeString(entityName.toString()))
      .replaceAll("\\{" + "command" + "\\}", apiClient.escapeString(command.toString()));

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
        return (TStamp) apiClient.deserialize(response, "", TStamp.class);
      }
      else {
        return null;
      }
    } catch (ApiException ex) {
      throw ex;
    }
  }
  
  /**
   * Get the status for a specific entity.
   * Get the status of a particular state, Paused, Running and Stopping.\n
   * @param entityName The name of the entity to receive status request.
   * @param state The status returns refers to this state.
   * @return Boolean
   */
  public Boolean controlEntityEntityNameStatusStateGet (String entityName, String state) throws ApiException {
    Object postBody = null;
    
    // verify the required parameter 'entityName' is set
    if (entityName == null) {
       throw new ApiException(400, "Missing the required parameter 'entityName' when calling controlEntityEntityNameStatusStateGet");
    }
    
    // verify the required parameter 'state' is set
    if (state == null) {
       throw new ApiException(400, "Missing the required parameter 'state' when calling controlEntityEntityNameStatusStateGet");
    }
    

    // create path and map variables
    String path = "/control/entity/{entityName}/status/{state}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "entityName" + "\\}", apiClient.escapeString(entityName.toString()))
      .replaceAll("\\{" + "state" + "\\}", apiClient.escapeString(state.toString()));

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
        return (Boolean) apiClient.deserialize(response, "", Boolean.class);
      }
      else {
        return null;
      }
    } catch (ApiException ex) {
      throw ex;
    }
  }
  
}
