package com.incidentcomm.chatservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {
    private boolean success;
    private String message;

    public static StatusResponse success(String message) {
        return new StatusResponse(true, message);
    }

    public static StatusResponse error(String message) {
        return new StatusResponse(false, message);
    }
}