package net.sosuisen.ai.producer;

import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import net.sosuisen.ai.annotation.MaxResults;
import net.sosuisen.ai.annotation.EmbeddingModelName;
import net.sosuisen.ai.annotation.StoreJsonPath;
import net.sosuisen.ai.annotation.Threshold;
import net.sosuisen.ai.service.EmbeddingSearchService;
import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

public class EmbeddingSearchServiceProducer {

    @Produces
    public EmbeddingSearchService createEmbeddingSearchService(InjectionPoint injectionPoint) {
        String storePath = "";
        OpenAiEmbeddingModelName modelName = TEXT_EMBEDDING_3_SMALL;
        int maxResults = 10;
        double threshold = 0.8;

        if (injectionPoint.getAnnotated().isAnnotationPresent(StoreJsonPath.class)) {
            storePath = injectionPoint.getAnnotated().getAnnotation(StoreJsonPath.class).value();
        }
        if (injectionPoint.getAnnotated().isAnnotationPresent(EmbeddingModelName.class)) {
            modelName = injectionPoint.getAnnotated().getAnnotation(EmbeddingModelName.class).value();
        }
        if (injectionPoint.getAnnotated().isAnnotationPresent(MaxResults.class)) {
            maxResults = injectionPoint.getAnnotated().getAnnotation(MaxResults.class).value();
        }
        if (injectionPoint.getAnnotated().isAnnotationPresent(Threshold.class)) {
            threshold = injectionPoint.getAnnotated().getAnnotation(Threshold.class).value();
        }

        return new EmbeddingSearchService(storePath, modelName, maxResults, threshold);
    }
}