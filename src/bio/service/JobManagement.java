package bio.service;

import java.util.HashMap;
import java.util.UUID;

import bio.utils.UUIDGenerator;

public class JobManagement {
	private static HashMap<UUID, Job> jobManager;
	private static JobManagement instance;

	public JobManagement() {
		jobManager = new HashMap<UUID, Job>();
	}

	public static JobManagement getInstance() {
		if (instance == null) {
			instance = new JobManagement();
			return instance;
		} else {
			return instance;
		}
	}

	/**
	 * @return jobID
	 */
	public String createJob() {
		Job newJob = new Job("");
		UUID jobID = UUIDGenerator.nextUUID();
		jobManager.put(jobID, newJob);
		System.out.println("Job " + jobID.toString());
		return jobID.toString();
	}

	public String getJobResult(String jobID) {
		Job job = new Job();
		UUID uuid = UUIDGenerator.UUIDfromString(jobID);
		if (jobManager.containsKey(uuid)) {
			job = (Job) jobManager.get(uuid);
		}
		return job.getResult();
	}

	public int getJobState(String jobID) {
		Job job = null;
		UUID uuid = UUIDGenerator.UUIDfromString(jobID);
		System.out
				.println(this.instance.getClass().getName() + uuid.toString());
		if (jobManager.containsKey(uuid)) {
			job = (Job) jobManager.get(uuid);
			System.out.println(this.instance.getClass().getName()
					+ job.getState());
			return job.getState();
		} else {
			return -1;
		}
	}
}
