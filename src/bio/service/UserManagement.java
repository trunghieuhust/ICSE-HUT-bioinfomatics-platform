package bio.service;

import java.util.NoSuchElementException;

import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.features.TenantApi;
import org.jclouds.openstack.keystone.v2_0.features.UserApi;
import org.jclouds.rest.AuthorizationException;
import bio.service.User;
import bio.vm.CloudConfig;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

public class UserManagement {

	private KeystoneApi keystoneApi;
	private static UserManagement instance;

	public UserManagement() {
		Iterable<Module> modules = ImmutableSet
				.<Module> of(new SLF4JLoggingModule());
		keystoneApi = ContextBuilder
				.newBuilder(CloudConfig.keystoneProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(CloudConfig.openstackIdentity,
						CloudConfig.openstackCredentials).modules(modules)
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
			org.jclouds.openstack.keystone.v2_0.domain.User user = userApi
					.getByName(username);

			if (user != null) {
				TenantApi tenantApi = tenantApis.get();
				String tenantName = tenantApi.get(user.getTenantId()).getName();
				String userIdentity = tenantName + ":" + username;

				try {
					KeystoneApi userKeystoneApi = ContextBuilder
							.newBuilder(CloudConfig.keystoneProvider)
							.endpoint(CloudConfig.endpoint)
							.credentials(userIdentity, password)
							.buildApi(KeystoneApi.class);
					if (userKeystoneApi.getExtensionApi().list().isEmpty()) {
						return new User(username, password, "keypair");
					} else
						return new User(username, password, "keypair");
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

	public static UserManagement getInstance() {
		if (instance == null) {
			instance = new UserManagement();
			return instance;
		} else {
			return instance;
		}
	}

}
