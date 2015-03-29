package bio.service;

public class TestJob {
	private final static String clustalCommand = "/home/hieu/bio/clustal/clustalo --infile=/home/hieu/bio/clustal/input -o /home/hieu/bio/clustal/clustal.aln --outfmt=clustal -v --force";

	public String submitJob(String tool, String[] data) {
		String jobID = "";
		jobID = HandlerRequest.getInstance().submit(tool, data);
		return jobID;
	}

	public int getStatus(String jobID) {
		System.out.println(jobID);
		return JobManagement.getInstance().getJobState(jobID);
	}

	public String getResult(String jobID) {
		return JobManagement.getInstance().getJobResult(jobID);
	}

	// public String excuteCommand(String command) {
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

	// public String clustal(String input) {
	// String output = "";
	// // output = excuteCommand(makeClustalCommand(input));
	// output = excuteCommand(clustalCommand);
	//
	// return output;
	// }
	//
	// private String[] makeClustalCommand(String input) {
	// String[] command = { "xterm", "-e",
	// "/home/hieu/bio/clustal/clustalo-1.2.0-Ubuntu-x86_64",
	// "--infile=/home/hieu/bio/clustal/input",
	// "-o /home/hieu/bio/clustal/clustal.aln", "--outfmt=clustal",
	// "-v" };
	// return command;
	// }
}