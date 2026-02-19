package com.lmlasmo.tasklist.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.lmlasmo.tasklist.data.tool.TaskTestTool;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.TaskSummary;

@TestInstance(Lifecycle.PER_CLASS)
public class TaskControllerTest extends TaskListApplicationTests {
	
	@Autowired
	private WebTestClient webTClient;
	
	@Autowired
	private AuthTestTool authTestTool;
	
	@Autowired
	private TaskTestTool taskTestTool;
	
	private final String baseUri = "/api/task";

	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
					"'Task name', 'Task summary', '2030-02-09T12:30:00Z', 'UTC'",
					"'Task name', null, '2035-02-09T12:30:00Z', 'UTC'",
					"'Task name', 'Task summary', '2037-12-13T12:30:00Z', 'UTC'"
			}
	)
	void createTask(String name, String summary, String deadline, String timeZone) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("summary", summary);
		body.put("deadline", deadline);
		body.put("deadlineZone", timeZone);
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
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
			.jsonPath("$.deadline").value(t -> assertEquals(Instant.parse(deadline), Instant.parse(t.toString())))
			.jsonPath("$.deadlineZone").isEqualTo(timeZone);
		});	
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
					"'', 'Task summary', '2030-02-09T12:30:00Z', 'UTC'",
					"'    ', 'Task summary', '2030-02-09T12:30:00Z', 'UTC'",
					"null, 'Task summary', '2030-02-09T12:30:00Z', 'UTC'",
					"'Task name', 'Task summary', '', 'UTC'",
					"'Task name', 'Task summary', '   ', 'UTC'",
					"'Task name', 'Task summary', null, 'UTC'",
					"'Task name', 'Task summary', '2100-02-09T12:30:00Z', ''",
					"'Task name', 'Task summary', '2100-02-09T12:30:00Z', '  '",
					"'Task name', 'Task summary', '2100-02-09T12:30:00Z', null",		
			}
	)
	void failedCreateTaskWithInvalidBody(String name, String summary, String deadline, String timeZone) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("summary", summary);
		body.put("deadline", deadline);
		body.put("deadlineZone", timeZone);
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.post()
			.uri(baseUri)
			.headers(h -> h.setBearerAuth(accessToken))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest();
		});	
	}
	
	@Test
	void unauthorizedCreateTask() {
		authTestTool.runWithUniqueAuth(at -> {
			String token = at.getTokens().getRefreshToken().getToken();
			
			webTClient.post()
			.uri(baseUri)
			.headers(h -> h.setBearerAuth(token))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(Map.of())
			.exchange()
			.expectStatus().isUnauthorized();
		});	
	}
	
	@Test
	void deleteTask() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.delete()
				.uri(baseUri, t.getId())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\""+ t.getVersion() +"\"");
				})
				.exchange()
				.expectStatus().isNoContent();
			});
		});
	}
	
	@Test
	void failedDeleteTaskWithNotFound() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.delete()
			.uri(baseUri, Integer.MAX_VALUE)
			.headers(h -> h.setBearerAuth(accessToken))
			.exchange()
			.expectStatus().isNotFound();
		});
	}
	
	@Test
	void failedDeleteTaskWithIfMatch() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.delete()
				.uri(baseUri, t.getId())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\""+ t.getVersion()+1 +"\"");
				})
				.exchange()
				.expectStatus().isEqualTo(412);
			});
		});
	}
	
	@Test
	void unauthorizedDeleteTask() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			String token = at.getTokens().getRefreshToken().getToken();
			
			webTClient.delete()
			.uri(baseUri, Integer.MAX_VALUE)
			.headers(h -> h.setBearerAuth(token))
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
					"'Task name', null, null, null",
					"null, 'Task summary', null, null",
					"null, null, '2029-02-09T12:30:00Z', null",
					"null, null, '2031-02-09T13:30:00+01:00', 'Europe/Paris'",
			}
	)
	void updateTask(String name, String summary, String deadline, String timeZone) {
		String baseUri = this.baseUri + "/{id}";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("summary", summary);
		body.put("deadline", deadline);
		body.put("deadlineZone", timeZone);
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				BodyContentSpec resBodySpec = webTClient.patch()
						.uri(baseUri, t.getId())
						.headers(h -> {
							h.setBearerAuth(accessToken);
							h.setIfMatch("\"" + t.getVersion() + "\"");
						})
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(body)
						.exchange()
						.expectStatus().isOk()
						.expectBody();
				
				if(name != null) {
					resBodySpec.jsonPath("$.name").isEqualTo(name);
				}else if(summary != null) {
					resBodySpec.jsonPath("$.summary").isEqualTo(summary);
				}else if(deadline != null) {
					resBodySpec.jsonPath("$.deadline").isEqualTo(deadline);
					
					if(timeZone != null) {
						resBodySpec.jsonPath("$.deadlineZone").isEqualTo(timeZone);
					}
				}
			});
		});
	}
	
	@Test
	void failedUpdateTaskWithIfMatch() {
		String baseUri = this.baseUri + "/{id}";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", "New Task name");
		body.put("summary", "New Task summary");
		body.put("deadline", OffsetDateTime.now(ZoneId.of("Europe/Paris")).plusMinutes(30));
		body.put("deadlineZone", "Europe/Paris");
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.patch()
				.uri(baseUri, t.getId())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\""+ t.getVersion()+1 +"\"");
				})
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isEqualTo(412);
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
					"'   ', '    ', null, null",
			}
	)
	void failedUpdateTaskWithInvalidBody(String name, String summary, String deadline, String timeZone) {
		String baseUri = this.baseUri + "/{id}";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("summary", summary);
		body.put("deadline", deadline);
		body.put("deadlineZone", timeZone);
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.patch()
				.uri(baseUri, t.getId())
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
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.patch()
			.uri(baseUri, Integer.MAX_VALUE)
			.headers(h -> h.setBearerAuth(accessToken))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(Map.of())
			.exchange()
			.expectStatus().isNotFound();
		});
	}
	
	@Test
	void unauthorizedUpdateTask() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String token = at.getTokens().getRefreshToken().getToken();
				
				webTClient.patch()
				.uri(baseUri, t.getId())
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
	void updateTaskStatus(String status) {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.patch()
				.uri(uri -> uri.path(baseUri)
						.queryParam("status", status)
						.build(t.getId()))
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\"" + t.getVersion() + "\"");
				})
				.exchange()
				.expectStatus().isNoContent();
			});
		});
	}
	
	@Test
	void failedUpdateTaskStatusWithIfMatch() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.patch()
				.uri(uri -> uri.path(baseUri)
						.queryParam("status", TaskStatusType.COMPLETED)
						.build(t.getId()))
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfMatch("\""+ t.getVersion()+1 +"\"");
				})
				.exchange()
				.expectStatus().isEqualTo(412);
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {"PENDINGS", "''", "'   '", "null"}
	)
	void failedUpdateTaskStatusWithInvalidValue(String status) {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.patch()
				.uri(uri -> uri.path(baseUri)
						.queryParam("status", status)
						.build(t.getId()))
				.headers(h -> h.setBearerAuth(accessToken))
				.exchange()
				.expectStatus().isBadRequest();
			});
		});
	}
	
	@Test
	void failedUpdateTaskStatusWithNotFound() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.patch()
			.uri(uri -> uri.path(baseUri)
					.queryParam("status", TaskStatusType.COMPLETED)
					.build(Integer.MAX_VALUE))
			.headers(h -> h.setBearerAuth(accessToken))
			.exchange()
			.expectStatus().isNotFound();
		});
	}
	
	@Test
	void unauthorizedUpdateTaskStatus() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String token = at.getTokens().getRefreshToken().getToken();
				
				webTClient.patch()
				.uri(uri -> uri.path(baseUri)
						.queryParam("status", TaskStatusType.COMPLETED)
						.build(t.getId()))
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
		"1, 50, 'createdAt,ASC', 'name, summary, deadline, deadlineZone, fakeField',",
	})
	void findAll(int page, int size, String sort, String fields) {
		Set<String> splitFields = Arrays.stream(fields.split(","))
				.filter(s -> !s.isBlank())
				.map(String::trim)
				.collect(Collectors.toSet());
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), size*2, tns -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				BodyContentSpec bodySpec = webTClient.get()
						.uri(uri -> uri.path(baseUri)
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
					.jsonPath("$[0].deadline").isNotEmpty()
					.jsonPath("$[0].deadlineZone").isNotEmpty();
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
			taskTestTool.runWithUser(at.getUser(), 50, tns -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				long sumVersions = tns.stream()
						.map(Task::getVersion)
						.reduce(0L, Long::sum);
				
				webTClient.get()
				.uri(uri -> uri.path(baseUri)
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
					.queryParam("size", 59)
					.queryParam("sort", "createdAt,ASC")
					.queryParam("fields", "")
					.build())
			.headers(h -> h.setBearerAuth(token))
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
	@Test
	void findById() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.get()
				.uri(baseUri, t.getId())
				.headers(h -> h.setBearerAuth(accessToken))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isNotEmpty()
				.jsonPath("$.summary").isNotEmpty()
				.jsonPath("$.deadline").isNotEmpty()
				.jsonPath("$.deadlineZone").isNotEmpty()
				.jsonPath("$.status").isNotEmpty()
				.jsonPath("$.version").isNotEmpty()
				.jsonPath("$.createdAt").isNotEmpty()
				.jsonPath("$.updatedAt").isNotEmpty();
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
	void findByIdWithIfNoneMatch() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.get()
				.uri(baseUri, t.getId())
				.headers(h -> {
					h.setBearerAuth(accessToken);
					h.setIfNoneMatch("\""+ t.getVersion() +"\"");
				})
				.exchange()
				.expectStatus().isNotModified()
				.expectBody()
				.isEmpty();
			});
		});
	}
	
	@Test
	void unauthorizedFindById() {
		String baseUri = this.baseUri + "/{id}";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), t -> {
				String token = at.getTokens().getRefreshToken().getToken();
				
				webTClient.get()
				.uri(baseUri, t.getId())
				.headers(h -> h.setBearerAuth(token))
				.exchange()
				.expectStatus().isUnauthorized();
			});
		});
	}
	
	@Test
	void count() {
		String baseUri = this.baseUri + "/count";
		
		authTestTool.runWithUniqueAuth(at -> {
			taskTestTool.runWithUser(at.getUser(), 50, tns -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.get()
				.uri(baseUri)
				.headers(h -> h.setBearerAuth(accessToken))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.name").isEqualTo("task")
				.jsonPath("$.total").isEqualTo(tns.size());
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
