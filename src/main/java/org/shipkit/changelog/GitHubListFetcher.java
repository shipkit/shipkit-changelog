package org.shipkit.changelog;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.logging.Logger;

/**
 * This class contains standard operations for skim over GitHub API responses.
 */
class GitHubListFetcher {

    private static final Logger LOG = Logger.getLogger(GitHubListFetcher.class.getName());

    private static final String NO_MORE_PAGES = "none";
    private final String readOnlyAuthToken;
    private final String apiUrl;
    private final String repository;
    private String nextPageUrl;

    GitHubListFetcher(String apiUrl, String repository, String readOnlyAuthToken) {
        this.apiUrl = apiUrl;
        this.repository = repository;
        this.readOnlyAuthToken = readOnlyAuthToken;
    }

    /**
     * Returns true when 'nextPage()' was not yet executed
     * OR when 'nextPage()' was executed and there are more pages
     */
    boolean hasNextPage() {
        return !NO_MORE_PAGES.equals(nextPageUrl);
    }

    /**
     * Gets the next page
     */
    JsonArray nextPage() throws IOException {
        if (!hasNextPage()) {
            throw new IllegalStateException("GitHub API has no more issues to fetch. Did you run 'hasNextPage()' method?");
        }

        // see API doc: https://developer.github.com/v3/issues/
        nextPageUrl = apiUrl + "/repos/" + repository + "/issues?page=1"
                + "&per_page=100" //default page is 30
                + "&state=closed" //default state is open
                + "&filter=all" //default filter is 'assigned'
                + "&direction=desc"; //default is desc but setting it explicitly just in case

        URL url = new URL(nextPageUrl);
        LOG.info("GET " + nextPageUrl);
        URLConnection urlConnection = url.openConnection();
        // GitHub docs on authentication: https://developer.github.com/v3/auth/#authenticating-for-saml-sso
        urlConnection.setRequestProperty("Authorization", "token " + readOnlyAuthToken);
        LOG.info("Established connection to GitHub API");

        String resetInLocalTime = resetLimitInLocalTimeOrEmpty(urlConnection);

        String rateRemaining = urlConnection.getHeaderField("X-RateLimit-Remaining");
        String rateLimit = urlConnection.getHeaderField("X-RateLimit-Limit");
        LOG.info("GitHub API rate info => Remaining : " + rateRemaining + ", Limit : " + rateLimit + ", Reset at: " + resetInLocalTime);
        nextPageUrl = getNextPageUrl(urlConnection.getHeaderField("Link"));

        return parseJsonFrom(urlConnection);
    }

    private String resetLimitInLocalTimeOrEmpty(URLConnection urlConnection) {
        String rateLimitReset = urlConnection.getHeaderField("X-RateLimit-Reset");
        if (rateLimitReset == null) {
            return "";
        }
        Date resetInEpochSeconds = DateUtil.parseDateInEpochSeconds(rateLimitReset);
        return DateUtil.formatDateToLocalTime(resetInEpochSeconds);
    }

    private JsonArray parseJsonFrom(URLConnection urlConnection) throws IOException {
        InputStream response = urlConnection.getInputStream();

        LOG.info("Reading remote stream from GitHub API");
        String content = IOUtil.readFully(response);
        LOG.info("GitHub API responded successfully.");

        JsonValue result = Json.parse(content);

        return result.asArray();
    }


    private String getNextPageUrl(String linkHeader) {
        if (linkHeader == null) {
            //expected when there are no results
            return NO_MORE_PAGES;
        }

        // See GitHub API doc : https://developer.github.com/guides/traversing-with-pagination/
        // Link: <https://api.github.com/repositories/6207167/issues?access_token=a0a4c0f41c200f7c653323014d6a72a127764e17&state=closed&filter=all&page=2>; rel="next",
        //       <https://api.github.com/repositories/62207167/issues?access_token=a0a4c0f41c200f7c653323014d6a72a127764e17&state=closed&filter=all&page=4>; rel="last"
        for (String linkRel : linkHeader.split(",")) {
            if (linkRel.contains("rel=\"next\"")) {
                return linkRel.substring(
                        linkRel.indexOf("http"),
                        linkRel.indexOf(">; rel=\"next\""));
            }
        }
        return NO_MORE_PAGES;
    }
}
