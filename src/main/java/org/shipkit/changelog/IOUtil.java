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
        try(InputStream i = new FileInputStream(input)) {
            return readNow(i);
        } catch (Exception e) {
            throw new RuntimeException("Problems reading from: " + input, e);
        }
    }

    /**
     * Reads string from the stream and closes it
     */
    public static String readFully(InputStream input) {
        try {
            return readNow(input);
        } catch (Exception e) {
            throw new RuntimeException("Problems reading from: " + input, e);
        }
    }

    private static String readNow(InputStream is) {
        try (Scanner s = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    public static void writeFile(File target, String content) {
        target.getParentFile().mkdirs();
        try(PrintWriter p = new PrintWriter(new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8))) {
            p.write(content);
        } catch (Exception e) {
            throw new RuntimeException("Problems writing text to file: " + target, e);
        }
    }
}
