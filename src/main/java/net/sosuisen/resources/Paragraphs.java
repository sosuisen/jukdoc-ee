package net.sosuisen.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import net.sosuisen.model.ParagraphDTO;
import net.sosuisen.model.ParagraphDAO;

import java.sql.SQLException;
import java.util.ArrayList;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Log
@Path("/api/paragraphs")
public class Paragraphs {
    private final ParagraphDAO paragraphDAO;

    @GET
    public ArrayList<ParagraphDTO> getParagraphs() throws SQLException {
        return paragraphDAO.getAll();
    }
}