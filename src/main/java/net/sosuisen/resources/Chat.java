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

import java.sql.SQLException;
import java.util.regex.Pattern;

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
        return new ChatMessage("AI", staticMessage.getOpeningWords());
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
                    System.out.println("response: " + answer);
                    if (history.size() >= HISTORY_SIZE) {
                        history.removeFirst();
                    }
                    var answerInHistory = answer.replaceAll("\\[\\*\\d+]", "");

                    history.add(new HistoryDocument(query, answerInHistory));
                    return new ChatMessage("AI", answer);

                }
            }

        }

        return new ChatMessage("AI", "test");
    }

}
