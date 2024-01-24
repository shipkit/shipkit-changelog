package org.shipkit.changelog

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.shipkit.changelog.GenerateChangelogTask.provideGitDir

class GenerateChangelogTaskTest extends Specification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def "getGitDir is safe"() {
        expect:
        provideGitDir(null) == null
        provideGitDir(new File("missing")) == null

        and:
        def gitDir = tmp.newFolder(".git")
        provideGitDir(gitDir.parentFile) == gitDir
    }
}
