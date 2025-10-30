package com.lmlasmo.tasklist.service.applier;

import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.model.Subtask;

public interface UpdateSubtaskApplier {

	public static void apply(UpdateSubtaskDTO update, Subtask subtask) {
		applyDescription(update, subtask);
		applyDuration(update, subtask);
	}
	
	private static void applyDescription(UpdateSubtaskDTO update, Subtask subtask) {
		String name = update.getName();
		String summary = update.getSummary();
		
		if(name != null) {
			name = name.trim();
			
			if(!name.isBlank() && !subtask.getName().equals(name)) subtask.setName(name);			
		}
		
		if(summary != null) {
			summary = summary.trim();
			
			if(!subtask.getSummary().equals(summary)) subtask.setSummary(summary);;
		}
	}
	
	private static void applyDuration(UpdateSubtaskDTO update, Subtask subtask) {
		Integer duration = update.getDurationMinutes();
		
		if(duration != null) subtask.setDurationMinutes(duration);
	}
	
}
