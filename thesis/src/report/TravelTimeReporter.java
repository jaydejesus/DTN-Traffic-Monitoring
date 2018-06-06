package report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import core.DTNHost;
import core.Settings;
import core.SimScenario;
import core.TripProperties;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import movement.MovementModel;
import movement.Path;

public class TravelTimeReporter extends Report{
	private String directory = "/home/jaydejesus/git/dtn-traffic-monitoring/thesis/";
//	private static int row = 1;
	private static TreeMap<DTNHost, List<TripProperties>> hash = new TreeMap<DTNHost, List<TripProperties>>();
//	private List<String> list = new ArrayList<String>();
	private static List<String> list2 = new ArrayList<String>();
	
	private WriteExcel excel = new WriteExcel();
	private static List<String> headers = new ArrayList<String>();
	
	public void createReport(DTNHost host, TripProperties trip) {
		String from = "" + trip.getTripStart();
		String to = "" + trip.getTripDestination();
		String start = "" + trip.getTripStartTime();
		String end = "" + trip.getTripEndTime();
		String travelTime = "" + trip.getTravelTime();
		String rerouteCtr = "" + trip.getRerouteCtr();
		String distance = "" + trip.getTripStart().distance(trip.getTripDestination());
		String travelled = "" + trip.getTripTravelDistance();
		List<TripProperties> tripList = new ArrayList<TripProperties>();
		
		if(!hash.containsKey(host)) {
			tripList.clear();
			tripList.add(trip);
			hash.put(host, tripList);
		}
		else {
			tripList = hash.get(host);
			tripList.add(trip);
			hash.put(host, tripList);
		}
		
//		String str = String.format("%4s%5s%s%3s%s%3s%s%3s%s%3s%s%3s%s%3s%s%3s%s", host, ' ', from, ' ', to, ' ', start, ' ', 
//				end, ' ', travelTime, ' ', rerouteCtr, ' ', distance, '-', travelled);
//		list.add(str + " seed: " + getSeed());
	}
	
	public void rerouteReport(DTNHost host, Path p, double rerouteTime) {
		String str = host + " " + rerouteTime + " " + p;
		list2.add(str);
	}
	
	
	public void writeReport() {
		write("record of trips per node: " + hash.keySet().size() + " nodes");
		for(DTNHost h : hash.keySet()) {
			double averageTravelTime = 0;
			write(h + " trips: " + hash.get(h).size());
			for(TripProperties t : hash.get(h)) {
				averageTravelTime += t.getTravelTime();
				write(t.toString() + "_" + getSeed());
			}
			
			averageTravelTime = averageTravelTime/hash.get(h).size();
			write(h + "'s Average Travel Time: " + averageTravelTime + "\n");
		}
	}

	public void makeExcel() {
		Collections.addAll(headers, "Host", "Average Travel Time", "Nr Of Trips", "Reroute count", "Experiment Seed #");
		
//		excel.setOutputFile(directory + getReportDir() + getScenarioName() + ".xls");
		excel.setOutputFile(directory + getReportDir() + "AverageTravelTimeRecords.xls");
		try {
			excel.initialize(headers);
			int column;
			int row = excel.getExcelSheet().getRows();
			for(DTNHost h : hash.keySet()) {
				column = 0;
	
				double averageTravelTime = 0;
				int totalTrips = hash.get(h).size();
				int totalRerouteCount = 0;
				write(h + " trips: " + hash.get(h).size());
				for(TripProperties t : hash.get(h)) {
					averageTravelTime += t.getTravelTime();
					totalRerouteCount += t.getRerouteCtr();
					write(t.toString());
				}
				
				averageTravelTime = averageTravelTime/hash.get(h).size();
				
				excel.addLabel(excel.getExcelSheet(), column++, row, h.toString());
				excel.addDouble(excel.getExcelSheet(), column++, row, averageTravelTime);
				excel.addNumber(excel.getExcelSheet(), column++, row, totalTrips);
				excel.addNumber(excel.getExcelSheet(), column++, row, totalRerouteCount);
				excel.addNumber(excel.getExcelSheet(), column++, row, getSeed());
				row++;
				write(h + "'s Average Travel Time: " + averageTravelTime + "\n");
			}
			
			excel.write();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		}
	}
	
	public int getSeed() {
		Settings s = new Settings(MovementModel.MOVEMENT_MODEL_NS);
		int seed = s.getInt(MovementModel.RNG_SEED);
			
		return seed;
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
	
	public void done() {
		write("reroute reports:");
		for(String s : list2) {
			write(s);
		}
		write("\n");
		
		writeReport();
		makeExcel();
		
		write("Done!");
		hash.clear();
		super.done();
	}
}
