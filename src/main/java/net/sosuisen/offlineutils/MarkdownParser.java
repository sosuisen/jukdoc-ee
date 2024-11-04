package net.sosuisen.offlineutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MarkdownParser {
    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#+?)\\s(.+)$");
    private static final Pattern H1_PATTERN = Pattern.compile("^(#+)\\s+?Chapter\\s+?(\\d+):\\s+?(.+)$");
    private static final Pattern H2_3_PATTERN = Pattern.compile("^(#+)\\s+?([\\d.]+)\\s+?(.+)$");

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: mvn clean package exec:java -Dexec.mainClass=\"net.sosuisen.MarkdownParser\" -Dexec.args=\"path-to-markdown-file path-to-outout-file\"");
            System.exit(1);
        }
        System.out.println("Parsing markdown file..");
        var structuredParagraph = "";
        try {
            var inputPath = Path.of(args[0]);
            var markdown = Files.readString(inputPath);
            var parser = new MarkdownParser();
            structuredParagraph = parser.parseMarkdown(markdown);
            Path outputPath;
            if (args.length == 2) {
                outputPath = Path.of(args[1]);
            } else {
                outputPath = inputPath.resolveSibling("structured_paragraph.txt");
            }
            Files.writeString(outputPath, structuredParagraph);
            System.out.println("Structured paragraph created at: " + outputPath);
        } catch (IOException e) {
            System.out.println("File not found:" + e.getMessage());
            System.exit(1);
        }
    }

    private int calcHeaderDepth(String line) {
        var matcher = HEADER_PATTERN.matcher(line);
        return matcher.find() ? matcher.group(1).length() : -1;
    }

    public String parseMarkdown(String markdown) {
        String[] lines = markdown.split("\n");

        var currentHeaderDepth = 0; // depth of h1 is 1
        var headerDepthMap = new HashMap<Integer, Integer>(); // depth, count
        headerDepthMap.put(1, 0);
        headerDepthMap.put(2, 0);
        headerDepthMap.put(3, 0);

        var headerTags = "";
        var paragraphNumber = 1;
        var positionName = "";
        var sectionTitle = "";

        var intermediateText = new StringBuilder();

        for (var line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            var depth = calcHeaderDepth(line);

            if (depth < 0) {
                // line is paragraph
                intermediateText.append("{%s_p-%03d:%s:%s} %s\n".formatted(headerTags, paragraphNumber, positionName, sectionTitle, line));
                paragraphNumber++;
                continue;
            }

            // line is header
            paragraphNumber = 1;

            if (depth < currentHeaderDepth) {
                for (int i = depth + 1; i <= currentHeaderDepth; i++) {
                    // headerDepthMap.remove(i);
                    headerDepthMap.put(i, 0);
                }
            }
            currentHeaderDepth = depth;
            if (currentHeaderDepth == 1) {
                var matcher = H1_PATTERN.matcher(line);
                if (matcher.find()) {
                    positionName = "Chapter " + matcher.group(2);
                    sectionTitle = matcher.group(3);
                }
            } else {
                var matcher = H2_3_PATTERN.matcher(line);
                if (matcher.find()) {
                    positionName = "Section " + matcher.group(2);
                    sectionTitle = matcher.group(3);
                }
            }

            headerDepthMap.put(depth, headerDepthMap.getOrDefault(depth, 0) + 1);

            headerTags = headerDepthMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> "h%d-%03d".formatted(entry.getKey(), entry.getValue()))
                    .reduce("%s_%s"::formatted)
                    .orElse("");

            var matcher = HEADER_PATTERN.matcher(line);
            var headerLine = matcher.find() ? matcher.group(2) : "";

            intermediateText.append("{%s_p-000:%s:%s} %s\n".formatted(headerTags, positionName, sectionTitle, headerLine));
        }
        return intermediateText.toString();
    }
}