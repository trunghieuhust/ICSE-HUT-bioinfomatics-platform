package bio.service;

import bio.vm.VM;

public class Job {
	private JobResult result;
	private User user;
	private VM vm;
	private static String image = "ubuntu-14.04-server-cloudimg-amd64";
	private static String flavor = "m1.small";

	public Job(final String jobID, User users) {
		result = new JobResult();
		this.user = users;
		vm = user.getManager().launchInstance(jobID, image, flavor);
		System.out.println("VM created");
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				result.updateState(JobState.JOB_STILL_BEING_PROCESSED);
				result.setResult(vm.executeCommand("curl www.google.com"));
				System.out.println(result.getResult());
				result.updateState(JobState.JOB_COMPLETE_SUCCESSFULLY);
				System.out.println("Done. Terminating VM...");
				user.getManager().terminateInstance(vm);
				System.out.println("VM terminated.");
			}
		});
		thread.start();
		System.out.println("thread run.");
	}

	public void runJob() {
	}

	// private String excuteCommand(String command) {
	// StringBuffer result;
	// Process process;
	//
	// result = new StringBuffer();
	//
	// try {
	// process = Runtime.getRuntime().exec(command);
	//
	// System.out.println("exec " + process.toString());
	// System.out.println("exit code:" + process.waitFor());
	//
	// BufferedReader br = new BufferedReader(new InputStreamReader(
	// process.getInputStream()));
	//
	// String line = "";
	// while ((line = br.readLine()) != null) {
	// result.append(line + "\n");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// System.out.println("done");
	// return result.toString();
	// }
	//
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
