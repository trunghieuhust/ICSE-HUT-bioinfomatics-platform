package hust.icse.bio.infrastructure;

import hust.icse.bio.utils.LockObject;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jclouds.openstack.neutron.v2.domain.Network;
import org.jclouds.openstack.neutron.v2.features.NetworkApi;
import org.jclouds.openstack.nova.v2_0.domain.Address;
import org.jclouds.openstack.nova.v2_0.domain.Flavor;
import org.jclouds.openstack.nova.v2_0.domain.Image;
import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.Server.Status;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.jclouds.openstack.v2_0.domain.Resource;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.io.Closeables;

public class VMmanagement implements Closeable {
	private final Context context;
	private User user;

	public VMmanagement(User user) {
		this.user = user;
		context = new Context(user);
	}

	public Status checkServerStatus(String serverID) {
		ServerApi serverApi = context.novaApi
				.getServerApiForZone(context.defaultZone);
		Server server = serverApi.get(serverID);
		return server.getStatus();
	}

	public void listServers() {
		for (String zone : context.zones) {
			ServerApi serverApi = context.novaApi.getServerApiForZone(zone);

			System.out.println("Servers in " + zone);

			for (Server server : serverApi.listInDetail().concat()) {
				System.out.println(server);
			}
		}
	}

	public VM launchInstance(String name, String image, String flavor) {
		int timeoutCounting = 0;
		int readLogCount = 0;
		String floatingIP;
		String keypairName = user.getUsername();
		CreateServerOptions options = CreateServerOptions.Builder.keyPairName(
				keypairName).networks(
				this.getNetworkId(CloudConfig.internalNetwork));
		ServerApi serverApi = context.novaApi
				.getServerApiForZone(context.defaultZone);
		ServerCreated ser = serverApi.create(name, this.getImageId(image),
				this.getFlavorId(flavor), options);
		String serverID = ser.getId();

		synchronized (LockObject.getInstance()) {
			floatingIP = getOrCreateFloatingIP();
			System.err.println("attaching IP " + floatingIP);

			while (!attachIP(floatingIP, serverID)) {
				if (timeoutCounting < 100) {
					try {
						if (checkServerStatus(serverID) == Status.ERROR) {
							System.out.println("Build error.");
							terminateInstancebyId(serverID);
							return null;
						}
						Thread.sleep(500);
						timeoutCounting++;
					} catch (InterruptedException e) {
						System.out
								.println("Error when attaching floating IP.Terminated.");
						terminateInstancebyId(serverID);
						return null;
					}
				} else {
					terminateInstancebyId(serverID);
					System.out.println("Booting error.Terminated.");
					return null;
				}
			}
			System.err.println("IP attached.");
		}
		System.out.println("Waiting for complete booting");
		while (!checkLogInstance(serverID)) {
			if (readLogCount < 200) {
				try {
					Thread.sleep(500);
					readLogCount++;
				} catch (InterruptedException e) {
					System.out.println("Error occured");
					return null;
				}
			}
		}

		System.out.println("Boot complete, ready to go!");
		VM vm = new VM(this.context, name, serverID, floatingIP);
		vm.runInitScript();
		return vm;
	}

