package bio.vm;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.neutron.v2.domain.Network;
import org.jclouds.openstack.neutron.v2.features.NetworkApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
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
import org.jclouds.ssh.SshClient;
import org.jclouds.ssh.jsch.config.JschSshClientModule;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.common.net.HostAndPort;
import com.google.inject.Module;

public class VMmanagement implements Closeable {
	private final NovaApi novaApi;
	private final NeutronApi neutronApi;
	private final ComputeServiceContext computeContext;
	private final Set<String> zones;
	private String defaultZone = null;

	public VMmanagement() {
		Iterable<Module> modules = ImmutableSet
				.<Module> of(new SLF4JLoggingModule());
		Iterable<Module> sshModules = ImmutableSet
				.<Module> of(new JschSshClientModule());

		computeContext = ContextBuilder
				.newBuilder(CloudConfig.novaProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(CloudConfig.openstackIdentity,
						CloudConfig.openstackCredentials).modules(sshModules)
				.buildView(ComputeServiceContext.class);
		novaApi = ContextBuilder
				.newBuilder(CloudConfig.novaProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(CloudConfig.openstackIdentity,
						CloudConfig.openstackCredentials)
				.buildApi(NovaApi.class);
		neutronApi = ContextBuilder
				.newBuilder(CloudConfig.neutronProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(CloudConfig.openstackIdentity,
						CloudConfig.openstackCredentials).modules(modules)
				.buildApi(NeutronApi.class);
		zones = novaApi.getConfiguredZones();
		if (null == defaultZone) {
			defaultZone = zones.iterator().next();
		}

	}

	public Status checkServerStatus(String serverID) {
		ServerApi serverApi = novaApi.getServerApiForZone(this.defaultZone);
		Server server = serverApi.get(serverID);
		return server.getStatus();
	}

	public void listServers() {
		for (String zone : zones) {
			ServerApi serverApi = novaApi.getServerApiForZone(zone);

			System.out.println("Servers in " + zone);

			for (Server server : serverApi.listInDetail().concat()) {
				System.out.println(server);
			}
		}
	}

	public String launchInstance(String name, String image, String flavor,
			String keypairName) {
		CreateServerOptions options = CreateServerOptions.Builder.keyPairName(
				keypairName).networks(
				this.getNetworkId(CloudConfig.internalNetwork));
		ServerApi serverApi = this.novaApi.getServerApiForZone(defaultZone);
		ServerCreated ser = serverApi.create(name, this.getImageId(image),
				this.getFlavorId(flavor), options);
		String serverID = ser.getId();
		String floatingIP = getOrCreateFloatingIP();
		System.out.println("Waiting for server booting....");
		if (attachIP(floatingIP, serverID) == true) {
			System.out.println("New Server Floating IP:" + floatingIP);
			return floatingIP;
		} else {
			System.out.println("Cannot asscociate floating IP!");
			return null;
		}

	}

	public String getFlavorId(String flavor) {
		FlavorApi flavorApi = this.novaApi
				.getFlavorApiForZone(this.defaultZone);
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
		ImageApi imageApi = this.novaApi.getImageApiForZone(this.defaultZone);
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
		NetworkApi networkApi = this.neutronApi.getNetworkApi(this.defaultZone);
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
		Integer timeoutCounting = 0;
		FloatingIPApi floatingIPApi = this.novaApi
				.getFloatingIPExtensionForZone(this.defaultZone).get();
		if (ip == null)
			return false;
		if (checkServerStatus(server) == Status.ACTIVE) {
			floatingIPApi.addToServer(ip, server);
			return true;
		}
		while (checkServerStatus(server) != Status.ACTIVE) {
			if (timeoutCounting < 15) {
				try {
					Thread.sleep(10000);
					timeoutCounting++;
				} catch (InterruptedException e) {
					System.out.println("Error when attaching floating IP");
					return false;
				}
			} else {
				System.out.println("Waiting Timeout!");
				return false;
			}
		}
		// TODO ping thu den may ao de xem da ssh dc hay chua
		floatingIPApi.addToServer(ip, this.getServerId(server));
		try {
			System.out
					.println("Attach successfully, wait for complete booting");
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	public String getServerId(String server) {
		ServerApi serverApi = this.novaApi.getServerApiForZone(defaultZone);
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
		FloatingIPApi floatingIPApi = this.novaApi
				.getFloatingIPExtensionForZone(this.defaultZone).get();
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
			// return floatingIPApi.create().getIp();
			return floatingIPApi.allocateFromPool(this.getNetworkId("ext_net"))
					.getIp();
		}
	}

	public String excuteCommand(String server,
			LoginCredentials loginCredentials, String cmd) {
		HostAndPort targetServer = HostAndPort.fromParts(server, 22);

		try {
			SshClient sshClient = this.computeContext.utils()
					.getSshClientFactory()
					.create(targetServer, loginCredentials);
			sshClient.connect();
			ExecResponse respond = sshClient.exec(cmd);
			sshClient.disconnect();
			return respond.getOutput();
		} catch (Exception e) {
			return ("Authentication Fail!");
		}
	}

	public String excuteCommand(String server, String user, String password,
			String cmd) {
		LoginCredentials loginCredentials = new LoginCredentials.Builder()
				.user(user).password(password).build();
		return excuteCommand(server, loginCredentials, cmd);
	}

	public String getFloatingIP(String serverId) {
		ServerApi serverApi = this.novaApi.getServerApiForZone(defaultZone);
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
		Closeables.close(novaApi, true);
	}
}
