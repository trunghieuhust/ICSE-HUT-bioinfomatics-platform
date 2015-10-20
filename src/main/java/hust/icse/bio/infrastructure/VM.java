package hust.icse.bio.infrastructure;

import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.openstack.glance.v1_0.domain.Image;
import org.jclouds.openstack.glance.v1_0.domain.Image.Status;
import org.jclouds.openstack.glance.v1_0.domain.ImageDetails;
import org.jclouds.openstack.glance.v1_0.features.ImageApi;
import org.jclouds.openstack.glance.v1_0.options.UpdateImageOptions;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
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

	public String createSnapshot(String imageName) {
		ServerApi serverApi = context.novaApi
				.getServerApiForZone(context.defaultZone);
		String snapshotID = null;
		int timeout = 0;
		// TODO
		ImageApi imageApi = context.glanceApi
				.getImageApiForZone(context.defaultZone);
		snapshotID = serverApi.createImageFromServer(imageName, this.ID);
		if (snapshotID != null) {
			System.out.println("Waiting for Snapshot saving process......");
			while (imageApi.get(snapshotID).getStatus() != Status.ACTIVE) {
				if (timeout < 1000) {
					try {
						Thread.sleep(1000);
						timeout++;
					} catch (InterruptedException e) {
						System.out.println("Error Occured");
						return null;
					}
				} else {
					System.out.println("Creation timeout");
				}
			}
			System.out.println("Snapshot creation complete after " + timeout
					+ " retry!");
			imageApi.update(snapshotID, new UpdateImageOptions().isPublic(true));
		}
		return snapshotID;
	}

	public long getUpTime() {
		ServerApi serverApi = context.novaApi
				.getServerApiForZone(context.defaultZone);
		Server server = serverApi.get(this.ID);
		long duration = (System.currentTimeMillis() - server.getCreated()
				.getTime());
		return duration;
	}

	public void runInitScript() {
		System.err.println("runInitScript:VM ID: " + ID);
		// executeCommand("wget " + CloudConfig.initScriptLink);
		executeCommand("chmod u+x cloudfuse-config.sh");
		executeCommand("sed -i '1s/^/password=\"" + context.userPassword
				+ "\"\\n/' cloudfuse-config.sh");
		executeCommand("sed -i '1s/^/tenant=\""
				+ CloudConfig.bioServiceTenantName
				+ "\"\\n/' cloudfuse-config.sh");
		executeCommand("sed -i '1s/^/user=\"" + context.username
				+ "\"\\n/' cloudfuse-config.sh");
		System.out.println(executeCommand("sh cloudfuse-config.sh"));

	}
}
