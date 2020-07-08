package org.shipkit.changelog

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.shipkit.changelog.IOUtil.readFully
import static org.shipkit.changelog.IOUtil.writeFile

class IOUtilTest extends Specification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def "reads stream"() {
        expect:
        readFully(new ByteArrayInputStream("hey\njoe!".bytes)) == "hey\njoe!"
        readFully(new ByteArrayInputStream("\n".bytes)) == "\n"
        readFully(new ByteArrayInputStream("\n\n".bytes)) == "\n\n"
        readFully(new ByteArrayInputStream("".bytes)) == ""
    }

    def "writes file"() {
        def f = new File(tmp.root, "x/y/z.txt")
        writeFile(f, "ala\nma")

        expect:
        readFully(f) == "ala\nma"
    }

    def "clean exception when reading file"() {
        when:
        readFully((File) null)

        then:
        def e = thrown(RuntimeException)
        e.message == "Problems reading from: null"
        e.cause
    }

    def "clean exception when reading stream"() {
        when:
        readFully((InputStream) null)

        then:
        def e = thrown(RuntimeException)
        e.message == "Problems reading from: null"
        e.cause
    }
}
