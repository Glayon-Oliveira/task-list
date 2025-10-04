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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
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
import com.lmlasmo.tasklist.dto.update.UpdateDeadlineTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateDescriptionTaskDTO;
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
		task.setDeadline(OffsetDateTime.now().withSecond(0).withNano(0).plusMinutes(60).toInstant());
		task.setDeadlineZone(ZoneId.systemDefault().toString());
		task.setCreatedAt(Instant.now());
		task.setUpdatedAt(task.getCreatedAt());
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
				.header("Authorization", "Bearer " + getDefaultJwtToken())
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

	@Test
	public void getTasksByUser() throws Exception {
		when(taskService.findByUser(eq(getDefaultUser().getId()), eq(true), any())).thenReturn(new PageImpl<>(List.of(new TaskDTO(task, true))));

		getMockMvc().perform(MockMvcRequestBuilders.get(baseUri)
				.param("withSubtasks", "true")
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(200))
		.andExpect(MockMvcResultMatchers.jsonPath("$.size").value(1))
		.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].summary").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].subtasks").exists());
	}

	@Test
	public void getTaskById() throws Exception {
		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(true);
		when(taskService.findById(task.getId(), true)).thenReturn(new TaskDTO(task, true));

		getMockMvc().perform(MockMvcRequestBuilders.get(baseUri + task.getId())
				.param("withSubtasks", "true")
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(200))
		.andExpect(MockMvcResultMatchers.jsonPath("$.summary").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.subtasks").exists());
	}

	@Test
	public void deleteDefaultTask() throws Exception {
		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(true);

		getMockMvc().perform(MockMvcRequestBuilders.delete(baseUri + task.getId())
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(204))
		.andExpect(result -> VerifyResolvedException.verify(result, null));
	}

	@Test
	public void updateDescription() throws Exception {
		UpdateDescriptionTaskDTO update = new UpdateDescriptionTaskDTO();
		update.setName(UUID.randomUUID().toString());
		update.setSummary(UUID.randomUUID().toString());

		TaskDTO fullTaskDTO = new TaskDTO(task, true);

		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(true);
		when(taskService.updateDescription(eq(task.getId()), any())).thenReturn(fullTaskDTO);

		getMockMvc().perform(MockMvcRequestBuilders.put(baseUri + task.getId() + "/description")
				.header("Authorization", "Bearer " + getDefaultJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(update)))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(result -> VerifyResolvedException.verify(result, null))
		.andExpect(MockMvcResultMatchers.jsonPath("$.name").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.summary").exists());
	}

	@Test
	public void updateDeadline() throws Exception {
		UpdateDeadlineTaskDTO update = new UpdateDeadlineTaskDTO();
		update.setDeadline(OffsetDateTime.now().plusMinutes(1));
		update.setDeadlineZone("UTC");

		TaskDTO taskDTO = new TaskDTO(task);
		taskDTO.setDeadline(update.getDeadline());
		taskDTO.setDeadlineZone(ZoneId.of(update.getDeadlineZone()).toString());

		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(true);
		when(taskService.updateDeadline(eq(task.getId()), any())).thenReturn(taskDTO);

		getMockMvc().perform(MockMvcRequestBuilders.put(baseUri + task.getId() + "/deadline")
				.header("Authorization", "Bearer " + getDefaultJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(update)))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(result -> VerifyResolvedException.verify(result, null))
		.andExpect(result -> {
			String deadline = JsonPath.read(result.getResponse().getContentAsString(), "$.deadline");
			assertEquals(OffsetDateTime.parse(deadline), update.getDeadline());
		})
		.andExpect(MockMvcResultMatchers.jsonPath("$.deadlineZone").value(update.getDeadlineZone()));
	}

	@Test
	public void updateStatusOfDefaultTask() throws Exception {
		String baseUri = "/api/task";

		when(accessService.canAccessTask(task.getId(), getDefaultUser().getId())).thenReturn(true);

		getMockMvc().perform(MockMvcRequestBuilders.put(baseUri + "/" + task.getId())
				.param("status", TaskStatusType.COMPLETED.name())
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(204))
		.andExpect(result -> VerifyResolvedException.verify(result, null));
	}

}
