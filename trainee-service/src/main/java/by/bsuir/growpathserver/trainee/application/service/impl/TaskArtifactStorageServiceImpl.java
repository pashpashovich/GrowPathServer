package by.bsuir.growpathserver.trainee.application.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.trainee.application.service.TaskArtifactStorageService;
import by.bsuir.growpathserver.trainee.infrastructure.helper.MinioHelper;

@Service
public class TaskArtifactStorageServiceImpl implements TaskArtifactStorageService {

    private final MinioHelper minioHelper;
    private final String taskArtifactBucket;

    public TaskArtifactStorageServiceImpl(MinioHelper minioHelper,
                                          @Qualifier("taskArtifactBucket") String taskArtifactBucket) {
        this.minioHelper = minioHelper;
        this.taskArtifactBucket = taskArtifactBucket;
    }

    @Override
    public String createPresignedUploadUrl(String objectKey) {
        return minioHelper.getPutPresignedUrl(taskArtifactBucket, objectKey, 15 * 60);
    }

    @Override
    public String createPresignedDownloadUrl(String objectKey) {
        return minioHelper.getGetPresignedUrl(taskArtifactBucket, objectKey, 7 * 24 * 60 * 60);
    }
}
