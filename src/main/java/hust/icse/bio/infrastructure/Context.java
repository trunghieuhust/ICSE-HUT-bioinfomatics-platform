package hust.icse.bio.infrastructure;

import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.openstack.glance.v1_0.GlanceApi;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.ssh.jsch.config.JschSshClientModule;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

public class Context {
	public final NovaApi novaApi;
	public final NovaApi adminNovaApi;
	public final NeutronApi neutronApi;
	public final ComputeServiceContext computeContext;
	public final GlanceApi glanceApi;
	public final Set<String> zones;
	public final LoginCredentials loginCredentials;
	public final String userPassword;
	public final String username;

	String defaultZone = null;

	public Context(User user) {
		// Iterable<Module> modules = ImmutableSet
		// .<Module> of(new SLF4JLoggingModule());
		Iterable<Module> sshModules = ImmutableSet
				.<Module> of(new JschSshClientModule());
		computeContext = ContextBuilder.newBuilder(CloudConfig.novaProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(user.getUserIdentity(), user.getPassword())
				.modules(sshModules).buildView(ComputeServiceContext.class);
		novaApi = ContextBuilder.newBuilder(CloudConfig.novaProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(user.getUserIdentity(), user.getPassword())
				.buildApi(NovaApi.class);
		adminNovaApi = ContextBuilder
				.newBuilder(CloudConfig.novaProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(CloudConfig.adminIdentity,
						CloudConfig.adminCredentials).buildApi(NovaApi.class);
		neutronApi = ContextBuilder.newBuilder(CloudConfig.neutronProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(user.getUserIdentity(), user.getPassword())
				.buildApi(NeutronApi.class);
		glanceApi = ContextBuilder.newBuilder(CloudConfig.glanceProvider)
				.endpoint(CloudConfig.endpoint)
				.credentials(user.getUserIdentity(), user.getPassword())
				.buildApi(GlanceApi.class);
		zones = novaApi.getConfiguredZones();
		if (null == defaultZone) {
			defaultZone = zones.iterator().next();
		}
		loginCredentials = new LoginCredentials.Builder().user("ubuntu")
				.privateKey(user.getKeypair()).build();
		this.username = user.getUsername();
		this.userPassword = user.getPassword();
	}
}