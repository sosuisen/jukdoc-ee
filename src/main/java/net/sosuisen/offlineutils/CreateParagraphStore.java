package net.sosuisen.aiutils;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static net.sosuisen.Constants.PARAGRAPH_PATTERN;

public class CreateParagraphStore {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: mvn clean package exec:java -Dexec.mainClass=\"net.sosuisen.CreateParagraphStore\" -Dexec.args=\"path-to-structured_paragraph.txt\"");
            System.exit(1);
        }
        try {
            var inputPath = Path.of(args[0]);
            var structuredParagraphStr = Files.readString(inputPath);

            var embeddingStore = new InMemoryEmbeddingStore<TextSegment>();

            OpenAiEmbeddingModel model = OpenAiEmbeddingModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("text-embedding-3-small")
                    .build();

            var embeddings = new ArrayList<Embedding>();
            var embedded = new ArrayList<TextSegment>();
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

                System.out.println("Get Embeddings..");
                var segment = TextSegment.from(paragraph);
                segment.metadata().put("position_tag", positionTag);
                segment.metadata().put("position_name", positionName);
                segment.metadata().put("section_title", sectionTitle);

                var embedding = model.embed(segment).content();
                System.out.println("Done.");
                embeddings.add(embedding);
                embedded.add(segment);
            }
            System.out.println("Adding embeddings to store..");
            embeddingStore.addAll(embeddings, embedded);

            var serializedStore = embeddingStore.serializeToJson();
            var outputPath = inputPath.getParent().resolve("paragraph_store.json");
            Files.writeString(outputPath, serializedStore);
            System.out.println("Store serialized to: " + outputPath);
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }
}
