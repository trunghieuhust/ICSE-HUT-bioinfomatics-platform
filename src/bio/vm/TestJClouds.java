package bio.vm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jclouds.domain.LoginCredentials;

import com.google.common.io.Files;

public class TestJClouds {

	public static void main(String[] args) throws IOException {
		VMmanagement jcloudsNova = new VMmanagement();

		try {// list servers
			jcloudsNova.listServers();
			jcloudsNova.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jcloudsNova.close();
		}

		// laucnch instance

		try {
			String newServerName = "Ubuntu";
			String keypair = "jclouds-keypair";
			String iptoSsh = null;
			String keypairFilePath = "jclouds-keypair.pem";
			String user = "ubuntu";
			String newServerId = jcloudsNova.launchInstance(newServerName,
					"ubuntu-14.04-server-cloudimg-amd64", "m1.small", keypair);
			 System.out.println("New Server ID: " + newServerId);
			
			 // attach floating IP vao may ao
			 String floatingIP = jcloudsNova.getOrCreateFloatingIP();
			 System.out.println("Waiting for server booting....");
			 if (jcloudsNova.attachIP(floatingIP, newServerName) == true) {
			
			 System.out.println("New Server Floating IP:" + floatingIP);
			 } else
			 System.out.println("Cannot asscociate floating IP!");
			
//			 tao ssh dieu khien may ao

			iptoSsh = jcloudsNova.getFloatingIP(jcloudsNova
					.getServerId(newServerName));
			System.out.println("ubuntu:" + iptoSsh);

			if (iptoSsh != null) {
				String privateKey = null;
				privateKey = Files.toString(new File(keypairFilePath),
						StandardCharsets.UTF_8);
				LoginCredentials loginCredentials = new LoginCredentials.Builder()
						.user(user).privateKey(privateKey).build();
				System.out.println(jcloudsNova.excuteCommand(iptoSsh,
						loginCredentials, "curl www.google.com"));

			}
			jcloudsNova.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jcloudsNova.close();
		}
	}
}
