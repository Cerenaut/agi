package io.agi.ef.http;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by dave on 11/09/15.
 */
public class RequestUtil {

//    public static final String METHOD_GET = "GET";
//    public static final String METHOD_POST = "POST";
//    public static final String METHOD_PUT = "PUT";
//    public static final String METHOD_PATCH = "PATCH";
//    public static final String METHOD_UPDATE = "UPDATE";
//    public static final String METHOD_DELETE = "DELETE";
//
//    public static final int DEFAULT_TIMEOUT = 20000;

    public static String postSync( String request ) {
        return doSync( new HttpPost( request ) );
    }

    public static String getSync( String request ) {
        return doSync( new HttpGet( request ) );
    }

    public static String deleteSync( String request ) {
        return doSync( new HttpDelete( request ) );
    }

    public static String patchSync( String request, String body ) {

        // http://stackoverflow.com/questions/18188041/write-in-body-request-with-httpclient
        HttpPatch httpRequest = new HttpPatch( request );
        if( body != null ) {
            ByteArrayEntity reqEntity = new ByteArrayEntity( body.getBytes() );
            httpRequest.setEntity( reqEntity );
        }

        return doSync( httpRequest );
    }

    public static String postSync( String request, String body ) {

        HttpPost httpRequest = new HttpPost( request );
        if( body != null ) {
            ByteArrayEntity reqEntity = new ByteArrayEntity( body.getBytes() );
            httpRequest.setEntity( reqEntity );
        }

        return doSync( httpRequest );
    }

    /**
     * Do a synchronous request, return the result. This means the code blocks until the request has finished.
     * Note the default timeout is 20 seconds! This is fairly typical, however.
     * This function can be used for all HTTP request methods (GET, POST etc).
     * Returns null on failure, or a string on success (maybe empty).
     *
     * @param httpRequest
     * @return
     */
    public static String doSync( HttpRequestBase httpRequest ) {

        // https://hc.apache.org/httpcomponents-client-4.5.x/tutorial/html/fundamentals.html#d5e95
        // https://hc.apache.org/httpcomponents-client-ga/quickstart.html
        String responseBody = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            try {
                // https://hc.apache.org/httpcomponents-client-4.5.x/httpclient/examples/org/apache/http/examples/client/ClientWithResponseHandler.java
                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status < 300) {
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                return EntityUtils.toString(entity);
                            } else {
                                return null;
                            }
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }
                };

                responseBody = httpClient.execute(httpRequest, responseHandler);
            }
            finally {
                httpClient.close();
            }
        }
        catch( IOException ioe ) {
            // nothing? response will be null..
        }

        return responseBody;
    }

//    public static String doSync( String request, String method, String body, int timeout ) {
//        try { // TODO: Make the below a utility
//            // http://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
//            URL url = new URL( request );
//            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
//            uc.setDoInput( true );
//            uc.setUseCaches(false);
//            uc.setConnectTimeout( timeout );
//
//            // http://stackoverflow.com/questions/25163131/httpurlconnection-invalid-http-method-patch
//            if( method != METHOD_PATCH ) {
//                uc.setRequestMethod( method );
//            }
//            else {
//                uc.setRequestProperty( "X-HTTP-Method-Override", "PATCH" );
//                uc.setRequestMethod(METHOD_POST);
//            }
//
//            //String charset = java.nio.charset.StandardCharsets.UTF_8.name();
//
//            if(    method.equalsIgnoreCase( RequestUtil.METHOD_POST  )
//                || method.equalsIgnoreCase( RequestUtil.METHOD_PATCH ) ) {
//                uc.setDoOutput( true ); // triggers a POST?
//
//                DataOutputStream dos = new DataOutputStream( uc.getOutputStream() );
//
//                if( body != null ) {
//                    dos.write( body.getBytes() );
//                }
//
//                dos.flush();
//                dos.close();
//            }
//
//            String response = "";
//
//            try {
//                BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//
//                String line = null;
//
//                while (null != (line = br.readLine())) {
//                    response = response + line;
//                }
//
//                br.close();
//            }
//            catch( Exception e ) {
//                return null;
//            }
//
//            return response;
//        }
//        catch( Exception e ) {
//            //e.printStackTrace();
//            return null;
//        }
//    }

}
