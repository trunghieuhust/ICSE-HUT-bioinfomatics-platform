package hust.icse.bio.service;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Container {
	long byteUsed;
	String name;
	long objectCount;
	List<File> fileList;

	public Container() {
		fileList = new ArrayList<File>();
	}

	public void setByteUsed(long byteUsed) {
		this.byteUsed = byteUsed;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setObjectCount(long objectCount) {
		this.objectCount = objectCount;
	}

	public long getByteUsed() {
		return byteUsed;
	}

	public String getName() {
		return name;
	}

	public long getObjectCount() {
		return objectCount;
	}

	public void setFileList(List<File> fileList) {
		this.fileList = fileList;
	}

	public List<File> getFileList() {
		return fileList;
	}

	public boolean insertToFileList(File file) {
		if (file != null) {
			return fileList.add(file);
		} else {
			return false;
		}
	}
}
