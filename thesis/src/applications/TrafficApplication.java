/* 
 * Copyright 2010 Aalto Universit	y, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */

package applications;

import java.util.List;
import java.util.Random;

import report.TrafficAppReporter;
import core.Application;
import core.Coord;
import core.DTNHost;
import core.Message;
import core.Road;
import core.RoadProperties;
import core.Settings;
import core.SimClock;
import core.SimScenario;
import core.World;

import movement.Path;
import movement.map.FastestPathFinder;
import movement.map.MapNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Simple ping application to demonstrate the application support. The 
 * application can be configured to send pings with a fixed interval or to only
 * answer to pings it receives. When the application receives a ping it sends
 * a pong message in response.
 * 
 * The corresponding <code>TrafficAppReporter</code> class can be used to record
 * information about the application behavior.
 * 
 * @see TrafficAppReporter
 * @author teemuk
 */
public class TrafficApplication extends Application{
	/** Ping generation interval */
	public static final String TRAFFIC_INTERVAL = "interval";
	/** Ping interval offset - avoids synchronization of ping sending */
	public static final String TRAFFIC_OFFSET = "offset";
	/** Destination address range - inclusive lower, exclusive upper */
	public static final String TRAFFIC_DEST_RANGE = "destinationRange";
	/** Seed for the app's random number generator */
	public static final String TRAFFIC_SEED = "seed";
	/** Size of the ping message */
	public static final String TRAFFIC_MESSAGE_SIZE = "pingSize";
	
	/** Application ID */
	public static final String APP_ID = "fi.tkk.netlab.TrafficApp";
	
	public static final String mTYPE = "type";
	public static final String mLOCATION = "location";
	public static final String mSPEED = "speed";
	public static final String mCURRENT_ROAD = "currentRoad";
	public static final String mCURRENT_ROAD_STATUS = "currentRoadStatus";
	public static final String mTIME_CREATED = "timeCreated";
	public static final String mDISTANCE_TO_FRONTNODE = "distanceToFrontNode";
	
	public static final String HEAVY_TRAFFIC = "HEAVY_TRAFFIC";
	public static final String LIGHT_MODERATE_TRAFFIC = "LIGHT_TO_MODERATE_TRAFFIC";
	
	public static final String FREE_FLOW = "FREE_FLOW";
	public static final String MEDIUM_FLOW = "MEDIUM_FLOW";
	public static final String TRAFFIC_JAM = "MIGHT CAUSE HEAVY TRAFFIC!!!!";

	public static final String LOW = "LOW_DENSITY";
	public static final String MEDIUM = "MEDIUM_DENSITY";
	public static final String HIGH = "HIGH_DENSITY";
	
	private static double FRESHNESS = 10.0;
	private String currentRoadCondition = "";
	private static final int VEHICLE_SIZE = 2;
	// Private vars
	private static final String TRAFFIC_PASSIVE = "passive";
	
	private double	lastAppUpdate = 0;
	private double	appUpdateInterval = 5;
	private int		seed = 0;
	private int		destMin=0;
	private int		destMax=1;
	private int		appMsgSize=1;
	private Random	rng;
	private List<Message> msgs_list;
	private List<Message> neededMsgs;
	private HashMap<DTNHost, Message> msgsHash;
	private HashMap<String, List<Message>> groupedMsgs;
	private HashMap<String, RoadProperties> roadProperties;
	private double averageRoadSpeed;
	private List<Message> roadMsgs;
		
	private List<DTNHost> sameLaneNodes;
	private List<Message> frontNodesMsgs;
	private FastestPathFinder alternativePathFinder;
	private int roadCapacity;
	private String roadDensity = "";
	private boolean passive = false;
	
