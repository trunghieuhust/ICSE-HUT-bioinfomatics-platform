package hust.icse.bio.tools;

import hust.icse.bio.dao.DAOFactory;
import hust.icse.bio.infrastructure.CloudConfig;
import hust.icse.bio.infrastructure.User;
import hust.icse.bio.infrastructure.UserManagement;
import hust.icse.bio.infrastructure.VM;

public class UbuntuDeployment extends Deployment {

	@Override
	public boolean deploy(Tool tool, VM vm) {
		if (!isCommandAvailable(tool.getName(), vm)) {
			// command not available. deploy
			// TODO get file from swift
			System.out.println("T "
					+ ToolManagement.getInstance().getPackageLink(tool));
			String wget = "wget "
					+ ToolManagement.getInstance().getPackageLink(tool);

			System.out.println(vm.executeCommand(wget));
			// extract package
			String extractResult = vm.executeCommand("tar -xf "
					+ ToolManagement.getInstance().getPackageName(tool));
			System.out.println(extractResult.trim().length());
			if (extractResult.trim().length() == 0) {
				// no error, continue. Add binary file to PATH
				String copyToBin = "sudo cp -R /home/ubuntu/"
						+ ToolManagement.getInstance().getPackageName(tool)
						+ "/bin /usr/";
				System.out.println(copyToBin);
				vm.executeCommand(copyToBin);
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
		boolean result = UbuntuDeployment.deploy(tool, vm);
		System.out.println(result);
		// user.getManager().terminateInstance(vm);
	}
}
