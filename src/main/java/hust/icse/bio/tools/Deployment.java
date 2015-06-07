package hust.icse.bio.tools;

import hust.icse.bio.infrastructure.VM;

public abstract class Deployment {
	public static final int UBUNTU = 0;

	public static Deployment getDeployment(int whichFactory) {
		switch (whichFactory) {
		case UBUNTU:
			return new UbuntuDeployment();
		default:
			return null;
		}
	}

	public abstract boolean deploy(Tool tool, VM vm);

	public abstract boolean isCommandAvailable(String command, VM vm);
}
