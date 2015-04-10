package bio.vm;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.ssh.SshClient;

import com.google.common.net.HostAndPort;

public class VM {
	private final Context context;
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
			return ("Authentication Fail!");
		}
	}

	public void runInitScript() {
		System.out
				.println(executeCommand("wget " + CloudConfig.initScriptLink));
		System.out.println(executeCommand("sh cloudfuse-config.sh"));
	}

}
