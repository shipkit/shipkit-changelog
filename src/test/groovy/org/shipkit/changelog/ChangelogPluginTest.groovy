package org.shipkit.changelog

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ChangelogPluginTest extends Specification {

    def project = new ProjectBuilder().build()

    def "applies cleanly"() {
        when:
        project.plugins.apply(ChangelogPlugin)

        then:
        GenerateChangelogTask t = project.tasks.generateChangelog
        t.gitDir

        when:
        t.workingDir = null

        then:
        !t.gitDir
    }
}