	/** 
	 * Creates a new ping application with the given settings.
	 * 
	 * @param s	Settings to use for initializing the application.
	 */
	public TrafficApplication(Settings s) {
		if (s.contains(TRAFFIC_PASSIVE)){
			this.passive = s.getBoolean(TRAFFIC_PASSIVE);
		}
		if (s.contains(TRAFFIC_INTERVAL)){
			this.appUpdateInterval = s.getDouble(TRAFFIC_INTERVAL);
		}
		if (s.contains(TRAFFIC_OFFSET)){
			this.lastAppUpdate = s.getDouble(TRAFFIC_OFFSET);
		}
		if (s.contains(TRAFFIC_SEED)){
			this.seed = s.getInt(TRAFFIC_SEED);
		}
		if (s.contains(TRAFFIC_MESSAGE_SIZE)) {
			this.appMsgSize = s.getInt(TRAFFIC_MESSAGE_SIZE);
		}
		if (s.contains(TRAFFIC_DEST_RANGE)){
			int[] destination = s.getCsvInts(TRAFFIC_DEST_RANGE,2);
			this.destMin = destination[0];
			this.destMax = destination[1];
		}
		
		rng = new Random(this.seed);
		super.setAppID(APP_ID);
	}	
	
	/** 
	 * Copy-constructor
	 * 
	 * @param a
	 */
	public TrafficApplication(TrafficApplication a) {
		super(a);
		this.passive = a.isPassive();
		this.lastAppUpdate = a.getLastPing();
		this.appUpdateInterval = a.getInterval();
		this.destMax = a.getDestMax();
		this.destMin = a.getDestMin();
		this.seed = a.getSeed();
		this.appMsgSize = a.getAppMsgSize();
		this.rng = new Random(this.seed);
		this.msgs_list = new ArrayList<Message>();
		this.sameLaneNodes = new ArrayList<DTNHost>();
		this.frontNodesMsgs = new ArrayList<Message>();
		this.neededMsgs = new ArrayList<Message>();
		this.msgsHash = new HashMap<DTNHost, Message>();
		this.groupedMsgs = new HashMap<String, List<Message>>();
		this.roadMsgs = new ArrayList<Message>();
		this.roadProperties = new HashMap<String, RoadProperties>();
	}
	
	private boolean isPassive() {
		return this.passive;
	}

	/** 
	 * Handles an incoming message. If the message is a ping message replies
	 * with a pong message. Generates events for ping and pong messages.
	 * 
	 * @param msg	message received by the router
	 * @param host	host to which the application instance is attached
	 */
	@Override
	public Message handle(Message msg, DTNHost host) {
		String type = (String)msg.getProperty(mTYPE);

		String basis = "";
		try {
			 if (type==null) return msg;
			 
			 if(this.passive) {
				 return msg;
			 }
			 
			if (type.equalsIgnoreCase("traffic")) {
					
					if(!this.msgsHash.containsKey(msg.getFrom())) {
						msgsHash.put(msg.getFrom(), msg);
					}
					else {
						Message m = msgsHash.get(msg.getFrom());
						if(msg.getCreationTime() > m.getCreationTime()) {
							msgsHash.put(msg.getFrom(), msg);
						}
					}

//					System.out.println(host + " Grouping msgs at time " + SimClock.getTime() + "==============================");
//					System.out.println(SimClock.getTime() + " Grouped msgs: " + this.groupedMsgs);
					groupMsgsByRoad(msgsHash, host);
//					System.out.println(host + " Updating msgs at time " + SimClock.getTime() + "==============================");
					updateGroupedMsgs(this.groupedMsgs);
//					System.out.println(host + " Computing average speeds based on msgs at time " + SimClock.getTime() + "==============================");
					computeAverageSpeedPerRoad(this.groupedMsgs, host);
//					System.out.println("Updated hashmap: " + this.groupedMsgs);
//					System.out.println("Updated hashmap size: " + this.groupedMsgs.entrySet().size());
					if(getRoadTrafficConditions(this.roadProperties, host) == TRAFFIC_JAM) {
//						System.out.println(host + " MUST REROUTE!!!!!");
//						System.out.println(host + " current path upon knowing traffic: " + host.getPath().getCoords());
						System.out.println(host + " current location: " + host.getLocation() + " at " + host.getCurrentRoad().getRoadName() + " distance to currDest: " + 
								host.getLocation().distance(host.getCurrentDestination()) + " speed: " + host.getCurrentSpeed());
						
						getAlternativePathV2(host.getPreviousDestination(), host.getCurrentDestination(), 
								host.getPathDestination(), host.getSubpath(), host, host.getCurrentSpeed(), host.getPathSpeed(), this.roadProperties);
					}
//					if(this.neededMsgs.size() < 1)
//						basis = " based on own speed ";
//					else
//						basis = " based on " + this.neededMsgs.size() + " same lane nodes ";
//					
//					super.sendEventToListeners("TrafficReport", host.getCurrentRoad(), basis, SimClock.getTime(), 
//							getAverageRoadSpeed(this.neededMsgs, host), this.currentRoadCondition, null, host);
					
				}				
		 }catch(Exception e) {	
			 e.printStackTrace();
		 }		
		return msg;
	}

