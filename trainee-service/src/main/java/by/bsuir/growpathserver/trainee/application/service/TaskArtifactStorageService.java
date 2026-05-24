package by.bsuir.growpathserver.trainee.application.service;

import java.io.InputStream;

public interface TaskArtifactStorageService {
    String createPresignedUploadUrl(String objectKey);

    String createPresignedDownloadUrl(String objectKey);

    void upload(String objectKey, InputStream stream, long size, String contentType);
}
