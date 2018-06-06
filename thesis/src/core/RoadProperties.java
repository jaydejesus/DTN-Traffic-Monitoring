package core;

public class RoadProperties {
	
	private String roadName;
	private int density;
	private double averageSpeed;
	private String condition = "";
	private Road road;
	private double infoRange;
	private int nrOfKnownRoads;
	private int averageNrOfKnownRoads;
	private double averageInfoRange;
	
	public RoadProperties(String roadName, Road road, double averageSpeed, int density, String condition) {
		this.roadName = roadName;
		this.road = road;
		this.averageSpeed = averageSpeed;
		this.density = density;
		this.condition = condition;
	}

	public String getRoadName() {
		return roadName;
	}
	
	public Road getRoad() {
		return road;
	}
	
	public void setRoad(Road r) {
		road = r;
	}
	
	public int getRoadDensity() {
		return density;
	}
	
	public void setRoadDensity(int d) {
		density = d;
	}
	
	public double getAverageSpeedOfRoad() {
		return averageSpeed;
	}
	
	public void setAverageSpeedOfRoad(double average) {
		averageSpeed = average;
	}
	
	public String getCondition() {
		return condition;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}

	public double getInfoRange() {
		return infoRange;
	}

	public void setInfoRange(double infoRange) {
		this.infoRange = infoRange;
	}

	public int getNrOfKnownRoads() {
		return nrOfKnownRoads;
	}

	public void setNrOfKnownRoads(int nrOfKnownRoads) {
		this.nrOfKnownRoads = nrOfKnownRoads;
	}
}
