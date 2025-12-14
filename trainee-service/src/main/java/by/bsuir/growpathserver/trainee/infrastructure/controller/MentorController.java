package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.MentorsApi;
import by.bsuir.growpathserver.dto.model.MentorInternsResponse;
import by.bsuir.growpathserver.dto.model.MentorListResponse;
import by.bsuir.growpathserver.dto.model.MentorResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MentorController implements MentorsApi {

    @Override
    public ResponseEntity<MentorResponse> getMentorById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MentorInternsResponse> getMentorInterns(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MentorListResponse> getMentors(Integer page, Integer limit, String search) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
