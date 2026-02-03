package com.lmlasmo.tasklist.controller.subtask;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.SubtaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;
import com.lmlasmo.tasklist.web.controller.SubtaskController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = SubtaskController.class)
@TestInstance(Lifecycle.PER_CLASS)
public class FailureAccessSubtaskControllerTest extends AbstractControllerTest{

	@MockitoBean
	private SubtaskService subtaskService;

	@MockitoBean
	private TaskStatusService statusService;

	@MockitoBean(name = "resourceAccessService")
	private ResourceAccessService accessService;

	@Autowired
	private ObjectMapper mapper;

	private final String baseUri = "/api/subtask";

	private Subtask subtask;

	@Override
	@BeforeEach
	public void setUp() {
		super.setUp();

		subtask = new Subtask();
		subtask.setId(1);
		subtask.setPosition(BigDecimal.ONE);
		subtask.setTaskId(1);
		subtask.setCreatedAt(Instant.now());
		subtask.setUpdatedAt(subtask.getCreatedAt());
		
		Mono<Void> accessError = Mono.error(new ResourceNotFoundException("Subask not found"));

		when(accessService.canAccessSubtask(eq(subtask.getId()), eq(getDefaultUser().getId()))).thenReturn(accessError);
		when(accessService.canAccessSubtask(anyList(), eq(getDefaultUser().getId()))).thenReturn(accessError);
		when(accessService.canAccessTask(eq(subtask.getId()), eq(getDefaultUser().getId()))).thenReturn(accessError);
		when(accessService.canAccessTask(eq(subtask.getTaskId()), eq(getDefaultUser().getId()))).thenReturn(accessError);
	}

	@Test
	void createSubtask() throws Exception {
		String createFormat = """
					{
							"name": "%s",
							"summary": "%s",
							"durationMinutes": 5,
							"taskId": %d
					}
				""";

		String create = String.format(createFormat,
				"Subtask Test - Name UUID = " + UUID.randomUUID().toString(),
				"Subtask Test - Summary UUID = " + UUID.randomUUID().toString(),
				subtask.getTaskId());
		
		when(subtaskService.save(any())).thenReturn(Mono.empty());
		
		getWebTestClient().post()
			.uri(baseUri)
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(create)
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void getSubtasksByTask() throws Exception {
		when(subtaskService.findByTask(
				anyInt(),
				any(Pageable.class),
				anyString(),
				any(TaskStatusType.class)))
			.thenReturn(Flux.empty());
		
		getWebTestClient().get()
			.uri(ub -> ub.path(baseUri)
					.queryParam("taskId", subtask.getTaskId())
					.build())
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void getSubtaskById() throws Exception {
		when(subtaskService.findById(anyInt())).thenReturn(Mono.empty());
		
		getWebTestClient().get()
			.uri(baseUri+"/"+subtask.getId())
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void update() throws Exception {		
		UpdateSubtaskDTO update = new UpdateSubtaskDTO();
		update.setName(UUID.randomUUID().toString());
		update.setSummary(UUID.randomUUID().toString());
		update.setDurationMinutes(5);
		
		when(subtaskService.update(anyInt(), any())).thenReturn(Mono.empty());
		
		getWebTestClient().patch()
			.uri(baseUri+"/"+subtask.getId())			
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(mapper.writeValueAsString(update))
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void deleteDefaultSubtask() throws Exception {
		String strIds = List.of(1, 2, 4, 5).stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));

		MultiValueMap<String, String> baseParams = new LinkedMultiValueMap<>();
		baseParams.add("subtaskIds", strIds);
		
		when(subtaskService.delete(anyList())).thenReturn(Mono.empty());
		
		getWebTestClient().delete()
			.uri(ub -> ub.path(baseUri)
					.queryParams(baseParams)
					.build())
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void updatePositions() throws Exception {
		String update = """
				{
					"moveType": "AFTER",
					"anchorSubtaskId": "%d"
				}
				""";
		
		update = update.formatted(1);
		
		when(subtaskService.updatePosition(anyInt(), any())).thenReturn(Mono.empty());
		
		getWebTestClient().patch()
			.uri(baseUri+"/"+subtask.getId()+"/position")
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(update)
			.exchange()
			.expectStatus().isNotFound();
	}

}
