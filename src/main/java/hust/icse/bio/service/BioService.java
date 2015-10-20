package hust.icse.bio.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(name = "bio", targetNamespace = "http://service.bio.icse.hust/")
public interface BioService {
	@WebMethod
	public String createWorkflow(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "workflow") String workflow);

	public String createWorkflowFromTemplate(
			@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "workflowName") String workflowName,
			@WebParam(name = "inputValue") String[] inputValue);

	@WebMethod
	public boolean authenticate(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password);

	@WebMethod
	public boolean signUp(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password);

	@WebMethod
	public TaskResult getResult(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "ID") String ID);

	@WebMethod
	public Status getStatus(@WebParam(name = "ID") String ID);

	@WebMethod
	public long uploadData(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "uploadFile") FileUploader uploadFile);

	@WebMethod
	public List<Container> getAllContainer(
			@WebParam(name = "username") String username,
			@WebParam(name = "password") String password);

	@WebMethod
	public List<File> getAllFileInContainer(
			@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "containerName") String containerName);

	@WebMethod
	public boolean deleteFile(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "containerName") String containerName,
			@WebParam(name = "filename") String filename);

	@WebMethod
	public boolean deleteContainer(
			@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "containerName") String containerName);

	@WebMethod
	public String getLinkFile(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "containerName") String containerName,
			@WebParam(name = "filename") String filename);

	@WebMethod
	public boolean uploadToolPackage(
			@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "toolPackage") FileUploader toolPackage);

	@WebMethod
	public List<Statitics> getStatitics(
			@WebParam(name = "username") String username,
			@WebParam(name = "password") String password);
}
