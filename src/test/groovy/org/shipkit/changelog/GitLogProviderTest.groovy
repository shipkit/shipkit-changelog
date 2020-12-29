package org.shipkit.changelog

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class GitLogProviderTest extends Specification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()
    ProcessRunner runner
    GitLogProvider provider

    def setup() {
        runner = new ProcessRunner(tmp.root)

        runner.run("git", "init")
        runner.run("git", "config", "user.email", "dummy@testing.com")
        runner.run("git", "config", "user.name", "Dummy For Testing")

        runner.run("git", "commit", "--allow-empty", "-m", "the initial commit")
        runner.run("git", "tag", "v0.0.0")
        runner.run("git", "commit", "--allow-empty", "-m", "the second test commit")
        runner.run("git", "tag", "v0.0.1")

        provider = new GitLogProvider(runner)
    }

    def "smoke test"() {
        when:
        def log = provider.getLog("v0.0.0", "v0.0.1", "--pretty=short")

        then:
        !log.contains("the initial commit")
        log.contains("the second test commit")
    }

    def "no previous revision"() {
        //this is the "first release" use case
        when:
        def log = provider.getLog("HEAD", "v0.0.1", "--pretty=short")

        then:
        log == ""
    }
}
