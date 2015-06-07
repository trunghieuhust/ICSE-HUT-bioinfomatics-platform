package hust.icse.bio.service;

import java.util.List;
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
	public long uploadData(String username, String password,
			FileUploader uploadFile) {
		return HandlerRequest.getInstance().uploadFile(username, password,
				uploadFile);
	}

	@Override
	public List<Container> getAllContainer(String username, String password) {
		return HandlerRequest.getInstance().getAllContainer(username, password);
	}

	@Override
	public List<File> getAllFileInContainer(String username, String password,
			String containerName) {
		return HandlerRequest.getInstance().getAllFileInContainer(username,
				password, containerName);
	}

	@Override
	public boolean deleteFile(String username, String password,
			String containerName, String filename) {
		return HandlerRequest.getInstance().deleteFile(username, password,
				containerName, filename);
	}

	@Override
	public boolean deleteContainer(String username, String password,
			String containerName) {
		return HandlerRequest.getInstance().deleteContainer(username, password,
				containerName);
	}

	@Override
	public boolean signUp(String username, String password) {
		return HandlerRequest.getInstance().signUp(username, password);
	}

	@Override
	public String getLinkFile(String username, String password,
			String containerName, String filename) {
		return HandlerRequest.getInstance().getLinkFile(username, password,
				containerName, filename);
	}

	@Override
	public String createWorkflowFromTemplate(String username, String password,
			String workflowName, String[] inputValue) {
		return HandlerRequest.getInstance().createWorkflowFromTemplate(
				username, password, workflowName, inputValue);
	}

	@Override
	public boolean uploadToolPackage(String username, String password,
			FileUploader toolPackage) {
		return HandlerRequest.getInstance().uploadToolPackage(username,
				password, toolPackage);
	}

}
