package net.sosuisen.ai.annotation;

import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EmbeddingModelName {
    OpenAiEmbeddingModelName value();
}