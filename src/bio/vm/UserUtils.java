package bio.vm;
import java.util.NoSuchElementException;

import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.domain.User;
import org.jclouds.openstack.keystone.v2_0.features.TenantApi;
import org.jclouds.openstack.keystone.v2_0.features.UserApi;
import org.jclouds.rest.AuthorizationException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

public class UserUtils {
	private KeystoneApi keystoneApi;

	public UserUtils() {
		Iterable<Module> modules = ImmutableSet
				.<Module> of(new SLF4JLoggingModule());
		keystoneApi = ContextBuilder
				.newBuilder(CloudConfig.keystoneProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(CloudConfig.openstackIdentity,
						CloudConfig.openstackCredentials).modules(modules)
				.buildApi(KeystoneApi.class);

	}

	public boolean isUserExist(String username, String password) {
		Optional<? extends UserApi> userApis = keystoneApi.getUserApi();
		Optional<? extends TenantApi> tenantApis = keystoneApi.getTenantApi();
		if (userApis.isPresent() && tenantApis.isPresent()) {
			UserApi userApi = userApis.get();
			User user = userApi.getByName(username);

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
						return true;
					} else
						return true;
				} catch (NoSuchElementException e) {
					System.out.println("User exist but password is null!");
					return false;
				} catch (AuthorizationException e1) {
					System.out.println("Wrong password!");
					return false;
				}
			} else
				System.out.println("User does not exist!");
				return false;
		} else
			return false;
	}

	public static void main(String[] args) {
		UserUtils userUtils = new UserUtils();
		if (userUtils.isUserExist("ducdmk55", "ducdmk55@123")) {
			System.out.println("user exist!");
		} else
			System.out.println("user don't exist");
	}
}
