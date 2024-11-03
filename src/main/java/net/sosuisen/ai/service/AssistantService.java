package net.sosuisen.ai.service;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import jakarta.enterprise.inject.Vetoed;
import lombok.NoArgsConstructor;

import java.util.List;

@Vetoed
@NoArgsConstructor(force = true)
public class AssistantService {
    private final String systemMessage;
    private final OpenAiChatModel model;

    public AssistantService(OpenAiChatModelName modelName, double temperature, String systemMessage) {
        this.systemMessage = systemMessage;
        model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    public String generate(String prompt) {
        var response = model.generate(List.of(
                SystemMessage.from(systemMessage),
                UserMessage.from(prompt)));
        return response.content().text();
    }
}
