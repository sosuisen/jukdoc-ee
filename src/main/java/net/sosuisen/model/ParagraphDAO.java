package net.sosuisen.model;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@ApplicationScoped
@NoArgsConstructor(force = true)
public class ParagraphDAO {
    @Resource
    private DataSource ds;

    public ArrayList<ParagraphDTO> getAll() throws SQLException {
        var paragraphs = new ArrayList<ParagraphDTO>();
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM paragraph")
        ) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                paragraphs.add(new ParagraphDTO(
                        rs.getString("position_tag"),
                        rs.getString("position_name"),
                        rs.getString("section_title"),
                        rs.getBoolean("is_header"),
                        rs.getString("paragraph")
                ));
            }
        }
        return paragraphs;
    }
}
