package com.incidentcomm.chatservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ChatRoomRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private boolean isGroup;

    private boolean isIncidentRelated;

    private Long incidentId;

    private List<Long> userIds;
}