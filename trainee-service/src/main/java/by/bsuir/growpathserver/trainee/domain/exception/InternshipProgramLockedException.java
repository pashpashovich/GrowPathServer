package by.bsuir.growpathserver.trainee.domain.exception;

public class InternshipProgramLockedException extends RuntimeException {

    public InternshipProgramLockedException() {
        super("Program fields cannot be changed after the start date; only archiving is allowed");
    }
}
