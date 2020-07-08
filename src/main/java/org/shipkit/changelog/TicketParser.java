package org.shipkit.changelog;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TicketParser {

    /**
     * Collects all ticked ids found in text, ticket format is #123
     */
    static Set<String> parseTickets(String text) {
        Set<String> tickets = new LinkedHashSet<>();
        Pattern ticket = Pattern.compile("#\\d+");
        Matcher m = ticket.matcher(text);
        while (m.find()) {
            String ticketId = m.group().substring(1); //remove leading '#'
            tickets.add(ticketId);
        }
        return tickets;
    }
}
