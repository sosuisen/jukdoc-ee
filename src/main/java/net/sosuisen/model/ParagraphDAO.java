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

    public ArrayList<ParagraphDTO> getAll(String userName) throws SQLException {
        var paragraphs = new ArrayList<ParagraphDTO>();
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                                SELECT p.*, r.reading_time FROM paragraph p\s
                                LEFT JOIN reading_record r
                                ON p.position_tag = r.position_tag AND r.user_name = ?
                                ORDER BY p.position_tag ASC
                                """
                )
        ) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                boolean hasReadingTime = rs.getTimestamp("reading_time") != null;

                paragraphs.add(new ParagraphDTO(
                        rs.getString("position_tag"),
                        rs.getString("position_name"),
                        rs.getString("section_title"),
                        rs.getBoolean("is_header"),
                        rs.getString("paragraph"),
                        rs.getString("summary"),
                        hasReadingTime
                ));
            }
        }
        return paragraphs;
    }

    public ParagraphDTO get(String positionTag, String userName) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                                SELECT p.*, r.reading_time FROM paragraph p
                                LEFT JOIN reading_record r
                                ON p.position_tag = r.position_tag AND r.user_name = ?
                                WHERE p.position_tag = ?
                                """)
        ) {
            pstmt.setString(1, userName);
            pstmt.setString(2, positionTag);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean hasReadingTime = rs.getTimestamp("reading_time") != null;

                return new ParagraphDTO(
                        rs.getString("position_tag"),
                        rs.getString("position_name"),
                        rs.getString("section_title"),
                        rs.getBoolean("is_header"),
                        rs.getString("paragraph"),
                        rs.getString("summary"),
                        hasReadingTime
                );
            }
        }
        return null;
    }

    public ParagraphDTO getFirstParagraph(String userName) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                                SELECT p.*, r.reading_time FROM paragraph p
                                LEFT JOIN reading_record r
                                ON p.position_tag = r.position_tag AND r.user_name = ?
                                WHERE is_header = FALSE ORDER BY p.position_tag ASC LIMIT 1
                                """
                )
        ) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean hasReadingTime = rs.getTimestamp("reading_time") != null;
                return new ParagraphDTO(
                        rs.getString("position_tag"),
                        rs.getString("position_name"),
                        rs.getString("section_title"),
                        rs.getBoolean("is_header"),
                        rs.getString("paragraph"),
                        rs.getString("summary"),
                        hasReadingTime
                );
            }
        }
        return null;
    }

    public ParagraphDTO getNextParagraph(String positionTag, String userName) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                                SELECT p.*, r.reading_time FROM paragraph p
                                LEFT JOIN reading_record r
                                ON p.position_tag = r.position_tag AND r.user_name = ?
                                WHERE p.position_tag > ? AND is_header = FALSE ORDER BY p.position_tag ASC LIMIT 1
                                """)
        ) {
            pstmt.setString(1, userName);
            pstmt.setString(2, positionTag);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean hasReadingTime = rs.getTimestamp("reading_time") != null;
                return new ParagraphDTO(
                        rs.getString("position_tag"),
                        rs.getString("position_name"),
                        rs.getString("section_title"),
                        rs.getBoolean("is_header"),
                        rs.getString("paragraph"),
                        rs.getString("summary"),
                        hasReadingTime
                );
            }
        }
        return null;
    }

    public ParagraphDTO getFirstUnreadParagraph(String userName) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                                SELECT p.* FROM paragraph p 
                                LEFT JOIN reading_record r
                                ON p.position_tag = r.position_tag AND r.user_name = ?           
                                WHERE r.reading_time IS NULL AND is_header = FALSE ORDER BY p.position_tag ASC LIMIT 1
                                """
                )
        ) {
            pstmt.setString(1, userName);
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
