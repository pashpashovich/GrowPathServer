package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.InternshipProgramsApi;
import by.bsuir.growpathserver.dto.model.CreateInternshipProgramRequest;
import by.bsuir.growpathserver.dto.model.InternshipProgramListResponse;
import by.bsuir.growpathserver.dto.model.InternshipProgramResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateInternshipProgramRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InternshipProgramController implements InternshipProgramsApi {

    @Override
    public ResponseEntity<InternshipProgramResponse> createInternshipProgram(CreateInternshipProgramRequest createInternshipProgramRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MessageResponse> deleteInternshipProgram(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<InternshipProgramResponse> getInternshipProgramById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<InternshipProgramListResponse> getInternshipPrograms(Integer page,
                                                                               Integer limit,
                                                                               String status,
                                                                               String search) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<InternshipProgramResponse> updateInternshipProgram(String id,
                                                                             UpdateInternshipProgramRequest updateInternshipProgramRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
