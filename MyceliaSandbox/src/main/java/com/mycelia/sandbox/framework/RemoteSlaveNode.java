package com.mycelia.sandbox.framework;

import java.io.Serializable;
import java.util.Set;

import com.mycelia.sandbox.exception.MyceliaRuntimeException;

public interface RemoteSlaveNode
{	
	/**
	 * Gets the different tasks this node can perform.
	 */
	public Set<Task> getTasks();
	
	/**
	 * Start execution of a specific task on this node.
	 * 
	 * @param task
	 * 			The task to execute.
	 * 
	 * @param parameter
	 * 			The task's parameters.
	 * 
	 * @return
	 * 			The task instance ID.
	 */
	public int startTask(Task task, Serializable... parameter) throws MyceliaRuntimeException;
	
	/**
	 * Gets a task's instance result, once the task is done execution. 
	 * 
	 * @param taskInstanceId
	 * 			Task's instance ID to get the result of.
	 * 
	 * @return
	 * 			The result of this specific task instance.
	 */
	public Object getTaskResult(int taskInstanceId);
	
	/**
	 * Get TaskInstance from task instance ID.
	 */
	public TaskInstance getTaskInstance(int taskInstanceId);
	
	/**
	 * Get the running task instances.
	 */
	public Set<TaskInstance> getRunningTaskInstance();
	
	/**
	 * Returns true if the specified task instance has finished executing.
	 */
	public boolean isTaskInstanceDone(int taskInstanceId);

	public String getNodeId();
}