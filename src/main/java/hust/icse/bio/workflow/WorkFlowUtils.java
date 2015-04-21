package hust.icse.bio.workflow;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WorkFlowUtils {
	private static WorkFlowUtils instance;
	private final static String WORKFLOW = "workflow";
	private final static String ACTIVITIES = "activities";
	private final static String ACTIVITY = "activity";
	private final static String ACTIVITY_NAME = "name";
	private final static String TASK = "task";
	private final static String TOOL_ALIAS = "tool-alias";
	private final static String OUTPUT_FILES = "output-files";
	private final static String INPUT_FILES = "input-files";
	private final static String OUTPUT = "output";
	private final static String INPUT = "input";

	private final static String TOOLS = "tools";
	private final static String TOOL = "tool";
	private final static String ALIAS = "alias";
	private final static String NAME = "name";
	private final static String VERSION = "version";
	private final static String PACKAGE = "package";
	private final static String COMMAND = "command";
	private final static String EXECUTE = "execute";

	private final static String TEST = "<workflow><activities><activity name='aligment'><task><tool-alias>clustal</tool-alias><input-files input='input'></input-files><output-files output='output'></output-files></task><task><tool-alias>clustalo2</tool-alias><input-files input='input2'></input-files><output-files output='output2'></output-files></task></activity><activity name='fasttree'><task><tool-alias>fasttree</tool-alias><input-files input='output'></input-files><output-files output='output-fasttree'></output-files></task></activity></activities></workflow><tools><tool><alias>clustal</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command=' --infile=$input -outfile=$output --outfmt=clustal -v'></execute></tool><tool><alias>clustalo2</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command=' --infile=$input -outfile=$output --outfmt=clustal -v'></execute></tool><tool><alias>fasttree</alias><name>fasttree</name><version>2.1</version><package>fasttree</package><execute command='$input > $output'></execute></tool></tools>";

	public WorkFlowUtils() {

	}

	public static WorkFlowUtils getInstance() {
		if (instance == null) {
			instance = new WorkFlowUtils();
			return instance;
		} else {
			return instance;
		}
	}

	public ArrayList<Activity> parse(String workflow) {
		ArrayList<Activity> activityList = new ArrayList<Activity>();
		if (isValid(workflow)) {
			Document doc = Jsoup.parse(workflow);
			Elements activities = doc.select(ACTIVITIES);
			Elements activityElements = activities.select(ACTIVITY);
			// ArrayList<Tool> tools = parseTools(toolsNode);
			activityList = parseActivities(activityElements);
		}
		return activityList;
	}

	public ArrayList<Tool> parseTools(String workflow) {
		Document doc = Jsoup.parse(workflow);

		Elements toolsNode = doc.select(TOOLS).select(TOOL);

		ArrayList<Tool> tools = new ArrayList<Tool>();
		System.out.println(toolsNode.size());
		for (int i = 0; i < toolsNode.size(); i++) {
			Tool tool = new Tool();
			tool.setAlias(toolsNode.get(i).select(ALIAS).text());
			tool.setCommand(toolsNode.get(i).select(EXECUTE).attr(COMMAND));
			tool.setName(toolsNode.get(i).select(NAME).text());
			tool.setPackageName(toolsNode.get(i).select(PACKAGE).text());
			tool.setVersion(toolsNode.get(i).select(VERSION).text());
			tools.add(tool);
//			System.out.println(tool.toString());
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
			// activity.print();
			activities.add(activity);
		}
		return activities;
	}

	private ArrayList<Task> parseTask(Elements taskElement) {
		ArrayList<Task> tasks = new ArrayList<Task>();

		for (int i = 0; i < taskElement.size(); i++) {
			Task task = new Task();
			task.setAlias(taskElement.get(i).select(TOOL_ALIAS).text());
			task.setInputFile(taskElement.get(i).select(INPUT_FILES)
					.attr(INPUT));
			task.setOutputFile(taskElement.get(i).select(OUTPUT_FILES)
					.attr(OUTPUT));
			tasks.add(task);
//			System.out.println(task.toString());
		}
		return tasks;
	}

	public boolean isValid(String workflow) {
		return true;
	}

//	public static void main(String[] args) {
//		WorkFlowUtils wf = new WorkFlowUtils();
//		wf.parse(TEST);
//	}

}
