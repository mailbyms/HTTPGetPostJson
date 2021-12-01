/**
 * HTTP post 和 HTTP get 的示例写法，经过 SonarLint 的扫描
 * DefaultHttpClient 已过时，应使用 CloseableHttpClient 和 CloseableHttpResponse
 * 官方示例 https://hc.apache.org/httpcomponents-client-5.1.x/quickstart.html
 */

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 代码来源
 * https://github.com/apache/metron/blob/master/metron-analytics/metron-maas-common/src/main/java/org/apache/metron/maas/util/RESTUtil.java
 * https://stackoverflow.com/questions/18188041/write-in-body-request-with-httpclient/18188408#18188408
 */
public enum RESTUtil {
    INSTANCE;
    public static final ThreadLocal<CloseableHttpClient> gCLIENT = ThreadLocal.withInitial(() -> HttpClientBuilder.create().build());

    private static final String CONTENT_TYPE = "application/json";

    /**
     * post 请求
     * @param endpointUrl
     * @param postArgs
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public String postRESTJSONResults(URL endpointUrl, Map<String, String> postArgs) throws IOException, URISyntaxException {
        HttpPost post = new HttpPost(endpointUrl.toURI());
        post.addHeader("Content-Type", CONTENT_TYPE);
        post.addHeader("accept", CONTENT_TYPE);

        Gson gson = new Gson();
        String argsJson = gson.toJson(postArgs);

        HttpEntity entity = new ByteArrayEntity(argsJson.getBytes(StandardCharsets.UTF_8));
        post.setEntity(entity);
        CloseableHttpResponse response = gCLIENT.get().execute(post);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IllegalStateException("HTTP error code : "
                    + response.getStatusLine().getStatusCode() + ", post -> " + endpointUrl);
        }

        return EntityUtils.toString(response.getEntity());
    }

    /**
     * get 请求
     * @param endpointUrl
     * @param getArgs
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public String getRESTJSONResults(URL endpointUrl, Map<String, String> getArgs) throws IOException, URISyntaxException {
        String encodedParams = encodeParams(getArgs);
        HttpGet get = new HttpGet(appendToUrl(endpointUrl, encodedParams).toURI());
        get.addHeader("accept", CONTENT_TYPE);
        CloseableHttpResponse response = gCLIENT.get().execute(get);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IllegalStateException("HTTP error code : "
                    + response.getStatusLine().getStatusCode() + ", get <- " + endpointUrl);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                StandardCharsets.UTF_8))){
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    public URL appendToUrl(URL endpointUrl, String params) throws MalformedURLException {
        return new URL(endpointUrl.toString() + "?" + params);
    }

    public String encodeParams(Map<String, String> params) {
        Iterable<NameValuePair> nvp = params.entrySet().stream().map(kv -> new BasicNameValuePair(kv.getKey(), kv.getValue())).collect(Collectors.toList());

        return URLEncodedUtils.format(nvp, Charset.defaultCharset());
    }
}