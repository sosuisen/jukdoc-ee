package net.sosuisen.model;

import jakarta.annotation.Resource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReadingRecordDAO {
    @Resource
    private DataSource ds;

    public List<String> getAll(String userName) throws SQLException {
        List<String> positionTags = new ArrayList<>();
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM reading_record WHERE user_name = ?")
        ) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                positionTags.add(rs.getString("position_tag"));
            }
        }
        return positionTags;
    }

    public void create(String userName, String positionTag) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                // MERGE INTO is a H2 specific SQL statement
                PreparedStatement pstmt = conn.prepareStatement("MERGE INTO reading_record (user_name, position_tag, reading_time) VALUES (?, ?, ?)")
        ) {
            pstmt.setString(1, userName);
            pstmt.setString(2, positionTag);
            pstmt.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
        }
    }
}