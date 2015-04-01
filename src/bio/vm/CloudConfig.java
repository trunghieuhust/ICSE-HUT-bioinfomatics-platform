package bio.vm;

public interface CloudConfig {
	public static final String openstackIdentity = "admin:admin";
	public static final String openstackCredentials = "Bkcloud12@Icse@2015";
	public static final String endpoint = "http://192.168.50.12:5000/v2.0/";
	public static final String swiftProvider = "openstack-swift";
	public static final String novaProvider = "openstack-nova";
	public static final String keystoneProvider = "openstack-keystone";
	public static final String internalNetwork = "int_net";
	public static final String neutronProvider = "openstack-neutron";
	public static final String UNDEFINED_FLAVOR = "Flavor not found";
	public static final String UNDEFINED_IMAGE = "Image not found";
	public static final String UNDEFINED_SERVER = "Server not found";
	public static final String UNDEFINED_INT_NET = "Internal network not found";
	public static final String ubuntuImage = "ubuntu-14.04-server-cloudimg-amd64";
}
