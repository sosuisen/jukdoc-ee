package net.sosuisen.ai.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.inject.Vetoed;
import lombok.NoArgsConstructor;
import net.sosuisen.service.VectorStoreLoader;

@Vetoed
@NoArgsConstructor(force = true)
public class EmbeddingSearchService {
    private final int maxResults;
    private final double threshold;

    private InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private OpenAiEmbeddingModel model;

    public EmbeddingSearchService(String storePath, OpenAiEmbeddingModelName modelName, int maxResults, double threshold) {
        this.maxResults = maxResults;
        this.threshold = threshold;
        embeddingStore = VectorStoreLoader.load(storePath);
        model = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(modelName)
                .build();
    }

    public EmbeddingSearchResult<TextSegment> search(String query) {
        var segment = TextSegment.from(query);
        var embedding = model.embed(segment).content();
        EmbeddingSearchRequest request = new EmbeddingSearchRequest(embedding, maxResults, threshold, null);
        return embeddingStore.search(request);
    }
}
