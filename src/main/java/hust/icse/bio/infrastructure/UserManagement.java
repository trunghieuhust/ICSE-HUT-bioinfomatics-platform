package hust.icse.bio.infrastructure;

import java.util.Calendar;
import java.util.NoSuchElementException;

import org.jclouds.ContextBuilder;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.extensions.TenantAdminApi;
import org.jclouds.openstack.keystone.v2_0.extensions.UserAdminApi;
import org.jclouds.openstack.keystone.v2_0.features.TenantApi;
import org.jclouds.openstack.keystone.v2_0.features.UserApi;
import org.jclouds.openstack.keystone.v2_0.options.CreateUserOptions;
import org.jclouds.rest.AuthorizationException;

import com.google.common.base.Optional;

public class UserManagement {

	private final KeystoneApi keystoneApi;
	private static UserManagement instance = new UserManagement();

	public UserManagement() {
		keystoneApi = ContextBuilder
				.newBuilder(CloudConfig.keystoneProvider)
				.endpoint(CloudConfig.adminServiceEndpoint)
				.credentials(CloudConfig.adminIdentity,
						CloudConfig.adminCredentials)
				.buildApi(KeystoneApi.class);
	}

	/**
	 * @param username
	 * @param password
	 * @return User object if authentication successful, null if failed.
	 * 
	 */
	public User authenticate(String username, String password) {
		Optional<? extends UserApi> userApis = keystoneApi.getUserApi();
		Optional<? extends TenantApi> tenantApis = keystoneApi.getTenantApi();
		if (userApis.isPresent() && tenantApis.isPresent()) {
			UserApi userApi = userApis.get();
			org.jclouds.openstack.keystone.v2_0.domain.User openstackUser = userApi
					.getByName(username);

			if (openstackUser != null) {
				String userIdentity = CloudConfig.bioServiceTenantName + ":"
						+ username;
				System.out.println(userIdentity);
				try {
					KeystoneApi userKeystoneApi = ContextBuilder
							.newBuilder(CloudConfig.keystoneProvider)
							.endpoint(CloudConfig.endpoint)
							.credentials(userIdentity, password)
							.buildApi(KeystoneApi.class);
					if (userKeystoneApi.getExtensionApi().list().isEmpty()) {
						User user = new User(username, password);
						user.setUserID(openstackUser.getId());
						return user;
					} else {
						User user = new User(username, password);
						user.setUserID(openstackUser.getId());
						return user;
					}
				} catch (NoSuchElementException e) {
					System.out.println("User exist but password is null!");
					return null;
				} catch (AuthorizationException e1) {
					System.out.println("Wrong password!");
					return null;
				}
			} else
				System.out.println("User does not exist!");
			return null;
		} else
			return null;
	}

	public boolean createUser(String username, String password) {
		UserAdminApi userAdminApi = this.keystoneApi.getUserAdminApi().get();
		TenantAdminApi tenantAdminApi = this.keystoneApi.getTenantAdminApi()
				.get();
		// Create user in Bio tenant and Member Role
		CreateUserOptions option = new CreateUserOptions().tenant(
				CloudConfig.bioServiceTenantName).enabled(true);

		if (!isUserExist(username)) {
			org.jclouds.openstack.keystone.v2_0.domain.User user = userAdminApi
					.create(username, password, option);
			tenantAdminApi.addRoleOnTenant(CloudConfig.bioServiceTenantID,
					user.getId(), CloudConfig.memberRoleID);
			System.out.println("User ID: " + user.getId());

			// Create keypair and upload to swift
			User createdUser = new User(username, password);
			VMmanagement vmManager = createdUser.getManager();
			if (vmManager.generateKeypair(username)) {
				StorageManagement.getAdminInstance().uploadFileFromPath(
						"/tmp/" + username + ".pem",
						CloudConfig.keypairContainer);
				createdUser.setKeypair();
				// Create user-upload container
				createdUser.getStorageManagement().createContainer(
						username + "-upload");
				return true;
			} else
				return false;

		}

		else {
			System.out.println("User is already exist!");
			return false;
		}

	}

	public boolean deleteUser(User user) {
		if (!isUserExist(user.getUsername()))
			return false;
		else {
			UserAdminApi userAdminApi = this.keystoneApi.getUserAdminApi()
					.get();
			UserApi userApi = this.keystoneApi.getUserApi().get();
			String userID = userApi.getByName(user.getUsername()).getId();
			if (userAdminApi.delete(userID)) {
				user.getStorageManagement().deleteContainer(
						user.getUsername() + "-upload");
				StorageManagement.getAdminInstance().deleteFile(
						user.getUsername() + ".pem", "keypair");
				return true;
			} else
				return false;
		}

	}

	public boolean isUserExist(String username) {
		UserApi userApi = this.keystoneApi.getUserApi().get();
		if (userApi.getByName(username) != null) {
			return true;
		} else
			return false;
	}

	public static UserManagement getInstance() {
		return instance;
	}

	public static void main(String[] args) {
		User user = UserManagement.getInstance().authenticate("ducdmk55",
				"ducdmk55@123");
		long startTime = System.currentTimeMillis();
		VM vm = user.getManager().launchInstance("test01",
				CloudConfig.ubuntuImage, "m1.small");
		long endTime = System.currentTimeMillis();
		System.err.println("VM Creation time:" + (endTime - startTime) / 1000);
		startTime = System.currentTimeMillis();
		String snapshotID = vm.createSnapshot("testSnapshot");
		endTime = System.currentTimeMillis();
		System.err.println("Snapshots " + snapshotID + " Creation time:"
				+ (endTime - startTime) / 1000);
		user.getManager().terminateInstance(vm);
	}
}