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
 * Wrapper for making REST requests to GitHub API
 */
public class GitHubApi {

    private static final Logger LOG = Logging.getLogger(GitHubApi.class);

    private final String authToken;

    public GitHubApi(String authToken) {
        this.authToken = authToken;
    }

    public String post(String url, String body) throws IOException {
        return doRequest(url, "POST", Optional.of(body)).content;
    }

    public Response get(String url) throws IOException {
        return doRequest(url, "GET", Optional.empty());
    }

    private Response doRequest(String urlString, String method, Optional<String> body) throws IOException {
        URL url = new URL(urlString);

        HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
        c.setRequestMethod(method);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
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
        LOG.lifecycle("GitHub API rate info => Remaining : " + rateRemaining + ", Limit : " + rateLimit + ", Reset at: " + resetInLocalTime);

        String linkHeader = c.getHeaderField("Link");
        LOG.info("Next page 'Link' from GitHub: {}", linkHeader);

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
            throw new IOException(errorMessage);
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
