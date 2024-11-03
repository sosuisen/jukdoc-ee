package net.sosuisen.model;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sosuisen.resources.Chat;

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
    public StaticMessage() throws IOException {
        var openingWordsPath = "opening_words.txt";
        var openingWordsBuilder = new StringBuilder();
        try (InputStream inputStream = StaticMessage.class.getClassLoader().getResourceAsStream(openingWordsPath);
             var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            var line = "";
            while ((line = reader.readLine()) != null) {
                openingWordsBuilder.append(line + "<br/>");
            }
        } catch (IOException e) {
            log.error("Failed to read opening words from {}", openingWordsPath);
            throw e;
        }
        openingWords = openingWordsBuilder.toString();
    }

}
