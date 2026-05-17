package by.bsuir.growpathserver.notification.application.service;

import org.springframework.core.io.Resource;

public interface EmailTemplateAttachmentStorageService {

    PresignUploadResult createPresignedUpload(String fileName);

    String createPresignedDownloadUrl(String objectKey);

    Resource downloadFile(String objectKey);

    byte[] downloadBytes(String objectKey);

    record PresignUploadResult(String objectKey, String uploadUrl) {
    }
}
