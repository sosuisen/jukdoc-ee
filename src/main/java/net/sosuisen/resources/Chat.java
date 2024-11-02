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

    @GET
    @Path("opening-words")
    public ChatMessage getOpeningWords() throws IOException {
        var openingWordsPath = "opening_words.txt";
        var openingWords = new StringBuilder();
        try (InputStream inputStream = Chat.class.getClassLoader().getResourceAsStream(openingWordsPath);
             var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            var line = "";
            while ((line = reader.readLine()) != null) {
                openingWords.append(line);
            }
        } catch (IOException e) {
            log.severe("Failed to read opening words from " + openingWordsPath);
            throw e;
        }

        return new ChatMessage("AI", openingWords.toString());
    }

    @POST
    @Path("query")
    public ChatMessage query(@Valid MessageDTO msg) throws SQLException {
        System.out.println("message: " + msg.getMessage());
        var response = qaService.query(msg.getMessage());
        // String response = paragraphDAO.getAll().stream().map(Object::toString).reduce("", (a, b) -> a + b);
        return new ChatMessage("AI", response);
    }

}
