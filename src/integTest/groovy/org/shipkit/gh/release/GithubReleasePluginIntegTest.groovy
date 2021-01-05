package org.shipkit.github.release


import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.shipkit.BaseSpecification

/**
 * Smoke test, we don't want an integration test that actually posts to Github
 */
class GithubReleasePluginIntegTest extends BaseSpecification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def setup() {
        file("settings.gradle")
    }

    def "basic task configuration"() {
        file("build.gradle") << """
            plugins {
                id 'org.shipkit.shipkit-github-release'
            }
                        
            tasks.named("githubRelease") {
                repository = "shipkit/shipkit-changelog"
                changelog = file("changelog.md")
                newTagRevision = "ff2fb22b3bb2fb08164c126c0e2055d57dee441b"
                githubToken = "secret"
            }
        """

        expect:
        runner("githubRelease", "-m").build()
    }

    def "complete task configuration"() {
        file("build.gradle") << """
            plugins {
                id 'org.shipkit.shipkit-github-release'
            }
                        
            tasks.named("githubRelease") {
                //Github API url, configure if you use Github Enterprise, default as below
                githubApiUrl = "https://api.github.com"
                
                //Repository where to create a release, *no default*
                repository = "shipkit/shipkit-changelog"
                
                //The file with changelog (release notes), *no default*
                changelog = file("changelog.md")
                
                //The name of the release name, default as below
                releaseName = "v" + project.version
                
                //The release tag, default as below
                releaseName = "v" + project.version
                
                //SHA of the revision from which release is created; *no default*
                newTagRevision = "ff2fb22b3bb2fb08164c126c0e2055d57dee441b"
                
                //Github token used for posting to Github API, *no default*
                githubToken = "secret"
            }
        """

        expect:
        runner("githubRelease", "-m").build()
    }

    def "fails with clean exception"() {
        file("build.gradle") << """
            plugins { id 'org.shipkit.shipkit-github-release' }
            version = "1.2.3"
            file("changelog.md") << "Spanking new release!"            
            tasks.named("githubRelease") {
                repository = "shipkit/shipkit-changelog"
                changelog = file("changelog.md")
                newTagRevision = "ff2fb22b3bb2fb08164c126c0e2055d57dee441b"
                githubToken = "secret"
            }
        """

        when:
        def result = runner("githubRelease", "-s").buildAndFail()

        then: //fails because we don't have the credentials
        result.output.contains """Unable to post release to Github.
  * url: https://api.github.com/repos/shipkit/shipkit-changelog/releases
  * release tag: v1.2.3
  * release name: v1.2.3
  * token: sec...
  * content:
Spanking new release!

  * underlying problem:"""
    }
}
