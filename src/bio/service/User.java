package bio.service;

public class User {
	private String username;
	private String password;
	private String keypair;

	public User(String username, String password, String keypair) {
		this.username = username;
		this.password = password;
		this.keypair = keypair;
	}
	public String getUsername(){
		return username;
	}
	public String getPassword(){
		return password;
	}

	public String getKeypair(){
		return keypair;
	}

}
