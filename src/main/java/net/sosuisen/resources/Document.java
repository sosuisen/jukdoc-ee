package net.sosuisen.resources;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Path("/api/document")
public class Document {
    private final ParagraphDAO paragraphDAO;
    private final ReadingRecordDAO readingRecordDAO;
    private final UserStatus userStatus;
    private final CsrfValidator csrfValidator;

    @GET
    public ArrayList<ParagraphDTO> getParagraphs() throws SQLException {
        csrfValidator.validateCsrfToken();

        var paragraphs = paragraphDAO.getAll(userStatus.getUserName());
        return paragraphs;
    }

    @DELETE
    @Path("reading-record")
    public boolean deleteReadingRecord() throws SQLException {
        csrfValidator.validateCsrfToken();
        readingRecordDAO.clearAll(userStatus.getUserName());
        log.debug("Deleted reading record for user {}", userStatus.getUserName());
        return true;
    }
}