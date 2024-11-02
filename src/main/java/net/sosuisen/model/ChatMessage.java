package net.sosuisen.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ChatMessage {
    private String speaker;
    private String message;

}
