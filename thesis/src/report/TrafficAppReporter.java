package report;

import java.util.HashMap;

import applications.TrafficApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import core.Road;
import core.RoadProperties;
import movement.Path;

public class TrafficAppReporter extends Report implements ApplicationListener{

	@Override
	public void gotEvent(String event, Road myRoad, String basis, double time, double averageSpeed, 
			String trafficCondition, Application app, DTNHost host) {
	
		//must add travel time 
		
		String report = host + " @ " + time + " on road segment " + myRoad.getStartpoint()+ ", " + 
				myRoad.getEndpoint() + " Basis: " + basis + " Average speed is: " 
				+ averageSpeed + " - " + trafficCondition;
		if (!(app instanceof TrafficApp)) return;
		
		if(event.equalsIgnoreCase("TrafficReport")) {
			write(report);
		}
	}

	@Override
	public void gotEvent(String event, Road myRoad, String trafficCondition, double time, HashMap<String, RoadProperties> roadProps, 
			Path currentPath, Application app, DTNHost host) {
		
		String rps ="";
		
		for(String key : roadProps.keySet()) {
			rps = rps + key + " : " + roadProps.get(key).getAverageSpeedOfRoad() + "\t" + roadProps.get(key).getRoadDensity() + "\n"; 
		}
		
		String report = host + "Time: " + time + "\n\t" + host + " at " + myRoad.getRoadName() + " : " + trafficCondition + "\n\t" + 
				"Current path: " + currentPath + "\n" + "\tKnown roads(road_name, average_speed, density):";
		
		for(String key : roadProps.keySet()) {
			report = report + "\n\t\t" + key + "\t" + roadProps.get(key).getAverageSpeedOfRoad() + "\t" 
					+ roadProps.get(key).getRoadDensity() + "\t" + roadProps.get(key).getCondition(); 
		}
		if(event.equalsIgnoreCase("TrafficReport")) {
			write(report);
		}
	}
	
	@Override
	public void gotEvent(String event, Road myRoad, double time, HashMap<String, RoadProperties> roadProps, Path newPath, 
			Application app, DTNHost host) {
		String rps = "";
		
		
		String report = host + "Rerouting Time: " + time + "\n\t" + host + " now on " + myRoad.getRoadName() + " : \n\t" + 
				"New path: " + newPath + "\n" + "\tKnown roads(road_name, average_speed, density): ";
		
		for(String key : roadProps.keySet()) {
			report = report + "\n\t\t" + key + "\t" + roadProps.get(key).getAverageSpeedOfRoad() + "\t" 
					+ roadProps.get(key).getRoadDensity() + "\t" + roadProps.get(key).getCondition(); 
		}
		
		if(event.equalsIgnoreCase("RerouteReport")) {
			write(report);
		}
	}

	@Override
	public void gotEvent(String event, Object params, Application app, DTNHost host) {
		
	}
	
	@Override
	public void done() {
		write("Done!");
		super.done();
	}
}
