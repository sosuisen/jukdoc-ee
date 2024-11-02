package net.sosuisen.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

@ApplicationScoped
public class QAService {
    private InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private OpenAiEmbeddingModel model;

    public QAService() {
        var qaStorePath = "qa_store.json";
        try (InputStream inputStream = QAService.class.getClassLoader().getResourceAsStream(qaStorePath);
             var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            var qaStoreJson = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                qaStoreJson.append(line);
            }
            embeddingStore = InMemoryEmbeddingStore.fromJson(qaStoreJson.toString());

            model = OpenAiEmbeddingModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("text-embedding-3-small")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String query(String query) {
        var segment = TextSegment.from(query);
        var embedding = model.embed(segment).content();

        EmbeddingSearchRequest request = new EmbeddingSearchRequest(embedding, 1, 0.8, null);
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        if (result.matches().isEmpty()) {
            return "";
        }
        else {
            return result.matches().getFirst().embedded().metadata().getString("answer");
        }
    }

}