	//groups messages according to its senders' current road, messages from different senders that are in same road are
	//stored in a hash using the road name, which are common to them, as the key
	public void groupMsgsByRoad(HashMap<DTNHost, Message> msgs, DTNHost host) {
//		System.out.println("Msgs received: " + msgs.size());
		for(Message m : msgs.values()) {
			this.roadMsgs.clear();
			Road r = (Road) m.getProperty(mCURRENT_ROAD);
//			System.out.println(host + " received: " + m + " with roadname: " + r.getRoadName());
			
			if(this.groupedMsgs.containsKey((String)r.getRoadName())) {
				List<Message> tempList = this.groupedMsgs.get(r.getRoadName());
				Iterator<Message> iterator = tempList.iterator();

				while(iterator.hasNext()) {
					Message msg = iterator.next();
					if(m.getFrom().equals(msg.getFrom())) {
						if(m.getCreationTime() == msg.getCreationTime()) {
							iterator.remove();
//							System.out.println("IF : " + m + " had a duplicate. Duplicate has been ignored.");
						}	
						
						else if(m.getCreationTime() > msg.getCreationTime()) {
							iterator.remove();
//							System.out.println("Replaced " + msg + " with an updated " + m);
						}
						
					}
				}
				tempList.add(m);
				this.groupedMsgs.put((String) r.getRoadName(), tempList);
			}
			else {
				List<Message> l = new ArrayList<Message>();
				l.add(m);
				this.groupedMsgs.put((String) r.getRoadName(), l);
//				System.out.println("ELSE : added " + m + " to hash with key " + r.getRoadName() + " and value " + this.groupedMsgs.get(r.getRoadName()));
			}
		}
		
		for(String key : this.groupedMsgs.keySet()) {
//			System.out.println(host + " key: " + key + " with size " + this.groupedMsgs.get(key).size());
			for(Message m : this.groupedMsgs.get(key)) {
//				System.out.println("has value " + m + " " + ((Road) m.getProperty(mCURRENT_ROAD)).getRoadName());
			} 
		}
		
	}
	
	//returns a HashMap containing the evaluated results of the grouped messages
	//computes the average speed on all known roads with respect to its freshness
	//fresh messages (not greater than 10 seconds ago) are still considered, else it is discarded
	public void updateGroupedMsgs(HashMap<String, List<Message>> hash) {
//		System.out.println("Updating messages per group......................");
		for(String key : hash.keySet()) {
			Iterator<Message> iterator = hash.get(key).iterator();
			while(iterator.hasNext()) {
				Message m = iterator.next();
				if((SimClock.getTime() - m.getCreationTime()) > FRESHNESS) {
//					System.out.println("Removed " + m + " from hash. " + (SimClock.getTime()-m.getCreationTime()) + "s ago since creation");
					iterator.remove();
				}
			}
		}
	}
	
