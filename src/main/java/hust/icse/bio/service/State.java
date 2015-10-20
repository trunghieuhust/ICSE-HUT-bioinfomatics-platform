package hust.icse.bio.service;

public class State {
	public static final int UNKNOWN_STATE = 0;
	public static final int STOP_WITH_ERROR = 1;
	public static final int COMPLETE_SUCCESSFULLY = 2;
	public static final int STILL_BEING_PROCESSED = 3;
	public static final int NOT_FOUND = 4;
	public static final int QUEUEING = 5;
	private static final String[] DESCRIPTION = {
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
		if (jobStateCode >= 0 && jobStateCode <= 5) {
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

	public static String getDescription(int stateCode) {
		if (stateCode >= 0 && stateCode <= 5) {
			return DESCRIPTION[stateCode];
		} else {
			return DESCRIPTION[UNKNOWN_STATE];
		}
	}

	public boolean updateState(int jobStateCode) {
		if (jobStateCode >= 0 && jobStateCode <= 5) {
			currentState = jobStateCode;
			return true;
		} else
			return false;
	}
}
