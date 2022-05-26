package org.shipkit.changelog;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Wrapper for making REST requests to Github API
 */
public class GithubApi {

    private static final Logger LOG = Logging.getLogger(GithubApi.class);

    private final String authToken;

    public GithubApi(String authToken) {
        this.authToken = authToken;
    }

    public String post(String url, String body) throws IOException {
        return doRequest(url, "POST", Optional.of(body)).content;
    }

    public String patch(String url, String body) throws IOException {
        return doRequest(url, "PATCH", Optional.of(body)).content;
    }

    public Response get(String url) throws IOException {
        return doRequest(url, "GET", Optional.empty());
    }

    private Response doRequest(String urlString, String method, Optional<String> body) throws IOException {
        URL url = new URL(urlString);

        HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
        //workaround for Java limitation (https://bugs.openjdk.java.net/browse/JDK-7016595), works with GitHub REST API
        if (method.equals("PATCH")) {
            c.setRequestMethod("POST");
        }
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
        if (method.equals("PATCH")) {
            c.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        }
        if (authToken != null) {
            c.setRequestProperty("Authorization", "token " + authToken);
        }

        if (body.isPresent()) {
            try (OutputStream os = c.getOutputStream()) {
                os.write(body.get().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        }

        String resetInLocalTime = resetLimitInLocalTimeOrEmpty(c);

        String rateRemaining = c.getHeaderField("X-RateLimit-Remaining");
        String rateLimit = c.getHeaderField("X-RateLimit-Limit");
        //TODO instead of a lifecycle message, we should include the rate limiting information only when the request fails
        LOG.lifecycle("Github API rate info => Remaining : " + rateRemaining + ", Limit : " + rateLimit + ", Reset at: " + resetInLocalTime);

        String linkHeader = c.getHeaderField("Link");
        LOG.info("Next page 'Link' from Github: {}", linkHeader);

        String content = call(method, c);
        return new Response(content, linkHeader);
    }

    private String resetLimitInLocalTimeOrEmpty(URLConnection urlConnection) {
        String rateLimitReset = urlConnection.getHeaderField("X-RateLimit-Reset");
        if (rateLimitReset == null) {
            return "";
        }
        Date resetInEpochSeconds = DateUtil.parseDateInEpochSeconds(rateLimitReset);
        return DateUtil.formatDateToLocalTime(resetInEpochSeconds, TimeZone.getDefault());
    }

    private String call(String method, HttpsURLConnection conn) throws IOException {
        if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
            return IOUtil.readFully(conn.getInputStream());
        } else {
            String errorMessage =
                String.format("%s %s failed, response code = %s, response body:%n%s",
                    method, conn.getURL(), conn.getResponseCode(), IOUtil.readFully(conn.getErrorStream()));
            throw new ResponseException(conn.getResponseCode(), errorMessage);
        }
    }

    public static class ResponseException extends IOException {
        public final int responseCode;

        public ResponseException(int responseCode, String errorMessage) {
            super(errorMessage);
            this.responseCode = responseCode;
        }
    }

    public static class Response {

        private final String content;
        private final String linkHeader;

        public Response(String content, String linkHeader) {
            this.content = content;
            this.linkHeader = linkHeader;
        }

        public String getLinkHeader() {
            return linkHeader;
        }

        public String getContent() {
            return content;
        }
    }
}
