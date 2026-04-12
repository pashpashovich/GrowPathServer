package by.bsuir.growpathserver.trainee.application.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.dto.model.CompetencyCatalogListResponse;
import by.bsuir.growpathserver.trainee.application.service.CompetencyCatalogService;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.CompetencyCatalogMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.CompetencyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompetencyCatalogServiceImpl implements CompetencyCatalogService {

    private final CompetencyRepository competencyRepository;
    private final CompetencyCatalogMapper competencyCatalogMapper;

    @Override
    @Transactional(readOnly = true)
    public CompetencyCatalogListResponse getListCatalog() {
        return competencyCatalogMapper.toCatalogListResponse(competencyRepository.findAllByOrderByNameAsc());
    }
}
