package com.muimi.kichat.entity;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private Type type;

    private String sender;
    private String receiver;
    private String content;
    private String time;
}
