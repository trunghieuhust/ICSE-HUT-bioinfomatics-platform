package hust.icse.bio.service;

import junit.framework.TestCase;

import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

public class ServiceTest extends TestCase {
	private final static String TEST = "<workflow><activities><activity name='aligment'><task><tool-alias>clustal</tool-alias><input-files input='input'></input-files><output-files output='output'></output-files></task><task><tool-alias>clustalo2</tool-alias><input-files input='input2'></input-files><output-files output='output2'></output-files></task></activity><activity name='fasttree'><task><tool-alias>fasttree</tool-alias><input-files input='output'></input-files><output-files output='output-fasttree'></output-files></task></activity></activities></workflow><tools><tool><alias>clustal</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command=' --infile=$input --outfile=$output --outfmt=clustal -v'></execute></tool><tool><alias>clustalo2</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command=' --infile=$input --outfile=$output --outfmt=clustal -v'></execute></tool><tool><alias>fasttree</alias><name>fasttree</name><version>2.1</version><package>fasttree</package><execute command='$input > $output'></execute></tool></tools>";

	public void testService() throws Exception {
		//BioService bc = newBioClient();
		// String ID = bc.submit("ducdmk55", "ducdmk55@123", TEST);
		// System.out.println(ID);
		//System.err.println(bc.getStatus("123").getName());
		//Thread.sleep(3000);
	}

	protected BioService newBioClient() {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(BioService.class);
		factory.setAddress("http://localhost:8080/BioService/BioService");
		return (BioService) factory.create();
	}
	//
	// static {
	// ServerFactoryBean svrFactory = new ServerFactoryBean();
	// svrFactory.getServiceFactory().setDataBinding(new
	// JaxWsProxyFactoryBean());
	// svrFactory.setServiceClass(BioService.class);
	// svrFactory.setAddress("http://localhost:8081/BioService");
	// svrFactory.setServiceBean(new BioServiceImpl());
	// svrFactory.create();
	// }
}
