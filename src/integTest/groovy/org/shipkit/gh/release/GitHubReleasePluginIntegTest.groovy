package org.shipkit.gh.release

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Smoke test, we don't want an integration test that actually posts to GitHub
 */
class GitHubReleasePluginIntegTest extends Specification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def setup() {
        file("settings.gradle")
        file("build.gradle") << """
            plugins {  id('org.shipkit.gh.release') }
        """
    }

    def "fails with clean exception"() {
        file("build.gradle") << """
            version = "1.2.3"
            tasks.named("githubRelease") {
                repository = "shipkit/shipkit-changelog"
                content = "Spanking new release!"
                writeToken = "secret"
            }
        """

        when:
        def result = runner("githubRelease", "-s").buildAndFail()

        then: //fails because we don't have the credentials
        result.output.contains """Unable to post release to GitHub.
    - url: /repos/shipkit/shipkit-changelog/releases
    - release tag: v1.2.3
    - release name: v1.2.3
    - token: sec...
    - content:
  Spanking new release!"""
    }

    def "task can be configured"() {
        //demonstrates all configuration properties on the task:
        file("build.gradle") << """
            tasks.named("githubRelease") {
                ghApiUrl = "https://api.github.com"
                repository = "shipkit/shipkit-changelog"
                releaseName = "5.0"
                releaseTag = "RELEASE-5.0"
                content = "Spanking new release!"
                writeToken = "secret"
            } 
        """

        when:
        def result = runner("githubRelease", "-s").buildAndFail()

        then: //fails because we don't have the credentials
        result.output.contains """Unable to post release to GitHub.
    - url: /repos/shipkit/shipkit-changelog/releases
    - release tag: RELEASE-5.0
    - release name: 5.0
    - token: sec...
    - content:
  Spanking new release!"""
    }

    File file(String path) {
        def f = new File(rootDir, path)
        if (!f.exists()) {
            f.parentFile.mkdirs()
            f.createNewFile()
            assert f.exists()
        }
        return f
    }

    File getRootDir() {
        return tmp.root
    }

    GradleRunner runner(String... args) {
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(args)
        runner.withProjectDir(rootDir)
        runner
    }
}
