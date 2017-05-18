package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static util.DebugHelper.*;

/**
 *
 * @author Kristian Hansen
 */
public final class SourceParser {

    private SourceParser() {}

    /**
     *
     * @param name Name (and directory) of the java source assignment task file to load
     * @return A list of strings (representing lines) that will be displayed
     */
    public static List<String> parseSourceFile(String name) {
        List<String> linesToShow = new ArrayList<String>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(name));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("@Hidden")) {
                    if (line.contains("(")) { // has args, so find them and use them
                        final String rawArgs = line.substring(line.indexOf('('), line.indexOf(')'));
                        // code checking for arguments of specific lines to hide
                        String lineExcludes = rawArgs.split("\"")[0];
                        Set<Integer> excludedLines = new HashSet<Integer>();
                        String[] excludeLineParams = lineExcludes.split(",");
                        for (String s : excludeLineParams) {
                            try {
                                if (s.contains("-")) { // is a range of lines
                                    String[] numberPair = s.split("-");
                                    int start = Integer.parseInt(numberPair[0]);
                                    int end = Integer.parseInt(numberPair[1]);
                                    if (start == end || start > end) { // invalid range
                                        throw new ParsingException(s);
                                    }
                                    while (start <= end) {
                                        excludedLines.add(start++);
                                    }
                                } else { // is a single line
                                    excludedLines.add(Integer.parseInt(s));
                                }
                            } catch (NumberFormatException except) {
                                throw new ParsingException(s);
                            }
                        }
                        // check for other parameters
                        String otherParams = rawArgs.split("\"")[1];
                        boolean hideSignature = false;
                        boolean shouldWriteComment = true;
                        if (!otherParams.isEmpty()) {
                            String[] bools = otherParams.split(",");
                            try {
                                hideSignature = Boolean.parseBoolean(bools[0]);
                                shouldWriteComment = Boolean.parseBoolean(bools[1]);
                            } catch (NumberFormatException exc) {
                                throw new ParsingException(otherParams);
                            }
                        }

                        // params all found out, now actually go through this block
                        hideRange(linesToShow, excludedLines, scanner, hideSignature, shouldWriteComment);
                    } else { // otherwise hide entire code block
                        hideRange(linesToShow,null, scanner, false, true);
                    }
                } else {
                    linesToShow.add(line);
                }
            }
        } catch (FileNotFoundException exc) {
            printError(exc);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return linesToShow;
    }

    private static void hideRange(List<String> lines, Set<Integer> range, Scanner scanner, boolean hideSignature, boolean writeComment) {
        int currentLine = 0;
        String signature = scanner.nextLine();
        if (!hideSignature) {
            lines.add(signature);
        }
        if (writeComment) {
            lines.add("/* omitted code */");
        }
        boolean scanning = true;
        int depth = 0;
        while (scanning) {
            String line = scanner.nextLine();

        }
    }
}