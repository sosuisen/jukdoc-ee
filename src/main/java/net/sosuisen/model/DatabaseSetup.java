package net.sosuisen.model;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import static net.sosuisen.Constants.PARAGRAPH_PATTERN;
import static net.sosuisen.Constants.SUMMARY_PATTERN;

@ApplicationScoped
@Slf4j
public class DatabaseSetup {

    @Resource
    private DataSource ds;

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object event) throws IOException {
        // Initialize a Payara Micro built-in H2 database

        update(ds, "DROP TABLE IF EXISTS paragraph");
        update(ds, "DROP TABLE IF EXISTS summary");

        update(ds, """
                CREATE TABLE IF NOT EXISTS paragraph(
                	position_tag VARCHAR(30) PRIMARY KEY,
                	position_name VARCHAR(30) NOT NULL,
                	section_title VARCHAR(100) NOT NULL,
                	is_header BOOLEAN NOT NULL DEFAULT FALSE,
                	paragraph TEXT NOT NULL
                	)
                """);

        update(ds, """
                CREATE TABLE IF NOT EXISTS summary (
                    position_tag VARCHAR(30) PRIMARY KEY,
                	summary TEXT NOT NULL
                	)
                """);

        var paragraphPath = "structured_paragraph.txt";
        try (InputStream inputStream = DatabaseSetup.class.getClassLoader().getResourceAsStream(paragraphPath);
             var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            var line = "";
            while ((line = reader.readLine()) != null) {
                var matcher = PARAGRAPH_PATTERN.matcher(line);
                if (!matcher.find()) {
                    continue;
                }
                var positionTag = matcher.group(1);
                var positionName = matcher.group(2);
                var sectionTitle = matcher.group(3);
                var paragraph = matcher.group(4);

                if (positionTag.isEmpty() || paragraph.isEmpty()) continue;

                boolean isHeader = !positionTag.contains("p#");

                var sql = "INSERT INTO paragraph VALUES (?, ?, ?, ?, ?)";
                try (
                        Connection con = ds.getConnection();
                        PreparedStatement pstmt = con.prepareStatement(sql);) {
                    pstmt.setString(1, positionTag);
                    pstmt.setString(2, positionName);
                    pstmt.setString(3, sectionTitle);
                    pstmt.setBoolean(4, isHeader);
                    pstmt.setString(5, paragraph);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }

        } catch (IOException e) {
            log.error("Error reading paragraph file: {}", e.getMessage());
            throw e;
        }

        var summaryPath = "summary.txt";
        try (InputStream inputStream = DatabaseSetup.class.getClassLoader().getResourceAsStream(summaryPath);
             var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            var line = "";
            var summaryBlock = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (!line.equals("---")) {
                    summaryBlock.append(line);
                    break;
                }
                var matcher = SUMMARY_PATTERN.matcher(summaryBlock.toString());
                summaryBlock.delete(0, summaryBlock.length());
                if (!matcher.find()) {
                    continue;
                }
                var positionTag = matcher.group(1);
                var summary = matcher.group(2);

                if (positionTag.isEmpty() || summary.isEmpty()) continue;

                var sql = "INSERT INTO summary (position_tag, summary) VALUES (?, ?)";

                try (
                        Connection con = ds.getConnection();
                        PreparedStatement pstmt = con.prepareStatement(sql);) {
                    pstmt.setString(1, positionTag);
                    pstmt.setString(2, summary);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }

        } catch (IOException e) {
            log.error("Error reading summary file: " + e.getMessage());
            throw e;
        }
    }

    public void onApplicationScopedDestroyed(@Observes @BeforeDestroyed(ApplicationScoped.class) Object event) {
        try {
            update(ds, "DROP TABLE IF EXISTS paragraph");
            update(ds, "DROP TABLE IF EXISTS summary");
        } catch (Exception e) {
            log.warn("Exception on drop tables: {}", e.getMessage());
        }
    }

    private void update(DataSource ds, String query) {
        try (
                Connection con = ds.getConnection();
                PreparedStatement pstmt = con.prepareStatement(query);) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}