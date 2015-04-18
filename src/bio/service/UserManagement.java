package bio.service;

import java.util.NoSuchElementException;

import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.extensions.TenantAdminApi;
import org.jclouds.openstack.keystone.v2_0.extensions.UserAdminApi;
import org.jclouds.openstack.keystone.v2_0.features.TenantApi;
import org.jclouds.openstack.keystone.v2_0.features.UserApi;
import org.jclouds.openstack.keystone.v2_0.options.CreateUserOptions;
import org.jclouds.rest.AuthorizationException;

import bio.vm.CloudConfig;
import bio.vm.StorageManagement;
import bio.vm.VMmanagement;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

public class UserManagement {

	private final KeystoneApi keystoneApi;
	private static UserManagement instance;

	public UserManagement() {
		Iterable<Module> modules = ImmutableSet
				.<Module> of(new SLF4JLoggingModule());
		keystoneApi = ContextBuilder
				.newBuilder(CloudConfig.keystoneProvider)
				.endpoint(CloudConfig.adminServiceEndpoint)
				.credentials(CloudConfig.adminIdentity,
						CloudConfig.adminCredentials).modules(modules)
				.buildApi(KeystoneApi.class);
	}

	/**
	 * @param username
	 * @param password
	 * @return User object if authentication successful, null if failed.
	 * 
	 */
	public User login(String username, String password) {
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
						username + ".pem", CloudConfig.keypairContainer);
				createdUser.setKeypair();
				// Create user-upload container
				createdUser.getStorageUtils().createContainer(
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

	// TODO Delete user in openstack
	public void deleteUser() {

	}

	public boolean isUserExist(String username) {
		UserApi userApi = this.keystoneApi.getUserApi().get();
		if (userApi.getByName(username) != null) {
			return true;
		} else
			return false;
	}

	public static UserManagement getInstance() {
		if (instance == null) {
			instance = new UserManagement();
			return instance;
		} else {
			return instance;
		}
	}

	public static void main(String[] args) {
		// User user = UserManagement.getInstance().login("ducdmk55",
		// "ducdmk55@123");
		// if (user != null) {
		// System.out.println(user.getKeypair());
		// user.getManager().generateKeypair("abc");
		// }

		UserManagement manager = new UserManagement();
		if (manager.createUser("anhnnk55", "anhnnk55@123")) {
			System.out.println("sucess");
		} else
			System.out.println("fail");

		// UserManagement manager = new UserManagement();
		// System.out.println(manager.isUserExist("dacdmk55"));
	}
}