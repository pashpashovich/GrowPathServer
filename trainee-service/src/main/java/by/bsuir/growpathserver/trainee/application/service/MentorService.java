package by.bsuir.growpathserver.trainee.application.service;

import java.util.List;

import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.trainee.application.query.GetMentorByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorInternsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;

public interface MentorService {
    Page<User> getMentors(GetMentorsQuery query);

    User getMentorById(GetMentorByIdQuery query);

    List<User> getMentorInterns(GetMentorInternsQuery query);
}
