package net.sosuisen.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Paragraph {
    private String positionTag;
    private String positionName;
    private String sectionTitle;
    private boolean header;
    private String paragraph;

    @Override
    public String toString() {
        return "Paragraph{" +
                "positionTag='" + positionTag + '\'' +
                ", positionName='" + positionName + '\'' +
                ", sectionTitle='" + sectionTitle + '\'' +
                ", isHeader='" + header + '\'' +
                ", paragraph='" + paragraph + '\'' +
                '}';
    }
}
