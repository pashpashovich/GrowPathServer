package by.bsuir.growpathserver.notification.application.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.notification.application.service.EmailTemplateAttachmentStorageService;
import by.bsuir.growpathserver.notification.infrastructure.helper.MinioHelper;

@Service
public class EmailTemplateAttachmentStorageServiceImpl implements EmailTemplateAttachmentStorageService {

    private static final int UPLOAD_URL_TTL_SECONDS = 15 * 60;
    private static final int DOWNLOAD_URL_TTL_SECONDS = 7 * 24 * 60 * 60;

    private final MinioHelper minioHelper;
    private final String attachmentBucket;

    public EmailTemplateAttachmentStorageServiceImpl(MinioHelper minioHelper,
                                                     @Qualifier("emailTemplateAttachmentBucket") String attachmentBucket) {
        this.minioHelper = minioHelper;
        this.attachmentBucket = attachmentBucket;
    }

    @Override
    public PresignUploadResult createPresignedUpload(String fileName) {
        String safeName = fileName == null || fileName.isBlank() ? "attachment.bin" : fileName.replace(" ", "_");
        String objectKey = "email-attachments/%s_%s".formatted(UUID.randomUUID(), safeName);
        String uploadUrl = minioHelper.getPutPresignedUrl(attachmentBucket, objectKey, UPLOAD_URL_TTL_SECONDS);
        return new PresignUploadResult(objectKey, uploadUrl);
    }

    @Override
    public String createPresignedDownloadUrl(String objectKey) {
        return minioHelper.getGetPresignedUrl(attachmentBucket, objectKey, DOWNLOAD_URL_TTL_SECONDS);
    }

    @Override
    public Resource downloadFile(String objectKey) {
        return minioHelper.download(attachmentBucket, objectKey);
    }

    @Override
    public byte[] downloadBytes(String objectKey) {
        return minioHelper.downloadBytes(attachmentBucket, objectKey);
    }
}
