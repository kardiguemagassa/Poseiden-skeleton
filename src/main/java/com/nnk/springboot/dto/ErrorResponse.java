package com.nnk.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Paris")
    private LocalDateTime timestamp;

    private HttpStatus status;
    private String message;
    private String details;
    private String reference;
    private String title;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public String getReference() {
        return reference;
    }

    public String getTitle() {
        return title;
    }

    // Get the numeric HTTP code from the status
    public int getCode() {
        return status != null ? status.value() : 0;
    }

    // Setters

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

