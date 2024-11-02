package net.sosuisen.service;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sosuisen.model.Document;
import net.sosuisen.model.History;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@ApplicationScoped
public class ChatService {
    OpenAiChatModel chatModel;
    private final ParagraphService paragraphService;
    private final QAService qaService;
    private final History history;

    @PostConstruct
    public void init() {
        chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O_MINI)
                .temperature(0.0)
                .build();
    }

    private String getPrompt(ArrayList<Document> retrievalDocs, String query) {
        var chatHistory = history.stream()
                .map(doc -> {
                    var a = "You: " + doc.getAnswer();
                    var q = doc.getQuery();
                    if (q.isEmpty()){
                        return a + "\n";
                    }
                    return "Me: " + q + "\nYou: " + a + "\n";
                })
                .collect(Collectors.joining("\n"));

        var count = 0;
        var contextBuilder = new StringBuilder();
        var refsBuilder = new StringBuilder();
        var refsHintBuilder = new StringBuilder();

        for (var doc : retrievalDocs) {
            count++;
            contextBuilder.append("[reference: ").append(count).append("] ").append(doc.getContext()).append("\n");
            refsBuilder.append("[reference: ").append(count).append("], ");
            refsHintBuilder.append("* If the response sentence includes the above [reference: ").append(count).append("], ")
                    .append("add [*").append(count).append("] at the end of the sentence.\n");
        }

        if (!refsBuilder.isEmpty()) {
            refsBuilder.insert(0, "please refer to the following references:");
        }
        if (contextBuilder.isEmpty()) {
            contextBuilder.append("(No relevant information found.)");
        }

        var prompt = """
                In the past, we discussed the following topics:
                %s
                
                Additionally, the following related conversations took place:
                %s
                
                Building upon these discussions, %s generate the best possible answer to my question.
                
                However, please adhere to the following "Constraints"
                
                [Constraints]
                * If a direct answer cannot be provided based on the above text, you will preface your response concisely with, "As the direct answer is not contained in this document, I cannot provide an exact response, but".
                * Replace outdated expressions with modern ones.
                * Avoid repeating the question in the response.
                * Ensure the response is easy for elementary-level readers to understand.
                * Do not omit any important keywords.
                * Keep idioms intact.
                * Begin with a concise summary, followed by a detailed explanation.
                * Add a line break at the end of each sentence.
                %s
                
                [My Question] %s
                """.formatted(chatHistory, contextBuilder.toString(), refsBuilder.toString(), refsHintBuilder.toString(), query);

        System.out.println("prompt: " + prompt);
        return prompt;
    }

    public String proceedByPrompt(ArrayList<Document> retrievalDocs, String query) {
        var prompt = getPrompt(retrievalDocs, query);
        var response = chatModel.generate(List.of(
                SystemMessage.from("You are a skilled assistant."),
                UserMessage.from(prompt)));
        return response.content().text();
    }

    public ArrayList<Document> retrieveDocuments(String message) throws SQLException {
        var maxParagraphDocs = 1;
        var paragraphDocs = paragraphService.query(message, maxParagraphDocs, 0.8);
        var qaDocs = qaService.query(message, 3, 0.8);

        var mergedDocs = new ArrayList<Document>();
        if (paragraphDocs != null) {
            mergedDocs.addAll(paragraphDocs);
        }
        if (qaDocs != null) {
            mergedDocs.addAll(qaDocs);
        }
        mergedDocs.sort(Comparator.comparingDouble(Document::getScore).reversed());

        var context = mergedDocs.stream()
                .map(Document::toString)
                .collect(Collectors.joining("\n\n"));
        System.out.println("context: " + context);

        return mergedDocs;
    }
}
