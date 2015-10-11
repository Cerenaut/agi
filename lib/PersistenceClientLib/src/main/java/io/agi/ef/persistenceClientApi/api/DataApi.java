package io.agi.ef.persistenceClientApi.api;

import io.agi.ef.persistenceClientApi.ApiException;
import io.agi.ef.persistenceClientApi.ApiClient;
import io.agi.ef.persistenceClientApi.Configuration;

import io.agi.ef.persistenceClientApi.model.*;

import java.util.*;

import io.agi.ef.persistenceClientApi.model.EntityType;
import io.agi.ef.persistenceClientApi.model.NodeModel;

import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import javax.ws.rs.core.MediaType;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

public class DataApi {
  private ApiClient apiClient;

  public DataApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DataApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  
  /**
   * Retrieves the Entity Type matching the search criteria.
   * 
   * @param id id of record
   * @param name name of Entity Type
   * @return List<EntityType>
   */
  public List<EntityType> entityTypesGet (Integer id, String name) throws ApiException {
    Object postBody = null;
    

    // create path and map variables
    String path = "/entity_types".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, String> formParams = new HashMap<String, String>();

    if (id != null)
      queryParams.put("id", apiClient.parameterToString(id));
    if (name != null)
      queryParams.put("name", apiClient.parameterToString(name));
    

    

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
        return (List<EntityType>) apiClient.deserialize(response, "array", EntityType.class);
      }
      else {
        return null;
      }
    } catch (ApiException ex) {
      throw ex;
    }
  }
  
  /**
   * Creates a new Entity Type.
   * 
   * @param body The Entity Type to add to DB.
   * @return void
   */
  public void entityTypesPost (EntityType body) throws ApiException {
    Object postBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
       throw new ApiException(400, "Missing the required parameter 'body' when calling entityTypesPost");
    }
    

    // create path and map variables
    String path = "/entity_types".replaceAll("\\{format\\}","json");

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
      String response = apiClient.invokeAPI(path, "POST", queryParams, postBody, headerParams, formParams, accept, contentType, authNames);
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
   * Retrieves the Nodes matching the search criteria.
   * 
   * @param id id of record
   * @param name name of node
   * @param host name of host
   * @param port port number
   * @return List<NodeModel>
   */
  public List<NodeModel> nodesGet (Integer id, String name, String host, Integer port) throws ApiException {
    Object postBody = null;
    

    // create path and map variables
    String path = "/nodes".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, String> formParams = new HashMap<String, String>();

    if (id != null)
      queryParams.put("id", apiClient.parameterToString(id));
    if (name != null)
      queryParams.put("name", apiClient.parameterToString(name));
    if (host != null)
      queryParams.put("host", apiClient.parameterToString(host));
    if (port != null)
      queryParams.put("port", apiClient.parameterToString(port));
    

    

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
        return (List<NodeModel>) apiClient.deserialize(response, "array", NodeModel.class);
      }
      else {
        return null;
      }
    } catch (ApiException ex) {
      throw ex;
    }
  }
  
  /**
   * Creates a new Node entry.
   * 
   * @param body The Node to add to DB.
   * @return void
   */
  public void nodesPost (NodeModel body) throws ApiException {
    Object postBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
       throw new ApiException(400, "Missing the required parameter 'body' when calling nodesPost");
    }
    

    // create path and map variables
    String path = "/nodes".replaceAll("\\{format\\}","json");

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
      String response = apiClient.invokeAPI(path, "POST", queryParams, postBody, headerParams, formParams, accept, contentType, authNames);
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
