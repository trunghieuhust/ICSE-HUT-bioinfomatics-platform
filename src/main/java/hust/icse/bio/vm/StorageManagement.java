package hust.icse.bio.vm;

import hust.icse.bio.service.User;

import static org.jclouds.io.Payloads.newByteSourcePayload;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.io.Payload;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.Container;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.features.ContainerApi;
import org.jclouds.openstack.swift.v1.features.ObjectApi;
import org.jclouds.openstack.swift.v1.options.CreateContainerOptions;
import org.jclouds.openstack.swift.v1.options.PutOptions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.inject.Module;

public class StorageManagement implements Closeable {
	private final Set<String> zones;
	private String defaultZone = null;
	public static final String CONTAINER_NAME = "jclouds-example";
	public static final String OBJECT_NAME = "jclouds-keypair.pem";
	private User user;
	private SwiftApi swiftApi;
	private static StorageManagement instance;

	public static void main(String[] args) throws IOException {
		String CONTAINER_NAME = "keypair";
		String OBJECT_NAME = "ducdmk55.pem";
		StorageManagement jcloudsSwift = new StorageManagement(new User(
				"ducdmk55", "ducdmk55@123"));
		jcloudsSwift.deleteAll();
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
			System.out.println("Fail to create container");
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

	public boolean deleteFile(String fileName, String container) {
		ObjectApi objectApi = swiftApi
				.getObjectApi(this.defaultZone, container);

		if (getFileLink(fileName, container) != null) {
			System.out.println("delete file: " + fileName);
			objectApi.delete(fileName);
			return true;
		} else {
			System.out.println("Fail to delete!");
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

	public void listContainers() {
		System.out.println("List Containers");

		ContainerApi containerApi = swiftApi.getContainerApi(this.defaultZone);
		Set<Container> containers = containerApi.list().toSet();

		for (Container container : containers) {
			System.out.println("  " + container);
		}
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
	public void listFile(String containerName) {
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
					System.out.println(swiftObject.getName() + ":"
							+ swiftObject.getUri());
				}
				return;
			}
		}

		System.out.println("Container's name provided mismatch!");
		return;

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
				Iterator<SwiftObject> objectIterators = objectApi.list()
						.iterator();
				while (objectIterators.hasNext()) {
					SwiftObject swiftObject = objectIterators.next();
					if (swiftObject.getName().contains(fileName)) {
						// System.out.println("File found");
						return swiftObject.getUri().toString();
					}
				}
				System.out.println("File not found in this container");
				return null;
			}
		}

		System.out.println("Container's name provided mismatch!1");
		return null;

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
