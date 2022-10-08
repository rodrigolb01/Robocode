package hangar;

import robocode.*;
import robocode.util.Utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.*;


// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html
 
/**
 * Bot - a robot by (your name here)
 */
public class Bot extends AdvancedRobot
{
	final double FIREPOWER = 3; // Max. power => violent as this robot can afford it!

	// Scanning direction, where the radar turns to the right with positive values, and turns
	// to the left with negative values.
	double scanDir = 1;

	RobotData target;

	// Map containing data for all scanned robots.
	// The key to the map is a robot name and the value is an object containing robot data.
	final Map<String, RobotData> enemyMap;

    /**
	 *  Constructs this robot
	 */
    public Bot() {
		enemyMap = new LinkedHashMap<String, RobotData>(5, 2, true);
	}

	/**
	 * run: Bot's default behavior
	 */
	@Override
	public void run() {
		// Initialization of the robot should be put here
		initialize();

		handleRadar();
		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			ahead(100);
			turnGunRight(360);
			back(100);
			turnGunRight(360);
		}
	}

	private void initialize() {
		setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		// Let the robot body, gun, and radar turn independently of each other
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
	}

	private void handleRadar() {
		// Set the radar to turn infinitely to the right if the scan direction is positive;
		// otherwise the radar is moved to the left, if the scan direction is negative.
		// Notice that onScannedRobot(ScannedRobotEvent) is responsible for determining the scan
		// direction.
		setTurnRadarRightRadians(scanDir * Double.POSITIVE_INFINITY);
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		fire(1);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		back(20);
	}	

	@Override
	public void onPaint(Graphics2D g) {
		// Set the line width to 2 pixels
		g.setStroke(new BasicStroke(2f));

		// Prepare colors for painting the scanned coordinate and target coordinate
		Color color1 = new Color(0x00, 0xFF, 0x00, 0x40); // Green with 25% alpha blending
		Color color2 = new Color(0xFF, 0xFF, 0x00, 0x40); // Yellow with 25% alhpa blending

		// Paint a two circles for each robot in the enemy map. One circle where the robot was
		// scanned the last time, and another circle where our robot must point the gun in order to
		// hit it (target coordinate). In addition, a line is drawn between these circles.
		for (RobotData robot : enemyMap.values()) {
			// Paint the two circles and a line
			fillCircle(g, robot.scannedX, robot.scannedY, color1); // scanned coordinate
			fillCircle(g, robot.targetX, robot.targetY, color2); // target coordinate
			g.setColor(color1);
			g.drawLine((int) robot.scannedX, (int) robot.scannedY, (int) robot.targetX, (int) robot.targetY);
		}

		// Paint a two circles for the target robot. One circle where the robot was
		// scanned the last time, and another circle where our robot must point the gun in order to
		// hit it (target coordinate). In addition, a line is drawn between these circles.
		if (target != null) {
			// Prepare colors for painting the scanned coordinate and target coordinate
			color1 = new Color(0xFF, 0x7F, 0x00, 0x40); // Orange with 25% alpha blending
			color2 = new Color(0xFF, 0x00, 0x00, 0x80); // Red with 50% alpha blending

			// Paint the two circles and a line
			fillCircle(g, target.scannedX, target.scannedY, color1); // scanned coordinate
			fillCircle(g, target.targetX, target.targetY, color2); // target coordinate
			g.setColor(color1);
			g.drawLine((int) target.scannedX, (int) target.scannedY, (int) target.targetX, (int) target.targetY);
		}
	}

	class RobotData {
		final String name; // name of the scanned robot
		double scannedX; // x coordinate of the scanned robot based on the last update
		double scannedY; // y coordinate of the scanned robot based on the last update
		double scannedVelocity; // velocity of the scanned robot from the last update
		double scannedHeading; // heading of the scanned robot from the last update
		double targetX; // predicated x coordinate to aim our gun at, when firing at the robot
		double targetY; // predicated y coordinate to aim our gun at, when firing at the robot

		/**
		 * Creates a new robot data entry based on new scan data for a scanned robot.
		 * 
		 * @param event
		 *            is a ScannedRobotEvent event containing data about a scanned robot.
		 */
		RobotData(ScannedRobotEvent event) {
			// Store the name of the scanned robot
			name = event.getName();
			// Updates all scanned facts like position, velocity, and heading
			update(event);
			// Initialize the coordinates (x,y) to fire at to the updated scanned position
			targetX = scannedX;
			targetY = scannedY;
		}

		/**
		 * Updates the scanned data based on new scan data for a scanned robot.
		 * 
		 * @param event
		 *            is a ScannedRobotEvent event containing data about a scanned robot.
		 */
		void update(ScannedRobotEvent event) {
			// Get the position of the scanned robot based on the ScannedRobotEvent
			Point2D.Double pos = getPosition(event);
			// Store the scanned position (x,y)
			scannedX = pos.x;
			scannedY = pos.y;
			// Store the scanned velocity and heading
			scannedVelocity = event.getVelocity();
			scannedHeading = event.getHeadingRadians();
		}

		/**
		 * Returns the position of the scanned robot based on new scan data for a scanned robot.
		 * 
		 * @param event
		 *            is a ScannedRobotEvent event containing data about a scanned robot.
		 * @return the position (x,y) of the scanned robot.
		 */
		Point2D.Double getPosition(ScannedRobotEvent event) {
			// Gets the distance to the scanned robot
			double distance = event.getDistance();
			// Calculate the angle to the scanned robot (our robot heading + bearing to scanned
			// robot)
			double angle = getHeadingRadians() + event.getBearingRadians();

			// Calculate the coordinates (x,y) of the scanned robot
			double x = getX() + Math.sin(angle) * distance;
			double y = getY() + Math.cos(angle) * distance;

			// Return the position as a point (x,y)
			return new Point2D.Double(x, y);
		}
	}

	private void fillCircle(Graphics2D gfx, double x, double y, Color color) {
		// Set the pen color
		gfx.setColor(color);
		// Paint a filled circle (oval) that has a radius of 20 pixels with a center at the input
		// coordinates.
		gfx.fillOval((int) x - 20, (int) y - 20, 40, 40);
	}
}
