package net.sosuisen.hello;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

@Path("hello")
public class HelloWorldResource {

    @GET
    public Response hello(@QueryParam("name") String query) {
        if ((query == null) || query.trim().isEmpty()) {
            query = "medicine";
        }

        var qaStorePath = "qa_store.json";
        var responseText = new StringBuilder();
        try (InputStream inputStream = HelloWorldResource.class.getClassLoader().getResourceAsStream(qaStorePath);
             var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            var qaStoreJson = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                qaStoreJson.append(line);
            }
            InMemoryEmbeddingStore<TextSegment> embeddingStore = InMemoryEmbeddingStore.fromJson(qaStoreJson.toString());

            OpenAiEmbeddingModel model = OpenAiEmbeddingModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("text-embedding-3-small")
                    .build();
            var segment = TextSegment.from(query);
            var embedding = model.embed(segment).content();

            EmbeddingSearchRequest request = new EmbeddingSearchRequest(embedding, 3, 0.1, null);
            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

            for (var match : result.matches()) {
                responseText.append("""
                        score: %f
                        question: %s
                        position_tag: %s
                        position_name: %s
                        section_title: %s
                        answer: %s
                        """.formatted(match.score(),
                        match.embedded().text(),
                        match.embedded().metadata().getString("position_tag"),
                        match.embedded().metadata().getString("position_name"),
                        match.embedded().metadata().getString("section_title"),
                        match.embedded().metadata().getString("answer")));
            }
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
        return Response
                .ok(responseText.toString())
                .build();
    }


}