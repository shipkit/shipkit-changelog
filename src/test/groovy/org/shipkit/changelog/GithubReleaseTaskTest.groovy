package org.shipkit.changelog

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.shipkit.github.release.GithubReleasePlugin
import org.shipkit.github.release.GithubReleaseTask
import spock.lang.Specification

class GithubReleaseTaskTest extends Specification {

    def apiMock = Mock(GithubApi)
    def project = ProjectBuilder.builder().build()

    def setup() {
        project.plugins.apply(GithubReleasePlugin)
    }

    def "knows if release already exists"() {
        GithubReleaseTask task = project.tasks.githubRelease
        apiMock.get("dummy/releases/tags/v1.2.3") >> new GithubApi.Response('{"id": 10}', '')

        when:
        def result = task.existingRelease(apiMock, "dummy/releases", "v1.2.3")

        then:
        result.get() == 10
    }

    def "knows if release does not yet exist"() {
        GithubReleaseTask task = project.tasks.githubRelease
        apiMock.get("dummy/releases/tags/v1.2.3") >> { throw new GithubApi.ResponseException(404, "") }

        when:
        def result = task.existingRelease(apiMock, "dummy/releases", "v1.2.3")

        then:
        !result.present
    }

    def "creates new release"() {
        GithubReleaseTask task = project.tasks.githubRelease
        apiMock.post("dummy/url", "dummy body") >> '{"html_url": "dummy html url"}'

        when:
        def result = task.performRelease(Optional.empty(), apiMock, "dummy/url", "dummy body")

        then:
        result == "dummy html url"
    }

    def "updates existing release"() {
        GithubReleaseTask task = project.tasks.githubRelease
        apiMock.patch("api/releases/123", "dummy body") >> '{"html_url": "dummy html url"}'

        when:
        def result = task.performRelease(Optional.of(123), apiMock, "api/releases", "dummy body")

        then:
        result == "dummy html url"
    }

    /**
     * Update githubToken and repo name for manual integration testing
     */
    def "manual integration test"() {
        project.version = "1.2.4"
        project.file("changelog.md") << "Spanking new release! " + System.currentTimeSeconds()
        project.tasks.named("githubRelease") { GithubReleaseTask it ->
            it.changelog = project.file("changelog.md")
            it.repository = "mockitoguy/shipkit-demo" //feel free to change to your private repo
            it.newTagRevision = "aa51a6fe99d710c0e7ca30fc1d0411a8e9cdb7a8" //use sha of the repo above
            it.githubToken = "secret" //update, use your token, DON'T CHECK IN
        }

        when:
        project.tasks.githubRelease.postRelease()

        then:
        //when doing manual integration testing you won't get an exception here
        //remove below / change the assertion when integ testing
        thrown(GradleException)
//        true
    }
}
