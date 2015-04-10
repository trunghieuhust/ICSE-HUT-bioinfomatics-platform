package bio.vm;
import static org.jclouds.io.Payloads.newByteSourcePayload;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

import bio.service.User;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.inject.Module;

public class StorageUtils implements Closeable {
	private final Set<String> zones;
	private String defaultZone = null;
	public static final String CONTAINER_NAME = "jclouds-example";
	public static final String OBJECT_NAME = "jclouds-keypair.pem";
	// private User user;
	private SwiftApi swiftApi;

	public static void main(String[] args) throws IOException {
		String CONTAINER_NAME = "keypair";
		String OBJECT_NAME = "ducdmk55.pem";
		StorageUtils jcloudsSwift = new StorageUtils(new User("ducdmk55",
				"ducdmk55@123"));
		String privatekey;
		try {

			privatekey = Files.toString(jcloudsSwift.getFile(jcloudsSwift
					.getFileLink(OBJECT_NAME, CONTAINER_NAME)),
					StandardCharsets.UTF_8);
			System.out.println(privatekey);
			// jcloudsSwift.createContainer(CONTAINER_NAME);
			// jcloudsSwift.uploadFileFromPath(OBJECT_NAME, CONTAINER_NAME);
			// jcloudsSwift.listContainers();
			// jcloudsSwift.listFile(CONTAINER_NAME);
			// jcloudsSwift.getFileLink(OBJECT_NAME, CONTAINER_NAME);
			// jcloudsSwift.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jcloudsSwift.close();
		}
	}

	public StorageUtils(User user) {
		// this.user = user;
		Iterable<Module> modules = ImmutableSet
				.<Module> of(new SLF4JLoggingModule());

		String identity = CloudConfig.bioServiceTenantName + ":"
				+ user.getUsername();
		swiftApi = ContextBuilder.newBuilder(CloudConfig.swiftProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(identity, user.getPassword()).modules(modules)
				.buildApi(SwiftApi.class);
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

		containerApi.create(container, options);

		System.out.println("  " + container);
	}

	/**
	 * 
	 * @param filePath
	 * @param container
	 * @return Upload file to specified container
	 */
	public void uploadFileFromPath(String filePath, String container) {
		System.out.println("Upload file");

		ObjectApi objectApi = swiftApi
				.getObjectApi(this.defaultZone, container);
		ByteSource data = Files.asByteSource(new File(filePath));
		Payload payload = newByteSourcePayload(data);

		objectApi.put(filePath, payload,
				PutOptions.Builder.metadata(ImmutableMap.of("key1", "value1")));

		System.out.println("  " + filePath);
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
				System.out.println("List Files:");
				ObjectApi objectApi = swiftApi.getObjectApi(this.defaultZone,
						containerName);
				Iterator<SwiftObject> objectIterators = objectApi.list()
						.iterator();
				while (objectIterators.hasNext()) {
					SwiftObject swiftObject = objectIterators.next();
					if (swiftObject.getName().equals(fileName)) {
						System.out.println("File found");
						return swiftObject.getUri().toString();
					}
				}
				System.out.println("File not found in this container");
				return null;
			}
		}

		System.out.println("Container's name provided mismatch!");
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

	public void close() throws IOException {
		Closeables.close(swiftApi, true);
	}
}
