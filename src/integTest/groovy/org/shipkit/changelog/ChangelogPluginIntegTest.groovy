package org.shipkit.changelog

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.shipkit.BaseSpecification

/**
 * Only smoke test, forever! Don't add more tests here, instead cover the complexity in lower level unit tests.
 */
class ChangelogPluginIntegTest extends BaseSpecification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def "basic task configuration with no previous version"() {
        file("build.gradle") << """
            plugins {  
                id 'org.shipkit.shipkit-changelog'
            }
            
            tasks.named("generateChangelog") {
                githubToken = "secret"
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
                
                //Github url, configure if you use Github Enterprise, default as below
                githubUrl = "https://github.com"
                
                //Github API url, configure if you use Github Enterprise, default as below
                githubApiUrl = "https://api.github.com"
                
                //The release date, the default is today date 
                date = "2020-06-06"
                
                //Previous revision to generate changelog, *no default*
                previousRevision = "v3.3.10"
                
                //Current revision to generate changelog, default as below 
                revision = "HEAD" 
                
                //The release version, default as below
                version = project.version       
                
                //Token that enables querying Github, safe to check-in because it is read-only, *no default*              
                githubToken = "a0a4c0f41c200f7c653323014d6a72a127764e17"
                
                //Repository to look for tickets, *no default*
                repository = "mockito/mockito"
            }          
        """

        expect: "run in dry-run mode to smoke test the configuration"
        runner("generateChangelog", "-m").build()
    }
}
