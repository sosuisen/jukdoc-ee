package net.sosuisen.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Document {
    private String type;
    private String positionTag;
    private String positionName;
    private String sectionTitle;
    private String context;
    private double score;

    public String toString() {
        return "Document{" +
                "type='" + type + '\'' +
                ", positionTag='" + positionTag + '\'' +
                ", positionName='" + positionName + '\'' +
                ", sectionTitle='" + sectionTitle + '\'' +
                ", context='" + context + '\'' +
                ", score=" + score +
                '}';
    }
}
