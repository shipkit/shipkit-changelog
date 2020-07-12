package org.shipkit.gh.release


import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.shipkit.BaseSpecification

/**
 * Smoke test, we don't want an integration test that actually posts to GitHub
 */
class GitHubReleasePluginIntegTest extends BaseSpecification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def setup() {
        file("settings.gradle")
        file("build.gradle") << """
            plugins {  id('org.shipkit.shipkit-gh-release') }
        """
    }

    def "fails with clean exception"() {
        file("build.gradle") << """
            version = "1.2.3"
            file("changelog.md") << "Spanking new release!"            
            tasks.named("githubRelease") {
                repository = "shipkit/shipkit-changelog"
                changelog = file("changelog.md")
                writeToken = "secret"
            }
        """

        when:
        def result = runner("githubRelease", "-s").buildAndFail()

        then: //fails because we don't have the credentials
        result.output.contains """Unable to post release to GitHub.
    - url: https://api.github.com/repos/shipkit/shipkit-changelog/releases
    - release tag: v1.2.3
    - release name: v1.2.3
    - token: sec...
    - content:
  Spanking new release!"""
    }

    def "task can be configured"() {
        //demonstrates all configuration properties on the task:
        file("build.gradle") << """
            file("changelog.md") << "Spanking new release!"
            tasks.named("githubRelease") {
                ghApiUrl = "https://api.github.com"
                repository = "shipkit/shipkit-changelog"
                releaseName = "5.0"
                releaseTag = "RELEASE-5.0"
                changelog = file("changelog.md")
                writeToken = "secret"
            } 
        """

        when:
        def result = runner("githubRelease", "-s").buildAndFail()

        then: //fails because we don't have the credentials
        result.output.contains """Unable to post release to GitHub.
    - url: https://api.github.com/repos/shipkit/shipkit-changelog/releases
    - release tag: RELEASE-5.0
    - release name: 5.0
    - token: sec...
    - content:
  Spanking new release!"""
    }
}
