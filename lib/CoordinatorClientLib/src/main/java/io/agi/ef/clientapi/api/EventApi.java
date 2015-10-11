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

public class EventApi {
  private ApiClient apiClient;

  public EventApi() {
    this(Configuration.getDefaultApiClient());
  }

  public EventApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  
  /**
   * Notify the system of a data event.
   * Dynamically creates an entity of the specified type, using the factories registered within the Java code.\n
   * @param name Name of the entity to be created.
   * @param type Type of the entity to be created.
   * @param parent Name of the parent entity of the entity to be created.
   * @param config A JSON object as a string that describes the configuration of the entity to be created, such as parameter overrides.
   * @return void
   */
  public void apiCreateGet (String name, String type, String parent, String config) throws ApiException {
    Object postBody = null;
    
    // verify the required parameter 'name' is set
    if (name == null) {
       throw new ApiException(400, "Missing the required parameter 'name' when calling apiCreateGet");
    }
    
    // verify the required parameter 'type' is set
    if (type == null) {
       throw new ApiException(400, "Missing the required parameter 'type' when calling apiCreateGet");
    }
    
    // verify the required parameter 'parent' is set
    if (parent == null) {
       throw new ApiException(400, "Missing the required parameter 'parent' when calling apiCreateGet");
    }
    
    // verify the required parameter 'config' is set
    if (config == null) {
       throw new ApiException(400, "Missing the required parameter 'config' when calling apiCreateGet");
    }
    

    // create path and map variables
    String path = "/api/create".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, String> formParams = new HashMap<String, String>();

    if (name != null)
      queryParams.put("name", apiClient.parameterToString(name));
    if (type != null)
      queryParams.put("type", apiClient.parameterToString(type));
    if (parent != null)
      queryParams.put("parent", apiClient.parameterToString(parent));
    if (config != null)
      queryParams.put("config", apiClient.parameterToString(config));
    

    

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
  
  /**
   * Notify the system of a data event.
   * These events allow Entities to wait on modifications to input data. These events are automatically fired when data is modified by the Java API, but may need to be called when data is changed from other APIs.\n
   * @param entity Name of the data affected.
   * @param action Action affecting the data.
   * @return void
   */
  public void apiDataGet (String entity, String action) throws ApiException {
    Object postBody = null;
    
    // verify the required parameter 'entity' is set
    if (entity == null) {
       throw new ApiException(400, "Missing the required parameter 'entity' when calling apiDataGet");
    }
    
    // verify the required parameter 'action' is set
    if (action == null) {
       throw new ApiException(400, "Missing the required parameter 'action' when calling apiDataGet");
    }
    

    // create path and map variables
    String path = "/api/data".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, String> formParams = new HashMap<String, String>();

    if (entity != null)
      queryParams.put("entity", apiClient.parameterToString(entity));
    if (action != null)
      queryParams.put("action", apiClient.parameterToString(action));
    

    

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
  
  /**
   * Notify the system of an entity/action event.
   * This can be used to trigger an action by an entity, or to notify the system that an entity has completed and action, which in turn may cause further events. For example, event/experiment1/step will cause the experiment to step and emit event/experiment1/stepped. This in turn will be noticed by other entities (such as Agents), who will step themselves.\n
   * @param entity Name of the entity affected.
   * @param action Action affecting the entity.
   * @return void
   */
  public void apiEntityGet (String entity, String action) throws ApiException {
    Object postBody = null;
    
    // verify the required parameter 'entity' is set
    if (entity == null) {
       throw new ApiException(400, "Missing the required parameter 'entity' when calling apiEntityGet");
    }
    
    // verify the required parameter 'action' is set
    if (action == null) {
       throw new ApiException(400, "Missing the required parameter 'action' when calling apiEntityGet");
    }
    

    // create path and map variables
    String path = "/api/entity".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, String> formParams = new HashMap<String, String>();

    if (entity != null)
      queryParams.put("entity", apiClient.parameterToString(entity));
    if (action != null)
      queryParams.put("action", apiClient.parameterToString(action));
    

    

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
