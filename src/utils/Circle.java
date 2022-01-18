package utils;

import java.util.Collection;

public class Circle {

	public final float radius; //Circle's radius and center point
	public Point p;
	private static final double MULTIPLICATIVE_EPSILON = 1 + 1e-14;
	public Circle(Point p, float radius) {
		this.p = p;
		this.radius = radius;
	}

	public Point getPoint() { //Returns Circel's center point
		return this.p;
	}

	public float getRadius() { //Returns Circel's radius
		return this.radius;
	}

	@Override
	public String toString() { ////toString function
		return "Circle [radius=" + radius + ", p=" + p + "]";
	}
	
	
    public boolean isContained(Point po) { //Checks if point is in the circle
        return this.getPoint().distance(po) <= radius * MULTIPLICATIVE_EPSILON;
    }


    public boolean isContained(Collection<Point> ps) { //Checks if few points is in the circle
        for (Point p : ps) {
            if (!isContained(p))
                return false;
        }
        return true;
    }

}
