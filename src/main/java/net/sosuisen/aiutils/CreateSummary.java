package net.sosuisen.aiutils;

import dev.langchain4j.model.openai.OpenAiChatModel;

import java.nio.file.Files;
import java.nio.file.Path;

import static net.sosuisen.Constants.PARAGRAPH_PATTERN;

public class CreateSummary {
    private static String getPrompt(String paragraph) {
        return """
                Based on the following constraints and the provided input text, output the best possible summary.
                
                # Constraints:
                
                - Write shorter than the original text.
                - Easy enough for elementary school students to understand.
                - Do not omit important keywords.
                - Do not alter idioms or set phrases.
                - Keep sentences concise.
                - Replace outdated expressions with modern ones.
                - Use a polite tone.
                - First, write a concise summary within 70 characters.
                - Then, under the summary, use bullet points starting with "-" for each topic.
                - Begin each bullet with a keyword representing the content, followed by ":" and then the details.
                
                Input sentence:
                %s
                """.formatted(paragraph);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: mvn clean package exec:java -Dexec.mainClass=\"net.sosuisen.CreateSummary\" -Dexec.args=\"path-to-structured_paragraph.txt\"");
            System.exit(1);
        }
        try {
            var inputPath = Path.of(args[0]);

            System.out.println("Creating summary..");

            var structuredParagraphStr = Files.readString(inputPath);
            var summaries = new StringBuilder();
            for (var line : structuredParagraphStr.split("\n")) {
                var matcher = PARAGRAPH_PATTERN.matcher(line);
                if (!matcher.find()) {
                    continue;
                }

                var positionTag = matcher.group(1);
                var positionName = matcher.group(2);
                var sectionTitle = matcher.group(3);
                var paragraph = matcher.group(4);

                if (positionTag.isEmpty() || paragraph.isEmpty()) continue;

                if (!positionTag.contains("p#")) {
                    continue;
                }

                // is paragraph

                System.out.printf("""
                        position_tag: %s
                        position_name: %s
                        section_title: %s
                        paragraph: %s
                        """, positionTag, positionName, sectionTitle, paragraph);

                var prompt = getPrompt(paragraph);

                OpenAiChatModel model = OpenAiChatModel.builder()
                        .apiKey(System.getenv("OPENAI_API_KEY"))
                        .modelName("gpt-4o")
                        .build();
                String summary = model.generate(prompt);
                System.out.println("Summary: " + summary);

                summaries.append("""
                        {%s} %s
                        ---
                        """.formatted(positionTag, summary));
            }
            var outputPath = inputPath.resolveSibling("summary.txt");
            Files.writeString(outputPath, summaries.toString());
            System.out.println("Summary created at: " + outputPath);
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }
}
