package hust.icse.bio.service;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;

public class FileUploader {
	private String name;
	private DataHandler dataHandler;

	public String getName() {
		return this.name;
	}

	public void setName(String Name) {
		this.name = Name;
	}

	public DataHandler getHandler() {
		return this.dataHandler;
	}

	@XmlElement(name = "File", required = true)
	@XmlMimeType("application/octet-stream")
	public void setDataHandlerfile(DataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}

}
