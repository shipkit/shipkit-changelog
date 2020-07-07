package org.shipkit.changelog;

import java.util.Collection;
import java.util.Set;

class GitCommit {

    private final String author;
    private final Set<String> tickets;

    GitCommit(String author, String message) {
        this.author = author;
        this.tickets = TicketParser.parseTickets(message);
    }

    public String getAuthor() {
        return author;
    }

    public Collection<String> getTickets() {
        return tickets;
    }

    @Override
    public String toString() {
        return '{' +
                "author='" + getAuthor() + '\'' +
                ", tickets=" + getTickets() +
                '}';
    }
}
