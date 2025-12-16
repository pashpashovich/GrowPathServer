package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetMentorByIdQuery;
import by.bsuir.growpathserver.trainee.application.service.MentorService;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetMentorByIdHandler {

    private final MentorService mentorService;

    public User handle(GetMentorByIdQuery query) {
        return mentorService.getMentorById(query);
    }
}
