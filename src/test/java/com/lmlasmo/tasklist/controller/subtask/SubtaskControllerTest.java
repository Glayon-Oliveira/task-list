package com.lmlasmo.tasklist.controller.subtask;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.SubtaskController;
import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.mapper.MapperTestConfig;
import com.lmlasmo.tasklist.mapper.SubtaskMapper;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.param.subtask.CreateSubtaskSource;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.SubtaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = SubtaskController.class)
@TestInstance(Lifecycle.PER_CLASS)
@Import(MapperTestConfig.class)
public class SubtaskControllerTest extends AbstractControllerTest{

	@MockitoBean
	private SubtaskService subtaskService;

	@MockitoBean
	private TaskStatusService statusService;

	@MockitoBean(name = "resourceAccessService")
	private ResourceAccessService accessService;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private SubtaskMapper stMapper;

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

		String create = String.format(createFormat, data.getName(), data.getSummary(), data.getDurationMinutes(), subtask.getTaskId());

		subtask.setName(data.getName());
		subtask.setSummary(data.getSummary());
		subtask.setDurationMinutes(data.getDurationMinutes());

		when(accessService.canAccessTask(subtask.getId(), getDefaultUser().getId())).thenReturn(Mono.empty());
		when(subtaskService.save(any())).thenReturn(Mono.just(stMapper.toDTO(subtask)));
		
		ResponseSpec response = getWebTestClient().post()
				.uri(baseUri)
				.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(create)
				.exchange()
				.expectStatus().isEqualTo(data.getStatus());

		assumeTrue(data.getStatus() >= 200 && data.getStatus() < 300);
		
