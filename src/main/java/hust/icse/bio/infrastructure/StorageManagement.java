package hust.icse.bio.infrastructure;

import static org.jclouds.io.Payloads.newByteSourcePayload;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.Container;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.features.ContainerApi;
import org.jclouds.openstack.swift.v1.features.ObjectApi;
import org.jclouds.openstack.swift.v1.options.CreateContainerOptions;
import org.jclouds.openstack.swift.v1.options.PutOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class StorageManagement implements Closeable {
	private final Set<String> zones;
	private String defaultZone = null;
	public static final String CONTAINER_NAME = "jclouds-example";
	public static final String OBJECT_NAME = "jclouds-keypair.pem";
	private User user;
	private SwiftApi swiftApi;
	private static StorageManagement instance;
	public static final String NAME = "name";
	public static final String BYTES = "bytes";

	public static void main(String[] args) throws IOException {
		// String CONTAINER_NAME = "keypair";
		// String OBJECT_NAME = "ducdmk55.pem";
		StorageManagement jcloudsSwift = new StorageManagement(new User(
				"ducdmk55", "ducdmk55@123"));
		System.out.println("Size:"
				+ jcloudsSwift.getFileSize("input", "ducdmk55-upload"));
		jcloudsSwift.close();
	}

	public StorageManagement(User user) {
		this.user = user;

		swiftApi = ContextBuilder
				.newBuilder(CloudConfig.swiftProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(this.user.getUserIdentity(),
						this.user.getPassword()).buildApi(SwiftApi.class);
		zones = swiftApi.getConfiguredRegions();
		if (null == defaultZone) {
			defaultZone = zones.iterator().next();
		}
	}

	public StorageManagement() {
		swiftApi = ContextBuilder
				.newBuilder(CloudConfig.swiftProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(CloudConfig.adminIdentity,
						CloudConfig.adminCredentials).buildApi(SwiftApi.class);
		zones = swiftApi.getConfiguredRegions();
		if (null == defaultZone) {
			defaultZone = zones.iterator().next();
		}
	}

	/**
	 * @param container
	 * @return Create user's container(public container)
	 */
	public void createContainer(String container) {
		System.out.println("Create Container");

		ContainerApi containerApi = swiftApi.getContainerApi(this.defaultZone);
		CreateContainerOptions options = CreateContainerOptions.Builder
				.metadata(ImmutableMap.of("key1", "value1", "key2", "value2"))
				.anybodyRead();

		if (containerApi.create(container, options)) {
			System.out.println("Created " + container);
		} else {
			System.out.println("Fail to create container: " + container);
		}
	}

	/**
	 * 
	 * @param filePath
	 * @param container
	 * @return Upload file to specified container
	 */
	public void uploadFileFromPath(String filePath, String container) {
		ObjectApi objectApi = swiftApi
				.getObjectApi(this.defaultZone, container);
		ByteSource data = Files.asByteSource(new File(filePath));
		Payload payload = newByteSourcePayload(data);

		objectApi.put(filePath, payload,
				PutOptions.Builder.metadata(ImmutableMap.of("key1", "value1")));

		System.out.println("  " + filePath);
	}

	public String uploadFileFromInputStream(DataHandler datahandler,
			String container) {
		ObjectApi objectApi = swiftApi
				.getObjectApi(this.defaultZone, container);
		Payload payload = null;
		try {
			payload = Payloads.newInputStreamPayload(datahandler
					.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(payload.toString());
		return objectApi.put(datahandler.getName(), payload,
				PutOptions.Builder.metadata(ImmutableMap.of("key1", "value1")));
	}

	public boolean deleteFile(String fileName, String container) {
		ObjectApi objectApi = swiftApi
				.getObjectApi(this.defaultZone, container);

		if (getFileLink(fileName, container) != null) {
			System.out.println("delete file: " + fileName);
			objectApi.delete(fileName);
			return true;
		} else {
			System.out.println("Fail to delete file: " + fileName + " in: "
					+ container);
			return false;
		}
	}

	public boolean deleteContainer(String containerName) {
		ContainerApi containerApi = swiftApi.getContainerApi(this.defaultZone);
		Set<Container> containers = containerApi.list().toSet();
		for (Container container : containers) {
			if (container.getName().equals(containerName)) {
				System.out.println("List Files:");

				ObjectApi objectApi = swiftApi.getObjectApi(this.defaultZone,
						containerName);
				Iterator<SwiftObject> objectIterators = objectApi.list()
						.iterator();
				while (objectIterators.hasNext()) {
					SwiftObject swiftObject = objectIterators.next();
					this.deleteFile(swiftObject.getName(), containerName);
				}
				if (containerApi.deleteIfEmpty(containerName)) {
					return true;
				} else
					return false;
			}
		}

		System.out.println("Container's name provided mismatch!");
		return false;

	}

	/**
	 * List all containers belong to user
	 */

	public List<hust.icse.bio.service.Container> listContainers(String username) {
		ContainerApi containerApi = swiftApi.getContainerApi(this.defaultZone);
		Set<Container> containers = containerApi.list().toSet();
		ArrayList<hust.icse.bio.service.Container> containerList = new ArrayList<hust.icse.bio.service.Container>();
		for (Container container : containers) {
			if (container.getName().contains("upload")
					&& !container.getName().contains(username + "-upload")) {
				continue;
			}
			hust.icse.bio.service.Container cont = new hust.icse.bio.service.Container();
			cont.setByteUsed(container.getBytesUsed());
			cont.setName(container.getName());
			cont.setObjectCount(container.getObjectCount());
			cont.setFileList(containerDetails(cont.getName()));
			containerList.add(cont);
		}
		return containerList;
	}

	public void deleteAll() {
		System.out.println("List Containers");

		ContainerApi containerApi = swiftApi.getContainerApi(this.defaultZone);
		Set<Container> containers = containerApi.list().toSet();

		for (Container container : containers) {
			if (container.getName().length() == 36) {

				System.out.println("delete:" + container.getName() + " "
						+ this.deleteContainer(container.getName()));
			}
		}
		System.out.println("Done");
	}

	/**
	 * 
	 * @param containerName
	 * @return List all file in specified container
	 */
	public String[] listFile(String containerName) {
		ContainerApi containerApi = swiftApi.getContainerApi(this.defaultZone);
		Set<Container> containers = containerApi.list().toSet();
		for (Container container : containers) {
			if (container.getName().equals(containerName)) {
				ObjectApi objectApi = swiftApi.getObjectApi(this.defaultZone,
						containerName);
				Iterator<SwiftObject> objectIterators = objectApi.list()
						.iterator();
				ArrayList<String> fileList = new ArrayList<String>();
				while (objectIterators.hasNext()) {
					SwiftObject swiftObject = objectIterators.next();
					Set<String> set = swiftObject.getMetadata().keySet();
					fileList.add(swiftObject.getName());
				}
				return fileList.toArray(new String[fileList.size()]);
			}
		}

		System.out.println("Container's name provided mismatch!");
		return null;

	}

	/**
	 * 
	 * @param fileName
	 * @param containerName
	 * @return File's URL in Swift
	 */
	public String getFileLink(String fileName, String containerName) {
		ContainerApi containerApi = swiftApi.getContainerApi(this.defaultZone);
		Set<Container> containers = containerApi.list().toSet();
		for (Container container : containers) {
			if (container.getName().equals(containerName)) {
				// System.out.println("List Files:");
				ObjectApi objectApi = swiftApi.getObjectApi(this.defaultZone,
						containerName);
				if (objectApi.list() != null) {
					Iterator<SwiftObject> objectIterators = objectApi.list()
							.iterator();
					while (objectIterators.hasNext()) {
						SwiftObject swiftObject = objectIterators.next();
						if (swiftObject.getName().contains(fileName)) {
							// System.out.println("File found");
							return swiftObject.getUri().toString();
						}
					}
					System.out.println("File: " + fileName
							+ " not found container: " + containerName);
					return null;
				}
			}
		}

		System.out.println("Container's name :" + containerName
				+ "provided mismatch. File: " + fileName);
		return null;

	}

	private String getToken() {
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			String httpRequestLink = "http://192.168.50.12:35357/v2.0/tokens";
			HttpPost request = new HttpPost(httpRequestLink);
			String json_data = "{\"auth\": {\"tenantName\": \""
					+ CloudConfig.bioServiceTenantName
					+ "\",\"passwordCredentials\": {\"username\": \""
					+ this.user.getUsername() + "\",\"password\": \""
					+ this.user.getPassword() + "\"}}}";
			StringEntity params = new StringEntity(json_data);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			// proceed only if status code = 200
			if (response.getStatusLine().getStatusCode() == 200) {
				JSONObject rootObject = new JSONObject(
						EntityUtils.toString(response.getEntity()));
				JSONObject tokenObject = rootObject.getJSONObject("access")
						.getJSONObject("token");
				return tokenObject.getString("id");

			} else
				return null;
		} catch (Exception ex) {
			// handle exception here
			return null;
		}
	}

	public long getFileSize(String fileName, String containerName) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			String httpRequestLink = "http://192.168.50.188:8080/v1/AUTH_"
					+ CloudConfig.bioServiceTenantID + "/" + containerName
					+ "?format=json";
			String token = this.getToken();
			System.out.println(token);
			if (token == null) {
				System.out.println("Null token, Authorize Failed!");
				return -1;
			} else {
				HttpGet fileSizeRequest = new HttpGet(httpRequestLink);
				fileSizeRequest.addHeader("X-Auth-Token", token);
				HttpResponse response = httpClient.execute(fileSizeRequest);
				// proceed only if status code = 200

				if (response.getStatusLine().getStatusCode() == 200) {
					JSONArray array = new JSONArray(
							EntityUtils.toString(response.getEntity()));
					for (int i = 0; i < array.length(); i++) {
						JSONObject object = array.getJSONObject(i);
						if (object.getString("name").equals(fileName)) {
							return object.getLong("bytes");
						}
					}
				} else {
					return -1;
				}
				return -1;
			}

		} catch (Exception ex) {
			return -1;
		}
	}

	public ArrayList<hust.icse.bio.service.File> containerDetails(
			String containerName) {
		ArrayList<hust.icse.bio.service.File> resultList = null;
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			String httpRequestLink = "http://192.168.50.188:8080/v1/AUTH_"
					+ CloudConfig.bioServiceTenantID + "/" + containerName
					+ "?format=json";
			String token = this.getToken();
			if (token == null) {
				System.out.println("Null token, Authorize Failed!");
				return resultList;
			} else {
				HttpGet fileSizeRequest = new HttpGet(httpRequestLink);
				fileSizeRequest.addHeader("X-Auth-Token", token);
				HttpResponse response = httpClient.execute(fileSizeRequest);
				// proceed only if status code = 200

				if (response.getStatusLine().getStatusCode() == 200) {
					JSONArray array = new JSONArray(
							EntityUtils.toString(response.getEntity()));
					resultList = new ArrayList<hust.icse.bio.service.File>();
					for (int i = 0; i < array.length(); i++) {
						JSONObject object;
						object = array.getJSONObject(i);
						hust.icse.bio.service.File file = new hust.icse.bio.service.File();
						file.setName(object.getString("name"));
						file.setBytes(object.getLong("bytes"));
						file.setFileURL(getFileLink(file.getName(),
								containerName));
						resultList.add(file);
					}
				}
				return resultList;
			}

		} catch (Exception ex) {
			return null;
		}

	}

	/**
	 * 
	 * @param fileLink
	 * @return actual file in Swift
	 * @throws IOException
	 */
	public File getFile(String fileLink) throws IOException {
		URL url = new URL(fileLink);
		File downloadedFile = new File("downloaded-file");
		org.apache.commons.io.FileUtils.copyURLToFile(url, downloadedFile);
		return downloadedFile;
	}

	public boolean copyFileToOtherContainer(String filename,
			String sourceContainer, String destinationContainer) {
		ObjectApi objectApi = swiftApi.getObjectApi(this.defaultZone,
				destinationContainer);
		return objectApi.copy(filename, sourceContainer, filename);
	}

	public String getUploadContainer() {
		return user.getUsername() + "-upload";
	}

	public String getUploadFolder() {
		return "swift-folder/" + getUploadContainer();
	}

	public static StorageManagement getAdminInstance() {
		if (instance == null) {
			instance = new StorageManagement();
			return instance;
		} else {
			return instance;
		}
	}

	public void close() throws IOException {
		Closeables.close(swiftApi, true);
	}
}
