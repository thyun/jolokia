package org.jolokia.push;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jolokia.util.JulLogHandler;
import org.jolokia.util.LogHandler;

/**
 * HttpPostLogger
 */
public class HttpPostLogger implements EventLogger {
	private static final LogHandler logHandler = new JulLogHandler();
    public static final String URL_PROP_NAME = "url";

    private String url;
    private CloseableHttpClient httpClient;

    public HttpPostLogger(Map<String, Object> props) throws NoSuchAlgorithmException {
        this.url = (String) props.get(URL_PROP_NAME);
        SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(SSLContext.getDefault(), new NoopHostnameVerifier());
        this.httpClient = HttpClientBuilder.create().setSSLSocketFactory(sf).build();
    }

    public void logEvent(String event, Map<String, Object> producerConfig) {
        logEvent(event);
    }
    
    public void logEvent(String event) {
    		CloseableHttpResponse response = null;
    		
        try {
            HttpPost request = new HttpPost(url);
            StringEntity input = new StringEntity(event);
            input.setContentType("application/json");
            request.setEntity(input);
            response = httpClient.execute(request);
            if (response != null) {
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) 
                    		EntityUtils.consume(resEntity);
            }
        } catch (Exception e) {
        		logHandler.error("HttpPostLogger logEvent exception", e);
        } finally {
            try { if (response != null) response.close(); } catch (IOException e) { }
        }
    }

    public void shutdown() {
        try {
            httpClient.close();
        } catch (IOException e) { 
        		logHandler.error("HttpPostLogger shutdown exception", e);
        }
    }
}
