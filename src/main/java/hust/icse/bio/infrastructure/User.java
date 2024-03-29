package hust.icse.bio.infrastructure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Files;

public class User {
	private String username;
	private String password;
	private String keypair;
	private String userID;
	private StorageManagement storage;
	private VMmanagement manager;
	private String userIdentity;
	private boolean isAdmin;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		if (!username.equals("admin")) {
			this.userIdentity = CloudConfig.bioServiceTenantName + ":"
					+ this.username;
			this.isAdmin = false;
		} else {
			this.userIdentity = "admin:" + this.username;
			this.isAdmin = true;
		}

		this.storage = new StorageManagement(this);
		this.setKeypair();
		this.manager = new VMmanagement(this);
	}

	public void setKeypair() {
		String keypairLink = StorageManagement.getAdminInstance().getFileLink(
				username + ".pem", CloudConfig.keypairContainer);
		try {
			this.keypair = Files.toString(StorageManagement.getAdminInstance()
					.getFile(keypairLink), StandardCharsets.UTF_8);
			System.out.println("keypair is set");
		} catch (IOException e) {
			System.out.println("Keypair not found");
		}
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public boolean isAdmin() {
		return this.isAdmin;
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

	public VMmanagement getManager() {
		return manager;
	}

	public StorageManagement getStorageManagement() {
		return storage;
	}
}
