package net.sosuisen.model;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@NoArgsConstructor
public class SummaryDAO {
    @Resource
    private DataSource ds;

    public SummaryDTO get(String positionTag) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM summary WHERE position_tag = ?")
        ) {
            pstmt.setString(1, positionTag);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new SummaryDTO(
                        rs.getString("position_tag"),
                        rs.getString("summary")
                );
            }
            return null;
        }
    }


    public SummaryDTO getFirst() throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT * FROM summary LIMIT 1")
        ) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new SummaryDTO(
                        rs.getString("position_tag"),
                        rs.getString("summary")
                );
            }
        }
        return null;
    }

    public SummaryDTO getNext(String positionTag) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT * FROM summary " +
                        "WHERE position_tag > ? ORDER BY position_tag ASC LIMIT 1")
        ) {
            pstmt.setString(1, positionTag);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new SummaryDTO(
                        rs.getString("position_tag"),
                        rs.getString("summary")
                );
            }
        }
        return null;
    }
}
