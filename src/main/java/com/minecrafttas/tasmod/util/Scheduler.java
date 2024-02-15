package com.minecrafttas.tasmod.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A simple scheduling interface
 * 
 * @author Scribble
 */
public class Scheduler {
	
	Queue<Task> queue = new ConcurrentLinkedQueue<>();
	
	public void runAllTasks() {
		Task task;
		while((task = queue.poll()) != null) {
			task.runTask();
		}
	}
	
	public void add(Task task) {
		queue.add(task);
	}
	
	@FunctionalInterface
	public interface Task {
		public void runTask();
	}
}
