package net.sosuisen.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

@Slf4j
public class VectorStoreLoader {
    public static InMemoryEmbeddingStore<TextSegment> load(String jsonName) {
        try (InputStream inputStream = ParagraphService.class.getClassLoader().getResourceAsStream(jsonName);
             var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            var paragraphStoreJson = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                paragraphStoreJson.append(line);
            }
            return InMemoryEmbeddingStore.fromJson(paragraphStoreJson.toString());
        } catch (Exception e) {
            log.error("Failed to load vector store", e);
            return new InMemoryEmbeddingStore<TextSegment>();
        }
    }
}
