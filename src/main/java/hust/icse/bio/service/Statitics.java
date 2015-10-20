package hust.icse.bio.service;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Statitics {
	private String flavor;
	private long time;

	public Statitics() {
		// TODO Auto-generated constructor stub
	}

	public void setFlavor(String flavor) {
		this.flavor = flavor;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getFlavor() {
		return flavor;
	}

	public long getTime() {
		return time;
	}
}
