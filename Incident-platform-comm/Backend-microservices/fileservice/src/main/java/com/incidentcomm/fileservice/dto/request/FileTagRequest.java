package com.incidentcomm.fileservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileTagRequest {
    private Long fileId;

    @NotBlank
    @Size(min = 1, max = 50)
    private String tagName;

    // For batch operations
    private List<String> tagNames;
}