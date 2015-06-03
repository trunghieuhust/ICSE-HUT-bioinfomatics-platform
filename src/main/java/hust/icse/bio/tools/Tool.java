package hust.icse.bio.tools;

public class Tool {
	private String alias;
	private String name;
	private String version;
	private String packageName;
	private String command;

	public Tool() {
	}

	public Tool(String alias, String name, String version, String packageName,
			String command) {
		super();
		this.alias = alias;
		this.name = name;
		this.version = version;
		this.packageName = packageName;
		this.command = command;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getAlias() {
		return alias;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getCommand() {
		return command;
	}

	@Override
	public String toString() {
		return "Tool:" + "\n\tAlias: " + alias + "\n\tName: " + name
				+ "\n\tVersion: " + version + "\n\tPackage: " + packageName
				+ "\n\tCommand:" + command;
	}
	// TODO: doc, ghi template tu db

}