	public void computeAverageSpeedPerRoad(HashMap<String, List<Message>> hash, DTNHost host) {
		double NaN = -1.0;
		for(String key : hash.keySet()) {
			RoadProperties rps;
//			System.out.println("Road " + key + ": ");
			if(hash.get(key).isEmpty()) {
				rps = new RoadProperties(key, NaN, 0);
				this.roadProperties.put(key, rps);
//				System.out.println(this.roadProperties.get(key).getRoadName() + ": "  +this.roadProperties.get(key).getAverageSpeedOfRoad() 
//						+ " , " + this.roadProperties.get(key).getRoadDensity());
			}
//			if(key.equals(host.getCurrentRoad().getRoadName())) {
//				Iterator<Message> iterator = hash.get(key).iterator();
//				double average = 0;
//				int ctr = 0;
//				while(iterator.hasNext()) {
//					Message m = iterator.next();
//					Coord c = (Coord) m.getProperty(mLOCATION);
//					//f
//					if(host.getLocation().distance(host.getCurrentDestination()) > c.distance(host.getCurrentDestination())) {
//						
//					}
//				}
//				
//			}
			else {
				Iterator<Message> iterator = hash.get(key).iterator();
				double average = 0;
				int ctr = 0;
				while(iterator.hasNext()) {
					double s = (double) iterator.next().getProperty(mSPEED);
					average = average + s;
					ctr++;
				}
				average = average/ctr;
				rps = new RoadProperties(key, average, ctr);
				this.roadProperties.put(key, rps);
//				System.out.println(this.roadProperties.get(key).getRoadName() + ": "  +this.roadProperties.get(key).getAverageSpeedOfRoad() 
//						+ " , " + this.roadProperties.get(key).getRoadDensity());
			}
		}
		
	}
	
	//an local road traffic condition awareness kay dapat based la han iya knowledge about han mga front nodes
	//so an average speed ngan density, based la ghap ha mga front nodes
	//kay kun igconsider han host pati an mga aadi ha luyo, madako talaga an density han road,
	//tas mareroute hira tanan if TRAFFIC_JAM an evaluation, 
	//magkakaproblema kun tanan hira man reroute kay instead nga magmamadagmit an travel time han mga mareroute na dapat an mga
	//latecomers/mga urhi na nga umabot na nodes, mareroute man gihap an mga nauuna na nodes kay an ira evaluation han traffic
	//kay as bug os na lane, dapat an ira la unahan para while nareroute an iba na adto luyo, naiibanan an traffic ngan 
	//dapat naresult to medium flow nala. as a result, diri naiiha paghinulat nga makaovertake an mga aadi ha luyo,
	//tapos makakaovertake liwat dayon an mga nauuna kay maiibanan an opposite nodes na kailangan maglapos anay para makaovertake hira
	public String getRoadTrafficConditions(HashMap<String, RoadProperties> hash, DTNHost host) {
//		double ave_speed = getAverageRoadSpeed(filterFrontNodes(msgs, host), host);
//		double ave_speed = hash.get(host.getCurrentRoad().getRoadName());
		
//		System.out.println(host + "is now getting road traffic conditions...");
//		System.out.println("My road " + host.getCurrentRoad() + " avespeed: " + hash.get(host.getCurrentRoad()).getRoadDensity());
		double ave_speed;
		int density;
		if(hash.containsKey(host.getCurrentRoad().getRoadName())) {
			ave_speed = hash.get(host.getCurrentRoad().getRoadName()).getAverageSpeedOfRoad();
			density  = hash.get(host.getCurrentRoad().getRoadName()).getRoadDensity();
		}
		else {
			ave_speed = host.getCurrentSpeed();
			density = 1;
		}
		
		if(ave_speed >= 8.0) {
			if(getRoadDensity(host.getCurrentRoad(), density).equals(HIGH))
				this.currentRoadCondition = MEDIUM_FLOW;
			else
				this.currentRoadCondition = FREE_FLOW;
		}
		else if(ave_speed <= 1.0) {
			if(getRoadDensity(host.getCurrentRoad(), density).equals(HIGH)) {
				this.currentRoadCondition = TRAFFIC_JAM;
				System.out.println(host + " Traffic on road " + host.getCurrentRoad().getRoadName() + " currpath: " + host.getPath().getCoords());
			}
			else if(getRoadDensity(host.getCurrentRoad(), density).equals(MEDIUM))
				this.currentRoadCondition = MEDIUM_FLOW;
			else {
				this.currentRoadCondition = FREE_FLOW;
			}
		}
		else {
			if(getRoadDensity(host.getCurrentRoad(), density).equals(LOW))
				this.currentRoadCondition = FREE_FLOW;
			else
				this.currentRoadCondition = MEDIUM_FLOW;
		}
				
//		System.out.println(host + " road condition: " + this.currentRoadCondition + " ------------" + SimClock.getTime());
//		System.out.println("===============================================================================================");
		return this.currentRoadCondition;
	}
	
