package hust.icse.bio.workflow;

import hust.icse.bio.tools.Tool;

import java.util.ArrayList;

public class WorkflowInfo {
	private String name = "";
	private ArrayList<Activity> activityList = null;
	private ArrayList<Tool> toolList = null;
	private boolean saveAsTemplate = false;
	private long deleteAfter = 0;

	public WorkflowInfo(ArrayList<Activity> activityList,
			ArrayList<Tool> toolList, boolean saveAsTemplate, long deleteAfter) {
		this.activityList = activityList;
		this.toolList = toolList;
		this.saveAsTemplate = saveAsTemplate;
		this.deleteAfter = deleteAfter;
	}

	public WorkflowInfo() {
	}

	public ArrayList<Activity> getActivityList() {
		return activityList;
	}

	public long getDeleteAfter() {
		return deleteAfter;
	}

	public String getName() {
		return name;
	}

	public ArrayList<Tool> getToolList() {
		return toolList;
	}

	public boolean isSaveAsTemplate() {
		return saveAsTemplate;
	}

	public void setActivityList(ArrayList<Activity> activityList) {
		this.activityList = activityList;
	}

	public void setDeleteAfter(long deleteAfter) {
		this.deleteAfter = deleteAfter;
	}

	public void setSaveAsTemplate(boolean saveAsTemplate) {
		this.saveAsTemplate = saveAsTemplate;
	}

	public void setToolList(ArrayList<Tool> toolList) {
		this.toolList = toolList;
	}

	public void setName(String name) {
		this.name = name;
	}
}
