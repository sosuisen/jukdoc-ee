package net.sosuisen.model;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

@ApplicationScoped
@Getter
@Slf4j
public class StaticMessage {
    private final String openingWords;

    private final String proceedCurrentTopicStartHeader = "<p>I will read from the beginning.</p>";
    private final String proceedCurrentTopicHeader = "<p>I will continue reading.</p>";
    private final String isFinalTopic = "<p>I have finished reading all the topics.</p>";

    public StaticMessage() throws IOException {
        var openingWordsPath = "opening_words.txt";
        var openingWordsBuilder = new StringBuilder();
        try (InputStream inputStream = StaticMessage.class.getClassLoader().getResourceAsStream(openingWordsPath);
             var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            var line = "";
            while ((line = reader.readLine()) != null) {
                openingWordsBuilder.append(line);
            }
        } catch (IOException e) {
            log.error("Failed to read opening words from {}", openingWordsPath);
            throw e;
        }
        openingWords = openingWordsBuilder.toString();
    }

}
