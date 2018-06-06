package report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import applications.TrafficApp;
import core.Application;
import core.ApplicationListener;
import core.Coord;
import core.DTNHost;
import core.NodeProperties;
import core.Road;
import core.RoadProperties;
import core.Settings;
import core.SimScenario;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import movement.MovementModel;
import movement.Path;

public class TrafficAppReporter extends Report implements ApplicationListener{

//	private static TreeMap<DTNHost,TreeMap<Double, HashMap<String, RoadProperties>>> nodeBasedHash = new TreeMap<DTNHost,TreeMap<Double, HashMap<String, RoadProperties>>>();
//	private TreeMap<Double, HashMap<String, RoadProperties>> timeBasedHash = new TreeMap<Double, HashMap<String, RoadProperties>>();
	private WriteExcel individualRecords = new WriteExcel();
	private WriteExcel averageRecords = new WriteExcel();
	private String directory = "/home/jaydejesus/git/dtn-traffic-monitoring/thesis/";
	private TreeMap<DTNHost, NodeProperties> nodeBasedHash = new TreeMap<DTNHost, NodeProperties>();
	private static List<String> headers = new ArrayList<String>();
	private static List<String> headers2 = new ArrayList<String>();
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
		double distance = 0;
		Road farthest = null;
		double max = 0;
		
		for(String key : roadProps.keySet()) {
			rps = rps + key + " : " + roadProps.get(key).getAverageSpeedOfRoad() + "\t" + roadProps.get(key).getRoadDensity() + "\n";
			Road r = roadProps.get(key).getRoad();
//			if(r != null)
			distance = host.getLocation().distance((Coord) r.getEndpoint());
			
			if(distance > max) {
				farthest = r;
				max = distance;
			}
			if(farthest == null) {
				farthest = r;
			}
		}
		
		
		if(!nodeBasedHash.containsKey(host)) {
			nodeBasedHash.put(host, new NodeProperties());
		}
		
		NodeProperties nodeProps = nodeBasedHash.get(host);
		nodeProps.addMaxRange(time, max);
		nodeProps.addTotalNrOfRoads(time, roadProps.size());
		nodeProps.addFarthestRoad(time, farthest);
//		System.out.println(host + " " + time + " called addKnownRoads " + roadProps.keySet().size() + " " + max);
//		nodeProps.addKnownRoads(time, roadProps);
		
		
		String report = host + "Time: " + time + "\n\t" + host + " at " + myRoad.getRoadName() + " : " + trafficCondition + "\n\t" + 
				"Current path: " + currentPath + "\n" + "\tKnown roads(road_name, average_speed, density):" + " Total roads known: " 
				+ roadProps.keySet().size() + "\t" + "InfoRange: " + max + " Farthest Road: " + farthest.getRoadName();
		
