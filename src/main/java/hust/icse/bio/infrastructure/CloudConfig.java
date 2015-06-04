package hust.icse.bio.infrastructure;

public interface CloudConfig {
	public static final String adminIdentity = "admin:admin";
	public static final String adminCredentials = "Bkcloud12@Icse@2015";
	public static final String bioServiceTenantName = "students";
	public static final String bioServiceTenantID = "1a1c2b5a4e3e4ff482fa557768aa0f94";
	public static final String memberRoleID = "ad8f8d1d76b94efdbcf6cf5c3f781373";
	public static final String endpoint = "http://192.168.50.12:5000/v2.0/";
	public static final String adminServiceEndpoint = "http://192.168.50.12:35357/v2.0/";
	public static final String swiftProvider = "openstack-swift";
	public static final String novaProvider = "openstack-nova";
	public static final String keystoneProvider = "openstack-keystone";
	public static final String glanceProvider = "openstack-glance";
	public static final String internalNetwork = "int_net";
	public static final String neutronProvider = "openstack-neutron";
	public static final String UNDEFINED_FLAVOR = "Flavor not found";
	public static final String UNDEFINED_IMAGE = "Image not found";
	public static final String UNDEFINED_SERVER = "Server not found";
	public static final String UNDEFINED_INT_NET = "Internal network not found";
	public static final String ubuntuImage = "ubuntu-14.04-server-cloudimg-amd64";
	public static final String initScriptLink = "http://192.168.50.188:8080/v1/AUTH_e9718a4e5275474f8b157edf2167022b/config/cloudfuse-config.sh";
	public static final String userSwiftEndPoint = "http://192.168.50.188:8080/v1/AUTH_1a1c2b5a4e3e4ff482fa557768aa0f94";
	public static final String keypairContainer = "keypair";
	public static final String instanceLogFolderLink = "http://192.168.50.12:8080/v1/AUTH_e9718a4e5275474f8b157edf2167022b/instance-log";
	// public static final String bootCompleteString =
	// "Cloud-init v. 0.7.5 finished at";
	// public static final String bootCompleteString = "Ubuntu 14.04.1 LTS";
	public static final String bootCompleteString = "-----END SSH HOST KEY KEYS-----";

}
