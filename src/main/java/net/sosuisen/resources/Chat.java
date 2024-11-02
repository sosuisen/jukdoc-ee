package net.sosuisen.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import net.sosuisen.model.*;
import net.sosuisen.service.ParagraphService;
import net.sosuisen.service.QAService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final Pattern RE_ANAPHORA = Pattern.compile("(that|this|those|these|such|it|he|she|they|his|her|its|their)[\\s?!.,]", Pattern.CASE_INSENSITIVE);

    private final ParagraphDAO paragraphDAO;
    private final StaticMessage staticMessage;
    private final ChatCommand chatCommand;
    private final ParagraphService paragraphService;
    private final QAService qaService;

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
                    var retrievalDocs = retirieveContext(mes);
                    var context = retrievalDocs.stream()
                            .map(Document::toString)
                            .collect(Collectors.joining("\n\n"));
                    System.out.println("context: " + context);
                }
            }

        }

        return new ChatMessage("AI", "test");
    }

    private ArrayList<Document> retirieveContext(String message) throws SQLException {
        var maxParagraphDocs = 1;
        var paragraphDocs = paragraphService.query(message, maxParagraphDocs, 0.8);
        var qaDocs = qaService.query(message, 3, 0.8);

        var mergedDocs = new ArrayList<Document>();
        if (paragraphDocs != null) {
            mergedDocs.addAll(paragraphDocs);
        }
        if (qaDocs != null) {
            mergedDocs.addAll(qaDocs);
        }
        mergedDocs.sort(Comparator.comparingDouble(Document::getScore).reversed());

        return mergedDocs;
    }
}
