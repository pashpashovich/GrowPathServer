package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.dto.model.CreateItDirectionRequest;
import by.bsuir.growpathserver.dto.model.ItDirectionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.ItDirectionRef;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateItDirectionRequest;

public interface ItDirectionCatalogService {

    ItDirectionCatalogListResponse list();

    ItDirectionRef getById(String id);

    ItDirectionRef create(CreateItDirectionRequest request);

    ItDirectionRef update(String id, UpdateItDirectionRequest request);

    MessageResponse delete(String id);
}
