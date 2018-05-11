package report;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import core.DTNHost;
import core.TripProperties;
import movement.Path;

public class TravelTimeReporter extends Report{
	private static HashMap<DTNHost, List<TripProperties>> hash = new HashMap<DTNHost, List<TripProperties>>();
	private static List<String> list = new ArrayList<String>();
	private static List<String> list2 = new ArrayList<String>();
	
	public void createReport(DTNHost host, TripProperties trip) {
		String from = "" + trip.getTripStart();
		String to = "" + trip.getTripDestination();
		String start = "" + trip.getTripStartTime();
		String end = "" + trip.getTripEndTime();
		String travelTime = "" + trip.getTravelTime();
		String rerouteCtr = "" + trip.getRerouteCtr();
		String distance = "" + trip.getTripStart().distance(trip.getTripDestination());
		List<TripProperties> tripList = new ArrayList<TripProperties>();
		
		if(!hash.containsKey(host)) {
			tripList.clear();
			tripList.add(trip);
			hash.put(host, tripList);
		}
		//if host is already stored in hash
		else {
			tripList = hash.get(host);
//			if(tripList.contains(trip))
			tripList.add(trip);
			hash.put(host, tripList);
		}
		
		String str = String.format("%4s%5s%s%3s%s%3s%s%3s%s%3s%s%3s%s%3s%s", host, ' ', from, ' ', to, ' ', start, ' ', 
				end, ' ', travelTime, ' ', rerouteCtr, ' ', distance);
		list.add(str);
	}
	
	public void rerouteReport(DTNHost host, Path p, double rerouteTime) {
		String str = host + " " + rerouteTime + " " + p;
		list2.add(str);
	}

	
	
	public void done() {
//		System.out.println("Writing traffic reports");
//		write("traffic reports:");
//		for(String s : list) {
//			write(s);
//		}
		System.out.println("Writing reroute reports");
		write("reroute reports:");
		for(String s : list2) {
			write(s);
		}
		write("\n");
		write("trips per node: ");
		for(DTNHost h : hash.keySet()) {
			double averageTravelTime = 0;
			write(h + " trips: " + hash.get(h).size());
			for(TripProperties t : hash.get(h)) {
				averageTravelTime += t.getTravelTime();
				write(t.toString());
			}
			write(h + "'s Average Travel Time: " + averageTravelTime/hash.get(h).size() + " basis: " + averageTravelTime + " / " + hash.get(h).size());
			write("\n");
		}
		write("Done!");
		super.done();
	}
}
