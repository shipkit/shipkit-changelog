package org.shipkit.changelog

import spock.lang.Specification
import spock.lang.Subject

class GitCommitProviderTest extends Specification {

    def logProvider = Mock(GitLogProvider)
    @Subject
            provider = new GitCommitProvider(logProvider)

    def log = """a5797f9e6cfc06e2fa70ed12ee6c9571af8a7fc9@@info@@mockitoguy@gmail.com@@info@@Szczepan Faber@@info@@Tidy-up in buildSrc
Merged pull request #10
@@commit@@
b9d694f4c25880d9dda21ac216053f2bd0f5673c@@info@@mockitoguy@gmail.com@@info@@Szczepan Faber@@info@@Tidy-up in buildSrc - fixes #20 and #30
@@commit@@
c76924d41c219f3b71b50a28d80c23c9c81b7a8c@@info@@john@doe@@info@@John R. Doe@@info@@dummy commit
@@commit@@"""

    def "provides commits"() {
        logProvider.getLog("v1.10.10", "HEAD", "--pretty=format:%H@@info@@%ae@@info@@%an@@info@@%B%N@@commit@@") >> log

        when:
        def commits = provider.getCommits("v1.10.10", "HEAD")

        then:
        commits.join("\n") == """{author='Szczepan Faber', tickets=[10]}
{author='Szczepan Faber', tickets=[20, 30]}
{author='John R. Doe', tickets=[]}"""
    }

    def "has basic handling of garbage in log"() {
        logProvider.getLog(_, _, _) >> (log + " some garbage \n@@commit@@\n more garbage")

        when:
        def commits = provider.getCommits("v1.10.10", "HEAD")

        then:
        commits.size() == 3
    }

    def "handles empty log"() {
        logProvider.getLog(_, _, _) >> ""

        when:
        def commits = provider.getCommits("v1.10.10", "HEAD")

        then:
        commits.isEmpty()
    }
}
