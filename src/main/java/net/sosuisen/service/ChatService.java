package net.sosuisen.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sosuisen.ai.annotation.SystemMessage;
import net.sosuisen.ai.annotation.Temperature;
import net.sosuisen.ai.service.AssistantService;
import net.sosuisen.model.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ChatService {
    @SuppressWarnings("FieldCanBeLocal")
    private final int HISTORY_SIZE = 3;
    // Pattern that matches anaphoric expressions
    private final Pattern RE_ANAPHORA = Pattern.compile("\\b(that|this|those|these|such|it|its|he|his|she|her|they|their)(?=[\\s?!.,]|$)", Pattern.CASE_INSENSITIVE);

    private final ParagraphService paragraphService;
    private final QAService qaService;
    private final UserStatus userStatus;
    private final ReadingRecordDAO readingRecordDAO;

    @Inject
    @SystemMessage("You are a skilled assistant.")
    @Temperature(0.0)
    private AssistantService assistantService;

    private String getPrompt(List<Document> retrievalDocs, String query) {
        var chatHistory = userStatus.getHistory().stream()
                .map(doc -> {
                    var a = "You: " + doc.getAnswer();
                    var q = doc.getQuery();
                    if (q.isEmpty()) {
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
                    .append("add a reference mark [*").append(count).append("] at the right after that sentence.\n");
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
                * If you place a reference mark, it must be immediately after each sentence. Placing all reference marks at the end of all your answers is not a good method.
                
                [My Question] %s
                """.formatted(chatHistory, contextBuilder.toString(), refsBuilder.toString(), refsHintBuilder.toString(), query);

        System.out.println("prompt: " + prompt);
        return prompt;
    }

    private List<Document> getDocumentFromLatestHistory() {
        if (!userStatus.getHistory().isEmpty()) {
            return userStatus.getHistory().getLast().getReferredDocs();
        }
        return new ArrayList<>();
    }

    private ChatMessage proceedCurrentTopic(String query) {
        return new ChatMessage("AI", "I'm sorry, but I don't have any information on that topic.", new ArrayList<>());
    }

    public ChatMessage proceedByCommand(ChatCommand.Command command, String query) throws SQLException {
        return switch (command) {
            case PROCEED_CURRENT_TOPIC -> proceedCurrentTopic(query);
            case REPEAT_ONLY_CURRENT_TOPIC -> promptToAI(getDocumentFromLatestHistory(), query);
        };
    }

    public ChatMessage proceedByPrompt(String query) throws SQLException {
        List<Document> retrievalDocs;
        // If the query is three characters or fewer, a search won't yield meaningful paragraphs.
        if (query.length() <= 3) {
            retrievalDocs = getDocumentFromLatestHistory();
        }
        // If the question contains anaphora (e.g., "What is that?"),
        // an appropriate search result will not be achieved.
        // Providing the previous conversation to GPT will automatically resolve the anaphora.
        else if (RE_ANAPHORA.matcher(query).find()) {
            retrievalDocs = getDocumentFromLatestHistory();
        } else {
            retrievalDocs = retrieveDocuments(query);
        }
        return promptToAI(retrievalDocs, query);
    }

    private ChatMessage promptToAI(List<Document> retrievalDocs, String query) throws SQLException {
        // Call API
        var prompt = getPrompt(retrievalDocs, query);
        var answer = assistantService.generate(prompt);
        System.out.println("response: " + answer);

        // Process references
        var reEachRef = Pattern.compile("\\[*(\\d+)]", Pattern.DOTALL);
        var refIndexList = new ArrayList<Integer>();
        var matcher = reEachRef.matcher(answer);
        while (matcher.find()) {
            int ref = Integer.parseInt(matcher.group(1));
            if (ref <= retrievalDocs.size()) {
                refIndexList.add(ref);
            }
        }
        List<Integer> uniqueRefIndexList = refIndexList.stream()
                .sorted()
                .distinct()
                .toList();
        var uniqueRefs = uniqueRefIndexList.stream()
                .map(ref -> ref + ":" + retrievalDocs.get(ref - 1).getPositionTag() + ":" + retrievalDocs.get(ref - 1).getPositionName())
                .collect(Collectors.toList());
        // Get referred documents
        var referredDocs = uniqueRefIndexList.stream()
                .map(ref -> retrievalDocs.get(ref - 1))
                .toList();

        // Save history
        if (userStatus.getHistory().size() >= HISTORY_SIZE) {
            userStatus.getHistory().removeFirst();
        }
        var sanitizedAnswer = answer.replaceAll("\\[\\*\\d+]", "");
        userStatus.getHistory().add(new HistoryDocument(query, sanitizedAnswer, referredDocs));

        for (var doc : referredDocs) {
            readingRecordDAO.create(userStatus.getUserName(), doc.getPositionTag());
        }

        return new ChatMessage("AI", answer, uniqueRefs);
    }

    /**
     * Retrieve documents from the Embedding Stores for RAG
     */
    private ArrayList<Document> retrieveDocuments(String message) throws SQLException {
        // Retrieve direct answers from the QA store
        var qaDocs = qaService.query(message);
        // Retrieve relevant paragraphs from the paragraph store
        var paragraphDocs = paragraphService.query(message);

        var mergedDocs = new ArrayList<Document>();
        if (qaDocs != null) {
            mergedDocs.addAll(qaDocs);
        }
        if (paragraphDocs != null) {
            mergedDocs.addAll(paragraphDocs);
        }
        mergedDocs.sort(Comparator.comparingDouble(Document::getScore).reversed());

        return mergedDocs;
    }
}
