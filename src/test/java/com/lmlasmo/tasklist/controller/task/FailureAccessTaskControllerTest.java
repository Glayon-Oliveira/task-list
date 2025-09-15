package com.lmlasmo.tasklist.controller.task;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.TaskController;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.TaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;
import com.lmlasmo.tasklist.util.VerifyResolvedException;

@WebMvcTest(controllers = TaskController.class)
@TestInstance(Lifecycle.PER_CLASS)
public class FailureAccessTaskControllerTest extends AbstractControllerTest {

	@MockitoBean
	private TaskService taskService;

	@MockitoBean
	private TaskStatusService statusService;

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
	}

	@Test
	public void deleteDefaultTask() throws Exception {
		when(accessService.canAccessTask(anyInt(), anyInt())).thenReturn(false);

		getMockMvc().perform(MockMvcRequestBuilders.delete(baseUri + task.getId()+1)
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(403))
		.andExpect(result -> VerifyResolvedException.verify(result, AccessDeniedException.class));
	}

	@Test
	public void updateStatusOfDefaultTask() throws Exception {
		when(accessService.canAccessTask(anyInt(), anyInt())).thenReturn(false);

		MultiValueMap<String, String> baseParams = new LinkedMultiValueMap<>();
		baseParams.add("taskId", String.valueOf(task.getId()+1));
		baseParams.add("status", TaskStatusType.COMPLETED.name());

		getMockMvc().perform(MockMvcRequestBuilders.put(baseUri.substring(0, baseUri.lastIndexOf("/")))
				.params(baseParams)
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(403))
		.andExpect(result -> VerifyResolvedException.verify(result, AccessDeniedException.class));
	}

}
