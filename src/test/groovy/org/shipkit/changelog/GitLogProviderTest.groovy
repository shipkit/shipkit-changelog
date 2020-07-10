package org.shipkit.changelog


import spock.lang.Specification

class GitLogProviderTest extends Specification {

    def "smoke test"() {
        File rootDir = findRootDir()
        def provider = new GitLogProvider(new ProcessRunner(rootDir))

        when:
        def log = provider.getLog("v0.0.0", "v0.0.1", "--pretty=short")

        then:
        log //TODO add assertions when we have more releases with small amount of commits
    }

    private File findRootDir() {
        def testDir = new File(".")
        while (!new File(testDir, ".git").directory && testDir != null) {
            testDir = testDir.parentFile
        }
        assert testDir != null
        testDir
    }
}
