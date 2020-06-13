package org.shipkit.changelog;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

public class Changelog {

    public static void main(String[] args) {
        ProcessRunner runner = new ProcessRunner(new File("/Users/sfaber/shipkit/src"));
        GitLogProvider logProvider = new GitLogProvider(runner);
        Collection<GitCommit> commits = new GitCommitProvider(logProvider).getCommits("v2.3.0", "HEAD");
        System.out.println("Completed!");

        List<String> tickets = new LinkedList<>();
        for (GitCommit c : commits) {
            tickets.addAll(c.getTickets());
        }

        GitHubTicketFetcher fetcher = new GitHubTicketFetcher();

        String readOnlyToken = "a0a4c0f41c200f7c653323014d6a72a127764e17";
        Collection<Improvement> improvements = fetcher.fetchTickets("https://api.github.com", "mockito/shipkit", readOnlyToken,
                tickets, asList(), false);

        System.out.println(improvements);
    }
}
