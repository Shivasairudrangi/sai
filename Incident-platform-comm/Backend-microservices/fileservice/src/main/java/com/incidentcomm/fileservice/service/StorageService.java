package com.incidentcomm.fileservice.service;

import com.incidentcomm.fileservice.exception.FileNotFoundException;
import com.incidentcomm.fileservice.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private final Path fileStorageLocation;
    private final List<String> allowedFileTypes;

    public StorageService(@Value("${file.upload-dir}") String uploadDir,
                          @Value("${file.allowed-types}") String allowedTypes) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedFileTypes = Arrays.asList(allowedTypes.split(","));

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        // Generate a unique file name to avoid collisions
        String fileName = generateUniqueFileName(file);

        try {
            // Copy file to the target location (replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found: " + fileName, ex);
        }
    }

    public boolean deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Error deleting file: " + fileName, ex);
        }
    }

    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file");
        }

        // Check file content type
        String contentType = file.getContentType();
        if (contentType == null || !allowedFileTypes.contains(contentType)) {
            throw new FileStorageException("File type not allowed. Allowed types: " + String.join(", ", allowedFileTypes));
        }

        // Validate file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        if (originalFileName.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalFileName);
        }
    }

    private String generateUniqueFileName(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        String fileExtension = "";

        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFileName.substring(lastDotIndex);
        }

        // Generate a UUID and append the original file extension
        return UUID.randomUUID().toString() + fileExtension;
    }

    public String getFileDownloadUri(String fileName) {
        return "/api/files/download/" + fileName;
    }

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}