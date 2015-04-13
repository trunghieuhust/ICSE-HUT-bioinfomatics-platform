package bio.vm;

public interface CloudConfig {
	public static final String adminIdentity = "admin:admin";
	public static final String adminCredentials = "Bkcloud12@Icse@2015";
	public static final String bioServiceTenantName = "students";
	public static final String bioServiceTenantID = "642e43d4ac2243e1928e695bebc2e783";
	public static final String memberRoleID = "ad8f8d1d76b94efdbcf6cf5c3f781373";
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
	public static final String cloudBioImage = "cloud-bio-v1";
	public static final String initScriptLink = "http://192.168.50.12:8080/v1/AUTH_e9718a4e5275474f8b157edf2167022b/script/cloudfuse-config.sh";
	public static final String keypairContainer = "keypair";
}