	private double getAverageRoadSpeed(List<Message> msgs, DTNHost host) {
		this.averageRoadSpeed = host.getCurrentSpeed();
		int nrofhosts = 1;

//		System.out.println(host + " is commputing ave spd of " + msgs.size() + " same lane nodes");
		
		for(Message m : msgs) {
			double spd = (double) m.getProperty(mSPEED);
//			System.out.println(host + " is adding " + m + " of " + m.getFrom() + ": spd=" + spd + " m.speed=" + (double) m.getProperty(mSPEED));
			
			this.averageRoadSpeed += spd;
			nrofhosts++;
		}
//		System.out.println(host + " ave road spd: " + this.averageRoadSpeed/(double)nrofhosts + " nrofsamelanenodes: " + (nrofhosts > 1 ? nrofhosts : 0));
		if(nrofhosts > 0)
			this.averageRoadSpeed = this.averageRoadSpeed/(double)nrofhosts;
		else
			this.averageRoadSpeed = host.getCurrentSpeed();
		
		return this.averageRoadSpeed;
	}
	
	private double getAverageFrontNodeDistance(List<Message> msgs, DTNHost host) {
		double averageFrontDistance = 0;
		double frontDistance;
		for(Message m : msgs) {
			frontDistance = (double) m.getProperty(mDISTANCE_TO_FRONTNODE);
			averageFrontDistance = averageFrontDistance + frontDistance;
		}
//		System.out.println(host + " same lane average front distance " + msgs.size() + ": " + averageFrontDistance);
		return (double)averageFrontDistance/msgs.size();
	}

	
	public List<Message> filterFrontNodes(List<Message> msgs, DTNHost host){
		this.frontNodesMsgs.clear();
		for(Message m : msgs) {
			DTNHost h = m.getFrom();
			if(h.getLocation().distance(h.getCurrentDestination()) < host.getLocation().distance(host.getCurrentDestination())) {
//				System.out.print(" " + h);
				this.frontNodesMsgs.add(m);
			}
		}
//		System.out.println();
		return this.frontNodesMsgs;
	}
	
	public int getRoadCapacity(Road r) {
		this.roadCapacity =(int) (((Coord)r.getStartpoint()).distance((Coord)r.getEndpoint()) / VEHICLE_SIZE);
		return this.roadCapacity;
	}
	
	public String getRoadDensity(Road r, int nrOfVehicles) {

		if(nrOfVehicles >= getRoadCapacity(r)/2)
			this.roadDensity = HIGH;
		else if(nrOfVehicles <= getRoadCapacity(r)/4)
			this.roadDensity = LOW;
		else
			this.roadDensity = MEDIUM;
		
		return this.roadDensity;
	}
	
