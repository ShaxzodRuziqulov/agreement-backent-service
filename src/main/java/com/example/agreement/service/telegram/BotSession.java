package com.example.agreement.service.telegram;

import lombok.Data;

@Data
public class BotSession {

    public enum State {
        NONE,
        WAIT_CONTACT,
        WAIT_FIRST_NAME,
        WAIT_LAST_NAME,
        WAIT_OTP_LOGIN,
        WAIT_OTP_REGISTER
    }

    private State state = State.NONE;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String token;
    private Long userId;

    // Contract flow temp fields
    private final java.util.ArrayDeque<State> history = new java.util.ArrayDeque<>();
}
