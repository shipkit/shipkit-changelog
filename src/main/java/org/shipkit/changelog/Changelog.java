package org.shipkit.changelog;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Arrays.asList;

public class Changelog {

    public static void main(String[] args) {
        String checkoutDir = "/Users/sfaber/shipkit/auto-version";
        String readOnlyToken = "a0a4c0f41c200f7c653323014d6a72a127764e17";
        String ghApiUrl = "https://api.github.com";
        String repo = "shipkit/shipkit-auto-version";
        String ghRepoUrl = "https://github.com" + repo;
        String prevRev = "v0.0.34";
        String newVer = "0.2.0";
        String newRev = "v" + newVer;

        ProcessRunner runner = new ProcessRunner(new File(checkoutDir));
        GitLogProvider logProvider = new GitLogProvider(runner);
        Collection<GitCommit> commits = new GitCommitProvider(logProvider).getCommits(prevRev, "HEAD");
        System.out.println("Completed!");

        List<String> tickets = new LinkedList<>();
        Set<String> contributors = new TreeSet<>();
        for (GitCommit c : commits) {
            tickets.addAll(c.getTickets());
            contributors.add(c.getAuthorName());
        }

        GitHubTicketFetcher fetcher = new GitHubTicketFetcher();

        Collection<Improvement> improvements = fetcher.fetchTickets(ghApiUrl, repo, readOnlyToken,
                tickets, asList(), false);

        String changelog = ChangelogFormat.formatChangelog(contributors, improvements, commits.size(), newVer,
                newRev, prevRev, ghRepoUrl);

        System.out.println(changelog);
        postRelease(changelog, repo, newVer);
    }

    private static void postRelease(String text, String repo, String newVer) {
        String url = "/repos/" + repo + "/releases";

        JsonObject body = new JsonObject();
        body.add("tag_name", newVer);
        body.add("name", newVer);
        body.add("body", text);

        GitHubApi gitHubApi = new GitHubApi("https://api.github.com", "secret");
        try {
            String response = gitHubApi.post(url, body.toString());
            String htmlUrl = Json.parse(response).asObject().getString("html_url", "");
            System.out.println("Successfully updated release notes on GitHub: " + htmlUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
