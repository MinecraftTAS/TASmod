package com.minecrafttas.tasmod.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Schedules a lambda to be run in the next tick
 * 
 * @author Scribble
 *
 */
public class TickScheduler {
	
	Queue<TickTask> queue = new ConcurrentLinkedQueue<>();
	
	public void runAllTasks() {
		TickTask task;
		while((task = queue.poll()) != null) {
			task.runTask();
		}
	}
	
	public void add(TickTask task) {
		queue.add(task);
	}
	
	@FunctionalInterface
	public interface TickTask {
		public void runTask();
	}
}