	//changed msgs to consider for local average speed computation. only frontNodes will be considered
	//change paramater to HashMap<String, Double(for averagespeed>, Double(for trafficdensity)>
	public String getTrafficCondition(List<Message> msgs, DTNHost host) {
		double ave_speed = getAverageRoadSpeed(filterFrontNodes(msgs, host), host);
//		double ave_speed = hash.get(host.getCurrentRoad().getRoadName());
		
		if(ave_speed >= 8.0) {
			if(getRoadDensity(host.getCurrentRoad(), msgs.size()).equals(HIGH))
				this.currentRoadCondition = MEDIUM_FLOW;
			else
				this.currentRoadCondition = FREE_FLOW;
		}
		else if(ave_speed <= 1.0) {
			if(getRoadDensity(host.getCurrentRoad(), msgs.size()).equals(HIGH)) {
				this.currentRoadCondition = TRAFFIC_JAM;
				System.out.println(host + " path===" + host.getPath());
				System.out.println("Traffic on road " + host.getCurrentRoad().getRoadName());
			}
			else if(getRoadDensity(host.getCurrentRoad(), msgs.size()).equals(MEDIUM))
				this.currentRoadCondition = MEDIUM_FLOW;
			else {
				this.currentRoadCondition = FREE_FLOW;
			}
		}
		else {
			if(getRoadDensity(host.getCurrentRoad(), msgs.size()).equals(LOW))
				this.currentRoadCondition = FREE_FLOW;
			else
				this.currentRoadCondition = MEDIUM_FLOW;
		}
				
//		System.out.println(host + " road condition: " + this.currentRoadCondition + " ------------" + SimClock.getTime());
//		System.out.println("===============================================================================================");
		return this.currentRoadCondition;
	}

	/** 
	 * Draws a random host from the destination range
	 * 
	 * @return host
	 */
	private DTNHost randomHost() {
		int destaddr = 0;
		if (destMax == destMin) {
			destaddr = destMin;
		}
		destaddr = destMin + rng.nextInt(destMax - destMin);
		World w = SimScenario.getInstance().getWorld();
		return w.getNodeByAddress(destaddr);
	}
	
	@Override
	public Application replicate() {
		return new TrafficApplication(this);
	}

	@Override
	public void update(DTNHost host) {
		double curTime = SimClock.getTime();

		try {
			if ((curTime - this.lastAppUpdate)% 2.0 == 0) {
						
				// Time to send a new ping
				String id = host + "traffic-" + SimClock.getTime();
						
				Message m = new Message(host, null, id, getAppMsgSize());
				m.addProperty(mTYPE, "traffic");
				m.addProperty(mLOCATION, host.getLocation());
				m.addProperty(mSPEED, host.getCurrentSpeed());
				m.addProperty(mCURRENT_ROAD, host.getCurrentRoad());
				m.addProperty(mCURRENT_ROAD_STATUS, host.getCurrentRoadStatus());
				m.addProperty(mTIME_CREATED, SimClock.getTime()); //para pagcheck freshness
				m.addProperty(mDISTANCE_TO_FRONTNODE, host.getLocation().distance(host.getFrontNode(host.getSameLaneNodes()).getLocation()));

				m.setAppID(APP_ID);
				host.createNewMessage(m);

				super.sendEventToListeners("SentPing", null, host);
				this.lastAppUpdate = curTime;
			}
		}catch(Exception e) {

		}
	}

	private List<DTNHost> getSameLaneNodes() {
		return this.sameLaneNodes;
	}
	
	public void getAlternativePath(Coord start, Coord currentDestination, Coord finalDestination, List<Coord> path, DTNHost host, double slowSpeed, double pathSpeed) {
		this.alternativePathFinder = new FastestPathFinder(host.getMovementModel().getOkMapNodeTypes2());
		Path p = new Path(pathSpeed);
		MapNode s = host.getMovementModel().getMap().getNodeByCoord(start);
		MapNode currentDest = host.getMovementModel().getMap().getNodeByCoord(currentDestination);
		MapNode dest = host.getMovementModel().getMap().getNodeByCoord(finalDestination);
		List<MapNode> altMapNodes = new ArrayList<MapNode>();
		altMapNodes = this.alternativePathFinder.getAlternativePath(s, currentDest, dest, host.getLocation(), slowSpeed, pathSpeed, path);
		System.out.println("Orig path " + host.getPathSpeed());
		
		if(altMapNodes == null)
			System.out.println("Path finder couldn't suggest faster routes. Sticking to current path.");
		else {
//			System.out.println("re: path= " + this.alternativePathFinder.getAlternativePath(s, dest, host.getLocation(), path, host.getCurrentSpeed()));
//			System.out.println("Getting reroute path");
			for(MapNode n : altMapNodes) {
				p.addWaypoint(n.getLocation());
			}

//			System.out.println("in app: " + p);
			System.out.println("called host reroute for " + host);
			host.reroute(p);
			System.out.println("done calling host reroute=================================");
		}
			
	}

