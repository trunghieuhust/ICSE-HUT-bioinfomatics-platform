package bio.vm;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.jclouds.openstack.neutron.v2.domain.Network;
import org.jclouds.openstack.neutron.v2.features.NetworkApi;
import org.jclouds.openstack.nova.v2_0.domain.Address;
import org.jclouds.openstack.nova.v2_0.domain.Flavor;
import org.jclouds.openstack.nova.v2_0.domain.FloatingIP;
import org.jclouds.openstack.nova.v2_0.domain.Image;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.Server.Status;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.jclouds.openstack.v2_0.domain.Resource;

import bio.service.User;

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
		String keypairName = user.getUsername();
		CreateServerOptions options = CreateServerOptions.Builder.keyPairName(
				keypairName).networks(
				this.getNetworkId(CloudConfig.internalNetwork));
		ServerApi serverApi = context.novaApi
				.getServerApiForZone(context.defaultZone);
		ServerCreated ser = serverApi.create(name, this.getImageId(image),
				this.getFlavorId(flavor), options);
		String serverID = ser.getId();
		String floatingIP = getOrCreateFloatingIP();
		System.out.println("Waiting for server booting....");

		while (!attachIP(floatingIP, serverID)) {
			if (timeoutCounting < 15) {
				try {
					Thread.sleep(3000);
					timeoutCounting++;
				} catch (InterruptedException e) {
					System.out.println("Error when attaching floating IP");
					return null;
				}
			} else {
				System.out.println("Booting error");
				return null;
			}
		}
		System.out.println("Waiting for complete booting");
		try {
			Thread.sleep(45000);
		} catch (InterruptedException e) {
			System.out.println("Error occured");
			return null;
		}
		System.out.println("Boot complete, ready to go!");
		VM vm = new VM(this.context, name, serverID, floatingIP);
		return vm;
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

	public String getFlavorId(String flavor) {
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

	public String getImageId(String image) {
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

	public String getNetworkId(String network) {
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

	public boolean attachIP(String ip, String server) {
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

	public String getOrCreateFloatingIP() {
		List<FloatingIP> freeIP = new LinkedList<FloatingIP>();
		FloatingIPApi floatingIPApi = context.novaApi
				.getFloatingIPExtensionForZone(context.defaultZone).get();
		Iterator<? extends FloatingIP> floatingIP = floatingIPApi.list()
				.iterator();
		while (floatingIP.hasNext()) {
			FloatingIP ip = floatingIP.next();
			if (ip.getInstanceId() == null) {
				freeIP.add(ip);
			}
		}
		if (freeIP.size() > 0) {
			return freeIP.get(0).getIp();
		} else {
			return floatingIPApi.allocateFromPool(this.getNetworkId("ext_net"))
					.getIp();
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

	public void close() throws IOException {
		Closeables.close(context.novaApi, true);
	}

}
