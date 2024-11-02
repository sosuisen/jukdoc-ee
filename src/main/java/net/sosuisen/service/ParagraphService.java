package net.sosuisen.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import net.sosuisen.model.Document;

import java.util.ArrayList;

@ApplicationScoped
public class ParagraphService {
    private static final String paragraphStorePath = "paragraph_store.json";

    private InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private OpenAiEmbeddingModel model;

    @PostConstruct
    public void init() {
        embeddingStore = VectorStoreLoader.load(paragraphStorePath);
        model = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("text-embedding-3-small")
                .build();
    }

    public ArrayList<Document> query(String query, int maxParagraphDocs, double threshold) {
        var segment = TextSegment.from(query);
        var embedding = model.embed(segment).content();

        EmbeddingSearchRequest request = new EmbeddingSearchRequest(embedding, maxParagraphDocs, threshold, null);
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        var documents = new ArrayList<Document>();
        if (result.matches().isEmpty()) {
            return null;
        } else {
            for (var match : result.matches()) {
                documents.add(new Document("paragraph",
                        match.embedded().metadata().getString("position_tag"),
                        match.embedded().metadata().getString("position_name"),
                        match.embedded().metadata().getString("section_title"),
                        match.embedded().text(),
                        match.score()));
            }
            return documents;
        }
    }
}
