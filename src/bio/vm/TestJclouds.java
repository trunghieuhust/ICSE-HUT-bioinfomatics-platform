package bio.vm;

import bio.service.User;
import bio.service.UserManagement;

public class TestJclouds {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		User user = UserManagement.getInstance().login("ducdmk55",
				"ducdmk55@123");
		VMmanagement manager = user.getManager();
		StorageUtils storage = user.getStorageUtils();

		manager.listServers();
		VM vm = manager.launchInstance("Ubuntu", CloudConfig.ubuntuImage,
				"m1.small");
		if (vm != null) {
			System.out.println(vm.getID());
		}
	}
}
