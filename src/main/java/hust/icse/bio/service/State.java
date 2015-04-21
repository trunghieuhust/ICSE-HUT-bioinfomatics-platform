package hust.icse.bio.service;

public class State {
	public static int UNKNOWN_STATE = 0;
	public static int STOP_WITH_ERROR = 1;
	public static int COMPLETE_SUCCESSFULLY = 2;
	public static int STILL_BEING_PROCESSED = 3;
	public static int NOT_FOUND = 4;
	public static int QUEUEING = 5;
	private static String[] DESCRIPTION = {
			"unknowing state",
			"has stopped in an error state, check the message field",
			"completed successfully, the appropriate results fields will contain data",
			"still being processed",
			"can not find a job with the given ID for that API", "Queueing." };

	private int currentState;

	public State() {
		currentState = UNKNOWN_STATE;
	}

	public State(int jobStateCode) {
		if (jobStateCode >= 0 && jobStateCode <= 4) {
			currentState = jobStateCode;
		} else {
			currentState = UNKNOWN_STATE;
		}
	}

	public int getState() {
		return currentState;
	}

	public String getDescription() {
		return DESCRIPTION[currentState];
	}

	public boolean updateState(int jobStateCode) {
		if (jobStateCode >= 0 && jobStateCode <= 4) {
			currentState = jobStateCode;
			return true;
		} else
			return false;
	}
}
