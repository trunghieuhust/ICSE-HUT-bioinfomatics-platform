package hust.icse.bio.service;

public interface BioService {

	public String submit(String username, String password, String workflow);

	public String getResult(String username, String password, String ID);

	public int getStatus(String ID);
}
