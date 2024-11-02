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

import java.io.IOException;
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
    private final Pattern RE_ANAPHORA = Pattern.compile("(that|this|those|these|such|it|he|she|they|his|her|its|their)[\\s?!.,]", Pattern.CASE_INSENSITIVE);

    private final StaticMessage staticMessage;
    private final ChatCommand chatCommand;
    private final ChatService chatService;

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
                    var retrievalDocs = chatService.retrieveDocuments(mes);
                    if (!retrievalDocs.isEmpty()) {
                        var response = chatService.proceedByPrompt("", retrievalDocs, mes);
                        System.out.println("response: " + response);
                        return new ChatMessage("AI", response);
                    }
                }
            }

        }

        return new ChatMessage("AI", "test");
    }

}
