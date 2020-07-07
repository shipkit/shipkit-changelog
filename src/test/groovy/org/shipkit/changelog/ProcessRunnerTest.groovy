package org.shipkit.changelog

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

//ignore the test when there is no 'ls' utility
@IgnoreIf({ !commandAvailable("ls") })
class ProcessRunnerTest extends Specification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def "runs processes and returns output"() {
        File dir = tmp.newFolder()
        new File(dir, "xyz.txt").createNewFile()
        new File(dir, "hey joe.jar").createNewFile()

        when:
        String output = new ProcessRunner(dir).run("ls")

        then:
        output.contains("xyz.txt")
        output.contains("hey joe.jar")
    }

    def "fails to start process"() {
        when:
        new ProcessRunner(tmp.root).run("bad-cli")

        then:
        def e = thrown(RuntimeException)
        e.message == """Problems executing command:
  bad-cli"""
        e.cause
    }

    def "process failure"() {
        when:
        new ProcessRunner(tmp.root).run("ls", "-bad-option")

        then:
        def e = thrown(RuntimeException)
        e.message.startsWith """Problems executing command (exit code: 1):
  command: ls -bad-option
  working dir: $tmp.root
  output:
ls: illegal option"""
    }

    static boolean commandAvailable(String command) {
        try {
            return command.execute().waitFor() == 0
        } catch (Exception e) {
            return false
        }
    }
}