	public VM launchInstanceWithoutInitScript(String name, String image,
			String flavor) {
		int timeoutCounting = 0;
		int readLogCount = 0;
		String floatingIP;
		String keypairName = user.getUsername();
		CreateServerOptions options = CreateServerOptions.Builder.keyPairName(
				keypairName).networks(
				this.getNetworkId(CloudConfig.internalNetwork));
		ServerApi serverApi = context.novaApi
				.getServerApiForZone(context.defaultZone);
		ServerCreated ser = serverApi.create(name, this.getImageId(image),
				this.getFlavorId(flavor), options);
		String serverID = ser.getId();

		synchronized (LockObject.getInstance()) {
			floatingIP = getOrCreateFloatingIP();
			System.err.println("attaching IP " + floatingIP);

			while (!attachIP(floatingIP, serverID)) {
				if (timeoutCounting < 100) {
					try {
						if (checkServerStatus(serverID) == Status.ERROR) {
							System.out.println("Build error.");
							terminateInstancebyId(serverID);
							return null;
						}
						Thread.sleep(500);
						timeoutCounting++;
					} catch (InterruptedException e) {
						System.out
								.println("Error when attaching floating IP.Terminated.");
						terminateInstancebyId(serverID);
						return null;
					}
				} else {
					terminateInstancebyId(serverID);
					System.out.println("Booting error.Terminated.");
					return null;
				}
			}
			System.err.println("IP attached.");
		}
		System.out.println("Waiting for complete booting");
		while (!checkLogInstance(serverID)) {
			if (readLogCount < 200) {
				try {
					Thread.sleep(500);
					readLogCount++;
				} catch (InterruptedException e) {
					System.out.println("Error occured");
					return null;
				}
			}
		}

		System.out.println("Boot complete, ready to go!");
		VM vm = new VM(this.context, name, serverID, floatingIP);
		return vm;
	}

	public boolean generateKeypair(String keypairName) {
		KeyPairApi keypairApi = context.novaApi.getKeyPairExtensionForZone(
				context.defaultZone).get();
		KeyPair keypair = keypairApi.create(keypairName);
		try {
			FileUtils.writeStringToFile(
					new File("/tmp/" + keypairName + ".pem"),
					keypair.getPrivateKey());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	private boolean checkLogInstance(String serverID) {
		String log = this.getInstanceLog(serverID);
		if (log == null)
			return false;
		else if (log.toLowerCase().contains(
				CloudConfig.bootCompleteString.toLowerCase()))
			return true;
		else
			return false;
	}

	private String getInstanceLog(String serverID) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			String httpRequestLink;
			if (!this.user.getUsername().equals("admin")) {
				httpRequestLink = "http://192.168.50.12:8774/v2/"
						+ CloudConfig.bioServiceTenantID + "/servers/"
						+ serverID + "/action";
			} else {
				httpRequestLink = "http://192.168.50.12:8774/v2/"
						+ CloudConfig.adminTenantID + "/servers/" + serverID
						+ "/action";
			}
			String token = this.getToken();
			if (token == null)
				return null;
			else {
				HttpPost request = new HttpPost(httpRequestLink);
				String json_data = "{\"os-getConsoleOutput\":{\"length\":15}}";
				StringEntity params = new StringEntity(json_data);
				request.addHeader("content-type", "application/json");
				request.addHeader("X-Auth-Token", token);
				request.setEntity(params);
				HttpResponse response = httpClient.execute(request);

				if (response.getStatusLine().getStatusCode() != 200) {
					return null;
				} else {
					JSONObject rootObject = new JSONObject(
							EntityUtils.toString(response.getEntity()));
					return rootObject.getString("output");
				}
			}
			// handle response here...
		} catch (Exception ex) {
			// handle exception here
			return null;
		}
	}

