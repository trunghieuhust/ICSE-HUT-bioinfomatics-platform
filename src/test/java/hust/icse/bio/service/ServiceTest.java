package hust.icse.bio.service;

import junit.framework.TestCase;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.frontend.ServerFactoryBean;

public class ServiceTest extends TestCase {
	private final static String TEST = "<workflow><activities><activity name='aligment'><task><tool-alias>clustal</tool-alias><input-files input='input'></input-files><output-files output='output'></output-files></task><task><tool-alias>clustalo2</tool-alias><input-files input='input2'></input-files><output-files output='output2'></output-files></task></activity><activity name='fasttree'><task><tool-alias>fasttree</tool-alias><input-files input='output'></input-files><output-files output='output-fasttree'></output-files></task></activity></activities></workflow><tools><tool><alias>clustal</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command=' --infile=$input --outfile=$output --outfmt=clustal -v'></execute></tool><tool><alias>clustalo2</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command=' --infile=$input --outfile=$output --outfmt=clustal -v'></execute></tool><tool><alias>fasttree</alias><name>fasttree</name><version>2.1</version><package>fasttree</package><execute command='$input > $output'></execute></tool></tools>";

	public void testService() throws Exception {
		// ServerFactoryBean svrFactory = new ServerFactoryBean();
		// svrFactory.getServiceFactory().setDataBinding(new
		// AegisDatabinding());
		// svrFactory.setServiceClass(BioService.class);
		// svrFactory.setAddress("http://localhost:8082/BioService");
		// svrFactory.setServiceBean(new BioServiceImpl());
		// svrFactory.create();
		// //
		// Thread.sleep(300000); // A little delay to allow manual testing.
		BioService bc = newBioClient();
		String ID = bc.submit("ducdmk55", "ducdmk55@123", TEST);
		System.out.println(ID);
		while (bc.getStatus(ID) != State.COMPLETE_SUCCESSFULLY) {
			Thread.sleep(30000);
		}
	}

	protected BioService newBioClient() {
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(BioService.class);
		factory.getServiceFactory().setDataBinding(new AegisDatabinding());
		factory.setAddress("http://localhost:8081/BioService");
		return (BioService) factory.create();
	}

	static {
		ServerFactoryBean svrFactory = new ServerFactoryBean();
		svrFactory.getServiceFactory().setDataBinding(new AegisDatabinding());
		svrFactory.setServiceClass(BioService.class);
		svrFactory.setAddress("http://localhost:8081/BioService");
		svrFactory.setServiceBean(new BioServiceImpl());
		svrFactory.create();
	}
}
