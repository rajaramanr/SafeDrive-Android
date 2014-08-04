package edu.cmu.Model;

public class ViolationsModel {

	private String date;
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getNoOfViolations() {
		return noOfViolations;
	}

	public void setNoOfViolations(String noOfViolations) {
		this.noOfViolations = noOfViolations;
	}

	private String noOfViolations;
	
	public ViolationsModel(String date,String noOfViolations){
		this.date = date;
		this.noOfViolations = noOfViolations;
	}
}