	private String getToken() {
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			String httpRequestLink = "http://192.168.50.12:35357/v2.0/tokens";
			HttpPost request = new HttpPost(httpRequestLink);
			String json_data;
			if (!this.user.getUsername().equals("admin")) {
				json_data = "{\"auth\": {\"tenantName\": \""
						+ CloudConfig.bioServiceTenantName
						+ "\",\"passwordCredentials\": {\"username\": \""
						+ this.user.getUsername() + "\",\"password\": \""
						+ this.user.getPassword() + "\"}}}";
			} else {
				json_data = "{\"auth\": {\"tenantName\": \"" + "admin"
						+ "\",\"passwordCredentials\": {\"username\": \""
						+ this.user.getUsername() + "\",\"password\": \""
						+ this.user.getPassword() + "\"}}}";
			}
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

	public boolean terminateInstancebyName(String serverName) {
		return terminateInstancebyId(getServerId(serverName));
	}

	public boolean terminateInstancebyId(String id) {
		ServerApi serverApi = context.novaApi
				.getServerApiForZone(context.defaultZone);
		return serverApi.delete(id);
	}

	public boolean terminateInstance(VM vm) {
		return terminateInstancebyId(vm.getID());
	}

	private String getFlavorId(String flavor) {
		FlavorApi flavorApi = context.novaApi
				.getFlavorApiForZone(context.defaultZone);
		try {
			Flavor flavorObj = flavorApi.get(flavor);
			return flavorObj.getId();
		} catch (NullPointerException e) {
			for (Resource f : flavorApi.list().concat()) {
				if (f.getName().equalsIgnoreCase(flavor))
					return f.getId();
			}
		}
		throw new NullPointerException("Flavor not found");
	}

	public String[] listFlavor() {
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			String request;
			if (!this.user.getUsername().equals("admin")) {
				request = "http://192.168.50.12:8774/v2/"
						+ CloudConfig.bioServiceTenantID + "/flavors";
			} else {
				request = "http://192.168.50.12:8774/v2/"
						+ CloudConfig.adminTenantID + "/flavors";
			}
			String token = this.getToken();
			// System.out.println(token);
			if (token == null) {
				System.out.println("Null token, Authorize Failed!");
				return null;
			} else {
				HttpGet listFlavorIpRequest = new HttpGet(request);
				listFlavorIpRequest.addHeader("content-type",
						"application/json");
				listFlavorIpRequest.addHeader("X-Auth-Token", token);
				HttpResponse response = httpClient.execute(listFlavorIpRequest);
				if (response.getStatusLine().getStatusCode() == 200) {
					JSONObject rootObject = new JSONObject(
							EntityUtils.toString(response.getEntity()));
					JSONArray flavorArray = rootObject.getJSONArray("flavors");
					String[] result = new String[flavorArray.length()];
					for (int i = 0; i < flavorArray.length(); i++) {
						result[i] = flavorArray.getJSONObject(i).getString(
								"name");
					}
					return result;
				} else {
					System.out
							.println(response.getStatusLine().getStatusCode());
				}
				return null;
			}
		} catch (Exception ex) {
			// handle exception here
			ex.printStackTrace();
			return null;
		}
	}

	private String getImageId(String image) {
		ImageApi imageApi = context.novaApi
				.getImageApiForZone(context.defaultZone);
		try {
			Image imageObj = imageApi.get(image);
			return imageObj.getId();
		} catch (NullPointerException e) {
			for (Resource i : imageApi.list().concat()) {
				if (i.getName().equalsIgnoreCase(image))
					return i.getId();
			}
		}
		throw new NullPointerException("Image not found");
	}

	private String getNetworkId(String network) {
		NetworkApi networkApi = context.neutronApi
				.getNetworkApi(context.defaultZone);
		try {
			Network networkObj = networkApi.get(network);
			return networkObj.getId();
		} catch (NullPointerException e) {
			for (Network i : networkApi.list().concat()) {
				if (i.getName().equalsIgnoreCase(network))
					return i.getId();
			}
		}
		throw new NullPointerException("Network not found");
	}

	private boolean attachIP(String ip, String server) {
		FloatingIPApi floatingIPApi = context.novaApi
				.getFloatingIPExtensionForZone(context.defaultZone).get();
		if (!InetAddressValidator.getInstance().isValid(ip))
			return false;
		else if (checkServerStatus(server) != Status.ACTIVE)
			return false;
		else {
			floatingIPApi.addToServer(ip, server);
			return true;
		}
	}

	public String getServerId(String server) {
		ServerApi serverApi = context.novaApi
				.getServerApiForZone(context.defaultZone);
		try {
			Server serverObj = serverApi.get(server);
			return serverObj.getId();
		} catch (NullPointerException e) {
			for (Resource s : serverApi.list().concat()) {
				if (s.getName().equalsIgnoreCase(server))
					return s.getId();
			}
		}
		throw new NullPointerException("Server not found");
	}

