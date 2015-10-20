package hust.icse.bio.infrastructure;

import java.util.NoSuchElementException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.extensions.TenantAdminApi;
import org.jclouds.openstack.keystone.v2_0.extensions.UserAdminApi;
import org.jclouds.openstack.keystone.v2_0.features.TenantApi;
import org.jclouds.openstack.keystone.v2_0.features.UserApi;
import org.jclouds.openstack.keystone.v2_0.options.CreateUserOptions;
import org.jclouds.rest.AuthorizationException;
import org.json.JSONObject;

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
	public User authenticate1(String username, String password) {
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

	public User authenticate(String name, String password) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			String httpRequestLink = "http://192.168.50.12:35357/v2.0/tokens";
			HttpPost request = new HttpPost(httpRequestLink);
			String json_data = null;
			if (!name.equals("admin")) {
				json_data = "{\"auth\": {\"tenantName\": \""
						+ CloudConfig.bioServiceTenantName
						+ "\",\"passwordCredentials\": {\"username\": \""
						+ name + "\",\"password\": \"" + password + "\"}}}";
			} else {
				json_data = "{\"auth\": {\"tenantName\": \"" + "admin"
						+ "\",\"passwordCredentials\": {\"username\": \""
						+ name + "\",\"password\": \"" + password + "\"}}}";
			}
			StringEntity params = new StringEntity(json_data);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			// proceed only if status code = 200
			if (response.getStatusLine().getStatusCode() == 200) {
				JSONObject rootObject = new JSONObject(
						EntityUtils.toString(response.getEntity()));
				User user = new User(name, password);
				JSONObject userObject = rootObject.getJSONObject("access")
						.getJSONObject("user");
				System.out.println();
				user.setUserID(userObject.getString("id"));
				if (!name.equals("admin")) {
					user.setAdmin(false);
				} else {
					user.setAdmin(true);
				}
				return user;
			} else
				return null;
		} catch (Exception ex) {
			// handle exception here
			return null;
		}
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
		long startTime = System.currentTimeMillis();
		User user = UserManagement.getInstance().authenticate("admin",
				"Bkcloud12@Icse@2015");
		String[] flavorList = user.getManager().listFlavor();
		System.out.println("Flavors List:");
		for (int i = 0; i < flavorList.length; i++) {
			System.out.println(flavorList[i]);
		}
		long endTime = System.currentTimeMillis();
		System.err.println("VM Creation time:" + (endTime - startTime) / 1000);

		// user.getStorageManagement().uploadFileFromPath("cloudfuse-config.sh",
		// "abc");
		// user.getManager().terminateInstance(vm);
		// startTime = System.currentTimeMillis();
		// String snapshotID = vm.createSnapshot("testSnapshot");
		// endTime = System.currentTimeMillis();
		// System.err.println("Snapshots " + snapshotID + " Creation time:"
		// + (endTime - startTime) / 1000);

	}
}