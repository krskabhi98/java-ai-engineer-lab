package com.AI.AILEAD.validation;

import com.AI.AILEAD.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidator {

    @Value("${file.max.size:5242880}") // Default to 5 MB if not set in application.properties
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final String JAVA_FILE_EXTENSION = ".java";

    public void validate(MultipartFile file) {

        validateNotNull(file);
        validateNotEmpty(file);
        validateFileName(file);
        validateExtension(file);
        validateFileSize(file);
    }

    private void validateNotNull(MultipartFile file) {
        if (file == null) {
            throw new InvalidFileException("No file was uploaded.");
        }
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty.");
        }
    }

    private void validateFileName(MultipartFile file) {

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new InvalidFileException("File name is missing.");
        }

        if (fileName.contains("..")) {
            throw new InvalidFileException("Invalid file name.");
        }
    }

    private void validateExtension(MultipartFile file) {

        String fileName = file.getOriginalFilename();

        if (!fileName.toLowerCase().endsWith(JAVA_FILE_EXTENSION)) {
            throw new InvalidFileException("Only .java files are supported.");
        }
    }

    private void validateFileSize(MultipartFile file) {

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    "File size exceeds the maximum limit of 5 MB."
            );
        }
    }
}