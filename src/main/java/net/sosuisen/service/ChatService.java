package net.sosuisen.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sosuisen.Constants;
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
@Slf4j
public class ChatService {
    @SuppressWarnings("FieldCanBeLocal")
    private final int HISTORY_SIZE = 3;

    private final ParagraphService paragraphService;
    private final QAService qaService;
    private final AssistantService assistantService;
    private final SuggestService suggestService;

    private final UserStatus userStatus;
    private final ReadingRecordDAO readingRecordDAO;
    private final ParagraphDAO paragraphDAO;
    private final StaticMessage staticMessage;

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
                * Use a polite tone.
                * First, write a concise summary within 70 characters.
                * It is not necessary to write "Summary:" before the summary.
                * Then, under the summary, use bullet points starting with "-" for each topic.
                * Begin each bullet with a keyword representing the content, followed by ":" and then the details.
                * Use simple HTML to format the response.
                * You may only use <ul>, <li>, <b> tags and [*1] [*2] [*3] .. tags for references.
                
                %s
                * If you place a reference mark, it must be immediately after each sentence. Placing all reference marks at the end of all your answers is not a good method.
                
                [My Question] %s
                """.formatted(chatHistory, contextBuilder.toString(), refsBuilder.toString(), refsHintBuilder.toString(), query);

        log.debug("## prompt: {}", prompt);
        return prompt;
    }

    private List<Document> getDocumentFromLatestHistory() {
        if (!userStatus.getHistory().isEmpty()) {
            return userStatus.getHistory().getLast().getReferredDocs();
        }
        return new ArrayList<>();
    }

    private ChatMessage proceedCurrentTopic(String query, String answer, ParagraphDTO nextParagraph) throws SQLException {
        if (nextParagraph != null) {
            userStatus.setCurrentPositionTag(nextParagraph.getPositionTag());
            answer += nextParagraph.getSummary();

            if (userStatus.getHistory().size() >= HISTORY_SIZE) {
                userStatus.getHistory().removeFirst();
            }
            var doc = new Document(
                    "paragraph",
                    nextParagraph.getPositionTag(),
                    nextParagraph.getPositionName(),
                    nextParagraph.getSectionTitle(),
                    nextParagraph.getParagraph(),
                    1.0);
            userStatus.getHistory().add(new HistoryDocument(query, answer, List.of(doc)));
            if (!nextParagraph.isRead()) {
                log.debug("## create reading record: {}", nextParagraph.getPositionTag());
                readingRecordDAO.create(userStatus.getUserName(), nextParagraph.getPositionTag());
            }

            var refs = List.of(":" + nextParagraph.getPositionTag() + ":" + nextParagraph.getPositionName());
            return new ChatMessage("AI", answer, refs, suggestService.suggest(answer));
        } else {
            answer = staticMessage.getIsFinalTopic();
            return new ChatMessage("AI", answer, List.of(), suggestService.suggestFirst());
        }
    }

    public ChatMessage proceedByCommand(ChatCommand.Command command, String query) throws SQLException {
        log.debug("## command: {}", command.name());
        String answer = "";
        ParagraphDTO nextParagraph = null;
        return switch (command) {
            case PROCEED_FROM_BEGINNING -> {
                userStatus.setCurrentPositionTag("");
                answer = staticMessage.getProceedCurrentTopicStartHeader();
                nextParagraph = paragraphDAO.getFirstParagraph(userStatus.getUserName());
                yield proceedCurrentTopic(query, answer, nextParagraph);
            }
            case PROCEED_FROM_UNREAD -> {
                var unreadParagraph = paragraphDAO.getFirstUnreadParagraph(userStatus.getUserName());
                userStatus.setCurrentPositionTag(unreadParagraph.getPositionTag());
                yield proceedCurrentTopic(query, staticMessage.getProceedFromUnreadParts(), unreadParagraph);
            }
            case PROCEED_CURRENT_TOPIC -> {
                if (userStatus.getCurrentPositionTag().isEmpty()) {
                    answer = staticMessage.getProceedCurrentTopicStartHeader();
                    nextParagraph = paragraphDAO.getFirstParagraph(userStatus.getUserName());
                } else {
                    answer = staticMessage.getProceedCurrentTopicHeader();
                    nextParagraph = paragraphDAO.getNextParagraph(userStatus.getCurrentPositionTag(), userStatus.getUserName());
                }
                yield proceedCurrentTopic(query, answer, nextParagraph);
            }
            case REPEAT_ONLY_CURRENT_TOPIC -> promptToAI(getDocumentFromLatestHistory(), query);
        };
    }

    public ChatMessage proceedByPrompt(String query) throws SQLException {
        log.debug("## proceed by prompt, query: {}", query);
        List<Document> retrievalDocs;
        // If the query is three characters or fewer, a search won't yield meaningful paragraphs.
        if (query.length() <= 3) {
            retrievalDocs = getDocumentFromLatestHistory();
        }
        // If the question contains anaphora (e.g., "What is that?"),
        // an appropriate search result will not be achieved.
        // Providing the previous conversation to GPT will automatically resolve the anaphora.
        else if (Constants.RE_ANAPHORA.matcher(query).find()) {
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
        log.debug("## response: {}", answer);

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
        if (!referredDocs.isEmpty()) {
            userStatus.setCurrentPositionTag(referredDocs.getFirst().getPositionTag());
        }
        if (userStatus.getHistory().size() >= HISTORY_SIZE) {
            userStatus.getHistory().removeFirst();
        }
        var sanitizedAnswer = answer.replaceAll("\\[\\*\\d+]", "");
        userStatus.getHistory().add(new HistoryDocument(query, sanitizedAnswer, referredDocs));

        for (var doc : referredDocs) {
            readingRecordDAO.create(userStatus.getUserName(), doc.getPositionTag());
        }

        return new ChatMessage("AI", answer, uniqueRefs, suggestService.suggest(answer));
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
