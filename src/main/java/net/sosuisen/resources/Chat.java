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
import net.sosuisen.service.ChatService;

import java.sql.SQLException;
import java.util.ArrayList;

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

    @GET
    @Path("opening-words")
    public ChatMessage getOpeningWords() {
        return new ChatMessage("AI", staticMessage.getOpeningWords(), new ArrayList<>());
    }

    @POST
    @Path("query")
    public ChatMessage query(@Valid QueryDTO queryObj) throws SQLException {
        var query = queryObj.getMessage();

        ChatCommand.Command command = chatCommand.get(query);
        if (command != null) {
            return chatService.proceedByCommand(command, query);
        }
        return chatService.proceedByPrompt(query);
    }
}
