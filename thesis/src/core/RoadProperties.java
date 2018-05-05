package core;

public class RoadProperties {
	
	private String roadName;
	private int density;
	private double averageSpeed;
	
	public RoadProperties(String roadName, double averageSpeed, int density) {
		this.roadName = roadName;
		this.averageSpeed = averageSpeed;
		this.density = density;
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
}
