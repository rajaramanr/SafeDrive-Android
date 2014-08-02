package edu.cmu.Model;

public class CarDataModel {
	
	private double latitude;
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(double speedLimit) {
		this.currentSpeed = speedLimit;
	}

	private double longitude;
	private double currentSpeed;
	
	public CarDataModel(double speedLimit, double latitude, double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
		this.currentSpeed = speedLimit;
	}

}
