package net.sosuisen.offlineutils;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import net.sosuisen.Constants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static net.sosuisen.Constants.PARAGRAPH_PATTERN;
import static net.sosuisen.Constants.QA_PATTERN;

public class CreateQAStore {
    private static String getPrompt(String paragraph) {
        return """
                Based on the following constraints, transform the input sentence into a set of question-and-answer pairs.
                Constraints:
                Condition 1: Keep the original sentence as intact as possible.
                Condition 2: Create one question per sentence.
                Condition 3: Formulate multiple question-and-answer pairs.
                Condition 4: Replace old expressions with modern ones.
                Condition 5: Make it simple enough for elementary school students to understand.
                Condition 6: Ensure no essential keywords are omitted.
                Condition 7: Do not rewrite idioms or set phrases.
                Condition 8: Use polite expressions.
                Condition 9: Please do not include anaphoric expressions, such as demonstrative pronouns, in the questions.
                Condition 10: Do not include 'the following', 'below' or 'above' in the questions.
                Condition 11: Do not include 'sentence', 'paragraph', 'section' or 'chapter' in the questions.
                
                For example, if the first pair is question 1 and answer 1, and the next pair is question 2 and answer 2, please respond as follows:
                
                [Q] Question 1
                [A] Answer 1
                
                [Q] Question 2
                [A] Answer 2
                
                As a note, always write the question on the [Q] line and the corresponding answer on the [A] line.
                You must not include terms like "Question 1" or "Answer 1" directly in the response.
                
                Input sentence:
                %s
                """.formatted(paragraph);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: mvn clean package exec:java -Dexec.mainClass=\"net.sosuisen.CreateQAStore\" -Dexec.args=\"path-to-structured_paragraph.txt\"");
            System.exit(1);
        }
        try {
            var inputPath = Path.of(args[0]);
            var qaPairPath = inputPath.resolveSibling("qa.txt");

            if (!Files.exists(qaPairPath)) {
                System.out.println("Creating QA pairs..");

                var structuredParagraphStr = Files.readString(inputPath);
                var qaResultsStr = new StringBuilder();
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

                    if (positionTag.contains("_p-000")) {
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

                    OpenAiChatModel qaModel = OpenAiChatModel.builder()
                            .apiKey(System.getenv("OPENAI_API_KEY"))
                            .modelName("gpt-4o")
                            .build();
                    String qaPair = qaModel.generate(prompt);

                    qaPair = qaPair.replaceAll("\\[Q]([^\n]*)\\n+\\[A]", "[Q]$1\n[A]");
                    System.out.println("QA Pair: " + qaPair);

                    qaResultsStr.append("""
                            {%s:%s:%s} %s
                            ---
                            """.formatted(positionTag, positionName, sectionTitle, qaPair));
                }
                Files.writeString(qaPairPath, qaResultsStr.toString());
                System.out.println("QA pairs created at: " + qaPairPath);
            }

            System.out.println("Creating QA store..");

            var embeddingStore = new InMemoryEmbeddingStore<TextSegment>();
            OpenAiEmbeddingModel model = OpenAiEmbeddingModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("text-embedding-3-small")
                    .build();
            var embeddings = new ArrayList<Embedding>();
            var embedded = new ArrayList<TextSegment>();

            // Load existing QA pairs
            var qaBlocksStr = Files.readString(qaPairPath);
            var qaBlocks = qaBlocksStr.split("---\n");

            for (var qaBlock : qaBlocks) {
                var matcher = PARAGRAPH_PATTERN.matcher(qaBlock);
                if (!matcher.find()) {
                    continue;
                }

                var positionTag = matcher.group(1);
                var positionName = matcher.group(2);
                var sectionTitle = matcher.group(3);

                System.out.printf("""
                        position_tag: %s
                        position_name: %s
                        section_title: %s
                        %n""", positionTag, positionName, sectionTitle);

                var qaPairsStr = matcher.group(4);
                var qaPairs = qaPairsStr.split("\n\n");
                var chapterPattern = Pattern.compile("\\b(the|this)(sentence|section|chapter)(?=[\\s?!.,]|$)", Pattern.CASE_INSENSITIVE);
                for (var qaPair : qaPairs) {
                    var qaMatcher = QA_PATTERN.matcher(qaPair);
                    if (!qaMatcher.find()) {
                        continue;
                    }
                    var question = qaMatcher.group(1);
                    // Skip anaphoric expressions
                    if (Constants.RE_ANAPHORA.matcher(question).find()) {
                        continue;
                    }
                    if (chapterPattern.matcher(question).find()) {
                        continue;
                    }

                    var answer = qaMatcher.group(2);

                    System.out.println("Get Embeddings..");
                    System.out.println("Question: " + question);
                    System.out.println("Answer: " + answer);
                    var segment = TextSegment.from(question);
                    segment.metadata().put("position_tag", positionTag);
                    segment.metadata().put("position_name", positionName);
                    segment.metadata().put("section_title", sectionTitle);
                    segment.metadata().put("answer", answer);

                    var embedding = model.embed(segment).content();
                    System.out.println("Done.");
                    embeddings.add(embedding);
                    embedded.add(segment);
                }

            }

            System.out.println("Adding embeddings to store..");
            embeddingStore.addAll(embeddings, embedded);

            var serializedStore = embeddingStore.serializeToJson();
            var outputPath = inputPath.getParent().resolve("qa_store.json");
            Files.writeString(outputPath, serializedStore);
            System.out.println("Store serialized to: " + outputPath);

        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }
}
