package bio.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.jclouds.domain.LoginCredentials;

import com.google.common.io.Files;

import bio.vm.VMmanagement;

public class Job {
	private JobResult result;
	private VMmanagement vm;

	public Job(final String jobID) {
		result = new JobResult();
		vm = new VMmanagement();

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				String output = "";
				result.updateState(JobState.JOB_STILL_BEING_PROCESSED);
				String serverIP = vm.launchInstance(jobID,
						"ubuntu-14.04-server-cloudimg-amd64", "m1.small",
						"jclouds-keypair");

				String privateKey = null;
				try { 
					privateKey = Files
							.toString(
									new File(
											"/home/hieu/bio/workspace/BioServiceProject/jclouds-keypair.pem"),
									StandardCharsets.UTF_8);
					LoginCredentials loginCredentials = new LoginCredentials.Builder()
							.user("ubuntu").privateKey(privateKey).build();
					result.setResult(vm.executeCommand(serverIP,
							loginCredentials, "curl www.google.com"));
					System.out.println(result.getResult());
					result.updateState(JobState.JOB_COMPLETE_SUCCESSFULLY);
					result.setResult(output);
					System.out.println("Done. Terminating VM...");
					vm.terminateInstancebyName(jobID);
					System.out.println("VM terminated.");
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});
		thread.start();
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

	public String getStateDescription() {
		return result.getStateDescription();
	}

	public String getResult() {
		return result.getResult();
	}
}
