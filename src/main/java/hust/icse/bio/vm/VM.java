package hust.icse.bio.vm;

import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.ssh.SshClient;

import com.google.common.net.HostAndPort;

public class VM {
	private Context context;
	private String name;
	private String ID;
	private String floatingIP;

	public VM(Context context, String name, String ID, String floatingIP) {
		this.context = context;
		this.name = name;
		this.ID = ID;
		this.floatingIP = floatingIP;
	}

	public String getName() {
		return name;
	}

	public String getFloatingIP() {
		return floatingIP;
	}

	public String getID() {
		return ID;
	}

	public String executeCommand(String cmd) {
		HostAndPort targetServer = HostAndPort.fromParts(this.floatingIP, 22);

		try {
			SshClient sshClient = context.computeContext.utils()
					.getSshClientFactory()
					.create(targetServer, context.loginCredentials);
			sshClient.connect();
			ExecResponse respond = sshClient.exec(cmd);
			sshClient.disconnect();
			return respond.getOutput();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Authentication Fail");
			return ("ssh fail");
		}
	}

	public void runInitScript() {
		System.err.println("runInitScript:VM ID: " + ID);
		System.out
				.println(executeCommand("wget " + CloudConfig.initScriptLink));
		executeCommand("chmod u+x cloudfuse-config.sh");

		System.out.println("Executing command:" + "sed -i '1s/^/password=\""
				+ context.userPassword + "\"\\n/' cloudfuse-config.sh");
		executeCommand("sed -i '1s/^/password=\"" + context.userPassword
				+ "\"\\n/' cloudfuse-config.sh");

		System.out.println("Executing command:" + "sed -i '1s/^/tenant=\""
				+ CloudConfig.bioServiceTenantName
				+ "\"\\n/' cloudfuse-config.sh");
		executeCommand("sed -i '1s/^/tenant=\""
				+ CloudConfig.bioServiceTenantName
				+ "\"\\n/' cloudfuse-config.sh");

		System.out.println("Executing command:" + "sed -i '1s/^/user=\""
				+ context.username + "\"\\n/' cloudfuse-config.sh");
		executeCommand("sed -i '1s/^/user=\"" + context.username
				+ "\"\\n/' cloudfuse-config.sh");

		System.out.println(executeCommand("sh cloudfuse-config.sh"));

	}
}