		for(String key : roadProps.keySet()) {
			report = report + "\n\t\t" + key + "\t" + roadProps.get(key).getAverageSpeedOfRoad() + "\t" 
					+ roadProps.get(key).getRoadDensity() + "\t" + roadProps.get(key).getCondition(); 
//					+ "\t" + host.getLocation().distance((Coord) roadProps.get(key).getRoad().getEndpoint()); 
		}
		if(event.equalsIgnoreCase("TrafficReport")) {
			write(report);
		}
	}
	
	public void writeTrafficReport(TreeMap<DTNHost, NodeProperties> hash) {
	
		for(DTNHost h : hash.keySet()) {
			TreeMap<Double, HashMap<String, RoadProperties>> timeBased = new TreeMap<Double, HashMap<String, RoadProperties>>(hash.get(h).getKnownRoads());
			write(h.toString());
			for(double t : timeBased.keySet()) {
//				System.out.println(hash.get(h).getFarthestRoadPerTime().get(t).getRoadName());
				write("\t@" + t + " Information Range: " + hash.get(h).getRangePerTime().get(t) + 
						" NrOfRoads: " + hash.get(h).getRoadsPerTime().get(t));
//				+ " Farthest Road: " + hash.get(h).getFarthestRoadPerTime().get(t));
				HashMap<String, RoadProperties> roadBased = new HashMap<String, RoadProperties>(timeBased.get(t));
				for(String roadName : roadBased.keySet()) {
					RoadProperties rps = hash.get(h).getKnownRoads().get(t).get(roadName);
					String str = "\t" + rps.getRoadName() + "\t" + rps.getAverageSpeedOfRoad() + 
							"\t" + rps.getRoadDensity() + "\t" + rps.getCondition() + "\t" + 
							rps.getInfoRange();
					write(str);
				}
				
			}
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
	
	public void makeExcel(TreeMap<DTNHost, NodeProperties> hash) throws WriteException, BiffException, IOException {
		Collections.addAll(headers, "Host", "Time", "InfoRange", "NrOfRoads", "Seed No.");
		Collections.addAll(headers2, "Host", "AverageInfoRange", "AverageNrOfRoads", "MaxInfoRange", "NrOfRoads", "MaxNrOfRoads", "Seed No.");
//		excel.setOutputFile(directory + getReportDir() + getScenarioName() + "AppIndividualRecords.xls");
//		individualRecords.setOutputFile(directory + getReportDir() + "RangeIndividualRecords_" + getSeed() + ".xls");
//		individualRecords.initialize(headers);
		
		averageRecords.setOutputFile(directory + getReportDir() + "RangeAverageRecords.xls");
		averageRecords.initialize(headers2);
		
//		int column;
//		int row = individualRecords.getExcelSheet().getRows();
		int rowAve = averageRecords.getExcelSheet().getRows();
		for(DTNHost h : hash.keySet()) {
			double maximumRange = 0;
			int maximumNrOfRoads = 0;
			int nrOfRoads = 0;
			double averageRange = 0;
			int averageNrOfRoads = 0;
			int ctr = 0;
			int colAve = 0;
			
			for(double t : hash.get(h).getRoadsPerTime().keySet()) {
//				column = 0;
//				individualRecords.addLabel(individualRecords.getExcelSheet(), column++, row, h.toString());
//				individualRecords.addDouble(individualRecords.getExcelSheet(), column++, row, t);
//				individualRecords.addDouble(individualRecords.getExcelSheet(), column++, row, hash.get(h).getRangePerTime().get(t));
//				individualRecords.addNumber(individualRecords.getExcelSheet(), column++, row, hash.get(h).getRoadsPerTime().get(t));
//				individualRecords.addNumber(individualRecords.getExcelSheet(), column++, row, getSeed());
				
				if(hash.get(h).getRangePerTime().get(t) > maximumRange) {
					maximumRange = hash.get(h).getRangePerTime().get(t);
					nrOfRoads = hash.get(h).getRoadsPerTime().get(t);
				}
				if(hash.get(h).getRoadsPerTime().get(t) > maximumNrOfRoads) {
					maximumNrOfRoads = hash.get(h).getRoadsPerTime().get(t);
				}
				
//				row++;
				averageRange += hash.get(h).getRangePerTime().get(t);
				averageNrOfRoads += hash.get(h).getRoadsPerTime().get(t);
				ctr++;
			}
			averageRange = averageRange/ctr;
			averageNrOfRoads = averageNrOfRoads/ctr;
			averageRecords.addLabel(averageRecords.getExcelSheet(), colAve++, rowAve, h.toString());
			averageRecords.addDouble(averageRecords.getExcelSheet(), colAve++, rowAve, averageRange);
			averageRecords.addNumber(averageRecords.getExcelSheet(), colAve++, rowAve, averageNrOfRoads);
			averageRecords.addDouble(averageRecords.getExcelSheet(), colAve++, rowAve, maximumRange);
			averageRecords.addNumber(averageRecords.getExcelSheet(), colAve++, rowAve, nrOfRoads);
			averageRecords.addNumber(averageRecords.getExcelSheet(), colAve++, rowAve, maximumNrOfRoads);
			averageRecords.addNumber(averageRecords.getExcelSheet(), colAve++, rowAve, getSeed());
			rowAve++;
		}
//		individualRecords.write();
		averageRecords.write();
	}
	
	public String getReportDir() {
		Settings s = new Settings();
		return s.getSetting(REPORTDIR_SETTING);
	}
	
	public String getScenarioName() {
		Settings s = new Settings(SimScenario.SCENARIO_NS);
		String name = s.getSetting(SimScenario.NAME_S);
		
		return name;
	}
	
	public int getSeed() {
		Settings s = new Settings(MovementModel.MOVEMENT_MODEL_NS);
		int seed = s.getInt(MovementModel.RNG_SEED);
			
		return seed;
	}
	
	@Override
	public void done() {
//		writeTrafficReport(nodeBasedHash);
		try {
			makeExcel(nodeBasedHash);
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		write("Done!");
		super.done();
	}
}
