package org.ozsoft.httpclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * HTTP DELETE request.
 * 
 * @author Oscar Stigter
 */
public class HttpDelete extends HttpRequest {

    /**
     * Constructs an HTTP DELETE request.
     * 
     * @param client
     *            The HTTP client.
     * @param url
     *            The URL.
     */
    /* package */HttpDelete(HttpClient client, String url) {
        super(client, url);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ozsoft.httpclient.HttpRequest#execute()
     */
    @Override
    public HttpResponse execute() throws MalformedURLException, IOException {
        client.updateProxySettings();
        URL url = new URL(this.url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);
        int statusCode = con.getResponseCode();
        String statusMessage = con.getResponseMessage();
        String responseBody = getResponseBody(statusCode, con);
        con.disconnect();
        return new HttpResponse(statusCode, statusMessage, responseBody);
    }

}
