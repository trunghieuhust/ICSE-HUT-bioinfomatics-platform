package hust.icse.bio.service;

import java.util.logging.Level;

import javax.jws.WebService;

import org.apache.cxf.common.logging.Slf4jLogger;

@WebService(endpointInterface = "hust.icse.bio.service.BioService")
public class BioServiceImpl implements BioService {
	static {
		Slf4jLogger.getGlobal().setLevel(Level.OFF);
	}

	@Override
	public String createWorkflow(String username, String password,
			String workflow) {
		return HandlerRequest.getInstance().createWorkflow(username, password,
				workflow);
	}

	@Override
	public TaskResult getResult(String username, String password, String ID) {
		return HandlerRequest.getInstance().getResult(username, password, ID);
	}

	@Override
	public Status getStatus(String ID) {
		return HandlerRequest.getInstance().getStatus(ID);
	}

	@Override
	public boolean authenticate(String username, String password) {
		return HandlerRequest.getInstance().authenticate(username, password);
	}

	@Override
	public long uploadData(FileUploader uploadFile) {
		return HandlerRequest.getInstance().uploadFile(uploadFile);
	}

}
