package net.sosuisen.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sosuisen.ai.annotation.MaxResults;
import net.sosuisen.ai.annotation.StoreJsonPath;
import net.sosuisen.ai.service.EmbeddingSearchService;
import net.sosuisen.model.StaticMessage;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SuggestService {
    private final StaticMessage staticMessage;

    @Inject
    @StoreJsonPath("qa_store.json")
    @MaxResults(2)
    private EmbeddingSearchService embeddingSearchService;

    public List<String> suggestFirst() {
        var currentMessage = staticMessage.getOpeningWords();
        EmbeddingSearchResult<TextSegment> result = embeddingSearchService.search(currentMessage);

        var suggestions = new ArrayList<String>();
        suggestions.add(staticMessage.getStartFromBeginning());
        suggestions.add(staticMessage.getReadUnreadParts());
        for (var match : result.matches()) {
            suggestions.add(match.embedded().text());
        }
        return suggestions;
    }

    public List<String> suggest(String currentMessage) {
        EmbeddingSearchResult<TextSegment> result = embeddingSearchService.search(currentMessage);

        var suggestions = new ArrayList<String>();
        suggestions.add(staticMessage.getMoveOnToNextTopic());
        suggestions.add(staticMessage.getReadUnreadParts());
        for (var match : result.matches()) {
            suggestions.add(match.embedded().text());
        }
        return suggestions;
    }
}
