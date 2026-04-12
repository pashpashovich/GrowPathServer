package by.bsuir.growpathserver.trainee.domain.exception;

public class DuplicateInternshipProgramTitleException extends RuntimeException {

    public DuplicateInternshipProgramTitleException() {
        super("An internship program with this title already exists");
    }
}
