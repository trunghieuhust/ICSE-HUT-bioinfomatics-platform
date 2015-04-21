package hust.icse.bio.service;

/**
 * Hello world!
 *
 */
public class BioServiceImpl implements BioService {

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

	public String getResult(String username, String password, String ID) {

		return null;
	}

	public int getStatus(String ID) {
		int status = -1;
		status = WorkflowManagement.getInstance().getState(ID);
		return status;
	}
}
