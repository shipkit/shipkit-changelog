package org.shipkit.changelog;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import java.io.IOException;

/**
 * This class contains standard operations for skim over GitHub API responses.
 */
class GitHubListFetcher {

    private static final String NO_MORE_PAGES = "none";
    private final String githubToken;
    private String nextPageUrl;

    GitHubListFetcher(String apiUrl, String repository, String githubToken) {
        this.githubToken = githubToken;

        // see API doc: https://developer.github.com/v3/issues/
        nextPageUrl = apiUrl + "/repos/" + repository + "/issues?page=1"
                + "&per_page=100" //default page is 30
                + "&state=closed" //default state is open
                + "&filter=all" //default filter is 'assigned'
                + "&direction=desc"; //default is desc but setting it explicitly just in case
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

        GitHubApi api = new GitHubApi(githubToken);
        GitHubApi.Response response = api.get(nextPageUrl);

        nextPageUrl = getNextPageUrl(response.getLinkHeader());

        return parseJsonFrom(response.getContent());
    }

    private JsonArray parseJsonFrom(String content) {
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
