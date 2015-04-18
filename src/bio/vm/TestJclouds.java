package bio.vm;

import bio.service.User;
import bio.service.UserManagement;

public class TestJclouds {

	public static void main(String[] args) {
		User user = UserManagement.getInstance().login("ducdmk55",
				"ducdmk55@123");
		VMmanagement manager = user.getManager();
		StorageManagement storage = user.getStorageUtils();

		// manager.listServers();

		VM vm = manager.launchInstance("Ubuntu", "cloud-bio-v2", "m1.small");
		if (vm != null) {
			System.out.println(vm.getID());
			System.out.println(vm.executeCommand("curl www.vnexpress.net"));
			vm.runInitScript();
			System.out.println(vm.executeCommand("cat cloudfuse-config.sh"));
			manager.terminateInstance(vm);
		}
	}
}
