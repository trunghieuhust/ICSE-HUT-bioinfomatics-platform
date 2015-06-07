package hust.icse.bio.tools;

import hust.icse.bio.dao.DAOFactory;
import hust.icse.bio.dao.PackageDAO;
import hust.icse.bio.infrastructure.StorageManagement;
import hust.icse.bio.infrastructure.User;
import hust.icse.bio.infrastructure.UserManagement;
import hust.icse.bio.infrastructure.VM;
import hust.icse.bio.infrastructure.VMmanagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ctc.wstx.io.BufferRecycler;
import com.google.common.io.Files;

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

	public String getImage(Tool tool) {

		return null;
	}

	public boolean deployToVM(Tool tool, VM vm) {
		Deployment deployment = Deployment.getDeployment(Deployment.UBUNTU);
		return deployment.deploy(tool, vm);
	}

	public boolean addToolPackage(File toolPackage, User user) {
		String filePath = toolPackage.getAbsolutePath();
		String metadataPath = executeCommand(
				"tar -xvf " + filePath + " "
						+ toolPackage.getName().replace(".bpm", "")
						+ "/metadata").trim();
		if (metadataPath.equals(toolPackage.getName().replace(".bpm", "")
				+ "/metadata")) {
			// extract success
			String medadata = null;
			try {
				medadata = new String(java.nio.file.Files.readAllBytes(Paths
						.get(metadataPath)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (medadata != null) {
				Package pack = parseMetadata(medadata);
				System.out.println(makeToolFlavor(pack, user));
				PackageDAO packageDAO = DAOFactory.getDAOFactory(
						DAOFactory.MYSQL).getPackageDAO();
				packageDAO.addPackage(user, pack);
				if (user.isAdmin()) {
					user.getStorageManagement().uploadFileFromPath(filePath,
							MAIN_REPOSITORY);
				} else {
					user.getStorageManagement().uploadFileFromPath(filePath,
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
		
		return false;
	}

	public static ToolManagement getInstance() {
		return instance;
	}

	public String getPackageName(Tool tool) {
		return tool.getPackageName() + "-" + tool.getVersion() + ".bpm";
	}

	public String getPackageLink(Tool tool) {
		String name = getPackageName(tool);
		String[] listPackage = StorageManagement.getAdminInstance().listFile(
				MAIN_REPOSITORY);
		// String name32bitVer = name + "_x86.bpm";
		// String name64bitVer = name + "_x64.bpm";
		// boolean has32Ver = false;
		for (int i = 0; i < listPackage.length; i++) {
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

	public static void main(String[] args) {
		User user = UserManagement.getInstance().authenticate("admin",
				"Bkcloud12@Icse@2015");
		// VM vm = user.getManager().launchInstance("test", "cloud-bio-v5",
		// "bio.mini");
		// Tool tool = DAOFactory.getDAOFactory(DAOFactory.MYSQL).getToolDAO()
		// .getTool(user, "clustal");
		// System.out.println(getInstance().deployToVM(tool, vm));
		File tool = new File("/tmp/clustalo-1.2.1.bpm");
		getInstance().addToolPackage(tool, user);
	}
}
