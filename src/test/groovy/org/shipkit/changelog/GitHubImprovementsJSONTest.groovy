package org.shipkit.changelog

import com.eclipsesource.json.Json
import spock.lang.Specification

class GitHubImprovementsJSONTest extends Specification {

    def "parses issue"() {
        def issue = Json.parse('{"number": 100, "html_url": "http://issues/100", "title": "Some bugfix"}')
                .asObject()

        when:
        def i = GitHubImprovementsJSON.toImprovement(issue)

        then:
        i.id == 100L
        i.title == "Some bugfix"
        i.url == "http://issues/100"
        !i.pullRequest
    }
}
