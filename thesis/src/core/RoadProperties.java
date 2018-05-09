package core;

public class RoadProperties {
	
	private String roadName;
	private int density;
	private double averageSpeed;
	private String condition = "";
	
	public RoadProperties(String roadName, double averageSpeed, int density, String condition) {
		this.roadName = roadName;
		this.averageSpeed = averageSpeed;
		this.density = density;
		this.condition = condition;
	}

	public String getRoadName() {
		return roadName;
	}
	
	public int getRoadDensity() {
		return density;
	}
	
	public double getAverageSpeedOfRoad() {
		return averageSpeed;
	}
	
	public String getCondition() {
		return condition;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
}
