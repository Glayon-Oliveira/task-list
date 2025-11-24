package com.lmlasmo.tasklist.controller.task;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.TaskController;
import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.mapper.MapperTestConfig;
import com.lmlasmo.tasklist.mapper.TaskMapper;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.param.task.CreateTaskSource;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.TaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = TaskController.class)
@TestInstance(Lifecycle.PER_CLASS)
@Import(MapperTestConfig.class)
public class TaskControllerTest extends AbstractControllerTest{

	@MockitoBean
	private TaskService taskService;

	@MockitoBean
	private TaskStatusService statusService;

	@MockitoBean(name = "resourceAccessService")
	private ResourceAccessService accessService;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private TaskMapper taskMapper;

	private final String baseUri = "/api/task";

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
		task.setDeadline(OffsetDateTime.now().withSecond(0).withNano(0).plusMinutes(60).toInstant());
		task.setDeadlineZone(ZoneId.systemDefault().toString());
		task.setCreatedAt(Instant.now());
		task.setUpdatedAt(task.getCreatedAt());
		task.setVersion(new Random().nextLong(Long.MAX_VALUE));
		task.setUserId(getDefaultUser().getId());
	}

	@ParameterizedTest
	@MethodSource("com.lmlasmo.tasklist.param.task.CreateTaskSource#source")
	void createTask(CreateTaskSource.CreateTaskData data) throws Exception {
		String createFormat = """
					{
							"name": "%s",
							"summary": "%s",
							"deadline": "%s",
							"deadlineZone": "%s"
					}
				""";

		String create = String.format(createFormat, data.getName(), data.getSummary(), data.getDeadline(), data.getDeadlineZone());

		when(taskService.save(any(CreateTaskDTO.class), anyInt())).thenReturn(Mono.just(taskMapper.toDTO(task)));
		
		ResponseSpec response = getWebTestClient().post()
				.uri(baseUri)
				.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(create)
				.exchange()
				.expectStatus().isEqualTo(data.getStatus());

		assumeTrue(data.getStatus() >= 200 && data.getStatus() < 300);
		
		response.expectBody()
			.jsonPath("$.name").isEqualTo(task.getName())
			.jsonPath("$.summary").isEqualTo(task.getSummary())
			.jsonPath("$.deadline").isEqualTo(task.getDeadline())
			.jsonPath("$.deadlineZone").isEqualTo(task.getDeadlineZone());
	}

	@RepeatedTest(2)
	public void getTasksByUser(RepetitionInfo info) throws Exception {
		when(taskService.findByUser(eq(getDefaultUser().getId()))).thenReturn(Flux.fromIterable(List.of(taskMapper.toDTO(task))));
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			String etag = Long.toString(task.getVersion());
			headers.setIfNoneMatch("\""+etag+"\"");
			when(taskService.sumVersionByUser(getDefaultUser().getId())).thenReturn(Mono.just(task.getVersion()));
		}
		
		ResponseSpec response = getWebTestClient().get()
				.uri(baseUri)
				.headers(h -> h.addAll(headers))
				.exchange();
		
		if(info.getCurrentRepetition() == 2) {
			response.expectStatus().isNotModified();
		}else if(info.getCurrentRepetition() == 1) {
			response.expectStatus().isOk()
				.expectHeader().exists("ETag")
				.expectBody()
					.jsonPath("$.length()").isEqualTo(1)
					.jsonPath("$[*].summary").exists();
		}
		
	}

	@RepeatedTest(2)
	public void getTaskById(RepetitionInfo info) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			String etag = Long.toString(task.getVersion());
			headers.setIfNoneMatch("\""+etag+"\"");
		}
		
		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(Mono.empty());
		when(taskService.findById(task.getId())).thenReturn(Mono.just(taskMapper.toDTO(task)));
		when(taskService.existsByIdAndVersion(task.getId(), task.getVersion())).thenReturn(Mono.just(true));
		
		ResponseSpec response = getWebTestClient().get()
				.uri(baseUri + "/" + task.getId())
				.headers(h -> h.addAll(headers))
				.exchange();
				
		if(info.getCurrentRepetition() == 2) {
			response.expectStatus().isNotModified();			
		}else {
			response.expectStatus().isOk()
				.expectHeader().exists("ETag")
				.expectBody()
					.jsonPath("$.summary").exists();
		}
	}

	@Test
	public void deleteDefaultTask() throws Exception {
		String etag = Long.toString(task.getVersion());
		String fEtag = Long.toString(task.getVersion()/2+1);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		headers.setIfMatch("\""+etag+"\"");
		
		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(Mono.empty());
		when(taskService.existsByIdAndVersion(anyInt(), anyLong())).thenReturn(Mono.just(false));
		when(taskService.existsByIdAndVersion(eq(task.getId()), eq(task.getVersion()))).thenReturn(Mono.just(true));
		when(taskService.delete(anyInt())).thenReturn(Mono.empty());
		
		getWebTestClient().delete()
			.uri(baseUri + "/" + task.getId())
			.headers(h -> h.addAll(headers))
			.exchange()
			.expectStatus().isNoContent();
		
		headers.setIfMatch("\""+fEtag+"\"");

		getWebTestClient().delete()
			.uri(baseUri + "/" + task.getId())
			.headers(h -> h.addAll(headers))
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED);
		
	}

	@RepeatedTest(3)
	public void updateDescription(RepetitionInfo info) throws Exception {
		UpdateTaskDTO update = new UpdateTaskDTO();
		update.setName(UUID.randomUUID().toString());
		update.setSummary(UUID.randomUUID().toString());

		TaskDTO fullTaskDTO = taskMapper.toDTO(task);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		String etag = Long.toString(task.getVersion());
		String fEtag = Long.toString(task.getVersion()/2+1);
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfMatch("\""+etag+"\"");
		}else if(info.getCurrentRepetition() == 3) {			
			headers.setIfMatch("\""+fEtag+"\"");
		}
		
		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(Mono.empty());
		when(taskService.update(eq(task.getId()), any())).thenReturn(Mono.just(fullTaskDTO));
		when(taskService.existsByIdAndVersion(anyInt(), anyLong())).thenReturn(Mono.just(false));
		when(taskService.existsByIdAndVersion(eq(task.getId()), eq(task.getVersion()))).thenReturn(Mono.just(true));
		
		ResponseSpec response = getWebTestClient().patch()
				.uri(baseUri + "/" + task.getId())
				.headers(h -> h.addAll(headers))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(mapper.writeValueAsString(update))
				.exchange();
		
		if(info.getCurrentRepetition() == 3) {
			response.expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED);
		}else {
			response.expectStatus().isOk()
				.expectBody()
					.jsonPath("$.name").exists()
					.jsonPath("$.summary").exists();
		}
	}

	@RepeatedTest(3)
	public void updateDeadline(RepetitionInfo info) throws Exception {
		UpdateTaskDTO update = new UpdateTaskDTO();
		update.setDeadline(OffsetDateTime.now().plusMinutes(1));
		update.setDeadlineZone("UTC");

		TaskDTO taskDTO = taskMapper.toDTO(task);
		taskDTO.setDeadline(update.getDeadline());
		taskDTO.setDeadlineZone(ZoneId.of(update.getDeadlineZone()).toString());
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		String etag = Long.toString(task.getVersion());
		String fEtag = Long.toString(task.getVersion()/2+1);
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfMatch("\""+etag+"\"");
		}else if(info.getCurrentRepetition() == 3) {			
			headers.setIfMatch("\""+fEtag+"\"");
		}

		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(Mono.empty());
		when(taskService.update(eq(task.getId()), any())).thenReturn(Mono.just(taskDTO));
		when(taskService.existsByIdAndVersion(anyInt(), anyLong())).thenReturn(Mono.just(false));
		when(taskService.existsByIdAndVersion(eq(task.getId()), eq(task.getVersion()))).thenReturn(Mono.just(true));
		
		ResponseSpec response = getWebTestClient().patch()
				.uri(baseUri + "/" + task.getId())
				.headers(h -> h.addAll(headers))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(mapper.writeValueAsString(update))
				.exchange();
				
		if(info.getCurrentRepetition() == 3) {
			response.expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED);
		}else {
			response.expectStatus().isOk()
				.expectBody()
					.jsonPath("$.deadline").isEqualTo(update.getDeadline().toInstant())
					.jsonPath("$.deadlineZone").isEqualTo(update.getDeadlineZone());
		}		
	}

	@RepeatedTest(3)
	public void updateStatusOfDefaultTask(RepetitionInfo info) throws Exception {
		String baseUri = "/api/task";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		String etag = Long.toString(task.getVersion());
		String fEtag = Long.toString(task.getVersion()/2+1);
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfMatch("\""+etag+"\"");
		}else if(info.getCurrentRepetition() == 3) {			
			headers.setIfMatch("\""+fEtag+"\"");
		}

		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(Mono.empty());
		when(taskService.existsByIdAndVersion(eq(task.getId()), eq(task.getVersion()))).thenReturn(Mono.just(true));
		when(taskService.existsByIdAndVersion(eq(task.getId()), eq(task.getVersion()/2+1))).thenReturn(Mono.just(false));
		when(statusService.updateTaskStatus(eq(task.getId()), eq(TaskStatusType.COMPLETED))).thenReturn(Mono.empty());
		
		ResponseSpec response = getWebTestClient().patch()
				.uri(ub -> ub.path(baseUri + "/" + task.getId())
						.queryParam("status", TaskStatusType.COMPLETED)
						.build())
				.headers(h -> h.addAll(headers))
				.exchange();
		
		if(info.getCurrentRepetition() == 3) {
			response.expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED);
		}else {
			response.expectStatus().isNoContent();
		}
		
	}

}
