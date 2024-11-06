package net.sosuisen.resources;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sosuisen.model.ChatCommand;
import net.sosuisen.model.ChatMessage;
import net.sosuisen.model.QueryDTO;
import net.sosuisen.model.StaticMessage;
import net.sosuisen.security.CsrfValidator;
import net.sosuisen.service.ChatService;
import net.sosuisen.service.SuggestService;

import java.sql.SQLException;
import java.util.List;

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Path("/api/chat")
public class Chat {
    private final StaticMessage staticMessage;
    private final ChatCommand chatCommand;
    private final ChatService chatService;
    private final SuggestService suggestService;
    private final CsrfValidator csrfValidator;

    @GET
    @Path("opening-words")
    public ChatMessage getOpeningWords() {
        csrfValidator.validateCsrfToken();
        return new ChatMessage("AI", staticMessage.getOpeningWords(), List.of(), suggestService.suggestFirst());
    }

    @POST
    @Path("query")
    public ChatMessage query(@Valid QueryDTO queryObj) throws SQLException {
        csrfValidator.validateCsrfToken();

        var query = queryObj.getMessage();
        ChatCommand.Command command;
        if (queryObj.getPositionTag() != null) {
            command = ChatCommand.Command.PROCEED_FROM_INDICATED_POSITION;
        } else {
            command = chatCommand.get(query);
        }
        if (command != null) {
            return chatService.proceedByCommand(command, queryObj);
        }
        return chatService.proceedByPrompt(query);
    }
}
