package com.smartbin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RetrieveRequest {

    @NotBlank(message = "Transfer code is required")
    @Pattern(regexp = "^[a-zA-Z0-9]{5,10}$", message = "Invalid transfer code format")
    private String transferCode;

    public RetrieveRequest() {
    }

    public RetrieveRequest(String transferCode) {
        this.transferCode = transferCode;
    }

    public String getTransferCode() {
        return transferCode;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }
}
