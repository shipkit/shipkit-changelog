package org.shipkit.changelog;

import com.eclipsesource.json.JsonObject;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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
        Collection<String> labels = extractLabels(issue);

        return new Ticket(id, title, issueUrl, labels, isPullRequest);
    }

    private static Collection<String> extractLabels(JsonObject issue) {
        Set<String> out = new LinkedHashSet<>();
//        JsonArray labels = (JsonArray) issue.get("labels");
//        for (Object o : labels.toArray()) {
//            JsonObject label = (JsonObject) o;
//            out.add((String) label.get("name"));
//        }
        return out;
    }
}
