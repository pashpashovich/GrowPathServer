package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.trainee.application.dto.InternProgressDto;

public interface InternProgressCalculationService {
    InternProgressDto calculateProgress(Long iprId);
}
