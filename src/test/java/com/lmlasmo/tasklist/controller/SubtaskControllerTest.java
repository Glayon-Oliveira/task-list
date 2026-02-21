package com.lmlasmo.tasklist.controller;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;

import com.lmlasmo.tasklist.TaskListApplicationTests;
import com.lmlasmo.tasklist.data.tool.AuthTestTool;
import com.lmlasmo.tasklist.data.tool.SubtaskTestTool;
import com.lmlasmo.tasklist.data.tool.TaskTestTool;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskPositionDTO.MovePositionType;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.repository.summary.TaskSummary;

@TestInstance(Lifecycle.PER_CLASS)
public class SubtaskControllerTest extends TaskListApplicationTests {
	
	@Autowired
	private WebTestClient webTClient;
	
	@Autowired
	private AuthTestTool authTestTool;
	
	@Autowired
	private TaskTestTool taskTestTool; 
	
	@Autowired
	private SubtaskTestTool subtaskTestTool;
	
	private final String baseUri = "/api/subtask";
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
				"'Subtask name', 'Subtask summary', 5",
				"'Subtask name', '', 5",
				"'Subtask name', null, 5",
				"'Subtask name', 'Subtask summary', null",
			}
	)
	void create(String name, String summary, Integer durationMinutes) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("summary", summary);
		body.put("durationMinutes", durationMinutes);
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				body.put("taskId", t.getId());
				
				webTClient.post()
				.uri(baseUri)
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.name").isEqualTo(name)
				.jsonPath("$.summary").isEqualTo(summary)
				.jsonPath("$.durationMinutes").isEqualTo(durationMinutes != null ? durationMinutes : 0);
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
				"'', 'Subtask summary', 5",
				"null, 'Subtask summary', 5",
				"'Subtask name', 'Subtask summary', -1",
			}
	)
	void failedCreateWithInvalidBody(String name, String summary, Integer durationMinutes) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("summary", summary);
		body.put("durationMinutes", durationMinutes);
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				body.put("taskId", t.getId());
				
				webTClient.post()
				.uri(baseUri)
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();
			});
		});
	}
	
	@Test
	void failedCreateWithNotFoundTask() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", "Subtask name");
		body.put("summary", "Subtask summary");
		body.put("durationMinutes", 5);
		body.put("taskId", Integer.MAX_VALUE);
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.post()
				.uri(baseUri)
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isNotFound();
			});
		});
	}
	
	@Test
	void unauthorizedCreate() {
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String token = at.getTokens().getRefreshToken().getToken();
				
				webTClient.post()
				.uri(baseUri)
				.headers(h -> h.setBearerAuth(token))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(Map.of())
				.exchange()
				.expectStatus().isUnauthorized();
			});
		});
	}
	
	@Test
	void delete() {
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.delete()
				.uri(uri -> uri
						.path(baseUri)
						.queryParam("subtaskIds", st.getId())
						.build())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\""+ st.getVersion() +"\"");
				})
				.exchange()
				.expectStatus().isNoContent();
			});
		});
	}
	
	@Test
	void failedDeleteWithNotFound() {
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.delete()
				.uri(uri -> uri
						.path(baseUri)
						.queryParam("subtaskIds", st.getId()+100)
						.build())
				.headers(h -> h.setBearerAuth(accessToken))
				.exchange()
				.expectStatus().isNotFound();
			});
		});
	}
	
	@Test
	void failedDeleteWithIfMatch() {
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.delete()
				.uri(uri -> uri
						.path(baseUri)
						.queryParam("subtaskIds", st.getId())
						.build())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\""+ st.getVersion()+1 +"\"");
				})
				.exchange()
				.expectStatus().isEqualTo(412);
			});
		});
	}
	
	@Test
	void unauthorizedDelete() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String token = at.getTokens().getRefreshToken().getToken();
				
				webTClient.delete()
				.uri(baseUri, st.getId())
				.headers(h -> h.setBearerAuth(token))
				.exchange()
				.expectStatus().isUnauthorized();
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
				"'New subtask name', 'New subtask summary', 5",
				"null, 'New subtask summary', 5",
				"'New subtask name', null, 5",
				"'New subtask name', 'New subtask summary', null",
			}
	)
	void update(String name, String summary, Integer durationMinutes) {
		String baseUri = this.baseUri + "/{id}";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("summary", summary);
		body.put("durationMinutes", durationMinutes);
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				BodyContentSpec bodyContent = webTClient.patch()
						.uri(baseUri, st.getId())
						.headers(h -> {
							h.setBearerAuth(accessToken);
							h.setIfMatch("\""+ st.getVersion() +"\"");
						})
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(body)
						.exchange()
						.expectStatus().isOk()
						.expectBody();
				
				if(name != null && !name.isBlank()) {
					bodyContent.jsonPath("$.name").isEqualTo(name);
				}
				if(summary != null && !summary.isBlank()) {
					bodyContent.jsonPath("$.summary").isEqualTo(summary);
				}
				if(durationMinutes != null) {
					bodyContent.jsonPath("$.durationMinutes").isEqualTo(durationMinutes);
				}
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
				"'', 'New subtask summary', 5",
				"'New subtask name', '', 5",
				"'New subtask name', 'New subtask summary', -1",
			}
	)
	void failedUpdateWithInvalidBody(String name, String summary, Integer durationMinutes) {
		String baseUri = this.baseUri + "/{id}";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("summary", summary);
		body.put("durationMinutes", durationMinutes);
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.patch()
				.uri(baseUri, st.getId())
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();
			});
		});
	}
	
	@Test
	void failedUpdateWithNotFound() {
		String baseUri = this.baseUri + "/{id}";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", "New subtask name");
		body.put("summary", "New subtask summary");
		body.put("durationMinutes", 1);
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.patch()
			.uri(baseUri, Integer.MAX_VALUE)
			.headers(h -> h.setBearerAuth(accessToken))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNotFound();
		});
	}
	
	@Test
	void failedUpdateWithIfMatch() {
		String baseUri = this.baseUri + "/{id}";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", "New subtask name");
		body.put("summary", "New subtask summary");
		body.put("durationMinutes", 1);
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.patch()
				.uri(baseUri, st.getId())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\""+ st.getVersion()+1 +"\"");
				})
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isEqualTo(412);
			});
		});
	}
	
	@Test
	void unauthorizedUpdate() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String token = at.getTokens().getRefreshToken().getToken();
				
				webTClient.patch()
				.uri(baseUri, st.getId()+100)
				.headers(h -> h.setBearerAuth(token))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(Map.of())
				.exchange()
				.expectStatus().isUnauthorized();
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource({
		"50, 25, 40, AFTER",
		"50, 25, 10, AFTER",
		"50, 25, 24, AFTER",
		"50, 25, 0, AFTER",
		"50, 25, 49, AFTER",
		"50, 0, 1, AFTER",
		"50, 25, 10, BEFORE",
		"50, 25, 40, BEFORE",
		"50, 25, 26, BEFORE",
		"50, 25, 0, BEFORE",
		"50, 49, 48, BEFORE",
	})
	void updatePosition(int quantityOfSubtasks, int targetSubtaskIndex, int targetSubtaskAnchorIndex, String moveType) {
		String baseUri = this.baseUri + "/{id}/position";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("moveType", moveType);
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), quantityOfSubtasks, sts -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				List<Subtask> subtaskList = sts.stream()
						.sorted(
								(st1, st2) -> st1.getPosition().compareTo(st2.getPosition())
								)
						.toList();
				
				Subtask targetSubtask = subtaskList.get(targetSubtaskIndex);
				Subtask targetSubtaskAnchor = subtaskList.get(targetSubtaskAnchorIndex);
				
				body.put("anchorSubtaskId", targetSubtaskAnchor.getId());
				
				webTClient.patch()
				.uri(baseUri, targetSubtask.getId())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setETag("\""+ targetSubtask.getVersion() +"\"");
				})
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isNoContent();
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource({
		"0, 40, AFTER",
		"25, 0, AFTER",
		"25, 40, AFTERER",
		"25, 40, BEFORER",
	})
	void failedUpdatePositionWithInvalidBody(int subtaskId, int subtaskAnchorId, String moveType) {
		String baseUri = this.baseUri + "/{id}/position";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("moveType", moveType);
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), 50, sts -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				body.put("anchorSubtaskId", subtaskAnchorId);
				
				webTClient.patch()
				.uri(baseUri, subtaskId)
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();
			});
		});
	}
	
	@Test
	void failedUpdatePositionWithNotFound() {
		String baseUri = this.baseUri + "/{id}/position";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("moveType", MovePositionType.AFTER);
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				body.put("anchorSubtaskId", st.getId());
				
				webTClient.patch()
				.uri(baseUri, Integer.MAX_VALUE)
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isNotFound();
			});
		});
	}
	
	@Test
	void failedUpdatePositionWithNotFoundSubtaskAnchor() {
		String baseUri = this.baseUri + "/{id}/position";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("moveType", MovePositionType.AFTER);
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				body.put("anchorSubtaskId", Integer.MAX_VALUE);
				
				webTClient.patch()
				.uri(baseUri, st.getId())
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isNotFound();
			});
		});
	}
	
	@Test
	void failedUpdatePositionWithIfMatch() {
		String baseUri = this.baseUri + "/{id}/position";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("moveType", MovePositionType.AFTER);
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				body.put("anchorSubtaskId", st.getId());
				
				webTClient.patch()
				.uri(baseUri, st.getId())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\""+st.getVersion()+1+"\"");
				})
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isEqualTo(412);
			});
		});
	}
	
	@Test
	void unauthorizedUpdatePosition() {
		String baseUri = this.baseUri + "/{id}/position";
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String token = at.getTokens().getRefreshToken().getToken();
				
				webTClient.patch()
				.uri(baseUri, st.getId())
				.headers(h -> h.setBearerAuth(token))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(Map.of())
				.exchange()
				.expectStatus().isUnauthorized();
			});
		});
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"PENDING", "IN_PROGRESS", "COMPLETED"})
	void updateStatus(String status) {
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), 50, sts -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				Set<Integer> ids = sts.stream()
						.map(Subtask::getId)
						.collect(Collectors.toSet());
				
				long sumVersions = sts.stream()
						.map(Subtask::getVersion)
						.reduce(0L, Long::sum);
				
				webTClient.patch()
				.uri(uri -> uri
						.path(baseUri)
						.queryParam("subtaskIds", ids)
						.queryParam("status", status)
						.build())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\""+sumVersions+"\"");
				})
				.exchange()
				.expectStatus().isNoContent();
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
					"PENDINGS", "IN_PROGRESSKS", "COMPLETEDDLE", "null"
			}
	)
	void failedUpdateStatusWithInvalidValue(String status) {
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.patch()
				.uri(uri -> uri
						.path(baseUri)
						.queryParam("subtaskIds", st.getId())
						.queryParam("status", status)
						.build())
				.headers(h -> h.setBearerAuth(accessToken))
				.exchange()
				.expectStatus().isBadRequest();
			});
		});
	}
	
	@Test
	void failedUpdateStatusWithNotFound() {
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			Set<Integer> ids = Set.of(1, 2, 4, 8, 16, 32, 64, 3);
			
			webTClient.patch()
			.uri(uri -> uri
					.path(baseUri)
					.queryParam("subtaskIds", ids)
					.queryParam("status", "COMPLETED")
					.build())
			.headers(h -> h.setBearerAuth(accessToken))
			.exchange()
			.expectStatus().isNotFound();
		});
	}
	
	@Test
	void failedUpdateStatusWithIfMatch() {
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.patch()
				.uri(uri -> uri
						.path(baseUri)
						.queryParam("subtaskIds", st.getId())
						.queryParam("status", "COMPLETED")
						.build())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\""+st.getVersion()+1+"\"");
				})
				.exchange()
				.expectStatus().isEqualTo(412);
			});
		});
	}
	
	@Test
	void unauthorizedUpdateStatus() {
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String token = at.getTokens().getRefreshToken().getToken();
				
				webTClient.patch()
				.uri(uri -> uri
						.path(baseUri)
						.queryParam("subtaskIds", st.getId())
						.queryParam("status", "COMPLETED")
						.build())
				.headers(h -> h.setBearerAuth(token))
				.exchange()
				.expectStatus().isUnauthorized();
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource({
		"0, 50, 'createdAt,DESC', ''",
		"1, 50, 'createdAt,ASC', ''",
		"1, 50, 'createdAt,ASC', 'name, summary, position, durationMinutes, fakeField',",
	})
	void findAll(int page, int size, String sort, String fields) {
		Set<String> splitFields = Arrays.stream(fields.split(","))
				.filter(s -> !s.isBlank())
				.map(String::trim)
				.collect(Collectors.toSet());
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), size*2, sts -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				int taskId = List.copyOf(sts).get(0).getTaskId();
				
				BodyContentSpec bodySpec = webTClient.get()
						.uri(uri -> uri.path(baseUri)
								.queryParam("taskId", taskId)
								.queryParam("page", page)
								.queryParam("size", size)
								.queryParam("sort", sort)
								.queryParam("fields", splitFields)
								.build())
						.headers(h -> h.setBearerAuth(accessToken))
						.exchange()
						.expectStatus().isOk()
						.expectBody();
				
				bodySpec
				.jsonPath("$[0].createdAt").isNotEmpty()
				.jsonPath("$[0].updatedAt").isNotEmpty()
				.jsonPath("$[0].version").isNotEmpty()
				.jsonPath("$.length()").isEqualTo(size);
				
				if(splitFields.size() == 0) {
					bodySpec
					.jsonPath("$[0].name").isNotEmpty()
					.jsonPath("$[0].summary").isNotEmpty()
					.jsonPath("$[0].status").isNotEmpty()
					.jsonPath("$[0].position").isNumber()
					.jsonPath("$[0].durationMinutes").isNumber();
				}else {
					splitFields.stream()
						.filter(TaskSummary.FIELDS::contains)
						.forEach(f -> 
							bodySpec
							.jsonPath("$[0].%s".formatted(f))
							.isNotEmpty()
						);
				}
			});
		});
	}
	
	@Test
	void findAllWithIfNoneMatch() {
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), 50, sts -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				int taskId = List.copyOf(sts).get(0).getTaskId();
				
				long sumVersions = sts.stream()
						.map(Subtask::getVersion)
						.reduce(0L, Long::sum);
				
				webTClient.get()
				.uri(uri -> uri.path(baseUri)
						.queryParam("taskId", taskId)
						.queryParam("page", 0)
						.queryParam("size", 50)
						.queryParam("sort", "createdAt,DESC")
						.queryParam("fields", "")
						.build())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfNoneMatch("\""+ sumVersions +"\"");
				})
				.exchange()
				.expectStatus().isNotModified()
				.expectBody()
				.isEmpty();
			});
		});
	}
	
	@Test
	void unauthorizedFindAll() {
		authTestTool.runWithUniqueAuth(at -> {
			String token = at.getTokens().getRefreshToken().getToken();
			
			webTClient.get()
			.uri(uri -> uri.path(baseUri)
					.queryParam("page", 1)
					.queryParam("size", 50)
					.queryParam("sort", "createdAt,ASC")
					.queryParam("fields", "")
					.build())
			.headers(h -> h.setBearerAuth(token))
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
	@RepeatedTest(2)
	void findById(RepetitionInfo info) {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.get()
				.uri(baseUri, st.getId())
				.headers(h -> h.setBearerAuth(accessToken))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isNumber()
				.jsonPath("$.name").isNotEmpty()
				.jsonPath("$.summary").exists()
				.jsonPath("$.durationMinutes").isNumber()
				.jsonPath("$.position").isNumber()
				.jsonPath("$.status").isNotEmpty()
				.jsonPath("$.version").isNumber()
				.jsonPath("$.createdAt").isNotEmpty()
				.jsonPath("$.updatedAt").isNotEmpty();
			});
		});
	}
	
	@Test
	void findByIdWithIfNotMatch() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), st -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.get()
				.uri(baseUri, st.getId())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfNoneMatch("\"" + st.getVersion() + "\"");
				})
				.exchange()
				.expectStatus().isNotModified()
				.expectBody()
				.isEmpty();
			});
		});
	}
	
	@Test
	void failedFindByIdWithNotFound() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.get()
			.uri(baseUri, Integer.MAX_VALUE)
			.headers(h -> h.setBearerAuth(accessToken))
			.exchange()
			.expectStatus().isNotFound();
		});
	}
	
	@Test
	void unauthorizedFindByIdWithNotFound() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			String token = at.getTokens().getRefreshToken().getToken();
			
			webTClient.get()
			.uri(baseUri, Integer.MAX_VALUE)
			.headers(h -> h.setBearerAuth(token))
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
	@Test
	void count() {
		String baseUri = this.baseUri + "/count/{taskId}";
		
		authTestTool.runWithUniqueAuth(at -> {
			subtaskTestTool.runWithUser(at.getUser(), 50, sts -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				int taskId = List.copyOf(sts).get(0).getTaskId();
				
				webTClient.get()
				.uri(baseUri, taskId)
				.headers(h -> h.setBearerAuth(accessToken))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.name").isEqualTo("subtask")
				.jsonPath("$.total").isEqualTo(sts.size());
			});
		});
	}
	
	@Test
	void unauthorizedCount() {
		String baseUri = this.baseUri + "/count";
		
		authTestTool.runWithUniqueAuth(at -> {
			String token = at.getTokens().getRefreshToken().getToken();
			
			webTClient.get()
			.uri(baseUri)
			.headers(h -> h.setBearerAuth(token))
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
}
