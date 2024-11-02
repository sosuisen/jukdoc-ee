package net.sosuisen.resources;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sosuisen.model.*;
import net.sosuisen.service.ParagraphService;
import net.sosuisen.service.QAService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
@Path("/api/chat")
public class Chat {
    // Pattern that matches anaphoric expressions
    private final Pattern RE_ANAPHORA = Pattern.compile("(that|this|those|these|such|it|he|she|they|his|her|its|their)[\\s?!.,]", Pattern.CASE_INSENSITIVE);

    private final ParagraphDAO paragraphDAO;
    private final StaticMessage staticMessage;
    private final ChatCommand chatCommand;
    private final ParagraphService paragraphService;
    private final QAService qaService;

    OpenAiChatModel chatModel;

    @PostConstruct
    public void init() {
        chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O_MINI)
                .build();
    }

    @GET
    @Path("opening-words")
    public ChatMessage getOpeningWords() throws IOException {
        return new ChatMessage("AI", staticMessage.getOpeningWords());
    }

    @POST
    @Path("query")
    public ChatMessage query(@Valid QueryDTO query) throws SQLException {
        var mes = query.getMessage();

        ChatCommand.Command command = chatCommand.getCommand(mes);
        System.out.println("command: " + command);
        if (command == null) {
            // If the question is three characters or fewer, a search won't yield meaningful paragraphs.
            // In this case, only the previous conversation history and the question itself should be provided to GPT.
            // Additionally, if the question contains anaphora (e.g., "What is that?"),
            // an appropriate search result will not be achieved.
            // Providing only the previous conversation history and question to GPT will automatically resolve the anaphora.
            if (mes.length() > 3) {
                System.out.println("mes: " + mes);
                if (RE_ANAPHORA.matcher(mes).find()) {
                    System.out.println("includes anaphora");
                } else {
                    var retrievalDocs = retrieveDocuments(mes);
                    if (!retrievalDocs.isEmpty()) {
                        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                                .apiKey(System.getenv("OPENAI_API_KEY"))
                                .modelName(GPT_4_O_MINI)
                                .build();
                        var response = proceedByPrompt("", retrievalDocs, mes);
                        System.out.println("response: " + response);
                        return new ChatMessage("AI", response);
                    }
                }
            }

        }

        return new ChatMessage("AI", "test");
    }

    private String getPrompt(String log, ArrayList<Document> retrievalDocs, String query) {
        var count = 0;
        var contextBuilder = new StringBuilder();
        var refsBuilder = new StringBuilder();
        var refsHintBuilder = new StringBuilder();

        for (var doc : retrievalDocs) {
            count++;
            contextBuilder.append("[reference: ").append(count).append("] ").append(doc.getContext()).append("\n");
            refsBuilder.append("[reference: ").append(count).append("], ");
            refsHintBuilder.append("* If the response includes the above 【reference: ").append(count).append("], ")
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
                """.formatted(log, contextBuilder.toString(), refsBuilder.toString(), refsHintBuilder.toString(), query);

        System.out.println("prompt: " + prompt);
        return prompt;
    }

    private String proceedByPrompt(String log, ArrayList<Document> retrievalDocs, String query) {
        var prompt = getPrompt(log, retrievalDocs, query);
        var response = chatModel.generate(List.of(
                SystemMessage.from("You are a skilled assistant."),
                UserMessage.from(prompt)));
        return response.content().text();
    }

    private ArrayList<Document> retrieveDocuments(String message) throws SQLException {
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
