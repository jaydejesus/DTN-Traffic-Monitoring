package core;

public class TripProperties {
	
	private Coord start;
	private Coord destination;
	private double travelTime;
	private double startTime;
	private double endTime;
	
	public TripProperties(Coord start, Coord destination, double travelTime) {
		this.start = start;
		this.destination = destination;
	}
	
	public Coord getTripStart() {
		return this.start;
	}
	
	public Coord getTripDestination() {
		return this.destination;
	}
	
	public void setTripStartTime(double startTime) {
		this.startTime = startTime;
	}
	
	public void setTripEndTime(double endTime) {
		this.endTime = endTime;
	}
	
	public double getTripStartTime() {
		return startTime;
	}
	
	public double getTripEndTime() {
		return endTime;
	}
	
	public double getTravelTime() {
		return travelTime;
	}
	
	public void setTravelTime(double time) {
		travelTime = time;
	}
	
	public String toString() {
		String str = "from:" + getTripStart() + "-to: " + getTripDestination() + " start_time:" + startTime + " end_time:" + endTime + " for " + travelTime + "s";
		
		return str;
	}
}
