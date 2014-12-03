package com.mycelia.sandbox.framework;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.mycelia.sandbox.runtime.LoadBalancer;
import com.mycelia.sandbox.runtime.NodeContainer;

public abstract class MyceliaSlaveNode extends MyceliaNode
{
	public class TaskInstanceThread extends Thread
	{
		private Serializable result;
		private TaskInstance taskInstance;
		private Serializable[] parameters;
		
		public TaskInstanceThread(ThreadGroup threadGroup, TaskInstance taskInstance, Serializable[] parameters)
		{
			super(threadGroup, "Task Insance ID "+taskInstance.getInstanceId());
			
			this.taskInstance=taskInstance;
			this.parameters=parameters;
		}
		
		public TaskInstance getTaskInstance()
		{
			return taskInstance;
		}
		
		public Serializable getResult()
		{
			return result;
		}
		
		@Override
		public void run()
		{
			result=executeTask(taskInstance.getTask(), parameters); 
		}
	}
	
	private Map<Integer, TaskInstance> startedTasks;
	private Map<TaskInstance, TaskInstanceThread> startedThreads;
	private ThreadGroup threadGroup;
	private int lastTaskInstanceId;
	
	//Methods provided by Mycelia framework.
	
	public MyceliaSlaveNode()
	{
		startedTasks=new HashMap<Integer, TaskInstance>();
		startedThreads=new HashMap<TaskInstance, TaskInstanceThread>();
		lastTaskInstanceId=0;
	}
	
	@Override
	public final void setNodeContainer(NodeContainer nodeContainer)
	{
		//We just override this with "final" so children cant access the NodeContainer.
	}
	
	@Override
	public final void setLoadBalancer(LoadBalancer loadBalancer)
	{
		//We just override this with "final" so children cant access the LoadBalancer.
	}
	
	private int getNewTaskInstanceId()
	{
		lastTaskInstanceId++;
		
		return lastTaskInstanceId;
	}
	
	/**
	 * Start execution of a specific task on this node.
	 * 
	 * @param task
	 * 			The task to execute.
	 * 
	 * @param parameters
	 * 			The task's parameters.
	 * 
	 * @return
	 * 			The task instance ID.
	 */
	public synchronized final int startTask(Task task, Serializable[] parameters)
	{
		TaskInstance taskInstance=new TaskInstance();
		taskInstance.setNodeId(getNodeId());
		taskInstance.setInstanceId(getNewTaskInstanceId());
		taskInstance.setTask(task);
		
		TaskInstanceThread taskInstanceThread=new TaskInstanceThread(threadGroup, taskInstance, parameters);
		taskInstanceThread.start();
		
		startedTasks.put(taskInstance.getInstanceId(), taskInstance);
		startedThreads.put(taskInstance, taskInstanceThread);
		
		return taskInstance.getInstanceId();
	}
	
	/**
	 * Gets a task's instance result, once the task is done execution. 
	 * 
	 * @param taskInstanceId
	 * 			Task's instance ID to get the result of.
	 * 
	 * @return
	 * 			The result of this specific task instance
	 * 			or null of the task does not exists or is not done yet.
	 */
	public synchronized final Serializable getTaskResult(int taskInstanceId)
	{
		TaskInstance taskInstance=startedTasks.get(taskInstanceId);
		
		if(taskInstance==null)
			return null;
		
		TaskInstanceThread taskInstanceThread=startedThreads.get(taskInstance);
		
		if(taskInstanceThread.isAlive())
			return null;
		
		return taskInstanceThread.getResult();
	}
	
	/**
	 * Get the running task instances.
	 */
	public synchronized final Set<TaskInstance> getRunningTaskInstances()
	{
		Thread[] activeThreads=new Thread[threadGroup.activeCount()];
		Set<TaskInstance> tasks=new LinkedHashSet<TaskInstance>();
		
		threadGroup.enumerate(activeThreads);
		
		for(Thread thread: activeThreads)
			tasks.add(((TaskInstanceThread)thread).getTaskInstance());
		
		return tasks;
	}
	
	/**
	 * Return a TaskInstance object given the taskInstanceId.
	 */
	public synchronized final TaskInstance getTaskInstance(int taskInstanceId)
	{
		return startedTasks.get(taskInstanceId);
	}
	
	/**
	 * Returns true if the specified task instance has finished executing.
	 */
	public synchronized final boolean isTaskInstanceDone(int taskInstanceId)
	{
		TaskInstance taskInstance=startedTasks.get(taskInstanceId);
		
		if(taskInstance==null)
			return true;
		
		return !startedThreads.get(taskInstance).isAlive();
	}
	
	//Methods implemented/overriden by the end user.
	
	/**
	 * Life cycle <strong>nodeStart</strong> event: called just after this node has been created.  
	 */
	public void nodeStart()
	{
		threadGroup=new ThreadGroup("Slave Node ID "+getNodeId()+"'s Tasks");
	}
	
	/**
	 * Life cycle <strong>nodeStop</strong> event: called just before this node is deleted.  
	 */
	public abstract void nodeStop();
	
	/**
	 * Gets the different tasks this node can perform.
	 */
	public abstract Set<Task> getTasks();
	
	/**
	 * Executes a specific task on this node.
	 * 
	 * @param task
	 * 			The task to execute.
	 * 
	 * @param parameter
	 * 			The task's parameters.
	 * 
	 * @return
	 * 			The task's result.
	 */
	protected abstract Serializable executeTask(Task task, Serializable[] parameters);
}
