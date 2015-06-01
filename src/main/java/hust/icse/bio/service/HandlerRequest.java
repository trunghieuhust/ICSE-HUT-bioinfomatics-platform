package hust.icse.bio.service;

import hust.icse.bio.infrastructure.User;
import hust.icse.bio.infrastructure.UserManagement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;

public class HandlerRequest {
	private static HandlerRequest instance = new HandlerRequest();

	public static HandlerRequest getInstance() {
		return instance;
	}

	public String createWorkflow(String username, String password,
			String workflow) {
		String workflowID = null;
		User user = getUser(username, password);
		if (user != null) {
			workflowID = WorkflowManagement.getInstance().createWorkflow(user,
					workflow);
		}
		return workflowID;
	}

	public boolean authenticate(String username, String password) {
		User user = getUser(username, password);
		if (user == null) {
			return false;
		} else {
			return true;
		}

	}

	public TaskResult getResult(String username, String password, String ID) {
		if (authenticate(username, password) == true) {
			return HandlerRequest.getInstance().getTaskResult(ID);
		} else {
			return null;
		}

	}

	private User getUser(String username, String password) {
		User user = UserManagement.getInstance().authenticate(username,
				password);
		return user;
	}

	public Status getStatus(String ID) {
		Status status = WorkflowManagement.getInstance().getStatus(ID);
		return status;
	}

	public TaskResult getTaskResult(String ID) {
		TaskResult result = WorkflowManagement.getInstance().getTaskResult(ID);
		return result;
	}

	public boolean signUp(String username, String password) {
		return UserManagement.getInstance().createUser(username, password);
	}

	public long uploadFile(String username, String password,
			FileUploader uploadFile) {
		User user = getUser(username, password);
		DataHandler dataHandler = uploadFile.getHandler();
		String etag = user.getStorageManagement().uploadFileFromInputStream(
				dataHandler, user.getStorageManagement().getUploadContainer());
		System.out.println("etag" + etag);
		return 10;
		// if (authenticate(username, password) == true) {
		// DataHandler dataHandler = uploadFile.getHandler();
		// try {
		// InputStream is = dataHandler.getInputStream();
		//
		// OutputStream os = new FileOutputStream(new File("/tmp/"
		// + uploadFile.getName()));
		// byte[] buffer = new byte[102400];
		// int byteRead = 0;
		// long receivedByte = 0;
		// while ((byteRead = is.read(buffer)) != -1) {
		// os.write(buffer, 0, byteRead);
		// receivedByte += byteRead;
		// }
		// os.flush();
		// os.close();
		// is.close();
		// return receivedByte;
		// } catch (Exception e) {
		// e.printStackTrace();
		// return -1;
		//
		// }
		// } else {
		// return -1;
		// }
	}

	public String[] getAllContainer(String username, String password) {
		User user = getUser(username, password);
		if (user != null) {
			return user.getStorageManagement().listContainers(username);
		} else {
			return null;
		}
	}

	public String[] getAllFileInContainer(String username, String password,
			String containerName) {
		User user = getUser(username, password);
		if (user != null) {
			return user.getStorageManagement().listFile(containerName);
		} else {
			return null;
		}
	}

	public boolean deleteFile(String username, String password,
			String containerName, String filename) {
		User user = getUser(username, password);
		if (user != null) {
			return user.getStorageManagement().deleteFile(filename,
					containerName);
		} else {
			return false;
		}
	}

	public boolean deleteContainer(String username, String password,
			String containerName) {
		User user = getUser(username, password);
		if (user != null) {
			return user.getStorageManagement().deleteContainer(containerName);
		} else {
			return false;
		}
	}

	public String getLinkFile(String username, String password,
			String containerName, String filename) {
		User user = getUser(username, password);
		if (user != null) {
			return user.getStorageManagement().getFileLink(filename,
					containerName);
		} else {
			return null;
		}
	}

	public String createWorkflowFromTemplate(String username, String password,
			String workflowName) {
		return null;
	}
}
