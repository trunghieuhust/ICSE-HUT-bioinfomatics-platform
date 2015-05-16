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
	public String submit(String username, String password, String workflow) {
		String ID = "";
		User user = UserManagement.getInstance().login(username, password);
		if (user == null)
			return "Invalid user name or password.";
		else {
			ID = HandlerRequest.getInstance().submit(user, workflow);
			return ID;
		}
	}

	@Override
	public TaskResult getResult(String username, String password, String ID) {
		if (login(username, password) == true) {
			return HandlerRequest.getInstance().getTaskResult(ID);
		} else {
			return null;
		}
	}

	@Override
	public Status getStatus(String ID) {
		return HandlerRequest.getInstance().getStatus(ID);
	}

	@Override
	public String getAllID(String workflowID) {
		return null;
	}

	@Override
	public boolean login(String username, String password) {
		User user = UserManagement.getInstance().login(username, password);
		if (user == null) {
			return false;
		} else {
			return true;
		}
	}

}
