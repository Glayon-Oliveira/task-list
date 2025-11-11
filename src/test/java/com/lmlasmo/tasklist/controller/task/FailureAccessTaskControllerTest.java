package com.lmlasmo.tasklist.controller.task;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.TaskController;
import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.TaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;

import reactor.core.publisher.Mono;

@WebFluxTest(controllers = TaskController.class)
@TestInstance(Lifecycle.PER_CLASS)
public class FailureAccessTaskControllerTest extends AbstractControllerTest {

	@MockitoBean
	private TaskService taskService;

	@MockitoBean
	private TaskStatusService statusService;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean(name = "resourceAccessService")
	private ResourceAccessService accessService;

	private final String baseUri = "/api/task/";

	private Task task;

	@Override
	@BeforeEach
	public void setUp() {
		super.setUp();
		String name = "Name - ID = " + UUID.randomUUID();
		String summary = "Summary - ID = " + UUID.randomUUID();

		task = new Task(1);
		task.setName(name);
		task.setSummary(summary);
		task.setDeadline(Instant.now().plusSeconds(60*60));
		task.setDeadlineZone(ZoneId.systemDefault().toString());
		task.setCreatedAt(Instant.now());
		task.setUpdatedAt(task.getCreatedAt());
		
		when(accessService.canAccessTask(anyInt(), eq(getDefaultUser().getId())))
			.thenReturn(Mono.error(new AccessDeniedException("Access denied")));
	}

	@Test
	public void delete() throws Exception {
		getWebTestClient().delete()
			.uri(baseUri + task.getId()+1)
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.exchange()
			.expectStatus().isForbidden();
	}

	@Test
	public void updateStatus() throws Exception {
		getWebTestClient().patch()
			.uri(ub -> ub.path(baseUri + task.getId())
					.queryParam("status", TaskStatusType.COMPLETED)
					.build())
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.exchange()
			.expectStatus().isForbidden();
	}

	@Test
	public void updateDescription() throws Exception {
		UpdateTaskDTO update = new UpdateTaskDTO();
		update.setName(UUID.randomUUID().toString());
		update.setSummary(UUID.randomUUID().toString());
		
		getWebTestClient().patch()
			.uri(baseUri + task.getId())
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(mapper.writeValueAsString(update))
			.exchange()
			.expectStatus().isForbidden();
	}

	@Test
	public void updateDeadline() throws Exception {
		UpdateTaskDTO update = new UpdateTaskDTO();
		update.setDeadline(OffsetDateTime.now().plusMinutes(1));
		update.setDeadlineZone("UTC");
		
		getWebTestClient().patch()
			.uri(baseUri + task.getId())
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(mapper.writeValueAsString(update))
			.exchange()
			.expectStatus().isForbidden();
	}

	@Test
	public void getTaskById() throws Exception {
		getWebTestClient().get()
			.uri(ub -> ub.path(baseUri + task.getId())
					.queryParam("withSubtasks", "true")
					.build())
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.exchange()
			.expectStatus().isForbidden();
	}

}
