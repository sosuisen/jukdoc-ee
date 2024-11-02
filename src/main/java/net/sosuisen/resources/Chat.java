package net.sosuisen.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sosuisen.model.*;
import net.sosuisen.service.ChatService;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
@Path("/api/chat")
public class Chat {
    // Pattern that matches anaphoric expressions
    private final Pattern RE_ANAPHORA = Pattern.compile("(that|this|those|these|such)[\\s?!.,]", Pattern.CASE_INSENSITIVE);

    private final StaticMessage staticMessage;
    private final ChatCommand chatCommand;
    private final ChatService chatService;
    private final History history;

    private final int HISTORY_SIZE = 3;

    @GET
    @Path("opening-words")
    public ChatMessage getOpeningWords() {
        return new ChatMessage("AI", staticMessage.getOpeningWords(), new ArrayList<>());
    }

    @POST
    @Path("query")
    public ChatMessage query(@Valid QueryDTO queryObj) throws SQLException {
        var query = queryObj.getMessage();

        ChatCommand.Command command = chatCommand.getCommand(query);
        System.out.println("command: " + command);
        if (command == null) {
            // If the question is three characters or fewer, a search won't yield meaningful paragraphs.
            // In this case, only the previous conversation history and the question itself should be provided to GPT.
            // Additionally, if the question contains anaphora (e.g., "What is that?"),
            // an appropriate search result will not be achieved.
            // Providing only the previous conversation history and question to GPT will automatically resolve the anaphora.
            if (query.length() > 3) {
                System.out.println("mes: " + query);
                if (RE_ANAPHORA.matcher(query).find()) {
                    System.out.println("includes anaphora");
                } else {
                    var retrievalDocs = chatService.retrieveDocuments(query);

                    var answer = chatService.proceedByPrompt(retrievalDocs, query);

                    var reEachRef = Pattern.compile("\\[*(\\d+)]", Pattern.DOTALL);
                    var refs = new ArrayList<Integer>();
                    var matcher = reEachRef.matcher(answer);
                    while (matcher.find()) {
                        int ref = Integer.parseInt(matcher.group(1));
                        if (ref <= retrievalDocs.size()) {
                            refs.add(ref);
                        }
                    }
                    System.out.println("response: " + answer);

                    List<String> uniqueRefs = refs.stream()
                            .sorted()
                            .distinct()
                            .map(ref -> ref + ":" + retrievalDocs.get(ref -1).getPositionTag() + ":" + retrievalDocs.get(ref - 1).getPositionName())
                            .toList();

                    for (var ref : uniqueRefs) {
                        System.out.println("uniqueRefs: " + ref);
                    }

                    if (history.size() >= HISTORY_SIZE) {
                        history.removeFirst();
                    }
                    var answerInHistory = answer.replaceAll("\\[\\*\\d+]", "");
                    history.add(new HistoryDocument(query, answerInHistory));
                    return new ChatMessage("AI", answer, uniqueRefs);

                }
            }

        }

        return new ChatMessage("AI", "no answer", new ArrayList<>());
    }

}
