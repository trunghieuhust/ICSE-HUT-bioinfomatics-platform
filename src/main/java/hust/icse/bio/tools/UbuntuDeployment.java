package hust.icse.bio.tools;

import java.util.List;

import hust.icse.bio.dao.DAOFactory;
import hust.icse.bio.dao.PackageDAO;
import hust.icse.bio.infrastructure.CloudConfig;
import hust.icse.bio.infrastructure.User;
import hust.icse.bio.infrastructure.UserManagement;
import hust.icse.bio.infrastructure.VM;

public class UbuntuDeployment extends Deployment {

	@Override
	public boolean deploy(Tool tool, VM vm, User user) {
		if (!isCommandAvailable(tool.getName(), vm)) {
			// command not available. deploy
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
			// TODO get file from swift
			System.out.println("get package from swift.");
			String wget = "wget "
					+ ToolManagement.getInstance().getPackageLink(tool, user);
			System.out.println(vm.executeCommand(wget));
			// extract package
			System.out.println("extracting...");
			String extractResult = vm.executeCommand("tar -xf "
					+ ToolManagement.getInstance().getPackageName(tool)
					+ ".bpm");
			if (extractResult.trim().length() == 0) {
				// no error, continue. Add binary file to PATH
				System.out.println("No error. Copy binary file to /usr/");
				String copyToBin = "sudo cp -R /home/ubuntu/"
						+ ToolManagement.getInstance().getPackageName(tool)
						+ "/bin /usr/";
				vm.executeCommand(copyToBin);
				// install depends
				String lib = vm.executeCommand("sudo apt-get install "
						+ toolpack.getDepends() + " -y");
				System.out.println(lib);
				if (isCommandAvailable(tool.getName(), vm)) {
					// command available. run post script
					vm.executeCommand("bash /home/ubuntu/"
							+ ToolManagement.getInstance().getPackageName(tool)
							+ "install");
				} else {
					// no command found, exit.
					System.err.println(extractResult);
					return false;
				}
			} else {
				// extract error, exit
				return false;
			}
			//
			return true;

		} else {
			// command available, then exit
			return true;
		}
	}

	@Override
	public boolean isCommandAvailable(String command, VM vm) {
		String commandLink = vm.executeCommand("command -v " + command);
		if (commandLink.trim().length() == 0) {
			return false;
		} else {
			return true;
		}
	}

	public static void main(String[] args) {
		User user = UserManagement.getInstance().authenticate("ducdmk55",
				"ducdmk55@123");
		VM vm = user.getManager().launchInstance("test",
				CloudConfig.ubuntuImage, "bio.mini");
		Tool tool = DAOFactory.getDAOFactory(DAOFactory.MYSQL).getToolDAO()
				.getTool(user, "clustal");
		Deployment UbuntuDeployment = Deployment.getDeployment(UBUNTU);
		boolean result = UbuntuDeployment.deploy(tool, vm, user);
		System.out.println(result);
		// user.getManager().terminateInstance(vm);
	}
}
