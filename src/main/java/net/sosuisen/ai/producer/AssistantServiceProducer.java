package net.sosuisen.ai.producer;

import dev.langchain4j.model.openai.OpenAiChatModelName;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import net.sosuisen.ai.annotation.ChatModelName;
import net.sosuisen.ai.annotation.SystemMessage;
import net.sosuisen.ai.annotation.Temperature;
import net.sosuisen.ai.service.AssistantService;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class AssistantServiceProducer {
    @Produces
    public AssistantService createAssistantService(InjectionPoint injectionPoint) {
        OpenAiChatModelName modelName = GPT_4_O_MINI;
        String systemMessage = "You are a polite assistant.";
        double temperature = 0.8;

        if (injectionPoint.getAnnotated().isAnnotationPresent(ChatModelName.class)) {
            modelName = injectionPoint.getAnnotated().getAnnotation(ChatModelName.class).value();
        }
        if (injectionPoint.getAnnotated().isAnnotationPresent(SystemMessage.class)) {
            systemMessage = injectionPoint.getAnnotated().getAnnotation(SystemMessage.class).value();
        }
        if (injectionPoint.getAnnotated().isAnnotationPresent(Temperature.class)) {
            temperature = injectionPoint.getAnnotated().getAnnotation(Temperature.class).value();
        }

        return new AssistantService(modelName, temperature, systemMessage);
    }
}