package com.lmlasmo.tasklist.controller.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.TaskController;
import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.exception.PreconditionFailedException;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.param.task.CreateTaskSource;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.TaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;
import com.lmlasmo.tasklist.util.VerifyResolvedException;

@WebMvcTest(controllers = TaskController.class)
@TestInstance(Lifecycle.PER_CLASS)
public class TaskControllerTest extends AbstractControllerTest{

	@MockitoBean
	private TaskService taskService;

	@MockitoBean
	private TaskStatusService statusService;

	@MockitoBean(name = "resourceAccessService")
	private ResourceAccessService accessService;

	@Autowired
	private ObjectMapper mapper;

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
		task.setUser(getDefaultUser());
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

		when(taskService.save(any(CreateTaskDTO.class), anyInt())).thenReturn(new TaskDTO(task, true));

		ResultActions resultActions = getMockMvc().perform(MockMvcRequestBuilders.post(baseUri)
				.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(create))
				.andExpect(MockMvcResultMatchers.status().is(data.getStatus()))
				.andExpect(result -> VerifyResolvedException.verify(result, data.getExpectedException()));

		assumeTrue(data.getStatus() >= 200 && data.getStatus() < 300);

		OffsetDateTime deadline = task.getDeadline().atZone(ZoneId.of(task.getDeadlineZone())).toOffsetDateTime();
		String expectedDeadline = mapper.writeValueAsString(deadline).replace("\"", "");

		resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(task.getName()))
		.andExpect(MockMvcResultMatchers.jsonPath("$.summary").value(task.getSummary()))
		.andExpect(MockMvcResultMatchers.jsonPath("$.deadline").value(expectedDeadline))
		.andExpect(MockMvcResultMatchers.jsonPath("$.deadlineZone").value(task.getDeadlineZone()));
	}

	@RepeatedTest(2)
	public void getTasksByUser(RepetitionInfo info) throws Exception {
		when(taskService.findByUser(eq(getDefaultUser().getId()), eq(true), any())).thenReturn(new PageImpl<>(List.of(new TaskDTO(task, true))));
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfNoneMatch(Long.toString(task.getVersion()));
			when(taskService.sumVersionByUser(getDefaultUser().getId())).thenReturn(task.getVersion());
		}
		
		ResultActions result = getMockMvc().perform(MockMvcRequestBuilders.get(baseUri)
				.param("withSubtasks", "true")
				.headers(headers));		
		
		if(info.getCurrentRepetition() == 2) {
			result.andExpect(MockMvcResultMatchers.status().isNotModified());			
		}else if(info.getCurrentRepetition() == 1) {
			result.andExpect(MockMvcResultMatchers.status().is(200))
			.andExpect(MockMvcResultMatchers.jsonPath("$.size").value(1))
			.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].summary").exists())
			.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].subtasks").exists())
			.andExpect(MockMvcResultMatchers.header().exists("ETag"));
		}
		
	}

	@RepeatedTest(2)
	public void getTaskById(RepetitionInfo info) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfNoneMatch(Long.toString(task.getVersion()));
		}
		
		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(true);
		when(taskService.findById(task.getId(), true)).thenReturn(new TaskDTO(task, true));
		when(taskService.existsByIdAndVersion(task.getId(), task.getVersion())).thenReturn(true);

		ResultActions result = getMockMvc().perform(MockMvcRequestBuilders.get(baseUri + "/" + task.getId())
				.param("withSubtasks", "true")
				.headers(headers));
				
		if(info.getCurrentRepetition() == 2) {
			result.andExpect(MockMvcResultMatchers.status().isNotModified());			
		}else {
			result.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.summary").exists())
			.andExpect(MockMvcResultMatchers.jsonPath("$.subtasks").exists())
			.andExpect(MockMvcResultMatchers.header().exists("ETag"));
		}
	}

	@Test
	public void deleteDefaultTask() throws Exception {
		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(true);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());		
		headers.setIfMatch(Long.toString(task.getVersion()));
		
		when(taskService.existsByIdAndVersion(task.getId(), task.getVersion())).thenReturn(true);
		
		getMockMvc().perform(MockMvcRequestBuilders.delete(baseUri + "/" + task.getId())
				.headers(headers))
		.andExpect(MockMvcResultMatchers.status().is(204))
		.andExpect(result -> VerifyResolvedException.verify(result, null));
		
		headers.setIfMatch(Long.toString(task.getVersion()/2+1));
		
		getMockMvc().perform(MockMvcRequestBuilders.delete(baseUri + "/" + task.getId())
				.headers(headers))
		.andExpect(MockMvcResultMatchers.status().isPreconditionFailed())
		.andExpect(result -> VerifyResolvedException.verify(result, PreconditionFailedException.class));
	}

	@RepeatedTest(3)
	public void updateDescription(RepetitionInfo info) throws Exception {
		UpdateTaskDTO update = new UpdateTaskDTO();
		update.setName(UUID.randomUUID().toString());
		update.setSummary(UUID.randomUUID().toString());

		TaskDTO fullTaskDTO = new TaskDTO(task, true);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfMatch(Long.toString(task.getVersion()));
		}else if(info.getCurrentRepetition() == 3) {
			headers.setIfMatch(Long.toString(task.getVersion()/2+1));
		}
		
		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(true);
		when(taskService.update(eq(task.getId()), any())).thenReturn(fullTaskDTO);
		when(taskService.existsByIdAndVersion(task.getId(), task.getVersion())).thenReturn(true);

		ResultActions result = getMockMvc().perform(MockMvcRequestBuilders.patch(baseUri + "/" + task.getId())
				.headers(headers)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(update)));
		
		if(info.getCurrentRepetition() == 3) {
			result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());			
		}else {
			result.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(r -> VerifyResolvedException.verify(r, null))
			.andExpect(MockMvcResultMatchers.jsonPath("$.name").exists())
			.andExpect(MockMvcResultMatchers.jsonPath("$.summary").exists());
		}
	}

	@RepeatedTest(3)
	public void updateDeadline(RepetitionInfo info) throws Exception {
		UpdateTaskDTO update = new UpdateTaskDTO();
		update.setDeadline(OffsetDateTime.now().plusMinutes(1));
		update.setDeadlineZone("UTC");

		TaskDTO taskDTO = new TaskDTO(task);
		taskDTO.setDeadline(update.getDeadline());
		taskDTO.setDeadlineZone(ZoneId.of(update.getDeadlineZone()).toString());
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfMatch(Long.toString(task.getVersion()));
		}else if(info.getCurrentRepetition() == 3) {
			headers.setIfMatch(Long.toString(task.getVersion()/2+1));
		}

		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(true);
		when(taskService.update(eq(task.getId()), any())).thenReturn(taskDTO);
		when(taskService.existsByIdAndVersion(task.getId(), task.getVersion())).thenReturn(true);

		ResultActions result = getMockMvc().perform(MockMvcRequestBuilders.patch(baseUri + "/" + task.getId())
				.headers(headers)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(update)));
				
		if(info.getCurrentRepetition() == 3) {
			result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
		}else {
			result.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(r -> VerifyResolvedException.verify(r, null))
			.andExpect(r -> {
				String deadline = JsonPath.read(r.getResponse().getContentAsString(), "$.deadline");
				assertEquals(OffsetDateTime.parse(deadline), update.getDeadline());
			})
			.andExpect(MockMvcResultMatchers.jsonPath("$.deadlineZone").value(update.getDeadlineZone()));
		}		
	}

	@RepeatedTest(3)
	public void updateStatusOfDefaultTask(RepetitionInfo info) throws Exception {
		String baseUri = "/api/task";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfMatch(Long.toString(task.getVersion()));
		}else if(info.getCurrentRepetition() == 3) {			
			headers.setIfMatch(Long.toString(task.getVersion()/2+1));
		}

		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(true);
		when(taskService.existsByIdAndVersion(task.getId(), task.getVersion())).thenReturn(true);
		when(taskService.existsByIdAndVersion(task.getId(), task.getVersion()+1)).thenReturn(false);

		ResultActions result = getMockMvc().perform(MockMvcRequestBuilders.patch(baseUri + "/" + task.getId())
				.param("status", TaskStatusType.COMPLETED.name())
				.headers(headers));
		
		if(info.getCurrentRepetition() == 3) {
			result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
		}else {
			result.andExpect(MockMvcResultMatchers.status().isNoContent())
			.andExpect(r -> VerifyResolvedException.verify(r, null));
		}
		
	}

}
