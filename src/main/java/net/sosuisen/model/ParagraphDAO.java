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
@NoArgsConstructor
public class ParagraphDAO {
    @Resource
    private DataSource ds;

    public ArrayList<ParagraphDTO> getAll() throws SQLException {
        var paragraphs = new ArrayList<ParagraphDTO>();
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM paragraph ORDER BY position_tag ASC")
        ) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                paragraphs.add(new ParagraphDTO(
                        rs.getString("position_tag"),
                        rs.getString("position_name"),
                        rs.getString("section_title"),
                        rs.getBoolean("is_header"),
                        rs.getString("paragraph"),
                        rs.getString("summary"),
                        false
                ));
            }
        }
        return paragraphs;
    }

    public ParagraphDTO get(String positionTag) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT * FROM paragraph WHERE position_tag = ?")
        ) {
            pstmt.setString(1, positionTag);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new ParagraphDTO(
                        rs.getString("position_tag"),
                        rs.getString("position_name"),
                        rs.getString("section_title"),
                        rs.getBoolean("is_header"),
                        rs.getString("paragraph"),
                        rs.getString("summary"),
                        false
                );
            }
        }
        return null;
    }

    public ParagraphDTO getFirstParagraph() throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT * FROM paragraph WHERE is_header = FALSE ORDER BY position_tag ASC LIMIT 1")
        ) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new ParagraphDTO(
                        rs.getString("position_tag"),
                        rs.getString("position_name"),
                        rs.getString("section_title"),
                        rs.getBoolean("is_header"),
                        rs.getString("paragraph"),
                        rs.getString("summary"),
                        false
                );
            }
        }
        return null;
    }

    public ParagraphDTO getNextParagraph(String positionTag) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT * FROM paragraph " +
                        "WHERE position_tag > ? AND is_header = FALSE ORDER BY position_tag ASC LIMIT 1")
        ) {
            pstmt.setString(1, positionTag);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new ParagraphDTO(
                        rs.getString("position_tag"),
                        rs.getString("position_name"),
                        rs.getString("section_title"),
                        rs.getBoolean("is_header"),
                        rs.getString("paragraph"),
                        rs.getString("summary"),
                        false
                );
            }
        }
        return null;
    }
}
