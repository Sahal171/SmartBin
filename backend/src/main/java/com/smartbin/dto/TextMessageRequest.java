package com.smartbin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TextMessageRequest {

    @NotBlank(message = "Message text cannot be empty.")
    @Size(max = 200000, message = "Message text cannot exceed 200,000 characters.")
    private String text;

    @Size(max = 100, message = "Title cannot exceed 100 characters.")
    private String title;

    public TextMessageRequest() {
    }

    public TextMessageRequest(String text, String title) {
        this.text = text;
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
