package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.dto.model.CompetencyCatalogListResponse;
import by.bsuir.growpathserver.dto.model.CompetencyRef;
import by.bsuir.growpathserver.dto.model.CreateCompetencyRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateCompetencyRequest;

public interface CompetencyCatalogService {

    CompetencyCatalogListResponse getListCatalog();

    CompetencyRef getById(String id);

    CompetencyRef create(CreateCompetencyRequest request);

    CompetencyRef update(String id, UpdateCompetencyRequest request);

    MessageResponse delete(String id);
}
