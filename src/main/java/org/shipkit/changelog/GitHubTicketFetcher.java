package org.shipkit.changelog;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Logger;

class GitHubTicketFetcher {

    private static final Logger LOG = Logger.getLogger(GitHubTicketFetcher.class.getName());

    Collection<Improvement> fetchTickets(String apiUrl, String repository, String readOnlyAuthToken, Collection<String> ticketIds, Collection<String> labels,
                                         boolean onlyPullRequests) {
        List<Improvement> out = new LinkedList<>();
        if (ticketIds.isEmpty()) {
            return out;
        }
        LOG.info("Querying GitHub API for " + ticketIds.size() + " tickets.");

        Queue<Long> tickets = queuedTicketNumbers(ticketIds);

        try {
            GitHubListFetcher fetcher = new GitHubListFetcher(apiUrl, repository, readOnlyAuthToken);

            while (!tickets.isEmpty() && fetcher.hasNextPage()) {
                JsonArray page = fetcher.nextPage();

                out.addAll(extractImprovements(
                        dropTicketsAboveMaxInPage(tickets, page),
                        page, onlyPullRequests));
            }
        } catch (Exception e) {
            throw new RuntimeException("Problems fetching " + ticketIds.size() + " tickets from GitHub", e);
        }
        return out;
    }

    /**
     * Remove the ticket IDs that are higher than the highest ticket in the page.
     * This prevents continuation of requests to find a ticket that will never be found.
     * TODO: we should fail in this case
     */
    private Queue<Long> dropTicketsAboveMaxInPage(Queue<Long> tickets, JsonArray page) {
        if (page.isEmpty()) {
            return tickets;
        }
        long highestId = page.get(0).asObject().get("number").asLong();
        while (!tickets.isEmpty() && tickets.peek() > highestId) {
            tickets.poll();
        }
        return tickets;
    }

    private Queue<Long> queuedTicketNumbers(Collection<String> ticketIds) {
        List<Long> tickets = new ArrayList<>();
        for (String id : ticketIds) {
            tickets.add(Long.parseLong(id));
        }
        Collections.sort(tickets);
        PriorityQueue<Long> longs = new PriorityQueue<>(tickets.size(), Collections.reverseOrder());
        longs.addAll(tickets);
        return longs;
    }

    private static List<Improvement> extractImprovements(Collection<Long> tickets, JsonArray issues,
                                                         boolean onlyPullRequests) {
        if (tickets.isEmpty()) {
            return Collections.emptyList();
        }

        List<Improvement> pagedImprovements = new ArrayList<>();
        for (JsonValue issue : issues) {
            Improvement i = GitHubImprovementsJSON.toImprovement(issue.asObject());
            if (tickets.remove(i.getId())) {
                if (!onlyPullRequests || i.isPullRequest()) {
                    pagedImprovements.add(i);
                }

                if (tickets.isEmpty()) {
                    return pagedImprovements;
                }
            }
        }
        return pagedImprovements;
    }
}
