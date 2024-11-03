package net.sosuisen.offlineutils;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * RetrieveQA
 * This is for testing the qa_store.json
 */
public class RetrieveQA {

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage: mvn clean package exec:java -Dexec.mainClass=\"net.sosuisen.RetrieveQA\" -Dexec.args=\"path-to-qa_store.json\"");
                System.exit(1);
            }

            var inputPath = Path.of(args[0]);
            InMemoryEmbeddingStore<TextSegment> embeddingStore = InMemoryEmbeddingStore.fromFile(inputPath);

            var scanner = new Scanner(System.in);
            System.out.println("Enter your query: ");
            var query = scanner.nextLine();
            OpenAiEmbeddingModel model = OpenAiEmbeddingModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("text-embedding-3-small")
                    .build();
            var segment = TextSegment.from(query);
            var embedding = model.embed(segment).content();

            EmbeddingSearchRequest request = new EmbeddingSearchRequest(embedding, 3, 0.1, null);
            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
            result.matches().forEach(match -> System.out.printf("""
                            score: %f
                            query: %s
                            position_tag: %s
                            position_name: %s
                            section_title: %s
                            answer: %s
                            """, match.score(),
                    match.embedded().text(),
                    match.embedded().metadata().getString("position_tag"),
                    match.embedded().metadata().getString("position_name"),
                    match.embedded().metadata().getString("section_title"),
                    match.embedded().metadata().getString("answer")
            ));
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }
}
