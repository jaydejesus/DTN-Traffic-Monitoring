package report;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import core.DTNHost;
import core.TripProperties;

public class TravelTimeReporter extends Report{
	private static HashMap<DTNHost, List<TripProperties>> hash = new HashMap<DTNHost, List<TripProperties>>();
	private static List<String> list = new ArrayList<String>();
	private List<TripProperties> tripList = new ArrayList<TripProperties>();
	int ctr = 0;
	
	public void createReport(DTNHost host, TripProperties trip) {
		String from = "From: " + trip.getTripStart();
		String to = "To: " + trip.getTripDestination();
		String start = "Start Time: " + trip.getTripStartTime();
		String end = "End Time: " + trip.getTripEndTime();
		String travelTime = "Travel Time: " + trip.getTravelTime();
		String endLoc = "End_loc: " + trip.getEndLocation();
		
		if(!hash.containsKey(host)) {
			tripList.clear();
			tripList.add(trip);
			hash.put(host, tripList);
		}
		//if host is already stored in hash
		else {
			hash.get(host).add(trip);
		}
		
		String str = String.format("%4s%5s%s%3s%s%3s%s%3s%s%3s%s%3s%s", host, ' ', from, ' ', to, ' ', endLoc, ' ', start, ' ', end, ' ', travelTime);
		list.add(str);
		ctr++;
	}

	
	
	public void done() {
		for(String s : list) {
			write(s);
		}
		for(DTNHost h : hash.keySet()) {
			write(h + " trips: ");
			for(TripProperties t : hash.get(h)) {
				write(t.toString());
			}
		}
		write("Done!");
		super.done();
	}
}
