/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package movement;

import java.util.List;

import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.PointsOfInterest;
import core.Coord;
import core.Settings;

/**
 * Map based movement model that uses Dijkstra's algorithm to find shortest
 * paths between two random map nodes and Points Of Interest
 */
public class ShortestPathMapBasedMovement extends MapBasedMovement implements
	SwitchableMovement{
	/** the Dijkstra shortest path finder */
	private DijkstraPathFinder pathFinder;

	/** Points Of Interest handler */
	private PointsOfInterest pois;
	private Coord initLocation;
	private boolean hasInitLoc = false;
	public static final String LOCATION_S = "nodeLocation";

	/**
	 * Creates a new movement model based on a Settings object's settings.
	 * @param settings The Settings object where the settings are read from
	 */
	public ShortestPathMapBasedMovement(Settings settings) {
		super(settings);
		double coords[];
		
		this.pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
		this.pois = new PointsOfInterest(getMap(), getOkMapNodeTypes(),
				settings, rng);
		
		if(settings.contains(LOCATION_S)) {
			this.hasInitLoc = true;
			coords = settings.getCsvDoubles(LOCATION_S, 2);
			this.initLocation = new Coord(coords[0], coords[1]);
			System.out.println("nodeLocation: " + this.initLocation);
		}
		
	}

	/**
	 * Copyconstructor.
	 * @param mbm The ShortestPathMapBasedMovement prototype to base
	 * the new object to
	 */
	protected ShortestPathMapBasedMovement(ShortestPathMapBasedMovement mbm) {
		super(mbm);
		this.pathFinder = mbm.pathFinder;
		this.pois = mbm.pois;
		this.initLocation = mbm.initLocation;
		this.hasInitLoc = mbm.hasInitLoc;
	}

	@Override
	public Path getPath() {
		Path p = new Path(generateSpeed());
		MapNode to = pois.selectDestination();
//		MapNode to = 
		
//		System.out.println(hasInitLoc + " " + initLocation);
//		if(hasInitLoc) {
//			System.out.println(pois.getNodeByCoord(initLocation));
//			lastMapNode = pois.getNodeByCoord(initLocation);
//			System.out.println("lastMapNode " + lastMapNode);
//			setInitialLocation(initLocation);
//		}
//		System.out.println("lastMapNode" + lastMapNode);

		List<MapNode> nodePath = pathFinder.getShortestPath(lastMapNode, to);
		// this assertion should never fire if the map is checked in read phase
		assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
			to + ". The simulation map isn't fully connected";

		for (MapNode node : nodePath) { // create a Path from the shortest path
			p.addWaypoint(node.getLocation());
		}

		lastMapNode = to;

//		System.out.println(this.getHost().toString() + " path: " + p);
		
		return p;
	}

	@Override
	public ShortestPathMapBasedMovement replicate() {
		return new ShortestPathMapBasedMovement(this);
	}
	
	public boolean hasInitLoc() {
		return this.hasInitLoc;
	}
	
//	@Override
//	public Coord getInitLocation() {
//		System.out.println("initLocation: " + initLocation);
//		System.out.println("this.pois.getNodeByCoord(initLocation) "  +  this.pois.getNodeByCoord(initLocation));
//		lastMapNode = this.pois.getNodeByCoord(initLocation);
//		return this.initLocation;
//	}

}
