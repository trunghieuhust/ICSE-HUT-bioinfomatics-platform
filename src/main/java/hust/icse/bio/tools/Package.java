package hust.icse.bio.tools;

public class Package {
	private String packageName;
	private String version;
	private String providers;
	private String architecture;
	private long installedSize;
	private String flavor;
	private String userID;
	private String packageLink;
	private String Depends;

	public Package() {
	}

	public String getArchitecture() {
		return architecture;
	}

	public String getFlavor() {
		return flavor;
	}

	public long getInstalledSize() {
		return installedSize;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getProviders() {
		return providers;
	}

	public String getUserID() {
		return userID;
	}

	public String getVersion() {
		return version;
	}

	public String getPackageLink() {
		return packageLink;
	}

	public String getDepends() {
		return Depends;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public void setFlavor(String flavor) {
		this.flavor = flavor;
	}

	public void setInstalledSize(long installedSize) {
		this.installedSize = installedSize;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setProviders(String providers) {
		this.providers = providers;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setPackageLink(String packageLink) {
		this.packageLink = packageLink;
	}

	public void setDepends(String depends) {
		Depends = depends;
	}

	@Override
	public String toString() {
		return "\nPakage: " + getPackageName() + "\nVersion: " + getVersion()
				+ "\nProvider: " + getProviders() + "\nArchitecture: "
				+ getArchitecture() + "\nDepends: " + getDepends()
				+ "\nInstalled-size: " + getInstalledSize() + "\nFlavor: "
				+ getFlavor();
	}
}
