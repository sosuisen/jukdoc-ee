package net.sosuisen.resources;

import jakarta.inject.Inject;
import jakarta.mvc.MvcContext;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import net.sosuisen.model.*;
import net.sosuisen.service.QAService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Log
@Path("/api/chat")
public class Chat {
    // Pattern that matches anaphoric expressions
    private final Pattern RE_ANAPHORA = Pattern.compile("(that|this|those|these|such|it|he|she|they|his|her|its|their)[\\s?!.,]", Pattern.CASE_INSENSITIVE);

    private final ParagraphDAO paragraphDAO;
    private final QAService qaService;
    private final StaticMessage staticMessage;
    private final ChatCommand chatCommand;

    @GET
    @Path("opening-words")
    public ChatMessage getOpeningWords() throws IOException {
        return new ChatMessage("AI", staticMessage.getOpeningWords());
    }

    @POST
    @Path("query")
    public ChatMessage query(@Valid MessageDTO message) throws SQLException {
        var query = message.getMessage();

        ChatCommand.Command command = chatCommand.getCommand(query);
        System.out.println("command: " + command);

        if (command == null) {
            // If the question is three characters or fewer, a search won't yield meaningful paragraphs.
            // In this case, only the previous conversation history and the question itself should be provided to GPT.
            // Additionally, if the question contains anaphora (e.g., "What is that?"),
            // an appropriate search result will not be achieved.
            // Providing only the previous conversation history and question to GPT will automatically resolve the anaphora.
            if (query.length() > 3) {
                if (RE_ANAPHORA.matcher(query).find()){
                    System.out.println("includes anaphora");
                }
                else {
                    return new ChatMessage("AI", query);
                }
            }

        }

        var response = qaService.query(query);
        return new ChatMessage("AI", response);
    }

}
