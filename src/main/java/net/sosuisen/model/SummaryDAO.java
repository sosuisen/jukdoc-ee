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
            if(rs.next()) {
                return new SummaryDTO(
                        rs.getString("position_tag"),
                        rs.getString("summary")
                );
            }
            return null;
        }
    }
 }
