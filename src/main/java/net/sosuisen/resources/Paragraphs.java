package net.sosuisen.resources;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sosuisen.model.ParagraphDAO;
import net.sosuisen.model.ParagraphDTO;
import net.sosuisen.model.ReadingRecordDAO;
import net.sosuisen.model.UserStatus;
import net.sosuisen.security.CsrfValidator;

import java.sql.SQLException;
import java.util.ArrayList;

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Path("/api/paragraphs")
public class Paragraphs {
    private final ParagraphDAO paragraphDAO;
    private final ReadingRecordDAO readingRecordDAO;
    private final UserStatus userStatus;
    private final CsrfValidator csrfValidator;

    @GET
    public ArrayList<ParagraphDTO> getParagraphs() throws SQLException {
        csrfValidator.validateCsrfToken();

        var paragraphs = paragraphDAO.getAll();
        var positionTags = readingRecordDAO.getAll(userStatus.getUserName());

        for(var paragraph : paragraphs) {
            if (positionTags.contains(paragraph.getPositionTag())) {
                paragraph.setRead(true);
            }
        }
        return paragraphs;
    }
}