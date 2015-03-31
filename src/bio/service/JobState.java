package bio.service;

public class JobState {
	public static int UNKNOWN_JOB_STATE = 0;
	public static int JOB_STOP_WITH_ERROR = 1;
	public static int JOB_COMPLETE_SUCCESSFULLY = 2;
	public static int JOB_STILL_BEING_PROCESSED = 3;
	public static int JOB_NOT_FOUND = 4;

	private static String[] JOB_DESCRIPTION = {
			"unknowing job state",
			"job has stopped in an error state, check the message field",
			"job completed successfully, the appropriate results fields will contain data",
			"job still being processed",
			"can not find a job with the given ID for that API" };

	private int currentState;

	public JobState() {
		currentState = UNKNOWN_JOB_STATE;
	}

	public JobState(int jobStateCode) {
		if (jobStateCode >= 0 && jobStateCode <= 4) {
			currentState = jobStateCode;
		} else {
			currentState = UNKNOWN_JOB_STATE;
		}
	}

	public int getState() {
		return currentState;
	}

	public String getDescription() {
		return JOB_DESCRIPTION[currentState];
	}

	public boolean updateState(int jobStateCode) {
		if (jobStateCode >= 0 && jobStateCode <= 4) {
			currentState = jobStateCode;
			return true;
		} else
			return false;
	}
}