		response.expectBody()
			.jsonPath("$.name").isEqualTo(data.getName())
			.jsonPath("$.summary").isEqualTo(data.getSummary())
			.jsonPath("$.durationMinutes").isEqualTo(data.getDurationMinutes());
	}

	@RepeatedTest(2)
	public void getSubtasksByTask(RepetitionInfo info) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			String etag = Long.toString(subtask.getVersion());
			headers.setIfNoneMatch("\""+etag+"\"");
			when(subtaskService.sumVersionByTask(
					anyInt(),
					any(Pageable.class),
					anyString(),
					any(TaskStatusType.class)))
				.thenReturn(Mono.just(subtask.getVersion()));
		}
		
		when(accessService.canAccessTask(eq(1), eq(getDefaultUser().getId()))).thenReturn(Mono.empty());
		when(subtaskService.findByTask(eq(1),
				any(Pageable.class),
				anyString(),
				any(TaskStatusType.class)))
			.thenReturn(Flux.fromIterable((List.of(stMapper.toDTO(subtask)))));
		
		ResponseSpec response = getWebTestClient().get()
				.uri(ub -> ub.path(baseUri)
						.queryParam("taskId", 1)
						.queryParam("contains", "")
						.queryParam("status", TaskStatusType.PENDING)
						.build())
				.headers(h -> h.addAll(headers))
				.exchange();
		
		if(info.getCurrentRepetition() == 2) {
			response.expectStatus().isNotModified();
		}else {
			response.expectHeader().exists("ETag")
				.expectBody().jsonPath("$.length()").isEqualTo(1);
		}
	}

	@RepeatedTest(2)
	public void getSubtaskById(RepetitionInfo info) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		if(info.getCurrentRepetition() == 2) {
			String etag = Long.toString(subtask.getVersion());
			headers.setIfNoneMatch("\""+etag+"\"");
			when(subtaskService.existsByIdAndVersion(subtask.getId(), subtask.getVersion())).thenReturn(Mono.just(true));
		}
		
		when(accessService.canAccessSubtask(subtask.getId(), getDefaultUser().getId())).thenReturn(Mono.empty());
		when(subtaskService.findById(subtask.getId())).thenReturn(Mono.just(stMapper.toDTO(subtask)));
		
		ResponseSpec response = getWebTestClient().get()
				.uri(baseUri + "/" + subtask.getId())
				.headers(h -> h.addAll(headers))
				.exchange();
		
		if(info.getCurrentRepetition() == 2) {
			response.expectStatus().isNotModified();
		}else {
			response.expectStatus().isOk()
				.expectHeader().exists("ETag")
				.expectBody().jsonPath("$.id").isEqualTo(subtask.getId());
		}
	}

	@RepeatedTest(3)
	public void update(RepetitionInfo info) throws Exception {
		UpdateSubtaskDTO update = new UpdateSubtaskDTO();
		update.setName(UUID.randomUUID().toString());
		update.setSummary(UUID.randomUUID().toString());
		update.setDurationMinutes(5);

		SubtaskDTO subtaskDTO = stMapper.toDTO(subtask);
		subtaskDTO.setName(update.getName());
		subtaskDTO.setSummary(update.getSummary());
		subtaskDTO.setDurationMinutes(update.getDurationMinutes());
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		String etag = Long.toString(subtask.getVersion());
		String fEtag = Long.toString(subtask.getVersion()/2+1);
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfMatch("\""+etag+"\"");			
		}else if(info.getCurrentRepetition() == 3) {
			headers.setIfMatch("\""+fEtag+"\"");
		}

		when(accessService.canAccessSubtask(subtask.getId(), getDefaultUser().getId())).thenReturn(Mono.empty());
		when(subtaskService.update(eq(subtask.getId()), any())).thenReturn(Mono.just(subtaskDTO));
		when(subtaskService.existsByIdAndVersion(anyInt(), anyLong())).thenReturn(Mono.just(false));
		when(subtaskService.existsByIdAndVersion(eq(subtask.getId()), eq(subtask.getVersion()))).thenReturn(Mono.just(true));
		
		ResponseSpec response = getWebTestClient().patch()
				.uri(baseUri + "/" + subtask.getId())
				.headers(h -> h.addAll(headers))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(mapper.writeValueAsString(update))
				.exchange();
		
		if(info.getCurrentRepetition() == 3) {
			response.expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED);
		}else {
			response.expectStatus().isOk()
				.expectBody()
					.jsonPath("$.name").isEqualTo(subtaskDTO.getName())
					.jsonPath("$.summary").isEqualTo(subtaskDTO.getSummary())
					.jsonPath("$.durationMinutes").isEqualTo(subtaskDTO.getDurationMinutes());
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
		
		String etag = Long.toString(sumIds);
		String fEtag = Long.toString(sumIds/2+1);

		MultiValueMap<String, String> baseParams = new LinkedMultiValueMap<>();
		baseParams.add("subtaskIds", strIds);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		headers.setIfMatch("\""+etag+"\"");

		when(accessService.canAccessSubtask(ids, getDefaultUser().getId())).thenReturn(Mono.empty());
		when(subtaskService.sumVersionByIds(ids)).thenReturn(Mono.just(sumIds));
		when(subtaskService.delete(anyList())).thenReturn(Mono.empty());
		
		getWebTestClient().delete()
				.uri(ub -> ub.path(baseUri)
						.queryParams(baseParams)
						.build())
				.headers(h -> h.addAll(headers))
				.exchange()
				.expectStatus().isNoContent();
		
		headers.setIfMatch("\""+fEtag+"\"");
		
		getWebTestClient().delete()
				.uri(ub -> ub.path(baseUri)
						.queryParams(baseParams)
						.build())
				.headers(h -> h.addAll(headers))
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED);
	}

	@RepeatedTest(3)
	public void updatePositions(RepetitionInfo info) throws Exception {
		String update = """
				{
					"moveType": "AFTER",
					"anchorSubtaskId": "%d"
				}
				""";
		
		update = update.formatted(1);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		String etag = Long.toString(subtask.getVersion());
		String fEtag = Long.toString(subtask.getVersion()/2+1);
		
		if(info.getCurrentRepetition() == 2) {
			headers.setIfMatch("\""+etag+"\"");			
		}else if(info.getCurrentRepetition() == 3) {
			headers.setIfMatch("\""+fEtag+"\"");
		}

		when(accessService.canAccessSubtask(eq(subtask.getId()), eq(getDefaultUser().getId()))).thenReturn(Mono.empty());
		when(accessService.canAccessSubtask(eq(1), eq(getDefaultUser().getId()))).thenReturn(Mono.empty());
		when(subtaskService.existsByIdAndVersion(anyInt(), anyLong())).thenReturn(Mono.just(false));
		when(subtaskService.existsByIdAndVersion(eq(subtask.getId()), eq(subtask.getVersion()))).thenReturn(Mono.just(true));		
		when(subtaskService.updatePosition(anyInt(), any())).thenReturn(Mono.empty());
		
		ResponseSpec response = getWebTestClient().patch()
				.uri(baseUri + "/" + subtask.getId() + "/position")
				.headers(h -> h.addAll(headers))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(update)
				.exchange();
		
		if(info.getCurrentRepetition() == 3) {
			response.expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED);			
		}else {
			response.expectStatus().isNoContent();
		}		
	}

}
