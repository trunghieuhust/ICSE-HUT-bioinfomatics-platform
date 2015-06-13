package hust.icse.bio.service;

import hust.icse.bio.dao.DAOFactory;
import hust.icse.bio.dao.StatiticsDAO;
import hust.icse.bio.infrastructure.User;
import hust.icse.bio.infrastructure.UserManagement;
import hust.icse.bio.tools.ToolManagement;
import hust.icse.bio.workflow.WorkflowManagement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
		user.getStorageManagement().uploadFileFromInputStream(dataHandler,
				user.getStorageManagement().getUploadContainer());
		return 10;
	}

	public List<Container> getAllContainer(String username, String password) {
		User user = getUser(username, password);
		if (user != null) {
			return user.getStorageManagement().listContainers(username);
		} else {
			return null;
		}
	}

	public List<hust.icse.bio.service.File> getAllFileInContainer(
			String username, String password, String containerName) {
		User user = getUser(username, password);
		if (user != null) {
			ArrayList<hust.icse.bio.service.File> fileList = user
					.getStorageManagement().containerDetails(containerName);

			return fileList;
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
			String workflowName, String[] inputValue) {
		User user = getUser(username, password);
		if (user != null) {
			return WorkflowManagement.getInstance().createWorkflowFromTemplate(
					user, workflowName);
		} else {
			return null;
		}
	}

	public boolean uploadToolPackage(String username, String password,
			FileUploader toolPackage) {
		User user = getUser(username, password);
		if (user != null) {
			DataHandler handler = toolPackage.getHandler();
			File toolpack = new java.io.File(toolPackage.getName());
			try {
				InputStream is = handler.getInputStream();

				OutputStream os = new FileOutputStream(toolpack);
				byte[] b = new byte[100000];
				int bytesRead = 0;
				while ((bytesRead = is.read(b)) != -1) {
					os.write(b, 0, bytesRead);
				}
				os.flush();
				os.close();
				is.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			return ToolManagement.getInstance().addToolPackage(
					toolPackage.getName(), user);
		} else {
			return false;
		}
	}

	public static void main(String[] args) {
		List<Container> a = getInstance().getAllContainer("ducdmk55",
				"ducdmk55@123");
		for (Container container : a) {
			System.out.println(container.getName() + " "
					+ container.getObjectCount() + " "
					+ container.getByteUsed());
			List<hust.icse.bio.service.File> fileList = container.getFileList();
			for (hust.icse.bio.service.File file : fileList) {
				System.out.println("\t" + file.getName() + " "
						+ file.getBytes());
			}
		}
	}

	public List<Statitics> getStatitics(String username, String password) {
		User user = getUser(username, password);
		if (user != null) {
			StatiticsDAO statiticsDAO = DAOFactory.getDAOFactory(
					DAOFactory.MYSQL).getStatiticsDAO();
			return statiticsDAO.getStatitics(user);
		} else {
			return null;
		}
	}
}
