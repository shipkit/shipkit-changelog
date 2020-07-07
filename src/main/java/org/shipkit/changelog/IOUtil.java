package org.shipkit.changelog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * IO utils. A bit of reinventing the wheel but we don't want extra dependencies at this stage and we want to be java.
 */
public class IOUtil {

    /**
     * Reads string from the file
     */
    public static String readFully(File input) {
        try {
            return readNow(new FileInputStream(input));
        } catch (Exception e) {
            throw new RuntimeException("Problems reading file: " + input, e);
        }
    }

    /**
     * Reads string from the stream and closes it
     */
    public static String readFully(InputStream stream) {
        try {
            return readNow(stream);
        } catch (Exception e) {
            throw new RuntimeException("Problems reading stream", e);
        }
    }

    /**
     * Closes the target. Does nothing when target is null. Is not silent, throws exception on IOException.
     *
     * @param closeable the target, may be null
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException("Problems closing closeable", e);
            }
        }
    }

    private static String readNow(InputStream is) {
        try (Scanner s = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    public static void writeFile(File target, String content) {
        PrintWriter p = null;
        try {
            target.getParentFile().mkdirs();
            p = new PrintWriter(new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8));
            p.write(content);
        } catch (Exception e) {
            throw new RuntimeException("Problems writing text to file: " + target, e);
        } finally {
            close(p);
        }
    }
}
