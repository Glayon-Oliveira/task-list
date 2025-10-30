package com.lmlasmo.tasklist.controller.subtask;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.SubtaskController;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.SubtaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;
import com.lmlasmo.tasklist.util.VerifyResolvedException;

@WebMvcTest(controllers = SubtaskController.class)
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
		subtask.setPosition(1);
		subtask.setTask(new Task(1));
		subtask.setCreatedAt(Instant.now());
		subtask.setUpdatedAt(subtask.getCreatedAt());

		when(accessService.canAccessTask(eq(subtask.getId()), eq(getDefaultUser().getId()))).thenReturn(false);
		when(accessService.canAccessTask(eq(subtask.getTask().getId()), eq(getDefaultUser().getId()))).thenReturn(false);
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
				subtask.getTask().getId());

		when(accessService.canAccessTask(subtask.getTask().getId(), getDefaultUser().getId())).thenReturn(false);

		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri)
				.header("Authorization", "Bearer " + getDefaultJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(create))
		.andExpect(MockMvcResultMatchers.status().is(403))
		.andExpect(result -> VerifyResolvedException.verify(result, AccessDeniedException.class));
	}

	@Test
	void getSubtasksByTask() throws Exception {
		getMockMvc().perform(MockMvcRequestBuilders.get(baseUri)
				.param("taskId", String.valueOf(subtask.getTask().getId()))
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(403))
		.andExpect(result -> VerifyResolvedException.verify(result, AccessDeniedException.class));
	}

	@Test
	void getSubtaskById() throws Exception {
		getMockMvc().perform(MockMvcRequestBuilders.get(baseUri + "/" + subtask.getId())
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(403))
		.andExpect(result -> VerifyResolvedException.verify(result, AccessDeniedException.class));
	}

	@Test
	void update() throws Exception {
		UpdateSubtaskDTO update = new UpdateSubtaskDTO();
		update.setName(UUID.randomUUID().toString());
		update.setSummary(UUID.randomUUID().toString());
		update.setDurationMinutes(5);

		getMockMvc().perform(MockMvcRequestBuilders.patch(baseUri + "/" + subtask.getId())
				.header("Authorization", "Bearer " + getDefaultJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(update)))
		.andExpect(MockMvcResultMatchers.status().is(403))
		.andExpect(result -> VerifyResolvedException.verify(result, AccessDeniedException.class));
	}

	@Test
	void deleteDefaultSubtask() throws Exception {
		String strIds = List.of(1, 2, 4, 5).stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));

		MultiValueMap<String, String> baseParams = new LinkedMultiValueMap<>();
		baseParams.add("subtaskIds", strIds);

		when(accessService.canAccessSubtask(anyList(), anyInt())).thenReturn(false);

		getMockMvc().perform(MockMvcRequestBuilders.delete(baseUri)
				.params(baseParams)
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(403))
		.andExpect(result -> VerifyResolvedException.verify(result, AccessDeniedException.class));
	}

	@Test
	void updatePositions() throws Exception {
		int newPosition = new Random().nextInt(1, 10);

		MultiValueMap<String, String> baseParams = new LinkedMultiValueMap<>();
		baseParams.add("position", String.valueOf(newPosition));

		getMockMvc().perform(MockMvcRequestBuilders.patch(baseUri+"/"+subtask.getId())
				.params(baseParams)
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(403));
	}

}
