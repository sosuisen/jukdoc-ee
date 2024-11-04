package net.sosuisen.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ParagraphDTO {
    private String positionTag;
    private String positionName;
    private String sectionTitle;
    private boolean header;
    private String paragraph;
    private String summary;
    private boolean isRead;
}
