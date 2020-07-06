package org.shipkit.changelog;

import com.eclipsesource.json.JsonObject;

/**
 * Provides means to parse JsonObjects returned from calling GitHub API.
 */
class GitHubImprovementsJSON {

    /**
     * Parses GitHub JsonObject in accordance to the API (https://developer.github.com/v3/issues/)
     * @param issue
     */
    static Ticket toImprovement(JsonObject issue) {
        long id = issue.get("number").asLong();
        String issueUrl = issue.get("html_url").asString();
        String title = issue.get("title").asString();
        boolean isPullRequest = issue.get("pull_request") != null;

        return new Ticket(id, title, issueUrl, isPullRequest);
    }
}
