package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.MentorsApi;
import by.bsuir.growpathserver.dto.model.MentorInternsResponse;
import by.bsuir.growpathserver.dto.model.MentorListResponse;
import by.bsuir.growpathserver.dto.model.MentorResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.trainee.application.handler.GetMentorByIdHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetMentorInternsHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetMentorsHandler;
import by.bsuir.growpathserver.trainee.application.query.GetMentorByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorInternsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.MentorMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MentorController implements MentorsApi {

    private final GetMentorsHandler getMentorsHandler;
    private final GetMentorByIdHandler getMentorByIdHandler;
    private final GetMentorInternsHandler getMentorInternsHandler;
    private final MentorMapper mentorMapper;
    private final TaskRepository taskRepository;
    private final AssessmentRepository assessmentRepository;

    @Override
    public ResponseEntity<MentorResponse> getMentorById(String id) {
        try {
            Long mentorId = Long.parseLong(id);
            GetMentorByIdQuery query = new GetMentorByIdQuery(mentorId);
            User mentor = getMentorByIdHandler.handle(query);
            MentorResponse response = mentorMapper.toMentorResponse(mentor, taskRepository, assessmentRepository);
            return ResponseEntity.ok(response);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        catch (NoSuchElementException | IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<MentorInternsResponse> getMentorInterns(String id) {
        try {
            Long mentorId = Long.parseLong(id);
            GetMentorInternsQuery query = new GetMentorInternsQuery(mentorId);
            List<User> interns = getMentorInternsHandler.handle(query);
            MentorInternsResponse response = mentorMapper.toMentorInternsResponse(interns, taskRepository);
            return ResponseEntity.ok(response);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        catch (NoSuchElementException | IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<MentorListResponse> getMentors(Integer page, Integer limit, String search) {
        try {
            GetMentorsQuery query = new GetMentorsQuery(page, limit, search);
            Page<User> mentorsPage = getMentorsHandler.handle(query);

            MentorListResponse response = new MentorListResponse();
            response.setData(mentorsPage.getContent().stream()
                                     .map(mentor -> mentorMapper.toMentorResponse(mentor, taskRepository,
                                                                                  assessmentRepository))
                                     .toList());

            PaginationResponse pagination = new PaginationResponse();
            pagination.setPage(mentorsPage.getNumber() + 1);
            pagination.setLimit(mentorsPage.getSize());
            pagination.setTotal((int) mentorsPage.getTotalElements());
            pagination.setTotalPages(mentorsPage.getTotalPages());
            response.setPagination(pagination);

            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
