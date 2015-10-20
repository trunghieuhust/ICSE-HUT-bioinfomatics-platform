package hust.icse.bio.service;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class File {
	String name;
	long bytes;
	String fileURL;

	public File() {
	}

	public long getBytes() {
		return bytes;
	}

	public String getName() {
		return name;
	}

	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileURL() {
		return fileURL;
	}

	public void setFileURL(String fileURL) {
		this.fileURL = fileURL;
	}
}
