package org.shipkit.changelog;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import java.util.*;
import java.util.logging.Logger;

class GitHubTicketFetcher {

    private static final Logger LOG = Logger.getLogger(GitHubTicketFetcher.class.getName());
    private final GitHubListFetcher fetcher;

    GitHubTicketFetcher(String apiUrl, String repository, String readOnlyToken) {
        this(new GitHubListFetcher(apiUrl, repository, readOnlyToken));
    }

    GitHubTicketFetcher(GitHubListFetcher fetcher) {
        this.fetcher = fetcher;
    }

    Collection<Ticket> fetchTickets(Collection<String> ticketIds, boolean onlyPullRequests) {
        List<Ticket> out = new LinkedList<>();
        if (ticketIds.isEmpty()) {
            return out;
        }
        LOG.info("Querying GitHub API for " + ticketIds.size() + " tickets.");

        Queue<Long> tickets = queuedTicketNumbers(ticketIds);

        try {
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

    private static List<Ticket> extractImprovements(Collection<Long> tickets, JsonArray issues,
                                                    boolean onlyPullRequests) {
        if (tickets.isEmpty()) {
            return Collections.emptyList();
        }

        List<Ticket> pagedTickets = new ArrayList<>();
        for (JsonValue issue : issues) {
            Ticket i = GitHubImprovementsJSON.toImprovement(issue.asObject());
            if (tickets.remove(i.getId())) {
                if (!onlyPullRequests || i.isPullRequest()) {
                    pagedTickets.add(i);
                }

                if (tickets.isEmpty()) {
                    return pagedTickets;
                }
            }
        }
        return pagedTickets;
    }
}
