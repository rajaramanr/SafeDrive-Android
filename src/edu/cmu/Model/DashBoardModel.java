package edu.cmu.Model;

public class DashBoardModel {

	public DashBoardModel(String currentSpeed, String speedLimit) {

		this.currentSpeed = currentSpeed;
		this.speedLimit = speedLimit;
	}

	private String currentSpeed;

	public String getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(String currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	private String speedLimit;

	public String getSpeedLimit() {
		return speedLimit;
	}

	public void setSpeedLimit(String speedLimit) {
		this.speedLimit = speedLimit;
	}

}
