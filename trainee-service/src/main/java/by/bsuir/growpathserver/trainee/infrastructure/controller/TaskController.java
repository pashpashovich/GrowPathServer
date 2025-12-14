package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.TasksApi;
import by.bsuir.growpathserver.dto.model.CommentResponse;
import by.bsuir.growpathserver.dto.model.CreateCommentRequest;
import by.bsuir.growpathserver.dto.model.CreateTaskRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.TaskListResponse;
import by.bsuir.growpathserver.dto.model.TaskResponse;
import by.bsuir.growpathserver.dto.model.TaskStatusResponse;
import by.bsuir.growpathserver.dto.model.UpdateTaskRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TaskController implements TasksApi {

    @Override
    public ResponseEntity<CommentResponse> addTaskComment(String id, CreateCommentRequest createCommentRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<TaskStatusResponse> completeTask(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<TaskResponse> createTask(CreateTaskRequest createTaskRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MessageResponse> deleteTask(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<TaskResponse> getTaskById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<TaskListResponse> getTasks(Integer page,
                                                     Integer limit,
                                                     String status,
                                                     String assignee,
                                                     String priority,
                                                     String internshipId,
                                                     String mentorId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<TaskResponse> updateTask(String id, UpdateTaskRequest updateTaskRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
