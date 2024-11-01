package net.sosuisen.model;

import dev.ai4j.openai4j.chat.Message;
import jakarta.annotation.Resource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ParagraphDAO {
    @Resource
    private DataSource ds;

    public ArrayList<Paragraph> getAll() throws SQLException {
        var paragraphs = new ArrayList<Paragraph>();
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM paragraph");
        ) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                paragraphs.add(new Paragraph(
                        rs.getString("position_tag"),
                        rs.getString("position_name"),
                        rs.getString("section_title"),
                        rs.getString("paragraph")
                ));
            }
        }
        return paragraphs;
    }
}
