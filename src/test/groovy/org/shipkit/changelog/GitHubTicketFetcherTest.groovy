package org.shipkit.changelog

import com.eclipsesource.json.Json
import spock.lang.Specification

class GitHubTicketFetcherTest extends Specification {

    def listFetcher = Mock(GitHubListFetcher)
    def fetcher = new GitHubTicketFetcher(listFetcher)

    def "empty tickets"() {
        expect:
        fetcher.fetchTickets([]).empty
    }

    def "fetches from 2 pages"() {
        listFetcher.hasNextPage() >>> [true, true, false]
        def page1 = Json.parse("""[{"number": 30, "html_url": "http://issues/x", "title": "fix1"},
                        {"number": 20, "html_url": "http://issues/x", "title": "fix2"}]""")
        def page2 = Json.parse("""[{"number": 10, "html_url": "http://issues/x", "title": "fix3"}]""")
        listFetcher.nextPage() >>> [page1, page2]

        when:
        def tickets = fetcher.fetchTickets(["10", "30", "40"])

        then:
        tickets.join("\n") == """{id=30, title='fix1', url='http://issues/x'}
{id=10, title='fix3', url='http://issues/x'}"""
    }
}
