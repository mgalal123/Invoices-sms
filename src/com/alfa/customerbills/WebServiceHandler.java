package com.alfa.customerbills;

//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Map;
//import java.util.Properties;
//import java.util.logging.Logger;
//
//import com.google.api.client.http.ByteArrayContent;
//import com.google.api.client.http.GenericUrl;
//import com.google.api.client.http.HttpRequest;
//import com.google.api.client.http.HttpRequestFactory;
//import com.google.api.client.http.HttpResponse;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.JsonObjectParser;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.client.util.GenericData;
//import com.google.auth.oauth2.IdToken;
//import com.google.auth.oauth2.IdTokenProvider;
//import com.google.auth.oauth2.ServiceAccountCredentials;
//
//import static com.alfa.customerbills.ConnectorUtil.duration;
//import static com.alfa.customerbills.ConnectorUtil.getPath;

class WebServiceHandler {
    
//    private static Logger logger = Logger.getLogger(WebServiceHandler.class.getName());
//
//    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
//    private static final com.google.api.client.json.JsonFactory JSON_FACTORY = new GsonFactory();
//
//    private final Properties config;
//    private IdToken token;
//
//    WebServiceHandler(Properties config) {
//        this.config = config;
//    }
//
//    public ArrayList<Map<String, Object>> connect(String url, String json) throws IOException {
//        if (token == null || System.currentTimeMillis() > (token.getExpirationTime().getTime() - 20_000)) {
//            retrieveAccessToken(url);
//        }
//        HttpResponse response = connectToWebService(url, json);
//        return parseResponse(response);
//    }
//
//    private ArrayList<Map<String, Object>> parseResponse(HttpResponse response) throws IOException {
//        GenericData responseData = (GenericData) response.parseAs(GenericData.class);
//        return (ArrayList<Map<String, Object>>) responseData.get("results");
//    }
//
//    private void retrieveAccessToken(String url) throws IOException {
//        logger.info("Retrieving Google access token...");
//        long t0 = System.currentTimeMillis();
//
//        ServiceAccountCredentials credential = (ServiceAccountCredentials) ServiceAccountCredentials.fromStream(new FileInputStream(getPath("flexforms-invoker.json")));
//        credential = (ServiceAccountCredentials) credential.createScoped(Arrays.asList("read", "write"));
//        token = credential.idTokenWithAudience(url, Arrays.asList(IdTokenProvider.Option.FORMAT_FULL, IdTokenProvider.Option.LICENSES_TRUE));
//
//        logger.info("Retrieved Google access token in " + duration(t0));
//        logger.fine("id token=" + token.getTokenValue()); // logging trace
//        logger.fine("" + token.getExpirationTime()); // logging trace
//
//        // Should not need this code as as Expiration is 1 hour
//        //credential.refreshIfExpired();
//        //AccessToken accessToken = credential.refreshAccessToken();
//    }
//
//    private HttpResponse connectToWebService(String url, String json) throws IOException {
//        long t0 = System.currentTimeMillis();
//        logger.info("Connecting to web service...");
//        int timeout = getTimeout();
//        logger.info("Timeout = " + timeout + " min");
//        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory((request) -> {
//            request.getHeaders().put("Authorization", Arrays.asList("Bearer " + token.getTokenValue()));
//            request.setConnectTimeout(timeout * 60_000);
//            request.setReadTimeout(timeout * 60_000);
//        });
//        HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(url), new ByteArrayContent("application/json", json.getBytes()));
//        request.setParser(new JsonObjectParser(JSON_FACTORY));
//
//        HttpResponse response = request.execute();
//        logger.info("Connected to web service in " + duration(t0));
//        return response;
//    }
//
//    private int getTimeout() {
//        try {
//            return Integer.parseInt(config.getProperty("serviceTimeout", "10"));
//
//        } catch (Exception e) {
//            return 10;
//        }
//    }

}
