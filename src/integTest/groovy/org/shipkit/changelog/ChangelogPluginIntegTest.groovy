package org.shipkit.changelog


import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.shipkit.BaseSpecification

/**
 * Only smoke test, forever! Don't add more tests here, instead cover the complexity in lower level unit tests.
 *
 * Once we have a couple of releases (tags) we should unignore this test
 * and let it run against "this" repo.
 */
class ChangelogPluginIntegTest extends BaseSpecification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def setup() {
        file("settings.gradle")
    }

    def "basic task configuration"() {
        file("build.gradle") << """
            plugins {  
                id 'org.shipkit.shipkit-changelog'
            }
            
            tasks.named("generateChangelog") {
                previousRevision = "v3.3.10"
                readOnlyToken = "a0a4c0f41c200f7c653323014d6a72a127764e17"
                repository = "mockito/mockito"
            }
        """

        expect: "run in dry-run mode to smoke test the configuration"
        runner("generateChangelog", "-m").build()
    }

    def "complete task configuration"() {
        file("build.gradle") << """
            plugins {  
                id 'org.shipkit.shipkit-changelog'
            }
            
            tasks.named("generateChangelog") {
                //file where the release notes are generated, default as below
                outputFile = new File(buildDir, "changelog.md")
                
                //Working directory for running 'git' commands, default as below
                workingDir = project.projectDir                
                
                //GitHub url, configure if you use GitHub Enterprise, default as below
                ghUrl = "https://github.com"
                
                //GitHub API url, configure if you use GitHub Enterprise, default as below
                ghApiUrl = "https://api.github.com"
                
                //The release date, the default is today date 
                date = "2020-06-06"
                
                //Previous revision to generate changelog, *no default*
                previousRevision = "v3.3.10"
                
                //Current revision to generate changelog, default as below 
                revision = "HEAD" 
                
                //The release version, default as below
                version = project.version       
                
                //Token that enables querying GitHub, safe to check-in because it is read-only, *no default*              
                readOnlyToken = "a0a4c0f41c200f7c653323014d6a72a127764e17"
                
                //Repository to look for tickets, *no default*
                repository = "mockito/mockito"
            }          
        """

        expect: "run in dry-run mode to smoke test the configuration"
        runner("generateChangelog", "-m").build()
    }
}
