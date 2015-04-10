package bio.service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import bio.vm.CloudConfig;
import bio.vm.StorageUtils;
import bio.vm.VMmanagement;

import com.google.common.io.Files;

public class User {
	private String username;
	private String password;
	private String keypair;
	private String userID;
	private StorageUtils storage;
	private VMmanagement manager;
	private String userIdentity;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.userIdentity = CloudConfig.bioServiceTenantName + ":"
				+ this.username;
		this.storage = new StorageUtils(this);
		this.setKeypair();
		this.manager = new VMmanagement(this);
	}

	private void setKeypair() {
		String keypairLink = this.storage.getFileLink(username + ".pem",
				CloudConfig.keypairContainer);

		try {
			this.keypair = Files.toString(storage.getFile(keypairLink),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getKeypair() {
		return keypair;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUserIdentity() {
		return userIdentity;
	}

	public static void main(String[] args) {
		User user = new User("ducdmk55", "ducdmk55@123");
		System.out.println(user.keypair);
	}

	public VMmanagement getManager() {
		return manager;
	}

	public StorageUtils getStorageUtils() {
		return storage;
	}
}
