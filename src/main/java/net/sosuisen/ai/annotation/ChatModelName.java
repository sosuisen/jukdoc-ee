package net.sosuisen.ai.annotation;

import dev.langchain4j.model.openai.OpenAiChatModelName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ChatModelName {
    OpenAiChatModelName value();
}