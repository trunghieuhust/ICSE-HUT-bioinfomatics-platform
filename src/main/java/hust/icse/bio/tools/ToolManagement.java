package hust.icse.bio.tools;

import hust.icse.bio.dao.DAOFactory;
import hust.icse.bio.dao.PackageDAO;
import hust.icse.bio.infrastructure.CloudConfig;
import hust.icse.bio.infrastructure.StorageManagement;
import hust.icse.bio.infrastructure.User;
import hust.icse.bio.infrastructure.UserManagement;
import hust.icse.bio.infrastructure.VM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;

public class ToolManagement {
	private static ToolManagement instance = new ToolManagement();
	private static String MAIN_REPOSITORY = "repository";

	public Tool getTool(User user, String alias) {
		Tool tool = DAOFactory.getDAOFactory(DAOFactory.MYSQL).getToolDAO()
				.getTool(user, alias);
		return tool;
	}

	public boolean isValidTool(Tool tool) {

		return false;
	}

	public String getImage(Tool tool, User user) {
		PackageDAO packageDAO = DAOFactory.getDAOFactory(DAOFactory.MYSQL)
				.getPackageDAO();
		List<Package> packs = packageDAO.getPackage(user,
				tool.getPackageName(), tool.getVersion());
		Package toolpack = null;
		for (Package pack : packs) {
			if (pack.isMainRepo() == false) {
				toolpack = pack;
				break;
			} else {
				toolpack = pack;
			}
		}
		System.out.println(toolpack.toString());
		if (toolpack.getDepends() == null
				|| toolpack.getDepends().trim().length() != 0) {
			// had depends-> had snapshot
			if (toolpack.isMainRepo()) {
				return toolpack.getPackageName() + "-" + toolpack.getVersion();
			} else {
				return user.getUsername() + "-" + toolpack.getPackageName()
						+ "-" + toolpack.getVersion();
			}
		} else {
			if (toolpack.getArchitecture().contains("amd64")) {
				return CloudConfig.BIO_IMAGE_X64;
			} else {
				return CloudConfig.BIO_IMAGE_X86;
			}
		}
	}

	public boolean deployToVM(Tool tool, VM vm, User user) {
		Deployment deployment = Deployment.getDeployment(Deployment.UBUNTU);
		return deployment.deploy(tool, vm, user);
	}

