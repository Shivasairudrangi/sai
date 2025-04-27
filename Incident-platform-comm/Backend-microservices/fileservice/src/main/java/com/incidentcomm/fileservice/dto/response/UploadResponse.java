package com.incidentcomm.fileservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private Long fileId;
    private boolean success;
    private String message;

    // Constructor for successful upload
    public static UploadResponse success(String fileName, String originalFileName, String fileType,
                                         Long fileSize, String downloadUrl, Long fileId) {
        UploadResponse response = new UploadResponse();
        response.setFileName(fileName);
        response.setOriginalFileName(originalFileName);
        response.setFileType(fileType);
        response.setFileSize(fileSize);
        response.setDownloadUrl(downloadUrl);
        response.setFileId(fileId);
        response.setSuccess(true);
        response.setMessage("File uploaded successfully");
        return response;
    }

    // Constructor for failed upload
    public static UploadResponse failure(String originalFileName, String message) {
        UploadResponse response = new UploadResponse();
        response.setOriginalFileName(originalFileName);
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}