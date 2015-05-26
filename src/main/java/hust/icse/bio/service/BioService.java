package hust.icse.bio.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(name = "bio", targetNamespace = "http://service.bio.icse.hust/")
public interface BioService {
	@WebMethod
	public String createWorkflow(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "workflow") String workflow);

	@WebMethod
	public boolean authenticate(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password);

	@WebMethod
	public TaskResult getResult(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "ID") String ID);

	@WebMethod
	public Status getStatus(@WebParam(name = "ID") String ID);

	@WebMethod
	public long uploadData(
			@WebParam(name = "uploadFile") FileUploader uploadFile);
}
