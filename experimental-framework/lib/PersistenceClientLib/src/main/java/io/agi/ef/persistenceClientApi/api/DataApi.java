package io.agi.ef.persistenceClientApi.api;

import com.sun.jersey.api.client.GenericType;

import io.agi.ef.persistenceClientApi.ApiException;
import io.agi.ef.persistenceClientApi.ApiClient;
import io.agi.ef.persistenceClientApi.Configuration;
import io.agi.ef.persistenceClientApi.Pair;

import io.agi.ef.persistenceClientApi.model.EntityType;
import io.agi.ef.persistenceClientApi.model.NodeModel;

import java.util.*;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-01-31T13:50:39.152+11:00")
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
   * Retrieves Entity Type.
   * Retrieves the Entity Type matching the search criteria.
   * @param id id of record
   * @param name name of Entity Type
   * @return List<EntityType>
   */
  public List<EntityType> entityTypesGet(Integer id, String name) throws ApiException {
    Object postBody = null;
    
    // create path and map variables
    String path = "/entity_types".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    
    queryParams.addAll(apiClient.parameterToPairs("", "id", id));
    
    queryParams.addAll(apiClient.parameterToPairs("", "name", name));
    

    

    

    final String[] accepts = {
      "application/json"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<List<EntityType>> returnType = new GenericType<List<EntityType>>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * Create new Entity Type.
   * Creates a new Entity Type.
   * @param body The Entity Type to add to DB.
   * @return void
   */
  public void entityTypesPost(EntityType body) throws ApiException {
    Object postBody = body;
    
     // verify the required parameter 'body' is set
     if (body == null) {
        throw new ApiException(400, "Missing the required parameter 'body' when calling entityTypesPost");
     }
     
    // create path and map variables
    String path = "/entity_types".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    apiClient.invokeAPI(path, "POST", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, null);
    
  }
  
  /**
   * Retrieve Nodes.
   * Retrieves the Nodes matching the search criteria.
   * @param id id of record
   * @param name name of node
   * @param host name of host
   * @param port port number
   * @return List<NodeModel>
   */
  public List<NodeModel> nodesGet(Integer id, String name, String host, Integer port) throws ApiException {
    Object postBody = null;
    
    // create path and map variables
    String path = "/nodes".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    
    queryParams.addAll(apiClient.parameterToPairs("", "id", id));
    
    queryParams.addAll(apiClient.parameterToPairs("", "name", name));
    
    queryParams.addAll(apiClient.parameterToPairs("", "host", host));
    
    queryParams.addAll(apiClient.parameterToPairs("", "port", port));
    

    

    

    final String[] accepts = {
      "application/json"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<List<NodeModel>> returnType = new GenericType<List<NodeModel>>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * Creates a new Node entry.
   * Creates a new Node entry.
   * @param body The Node to add to DB.
   * @return void
   */
  public void nodesPost(NodeModel body) throws ApiException {
    Object postBody = body;
    
     // verify the required parameter 'body' is set
     if (body == null) {
        throw new ApiException(400, "Missing the required parameter 'body' when calling nodesPost");
     }
     
    // create path and map variables
    String path = "/nodes".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    apiClient.invokeAPI(path, "POST", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, null);
    
  }
  
}
