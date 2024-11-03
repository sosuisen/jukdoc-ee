package net.sosuisen.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.sosuisen.ai.annotation.MaxResults;
import net.sosuisen.ai.annotation.StoreJsonPath;
import net.sosuisen.ai.annotation.Threshold;
import net.sosuisen.ai.service.EmbeddingSearchService;
import net.sosuisen.model.Document;

import java.util.ArrayList;
import java.util.Comparator;

@ApplicationScoped
public class ParagraphService {
    @Inject
    @StoreJsonPath("paragraph_store.json")
    @MaxResults(1)
    @Threshold(0.5)
    private EmbeddingSearchService embeddingSearchService;

    public ArrayList<Document> query(String query) {
        EmbeddingSearchResult<TextSegment> result = embeddingSearchService.search(query);

        if (result.matches().isEmpty()) {
            return null;
        } else {
            var documents = new ArrayList<Document>();
            for (var match : result.matches()) {
                documents.add(new Document("paragraph",
                        match.embedded().metadata().getString("position_tag"),
                        match.embedded().metadata().getString("position_name"),
                        match.embedded().metadata().getString("section_title"),
                        match.embedded().text(),
                        match.score()));
            }
            // Documents are sorted in descending order by score and in ascending order by position tag.
            documents.sort(
                    Comparator.comparingDouble(Document::getScore).reversed()
                            .thenComparing(Document::getPositionName)
            );
            return documents;
        }
    }
}
