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
import net.sosuisen.model.ChatMessage;
import net.sosuisen.model.MessageDTO;
import net.sosuisen.model.ParagraphDAO;
import net.sosuisen.model.StaticMessage;
import net.sosuisen.service.QAService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Objects;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Log
@Path("/api/chat")
public class Chat {
    private final ParagraphDAO paragraphDAO;
    private final QAService qaService;
    private final StaticMessage staticMessage;

    @GET
    @Path("opening-words")
    public ChatMessage getOpeningWords() throws IOException {
        return new ChatMessage("AI", staticMessage.getOpeningWords());
    }

    @POST
    @Path("query")
    public ChatMessage query(@Valid MessageDTO msg) throws SQLException {
        var response = qaService.query(msg.getMessage());
        return new ChatMessage("AI", response);
    }

}
