package core;

public class TripProperties {
	
	private Coord start;
	private Coord destination;
	private double travelTime;
	private double startTime;
	private double endTime;
	private double tripTravelDistance;
	private int rerouteCtr = 0;
	private String tripID;
	
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
		String str = "\t" + getTripStart() + "->" + getTripDestination() + " start_time:" + startTime + 
				" end_time:" + endTime + " reroute_count: " + getRerouteCtr() + " travel_time: " + travelTime + "s";
		
		return str;
	}

	public int getRerouteCtr() {
		return rerouteCtr;
	}

	public void setRerouteCtr(int rerouteCtr) {
		this.rerouteCtr = rerouteCtr;
	}

	public double getTripTravelDistance() {
		return tripTravelDistance;
	}

	public void setTripTravelDistance(double tripTravelDistance) {
		this.tripTravelDistance = tripTravelDistance;
	}

	public String getTripID() {
		return tripID;
	}

	public void setTripID(String tripID) {
		this.tripID = tripID;
	}
}