	public boolean addToolPackage(String fileName, User user) {

		String extract = "tar -xvf " + fileName + " "
				+ fileName.replace(".bpm", "") + "/metadata";
		System.out.println(extract);
		System.out.println(executeCommand("pwd"));
		String metadataPath = executeCommand(extract).trim();
		System.out.println(metadataPath);
		if (metadataPath.equals(fileName.replace(".bpm", "") + "/metadata")) {
			// extract success
			String medadata = null;
			try {
				medadata = new String(java.nio.file.Files.readAllBytes(Paths
						.get(metadataPath)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (medadata != null) {
				Package pack = parseMetadata(medadata);
				System.out.println(makeToolFlavor(pack, user));
				PackageDAO packageDAO = DAOFactory.getDAOFactory(
						DAOFactory.MYSQL).getPackageDAO();
				packageDAO.addPackage(user, pack);
				if (user.isAdmin()) {
					user.getStorageManagement().uploadFileFromPath(fileName,
							MAIN_REPOSITORY);
				} else {
					user.getStorageManagement().uploadFileFromPath(fileName,
							user.getStorageManagement().getPrivateRepository());
				}
				if (pack.getDepends().trim().length() != 0) {
					// had dependency. Create snapshot
					createSnapshot(user, pack);
				}
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean createSnapshot(User user, Package toolPackage) {
		String[] flavors = user.getManager().listFlavor();
		String flavor = "";
		String avaiFlavor = "";
		for (int i = 0; i < flavors.length; i++) {
			if (!user.isAdmin()) {
				if (flavors[i].contains(toolPackage.getPackageName())
						&& flavors[i].contains(user.getUsername())) {
					if (flavors[i].contains("small")) {
						flavor = flavors[i];
						break;
					}
					avaiFlavor = flavors[i];
				}
			} else {
				if (flavors[i].contains(toolPackage.getPackageName())) {
					if (flavors[i].contains("small")) {
						flavor = flavors[i];
						break;
					}

				}
			}
		}
		if (flavor.length() == 0) {
			flavor = avaiFlavor;
		}
		VM vm = user.getManager()
				.launchInstanceWithoutInitScript(toolPackage.getPackageName(),
						CloudConfig.BIO_IMAGE_X64, flavor);
		Tool tool = new Tool("", "", toolPackage.getVersion(),
				toolPackage.getPackageName(), "", false);
		System.out.println(tool.toString());
		ToolManagement.getInstance().deployToVM(tool, vm, user);
		if (user.isAdmin()) {
			vm.createSnapshot(toolPackage.getPackageName() + "-"
					+ toolPackage.getVersion());
		} else {
			vm.createSnapshot(user.getUsername() + "-"
					+ toolPackage.getPackageName() + "-"
					+ toolPackage.getVersion());

		}
		return false;
	}

	public static ToolManagement getInstance() {
		return instance;
	}

	public String getPackageName(Tool tool) {
		return tool.getPackageName() + "-" + tool.getVersion();
	}

	public String getPackageLink(Tool tool, User user) {
		String name = getPackageName(tool) + ".bpm";
		System.out.println(name);
		String[] listPrivatePackage = user.getStorageManagement().listFile(
				user.getStorageManagement().getPrivateRepository());
		if (listPrivatePackage != null && listPrivatePackage.length != 0) {
			for (int i = 0; i < listPrivatePackage.length; i++) {
				System.out.println(listPrivatePackage[i]);
				if (listPrivatePackage[i].equals(name)) {
					System.out.println(listPrivatePackage[i]);
					return user.getStorageManagement().getFileLink(name,
							user.getStorageManagement().getPrivateRepository());
				}
			}
		}

		String[] listPackage = StorageManagement.getAdminInstance().listFile(
				MAIN_REPOSITORY);
		// String name32bitVer = name + "_x86.bpm";
		// String name64bitVer = name + "_x64.bpm";
		// boolean has32Ver = false;
		for (int i = 0; i < listPackage.length; i++) {
			System.out.println(listPackage[i]);
			if (listPackage[i].equals(name)) {
				System.out.println(listPackage[i]);
				return StorageManagement.getAdminInstance().getFileLink(name,
						MAIN_REPOSITORY);
			}
		}
		return null;
	}

	private int makeToolFlavor(Package toolPack, User user) {
		String[] flavors = toolPack.getFlavor().split(" ");
		int done = 0;
		for (int i = 0; i < flavors.length; i++) {
			String flavorName = flavors[i].split(":")[0];
			String[] flavorDetails = flavors[i].split(":")[1].replace("{", "")
					.replace("}", "").trim().split(",");
			int vcpu = 0;
			int ram = 0;
			int disk = 0;
			for (int j = 0; j < flavorDetails.length; j++) {
				if (flavorDetails[j].contains("CPU")) {
					String value = flavorDetails[j].split("=")[1];
					vcpu = Integer.parseInt(value);
				}
				if (flavorDetails[j].contains("RAM")) {
					String value = flavorDetails[j].split("=")[1];
					ram = Integer.parseInt(value);
				}
				if (flavorDetails[j].contains("HDD")) {
					String value = flavorDetails[j].split("=")[1];
					disk = Integer.parseInt(value);
				}
			}
			boolean result = false;
			if (user.isAdmin()) {
				result = user.getManager().createFlavor(
						toolPack.getPackageName() + "-" + flavorName, vcpu,
						ram, disk);

			} else {
				result = user.getManager().createFlavor(
						user.getUsername() + "-" + toolPack.getPackageName()
								+ "-" + flavorName, vcpu, ram, disk);
			}
			if (result) {
				done++;
			}
		}
		return done;
	}

	private Package parseMetadata(String metadata) {
		Package pack = new Package();
		String[] line = metadata.split(System.getProperty("line.separator"));
		for (int i = 0; i < line.length; i++) {
			if (line[i].contains("Package")) {
				pack.setPackageName(line[i].replace("Package:", "").trim());
			}
			if (line[i].contains("Version")) {
				pack.setVersion(line[i].replace("Version:", "").trim());
			}
			if (line[i].contains("Provider")) {
				pack.setProviders(line[i].replace("Provider:", "").trim());
			}
			if (line[i].contains("Architecture")) {
				pack.setArchitecture(line[i].replace("Architecture:", "")
						.trim());
			}
			if (line[i].contains("Depends")) {
				pack.setDepends(line[i].replace("Depends:", "").trim());
			}
			if (line[i].contains("Installed-size")) {
				pack.setInstalledSize(Long.parseLong(line[i].replace(
						"Installed-size:", "").trim()));
			}
			if (line[i].contains("Flavor")) {
				pack.setFlavor(line[i].replace("Flavor:", "").trim());
			}
		}
		System.out.println(pack.toString());
		return pack;
	}

	private String executeCommand(String command) {
		StringBuffer sb = new StringBuffer();
		Process process;

		try {
			process = Runtime.getRuntime().exec(command);
			process.waitFor();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String output = "";

			while ((output = bufferedReader.readLine()) != null) {
				sb.append(output + "\n");
			}
		} catch (Exception e) {
		}
		return sb.toString();
	}

	public String getFlavor(Tool tool, User user, String flavorType) {
		String image = getImage(tool, user);
		String flavor = "small";
		String[] listFlavors = user.getManager().listFlavor();
		for (int i = 0; i < listFlavors.length; i++) {
			if (listFlavors[i].contains(tool.getPackageName() + "-"
					+ flavorType)) {
				if (listFlavors[i].contains(user.getUsername())) {
					return listFlavors[i];
				} else {
					flavor = listFlavors[i];
				}
			}
		}
		return flavor;
	}

	public static void main(String[] args) {
		User user = UserManagement.getInstance().authenticate("admin",
				"Bkcloud12@Icse@2015");
		// User user = UserManagement.getInstance().authenticate("ducdmk55",
		// "ducdmk55@123");

		// VM vm = user.getManager().launchInstance("test", "cloud-bio-v5",
		// "bio.mini");
		Tool tool = DAOFactory.getDAOFactory(DAOFactory.MYSQL).getToolDAO()
				.getTool(user, "clustal");
		// System.out.println(getInstance().deployToVM(tool, vm));
		// File tool = new File("clustalo-1.2.1.bpm");
		getInstance().addToolPackage("clustalo-1.2.1.bpm", user);
		// System.out.println(tool);
		// tool.setVersion("1.2.1");
		// System.out.println(getInstance().getFlavor(tool, user, "small"));
		// System.out.println(getInstance().getImage(tool, user));
		// Tool tool = new Tool("", "", "1.2.1",
		// "clustalo", "", false);
		// System.out.println(getInstance().getPackageLink(tool));

	}
}
