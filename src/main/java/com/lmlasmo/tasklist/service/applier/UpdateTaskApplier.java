package com.lmlasmo.tasklist.service.applier;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.model.Task;

public interface UpdateTaskApplier {

	public static void apply(UpdateTaskDTO update, Task task) {
		applyDescription(update, task);
		applyDeadline(update, task);
	}
	
	private static void applyDescription(UpdateTaskDTO update, Task task) {
		String name = update.getName();
		String summary = update.getSummary();
		
		if(name != null) {
			name = name.trim();
			
			if(!name.isBlank() && !task.getName().equals(name)) task.setName(name);			
		}
		
		if(summary != null) {
			summary = summary.trim();
			
			if(!task.getSummary().equals(summary)) task.setSummary(summary);;
		}
	}
	
	private static void applyDeadline(UpdateTaskDTO update, Task task) {
		OffsetDateTime deadline = update.getDeadline();
		String deadlineZone = update.getDeadlineZone();
		
		if(deadline != null) {
			ZoneId zoneId = (deadlineZone != null) ? ZoneId.of(deadlineZone) : ZoneId.of(task.getDeadlineZone());
			
			ZonedDateTime zonedDeadline = deadline.atZoneSameInstant(zoneId);
			
			task.setDeadline(zonedDeadline.toInstant());

			if(deadlineZone != null) task.setDeadlineZone(deadlineZone);
		}
	}
	
}
