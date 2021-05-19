package numidia.reader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
public class ServerRequest {

    // These are pulled from gradle.properties
    String oktaDomain;
    String clientId;
    String clientSecret;
    String redirectUri;
    String scope;
    String grantType;

    public ServerRequest() {
        // Load auth info from the app.properties
        try {
            loadProperties();


            // Request the authorization code from the Okta OAuth provider

            String code = requestAuthCode();

            // Exchange the auth code for the access token

            String token = getTokenForCode(code);

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public URI getAuthorizationEndpointUri() throws URISyntaxException, MalformedURLException {

        URIBuilder builder = new URIBuilder();

        builder.setScheme("https");
        builder.setHost(oktaDomain);
        builder.setPath("/oauth2/default/v1/authorize");
        builder.addParameter("client_id", clientId);
        builder.addParameter("redirect_uri", redirectUri);
        builder.addParameter("response_type", "code");
        builder.addParameter("state", "this is a state");
        builder.addParameter("scope", scope);

        URL url = builder.build().toURL();

        return url.toURI();

    }
    /**
     * Requests an authorization code from the auth server
     *
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public String requestAuthCode() throws MalformedURLException, URISyntaxException {

        // Generate the auth endpoint URI to request the auth code

        URI authorizationEndpoint = getAuthorizationEndpointUri();

        System.out.print("Authorization Endpoint URI: ");
        System.out.println(authorizationEndpoint.toString());

        final URI redirectUri = new URI(this.redirectUri);

        // Create the user agent and make the call to the auth endpoint

//        final UserAgent userAgent = new UserAgentImpl();
//
//        final AuthorizationResponse authorizationResponse = userAgent.requestAuthorizationCode(authorizationEndpoint, redirectUri);
//
//        // We should have the code, which we can trade for the token
//
        final String code =  ""; //authorizationResponse.getCode();

        System.out.print("Authorization Code: ");
        System.out.println(code);

        return code;

    }

    /**
     * Given an authorization code, calls the auth server to request a token
     *
     * @param code
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public String getTokenForCode(String code) throws URISyntaxException, IOException, IOException {

        // The token request URL

        final String tokenUrl = "https://"+ oktaDomain +"/oauth2/default/v1/token";

        // The original redirect URL

        final URI redirectUri = new URI(this.redirectUri);

        // Using HttpClient to make the POST to exchange the auth code for the token

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(tokenUrl);

        // Adding the POST params to the request

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", grantType));
        urlParameters.add(new BasicNameValuePair("code", code));
        urlParameters.add(new BasicNameValuePair("redirect_uri", redirectUri.toString()));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        urlParameters.add(new BasicNameValuePair("client_secret", clientSecret));
        urlParameters.add(new BasicNameValuePair("scope", scope));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        // Execute the request

        HttpResponse response = client.execute(post);

        // Print the status code

        System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

        // Get the content as a String

        String content = EntityUtils.toString(response.getEntity());

        System.out.println("Result : " + content.toString());

        return content.toString();
    }

    /**
     * Loads our config info from the app.properties file
     * @throws IOException
     */
    public void loadProperties() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("app.properties");
        Properties appProps = new Properties();
        appProps.load(inputStream);
        oktaDomain = appProps.getProperty("oktaDomain");
        clientId = appProps.getProperty("oktaClientId");
        clientSecret = appProps.getProperty("oktaClientSecret");
        redirectUri = appProps.getProperty("redirectUri");
        scope = appProps.getProperty("scope");
        grantType = appProps.getProperty("grantType");
    }

    /**
     * Uses com.google.code.gson to pretty print JSON, just for fun
     * @param json
     * @return
     */
//    public static String prettyPrintJson(String json) {
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        JsonParser jp = new JsonParser();
//        JsonElement je = jp.parse(json);
//        String prettyJsonString = gson.toJson(je);
//        return prettyJsonString;
//    }
}
