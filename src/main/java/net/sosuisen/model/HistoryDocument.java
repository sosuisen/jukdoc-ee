package net.sosuisen.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class HistoryDocument {
    private String query;
    private String answer;
    private List<Document> referredDocs;
}