	private String getOrCreateFloatingIP() {
		String availableIP = getAvailableFloatingIP();

		if (availableIP != null) {
			return availableIP;
		} else {
			HttpClient httpClient = HttpClientBuilder.create().build();
			try {
				String request;
				if (!this.user.getUsername().equals("admin")) {
					request = "http://192.168.50.12:8774/v2/"
							+ CloudConfig.bioServiceTenantID
							+ "/os-floating-ips";
				} else {
					request = "http://192.168.50.12:8774/v2/"
							+ CloudConfig.adminTenantID + "/os-floating-ips";
				}
				String token = this.getToken();
				// System.out.println(token);
				if (token == null) {
					System.out.println("Null token, Authorize Failed!");
					return null;
				} else {
					HttpPost listFloatingIpRequest = new HttpPost(request);
					listFloatingIpRequest.addHeader("content-type",
							"application/json");
					String json_data = "{\"pool\":\"ext_net\"}";
					StringEntity params = new StringEntity(json_data);
					listFloatingIpRequest.setEntity(params);
					listFloatingIpRequest.addHeader("X-Auth-Token", token);
					HttpResponse response = httpClient
							.execute(listFloatingIpRequest);
					if (response.getStatusLine().getStatusCode() != 200) {
						System.err.println("getOrCreateFloatingIP:"
								+ response.toString());
						return null;
					} else {
						JSONObject rootObject = new JSONObject(
								EntityUtils.toString(response.getEntity()));
						JSONObject object = rootObject
								.getJSONObject("floating_ip");
						System.err.println("getOrCreateFloatingIP:"
								+ object.toString());
						return object.getString("ip");
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private String getAvailableFloatingIP() {
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			String request;
			if (!this.user.getUsername().equals("admin")) {
				request = "http://192.168.50.12:8774/v2/"
						+ CloudConfig.bioServiceTenantID + "/os-floating-ips";
			} else {
				request = "http://192.168.50.12:8774/v2/"
						+ CloudConfig.adminTenantID + "/os-floating-ips";
			}
			String token = this.getToken();
			// System.out.println(token);
			if (token == null) {
				System.out.println("Null token, Authorize Failed!");
				return null;
			} else {
				HttpGet listFloatingIpRequest = new HttpGet(request);
				listFloatingIpRequest.addHeader("content-type",
						"application/json");
				listFloatingIpRequest.addHeader("X-Auth-Token", token);
				HttpResponse response = httpClient
						.execute(listFloatingIpRequest);

				if (response.getStatusLine().getStatusCode() != 200) {
					System.err
							.println(response.getStatusLine().getStatusCode());
					System.err.println(response);
					return null;
				} else {
					JSONObject rootObject = new JSONObject(
							EntityUtils.toString(response.getEntity()));
					JSONArray array = rootObject.getJSONArray("floating_ips");
					for (int i = 0; i < array.length(); i++) {
						JSONObject object = array.getJSONObject(i);
						// System.out
						// .println(object.get("instance_id").toString());
						// System.out.println(object.get("ip").toString());
						if (object.get("instance_id").toString().equals("null"))
							return object.get("ip").toString();
						else
							continue;
					}
					return null;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getFloatingIP(String serverId) {
		ServerApi serverApi = context.novaApi
				.getServerApiForZone(context.defaultZone);

		String address = null;
		try {
			Server serverObj = serverApi.get(serverId);

			Iterator<Address> addressIterator = serverObj.getAddresses()
					.asMap().get("int_net").iterator();
			addressIterator.next();
			if (addressIterator.hasNext()) {
				address = addressIterator.next().getAddr();
				return address;
			} else
				return null;
		} catch (NullPointerException e) {
			for (Resource s : serverApi.list().concat()) {
				if (s.getName().equalsIgnoreCase(serverId)) {
					return null;
				}
			}
		}
		return address;
	}

	// TODO create flavor
	public boolean createFlavor(String flavorName, int vcpu, int ram, int disk) {
		FlavorApi flavorApi = context.adminNovaApi
				.getFlavorApiForZone(context.defaultZone);
		Flavor flavor = Flavor.builder().name(flavorName).id("").vcpus(vcpu)
				.ram(ram).disk(disk).build();
		Flavor newFlavor = null;
		try {
			newFlavor = flavorApi.create(flavor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (newFlavor != null) {
			return true;
		} else
			return false;
	}

	public void close() throws IOException {
		Closeables.close(context.novaApi, true);
	}

}
