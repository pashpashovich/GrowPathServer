package by.bsuir.growpathserver.trainee.application.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.CompetencyCatalogListResponse;
import by.bsuir.growpathserver.dto.model.CompetencyRef;
import by.bsuir.growpathserver.dto.model.CreateCompetencyRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateCompetencyRequest;
import by.bsuir.growpathserver.trainee.application.service.CompetencyCatalogService;
import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.CompetencyCatalogMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.CompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.web.CatalogPathIds;
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

    @Override
    @Transactional(readOnly = true)
    public CompetencyRef getById(String id) {
        long lid = CatalogPathIds.parseLong(id);
        CompetencyEntity entity = competencyRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return competencyCatalogMapper.toCompetencyRef(entity);
    }

    @Override
    @Transactional
    public CompetencyRef create(CreateCompetencyRequest request) {
        String name = requireNonBlank(request.getName(), "name");
        if (competencyRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        CompetencyEntity e = new CompetencyEntity();
        e.setName(name);
        return competencyCatalogMapper.toCompetencyRef(competencyRepository.save(e));
    }

    @Override
    @Transactional
    public CompetencyRef update(String id, UpdateCompetencyRequest request) {
        long lid = CatalogPathIds.parseLong(id);
        CompetencyEntity entity = competencyRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String newName = request.getName() != null ? requireNonBlank(request.getName(), "name") : entity.getName();
        if (competencyRepository.existsByNameIgnoreCaseAndIdNot(newName, lid)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        entity.setName(newName);
        return competencyCatalogMapper.toCompetencyRef(competencyRepository.save(entity));
    }

    @Override
    @Transactional
    public MessageResponse delete(String id) {
        long lid = CatalogPathIds.parseLong(id);
        CompetencyEntity entity = competencyRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        competencyRepository.delete(entity);
        MessageResponse msg = new MessageResponse();
        msg.setMessage("Deleted");
        return msg;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field);
        }
        return value.trim();
    }
}
