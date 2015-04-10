package bio.service;
import java.util.NoSuchElementException;

import org.jclouds.ContextBuilder;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.domain.Role;
import org.jclouds.openstack.keystone.v2_0.extensions.RoleAdminApi;
import org.jclouds.openstack.keystone.v2_0.extensions.UserAdminApi;
import org.jclouds.openstack.keystone.v2_0.features.TenantApi;
import org.jclouds.openstack.keystone.v2_0.features.UserApi;
import org.jclouds.openstack.keystone.v2_0.options.CreateUserOptions;
import org.jclouds.rest.AuthorizationException;

import bio.vm.CloudConfig;

import com.google.common.base.Optional;

public class UserManagement {

	private KeystoneApi keystoneApi;
	private static UserManagement instance;

	public UserManagement() {
		// Iterable<Module> modules = ImmutableSet
		// .<Module> of(new SLF4JLoggingModule());
		keystoneApi = ContextBuilder
				.newBuilder(CloudConfig.keystoneProvider)
				.endpoint(CloudConfig.endpoint)
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

	// TODO Create user in openstack: not running
	public void createUser(String username, String password) {
		Optional<? extends UserAdminApi> userAdminApis = this.keystoneApi
				.getUserAdminApi();
		Optional<? extends RoleAdminApi> roleAdminApis = this.keystoneApi
				.getRoleAdminApi();
		// Create user in Bio tenant and Member Role

		if (userAdminApis.isPresent() && roleAdminApis.isPresent()) {
			UserAdminApi adminApi = userAdminApis.get();
			RoleAdminApi roleAdminApi = roleAdminApis.get();
			Role role = roleAdminApi.get(CloudConfig.memberRoleID);
			CreateUserOptions option = new CreateUserOptions();
			option.tenant(CloudConfig.bioServiceTenantID);
			option.enabled(true);
			org.jclouds.openstack.keystone.v2_0.domain.User user = adminApi
					.create(username, password, option);
			user.add(role);
			System.out.println("User ID: " + user.getId());
		} else if (!userAdminApis.isPresent()) {
			System.out.println("userAdminApis null");
		} else
			System.out.println("RoleAdimin Api null");
	}

	// TODO Delete user in openstack
	public void deleteUser() {

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
		User user = UserManagement.getInstance().login("ducdmk55",
				"ducdmk55@123");
		if (user != null) {
			System.out.println(user.getKeypair());
		}
		UserManagement.getInstance().createUser("dattsk55", "dattsk55@123");
	}
}
