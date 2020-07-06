package org.shipkit.changelog

import spock.lang.Specification

class GitHubTicketFetcherIntegTest extends Specification {

    def "fetches from GitHub"() {
        def fetcher = new GitHubTicketFetcher()

        when:
        //TODO: we need to query a repo that is dedicated for this test and validate that pagination works
        def tickets = fetcher.fetchTickets("https://api.github.com", "mockito/mockito", "a0a4c0f41c200f7c653323014d6a72a127764e17",
                ["1928", "1922", "1927"], false)

        then:
        tickets.join("\n") == """DefaultImprovement{id=1928, title='JUnit 5 strict stubs check should not suppress the regular test failure', url='https://github.com/mockito/mockito/pull/1928', labels=[], isPullRequest=true}
DefaultImprovement{id=1927, title='Fix import order', url='https://github.com/mockito/mockito/pull/1927', labels=[], isPullRequest=true}
DefaultImprovement{id=1922, title='[build] add ben-manes dependency upgrade finder', url='https://github.com/mockito/mockito/pull/1922', labels=[], isPullRequest=true}"""
    }
}
