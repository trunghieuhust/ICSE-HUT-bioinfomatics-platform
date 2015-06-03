package hust.icse.bio.workflow;

import hust.icse.bio.tools.Tool;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WorkFlowUtils {
	private static WorkFlowUtils instance = new WorkFlowUtils();
	private final static String WORKFLOW = "workflow";
	private final static String ACTIVITIES = "activities";
	private final static String ACTIVITY = "activity";
	private final static String ACTIVITY_NAME = "name";
	private final static String TASK = "task";
	private final static String TOOL_ALIAS = "tool-alias";
	private final static String OUTPUT_FILES = "output-files";
	private final static String INPUT_FILES = "input-files";
	private final static String TOOLS = "tools";
	private final static String TOOL = "tool";
	private final static String ALIAS = "alias";
	private final static String NAME = "name";
	private final static String VERSION = "version";
	private final static String PACKAGE = "package";
	private final static String COMMAND = "command";
	private final static String EXECUTE = "execute";
	private final static String SAVE_AS_TEMPLATE = "save-as-template";
	private final static String AUTO_DELETE_AFTER = "auto-delete-after";
	private final static String TEST = "<workflow name='2step' save-as-template='true'><activities><activity name='aligment'><task name='clustal1'><tool-alias>clustal</tool-alias><input-files in1='actin' in2='actin2' in3='actin3'></input-files><output-files out1='output1' out2='output2'></output-files><flavor>small</flavor></task><task name='clustal2'><tool-alias>clustalo2</tool-alias><input-files input='actin'></input-files><output-files output='output2'></output-files></task></activity><activity name='fasttree'><task name='fasttree'><tool-alias>fasttree</tool-alias><input-files input='output1'></input-files><output-files output='output-fasttree'></output-files><flavor>small</flavor></task></activity></activities></workflow><tools><tool><alias>clustal</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command='--infile=$input --outfile=$output -v'></execute></tool><tool><alias>clustalo2</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command='--infile=$input --outfile=$output --outfmt=clustal -v'></execute></tool><tool><alias>fasttree</alias><name>fasttree</name><version>2.1</version><package>fasttree</package><execute command='$input > $output'></execute></tool></tools>";

	public WorkFlowUtils() {

	}

	public static WorkFlowUtils getInstance() {
		return instance;
	}

	public WorkflowInfo parse(String workflow) {
		ArrayList<Activity> activityList = new ArrayList<Activity>();
		ArrayList<Tool> toolList = new ArrayList<Tool>();
		WorkflowInfo info = new WorkflowInfo();
		if (isValid(workflow)) {
			Document doc = Jsoup.parse(workflow);
			Elements workflowElements = doc.select(WORKFLOW);
			Elements toolsNode = doc.select(TOOLS).select(TOOL);
			Elements activities = workflowElements.select(ACTIVITIES);
			Elements activityElements = activities.select(ACTIVITY);
			toolList = parseTools(toolsNode);
			activityList = parseActivities(activityElements);
			info.setActivityList(activityList);
			info.setToolList(toolList);
			if (workflowElements.hasAttr(NAME)) {
				info.setName(workflowElements.attr(NAME));
			}
			if (workflowElements.hasAttr(SAVE_AS_TEMPLATE)) {
				String saveAs = workflowElements.attr(SAVE_AS_TEMPLATE);
				System.out.println("saveAs: " + saveAs);
				if (saveAs.equals("true")) {
					info.setSaveAsTemplate(true);
				} else {
					info.setSaveAsTemplate(false);
				}
			}
			if (workflowElements.hasAttr(AUTO_DELETE_AFTER)) {
				String periodString = workflowElements.attr(AUTO_DELETE_AFTER);
				System.out.println("period: " + periodString);
				long period = convertToDateTime(periodString);
				if (period > 0) {
					info.setDeleteAfter(period);
				}
			}
		}
		return info;
	}

	public String convertToTemplate(String rawXML) {
		Document doc = Jsoup.parse(rawXML);
		Elements workflowElements = doc.select(WORKFLOW);
		if (workflowElements.hasAttr(SAVE_AS_TEMPLATE)) {
			workflowElements.removeAttr(SAVE_AS_TEMPLATE);
		}
		return doc.toString();
	}

	public String mapInputToTemplate(String rawXML, String[] value) {
		Document doc = Jsoup.parse(rawXML);
		Elements inputElements = doc.select(WORKFLOW).select(ACTIVITIES)
				.select(INPUT_FILES);
		if (value == null || value.length == 0) {
			return null;
		} else {
			int currentIndex = 0;
			for (int i = 0; i < inputElements.size(); i++) {
				List<Attribute> attList = inputElements.get(i).attributes()
						.asList();
				for (Attribute attribute : attList) {
					if (currentIndex < value.length) {
						attribute.setValue(value[currentIndex]);
						currentIndex++;
					} else {
						break;
					}
				}
			}
		}

		return doc.toString();
	}

	private ArrayList<Tool> parseTools(Elements toolsElement) {
		ArrayList<Tool> tools = new ArrayList<Tool>();
		// System.out.println(toolsElement.size());
		for (int i = 0; i < toolsElement.size(); i++) {
			Tool tool = new Tool();
			tool.setAlias(toolsElement.get(i).select(ALIAS).text());
			tool.setCommand(toolsElement.get(i).select(EXECUTE).attr(COMMAND));
			tool.setName(toolsElement.get(i).select(NAME).text());
			tool.setPackageName(toolsElement.get(i).select(PACKAGE).text());
			tool.setVersion(toolsElement.get(i).select(VERSION).text());
			tools.add(tool);
		}
		return tools;
	}

	private ArrayList<Activity> parseActivities(Elements activityElements) {
		ArrayList<Activity> activities = new ArrayList<Activity>();
		for (int i = 0; i < activityElements.size(); i++) {
			Activity activity = new Activity(activityElements.get(i).attr(
					ACTIVITY_NAME));
			activity.insertMultipleTask(parseTask(activityElements.get(i)
					.select(TASK)));
			activities.add(activity);
		}
		return activities;
	}

	private ArrayList<Task> parseTask(Elements taskElement) {
		ArrayList<Task> tasks = new ArrayList<Task>();

		for (int i = 0; i < taskElement.size(); i++) {
			Task task = new Task();
			task.setAlias(taskElement.get(i).select(TOOL_ALIAS).text());
			Elements inputElement = taskElement.get(i).select(INPUT_FILES);
			for (int j = 0; j < inputElement.size(); j++) {
				Attributes attributes = inputElement.get(j).attributes();
				ArrayList<String> input = new ArrayList<String>();
				ArrayList<String> inputValue = new ArrayList<String>();
				for (Attribute attribute : attributes) {
					input.add(attribute.getKey());
					inputValue.add(attribute.getValue());
				}
				task.setInput(input.toArray(new String[input.size()]));
				task.setInputValue(inputValue.toArray(new String[inputValue
						.size()]));
			}
			Elements outputElement = taskElement.get(i).select(OUTPUT_FILES);
			for (int j = 0; j < outputElement.size(); j++) {
				Attributes attributes = outputElement.get(j).attributes();
				ArrayList<String> output = new ArrayList<String>();
				ArrayList<String> outputValue = new ArrayList<String>();
				for (Attribute attribute : attributes) {
					output.add(attribute.getKey());
					outputValue.add(attribute.getValue());
				}
				task.setOutput(output.toArray(new String[output.size()]));
				task.setOutputValue(outputValue.toArray(new String[outputValue
						.size()]));
			}

			task.setName(taskElement.get(i).attr(NAME));
			tasks.add(task);
		}
		return tasks;
	}

	public boolean isValid(String workflow) {
		return true;
	}

	/**
	 * 
	 * @param dateTime
	 *            . Example 1d-12h-20m-10s
	 */
	private long convertToDateTime(String dateTime) {
		String[] splitted = dateTime.split("-");
		long second = 0;
		try {
			for (int i = 0; i < splitted.length; i++) {
				if (splitted[i].contains("d")) {
					String dayStr = splitted[i].replace("d", "");
					int day = Integer.parseInt(dayStr);
					second += day * 24 * 60 * 60;
				}
				if (splitted[i].contains("h")) {
					String hourStr = splitted[i].replace("h", "");
					int hour = Integer.parseInt(hourStr);
					second += hour * 60 * 60;
				}
				if (splitted[i].contains("m")) {
					String minStr = splitted[i].replace("m", "");
					int min = Integer.parseInt(minStr);
					second += min * 60;
				}
				if (splitted[i].contains("s")) {
					String secStr = splitted[i].replace("s", "");
					int sec = Integer.parseInt(secStr);
					second += sec;
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			second = -1;
		}
		return second;
	}

	public static void main(String[] args) {
		String test = "<workflow name='2step' save-as-template='true'><activities><activity name='aligment'><task name='clustal1'><tool-alias>clustal</tool-alias><input-files in1='actin' in2='123'></input-files><output-files output='output1'></output-files><flavor>small</flavor></task><task name='clustal2'><tool-alias>clustalo2</tool-alias><input-files input='actin'></input-files><output-files output='output2'></output-files></task></activity><activity name='fasttree'><task name='fasttree'><tool-alias>fasttree</tool-alias><input-files input='output1'></input-files><output-files output='output-fasttree'></output-files><flavor>small</flavor></task></activity></activities></workflow><tools><tool><alias>clustal</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command='--infile=$input --outfile=$output -v'></execute></tool><tool><alias>clustalo2</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command='--infile=$input --outfile=$output --outfmt=clustal -v'></execute></tool><tool><alias>fasttree</alias><name>fasttree</name><version>2.1</version><package>fasttree</package><execute command='$input > $output'></execute></tool></tools>";
		// String map = WorkFlowUtils.getInstance().mapInputToTemplate(test,
		// new String[] { "new", "new1", "new2", "output2" });
		WorkflowInfo wf = getInstance().parse(TEST);
		ArrayList<Activity> list = wf.getActivityList();
		for (Activity activity : list) {
			ArrayList<Task> taskList = activity.getTaskList();
			for (Task task : taskList) {
				System.out.println(task.toString());
			}
		}
	}
}
