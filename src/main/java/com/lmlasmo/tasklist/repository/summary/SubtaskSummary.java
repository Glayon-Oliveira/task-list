package com.lmlasmo.tasklist.repository.summary;

public interface SubtaskSummary {
	
	public static interface IdPosition{		
		
		public int getId();
		public int getPosition();
		
	}
	
	public static interface IdStatusTask{
		public int getId();
		public int getStatus();
		public int getTaskId();
	}

}