	//version 2 of finding reroute path (messages received from other hosts are now considered
	private void getAlternativePathV2(Coord previousDestination, Coord currentDestination, Coord pathDestination,
			List<Coord> subpath, DTNHost host, double currentSpeed, double pathSpeed,
			HashMap<String, RoadProperties> roadProperties) {
		
		System.out.println(host + " current path before pathfinding: " + host.getPath().getCoords());
		Path p;
		this.alternativePathFinder = new FastestPathFinder(host.getMovementModel().getOkMapNodeTypes2());
		
		MapNode s = host.getMovementModel().getMap().getNodeByCoord(previousDestination);
		MapNode currentDest = host.getMovementModel().getMap().getNodeByCoord(currentDestination);
		MapNode dest = host.getMovementModel().getMap().getNodeByCoord(pathDestination);
		List<MapNode> altMapNodes = new ArrayList<MapNode>();
		altMapNodes = this.alternativePathFinder.getAlternativePathV2(s, currentDest, dest, host.getLocation(), currentSpeed, pathSpeed, subpath, roadProperties);
		
//		System.out.println(altMapNodes);
		if(altMapNodes == null)
			System.out.println("Path finder couldn't suggest faster routes. Sticking to current path.");
		else {
			System.out.println("Found new path: " + altMapNodes);
//			System.out.println("re: path= " + this.alternativePathFinder.getAlternativePath(s, dest, host.getLocation(), path, host.getCurrentSpeed()));
//			System.out.println("Getting reroute path");
			
			//FIX NEEDED IN HERE
			//pass by reference ada ini hiya nga part na pagtawag han altMapNodes asya nagkakalurong
			p = new Path(pathSpeed);
			for(MapNode n : altMapNodes) {
				p.addWaypoint(n.getLocation());
//				System.out.println("MapNode " + n);
			}

			System.out.println("New path for host: " + p);
//			System.out.println("in app: " + p);
//			System.out.println("called host reroute for " + host);
			host.reroute(p);
//			System.out.println("done calling host reroute=================================");
		}
	}
	
	/**
	 * @return the lastPing
	 */
	public double getLastPing() {
		return lastAppUpdate;
	}

	/**
	 * @param lastPing the lastPing to set
	 */
	public void setLastPing(double lastPing) {
		this.lastAppUpdate = lastPing;
	}

	/**
	 * @return the interval
	 */
	public double getInterval() {
		return appUpdateInterval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(double interval) {
		this.appUpdateInterval = interval;
	}

	/**
	 * @return the destMin
	 */
	public int getDestMin() {
		return destMin;
	}

	/**
	 * @param destMin the destMin to set
	 */
	public void setDestMin(int destMin) {
		this.destMin = destMin;
	}

	/**
	 * @return the destMax
	 */
	public int getDestMax() {
		return destMax;
	}

	/**
	 * @param destMax the destMax to set
	 */
	public void setDestMax(int destMax) {
		this.destMax = destMax;
	}

	/**
	 * @return the seed
	 */
	public int getSeed() {
		return seed;
	}

	/**
	 * @param seed the seed to set
	 */
	public void setSeed(int seed) {
		this.seed = seed;
	}

	/**
	 * @return the pingSize
	 */
	public int getAppMsgSize() {
		return this.appMsgSize;
	}

	/**
	 * @param pingSize the pingSize to set
	 */
	public void setPingSize(int size) {
		this.appMsgSize = size;
	}
	
	public List<Message> getMsgsList(){
		return this.msgs_list;
	}

}
