package bio.service;

public class JobState {
	public static int UNKNOWN_JOB_STATE = -1;
	public static int JOB_STOP_WITH_ERROR = 0;
	public static int JOB_COMPLETE_SUCCESSFULLY = 1;
	public static int JOB_STILL_BEING_PROCESSED = 2;
	public static int JOB_NOT_FOUND = 3;

	private static String[] JOB_DESCRIPTION = {
			"Unknowing job state.",
			"job has stopped in an error state, check the message field",
			"job completed successfully, the appropriate results fields will contain data",
			"can not find a job with the given ID for that API" };

	private int currentState;

	public JobState() {
		currentState = UNKNOWN_JOB_STATE;
	}

	public JobState(int jobStateCode) {
		if (jobStateCode >= -1 && jobStateCode <= 3) {
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
		if (jobStateCode >= -1 && jobStateCode <= 3) {
			currentState = jobStateCode;
			return true;
		} else
			return false;
	}
}
