package com.lmlasmo.tasklist.controller.subtask;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.SubtaskController;
import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.param.subtask.CreateSubtaskSource;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.SubtaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;
import com.lmlasmo.tasklist.util.VerifyResolvedException;

@WebMvcTest(controllers = SubtaskController.class)
@TestInstance(Lifecycle.PER_CLASS)
public class SubtaskControllerTest extends AbstractControllerTest{

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
		subtask.setVersion(new Random().nextLong(Long.MAX_VALUE));
	}

	@ParameterizedTest
	@MethodSource("com.lmlasmo.tasklist.param.subtask.CreateSubtaskSource#source")
	public void createSubtask(CreateSubtaskSource.CreateSubtaskData data) throws Exception {
		String createFormat = """
					{
							"name": "%s",
							"summary": "%s",
							"durationMinutes": %d,
							"taskId": %d
					}
				""";

		String create = String.format(createFormat, data.getName(), data.getSummary(), data.getDurationMinutes(), subtask.getTask().getId());

		subtask.setName(data.getName());
		subtask.setSummary(data.getSummary());
		subtask.setDurationMinutes(data.getDurationMinutes());

		when(accessService.canAccessTask(subtask.getId(), getDefaultUser().getId())).thenReturn(true);
		when(subtaskService.save(any())).thenReturn(new SubtaskDTO(subtask));

		ResultActions resultActions = getMockMvc().perform(MockMvcRequestBuilders.post(baseUri)
				.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(create))
				.andExpect(MockMvcResultMatchers.status().is(data.getStatus()))
				.andExpect(result -> VerifyResolvedException.verify(result, data.getExpectedException()));

		assumeTrue(data.getStatus() >= 200 && data.getStatus() < 300);

		resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(data.getName()))
		.andExpect(MockMvcResultMatchers.jsonPath("$.summary").value(data.getSummary()))
		.andExpect(MockMvcResultMatchers.jsonPath("$.durationMinutes").value(data.getDurationMinutes()));
	}

	@RepeatedTest(2)
	public void getSubtasksByTask(RepetitionInfo info) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfNoneMatch(Long.toString(subtask.getVersion()));
			when(subtaskService.sumVersionByTask(anyInt())).thenReturn(subtask.getVersion());
		}
		
		when(accessService.canAccessTask(eq(1), eq(getDefaultUser().getId()))).thenReturn(true);
		when(subtaskService.findByTask(eq(1), any())).thenReturn(new PageImpl<>(List.of(new SubtaskDTO(subtask))));

		ResultActions result = getMockMvc().perform(MockMvcRequestBuilders.get(baseUri)
				.param("taskId", "1")
				.headers(headers));
		
		if(info.getCurrentRepetition() == 2) {
			result.andExpect(MockMvcResultMatchers.status().isNotModified());
		}else {
			result.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.size").value(1))
			.andExpect(MockMvcResultMatchers.header().exists("ETag"));
		}	
	}

	@RepeatedTest(2)
	public void getSubtaskById(RepetitionInfo info) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfNoneMatch(Long.toString(subtask.getVersion()));
			when(subtaskService.existsByIdAndVersion(subtask.getId(), subtask.getVersion())).thenReturn(true);
		}
		
		when(accessService.canAccessSubtask(subtask.getId(), getDefaultUser().getId())).thenReturn(true);
		when(subtaskService.findById(subtask.getId())).thenReturn(new SubtaskDTO(subtask));

		ResultActions result = getMockMvc().perform(MockMvcRequestBuilders.get(baseUri + "/" + subtask.getId())
				.headers(headers));
		
		if(info.getCurrentRepetition() == 2) {
			result.andExpect(MockMvcResultMatchers.status().isNotModified());
		}else {
			result.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(subtask.getId()))
			.andExpect(MockMvcResultMatchers.header().exists("ETag"));
		}
	}

	@RepeatedTest(3)
	public void update(RepetitionInfo info) throws Exception {
		UpdateSubtaskDTO update = new UpdateSubtaskDTO();
		update.setName(UUID.randomUUID().toString());
		update.setSummary(UUID.randomUUID().toString());
		update.setDurationMinutes(5);

		SubtaskDTO subtaskDTO = new SubtaskDTO(subtask);
		subtaskDTO.setName(update.getName());
		subtaskDTO.setSummary(update.getSummary());
		subtaskDTO.setDurationMinutes(update.getDurationMinutes());
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfMatch(Long.toString(subtask.getVersion()));			
		}else if(info.getCurrentRepetition() == 3) {
			headers.setIfMatch(Long.toString(subtask.getVersion()/2+1));
		}

		when(accessService.canAccessSubtask(subtask.getId(), getDefaultUser().getId())).thenReturn(true);
		when(subtaskService.update(eq(subtask.getId()), any())).thenReturn(subtaskDTO);
		when(subtaskService.existsByIdAndVersion(subtask.getId(), subtask.getVersion())).thenReturn(true);

		ResultActions result = getMockMvc().perform(MockMvcRequestBuilders.patch(baseUri + "/" + subtask.getId())
				.headers(headers)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(update)));
		
		if(info.getCurrentRepetition() == 3) {
			result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
		}else {
			result.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(subtaskDTO.getName()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.summary").value(subtaskDTO.getSummary()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.durationMinutes").value(subtaskDTO.getDurationMinutes()))
			.andExpect(MockMvcResultMatchers.header().exists("ETag"));
		}		
	}

	@Test
	public void deleteDefaultSubtask() throws Exception {
		List<Integer> ids = List.of(1, 2, 4, 5);

		String strIds = ids.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
		
		long sumIds = ids.stream()
				.mapToInt(Integer::intValue)
				.sum();

		MultiValueMap<String, String> baseParams = new LinkedMultiValueMap<>();
		baseParams.add("subtaskIds", strIds);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		headers.setIfMatch(Long.toString(sumIds));

		when(accessService.canAccessSubtask(ids, getDefaultUser().getId())).thenReturn(true);
		when(subtaskService.sumVersionByIds(ids)).thenReturn(sumIds);

		getMockMvc().perform(MockMvcRequestBuilders.delete(baseUri)
				.params(baseParams)
				.headers(headers))
		.andExpect(MockMvcResultMatchers.status().isNoContent());
		
		headers.setIfMatch(Long.toString(sumIds/2+1));
		
		getMockMvc().perform(MockMvcRequestBuilders.delete(baseUri)
				.params(baseParams)
				.headers(headers))
		.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	@RepeatedTest(10)
	public void updatePositions(RepetitionInfo info) throws Exception {
		int newPosition = new Random().nextInt(1, 10);

		MultiValueMap<String, String> baseParams = new LinkedMultiValueMap<>();
		baseParams.add("position", String.valueOf(newPosition));
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfMatch(Long.toString(subtask.getVersion()));			
		}else if(info.getCurrentRepetition() == 3) {
			headers.setIfMatch(Long.toString(subtask.getVersion()/2+1));
		}

		when(accessService.canAccessSubtask(subtask.getId(), getDefaultUser().getId())).thenReturn(true);
		when(subtaskService.existsByIdAndVersion(subtask.getId(), subtask.getVersion())).thenReturn(true);

		ResultActions result = getMockMvc().perform(MockMvcRequestBuilders.patch(baseUri + "/" + subtask.getId())
				.params(baseParams)
				.headers(headers));
		
		if(info.getCurrentRepetition() == 3) {
			result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
		}else {
			result.andExpect(MockMvcResultMatchers.status().is(204));
		}		
	}

}
