package bio.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Job {
	private JobResult result;

	public Job() {
	}

	public Job(String job) {
		result = new JobResult();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				String output = "";
				result.updateState(JobState.JOB_STILL_BEING_PROCESSED);
				output = excuteCommand("ping -c 20 google.com");
				result.updateState(JobState.JOB_COMPLETE_SUCCESSFULLY);
				result.setResult(output);
			}
		});
		thread.start();
		result.updateState(JobState.JOB_STILL_BEING_PROCESSED);
		System.out.println("thread run.");
	}

	public void runJob() {
	}

	private String excuteCommand(String command) {
		StringBuffer result;
		Process process;

		result = new StringBuffer();

		try {
			process = Runtime.getRuntime().exec(command);

			System.out.println("exec " + process.toString());
			System.out.println("exit code:" + process.waitFor());

			BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			String line = "";
			while ((line = br.readLine()) != null) {
				result.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done");
		return result.toString();
	}

	public int getState() {
		System.out.println(result.toString());
		return result.getStateCode();
	}

	public String getResult() {
		return result.getResult();
	}
}
