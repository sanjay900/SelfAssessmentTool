package sat.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 *
 * @author Kristian Hansen
 */
public final class SourceParser {
    /* @hidden parameter names - for ease */
    private static final String LINE_PARAM_NAME = "lines";
    private static final String SHOW_SIGNATURE_NAME = "showFunctionSignature";
    private static final String WRITE_COMMENT_NAME = "shouldWriteComment";

    private SourceParser() {} // all methods are static - so instance is obsolete

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
                if (line.contains("@Hidden")) {
                    if (line.contains("(")) { // has args, so find them and use them
                        final String rawArgs = removeWhitespace(line.substring(line.indexOf('('), line.indexOf(')')));
                        Set<Integer> excludedLines = new HashSet<Integer>();
                        boolean hideSignature = false;
                        boolean shouldWriteComment = true;
                        // code checking for arguments of specific lines to hide
                        if (rawArgs.contains("lines")) {
                            int firstOccurence = rawArgs.indexOf("\"");
                            String lineArgs = rawArgs.substring(firstOccurence + 1, rawArgs.substring(firstOccurence).indexOf("\""));
                            System.out.println(lineArgs); // TODO: remove
                            String[] individualArgs = lineArgs.split(",");
                            for (String s : individualArgs) {
                                try {
                                    if (s.contains("-")) { // is range, so handle it accordingly
                                        String[] numbers = s.split("-");
                                        int first = Integer.parseInt(numbers[0]); // get start of range
                                        int last = Integer.parseInt(numbers[1]); // get end of range
                                        while (first <= last) {
                                            excludedLines.add(first++);
                                        }
                                    } else {
                                        excludedLines.add(Integer.parseInt(s));
                                    }
                                } catch (NumberFormatException exc) {
                                    throw new ParsingException(s);
                                }
                            }
                        }
                        if (rawArgs.contains(SHOW_SIGNATURE_NAME)) {
                            final int index = rawArgs.indexOf(SHOW_SIGNATURE_NAME) + SHOW_SIGNATURE_NAME.length() + 1;
                            try {
                                final String after = rawArgs.substring(index);
                                hideSignature = Boolean.parseBoolean(after.substring(0, after.contains(",") ? after.indexOf(",") : after.indexOf(")")));
                            } catch (NumberFormatException exc) {
                                throw new ParsingException(rawArgs.substring(index));
                            }
                        }
                        if (rawArgs.contains(WRITE_COMMENT_NAME)) {
                            final int index = rawArgs.indexOf(WRITE_COMMENT_NAME) + WRITE_COMMENT_NAME.length() + 1; // +1 to remove = symbol
                            try {
                                final String after = rawArgs.substring(index);
                                shouldWriteComment = Boolean.parseBoolean(after.substring(0, after.contains(",") ? after.indexOf(",") : after.indexOf(")")));
                            } catch (NumberFormatException exc) {
                                throw new ParsingException(rawArgs.substring(index));
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
            exc.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return linesToShow;
    }

    /*
     * helper function - not designed to be called anywhere else apart from where they're called now
     */
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
        int depth = 1; // depth of the curly braces we are currently at
        while (scanning) {
            currentLine++;
            String line = scanner.nextLine();
            if (range != null && !range.contains(currentLine)) {
                lines.add(line);
            }
            boolean containsLeft = line.contains("{");
            boolean containsRight = line.contains("}");
            if (containsLeft && !containsRight) {
                depth++;
            } else if (containsRight && !containsLeft) {
                depth--;
            } else if (containsLeft && containsRight) { // special case, need to handle individually
                char[] chars = line.toCharArray();
                for (char c : chars) { // scan over each char and count the amount of {}'s
                    if (c == '{') {
                        depth++;
                    } else if (c == '}') {
                        depth--;
                    }
                }
            }
            if (line.contains("}") && depth == 1) {
                scanning = false;
            }
        }
    }

    /*
     * gets rid of all whitespace in a string so it can be parsed easier
     */
    private static String removeWhitespace(String original) {
        StringBuilder sb = new StringBuilder();
        char[] chars = original.toCharArray();
        for (char c : chars) {
            if (c != ' ') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}