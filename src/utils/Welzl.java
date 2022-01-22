package utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Welzl
{

	public static Circle makeCircle(List<Point> points)
	{

		List<Point> shuffled = new ArrayList<>(points);
		Collections.shuffle(shuffled, new Random());

		Circle c = null;
		for (int i = 0; i < shuffled.size(); i++)
		{
			Point p = shuffled.get(i);
			if (c == null || !c.isContained(p))
				c = makeOnePointCircle(shuffled.subList(0, i + 1), p);
		}
		return c;
	}

	private static Circle makeOnePointCircle(List<Point> points, Point p)
	{
		Circle c = new Circle(p, 0);
		for (int i = 0; i < points.size(); i++)
		{
			Point q = points.get(i);
			if (!c.isContained(q))
			{
				if (c.getRadius() == 0)
					c = makeDiameter(p, q);
				else
					c = makeTwoPointsCircle(points.subList(0, i + 1), p, q);
			}
		}
		return c;
	}

	private static Circle makeTwoPointsCircle(List<Point> points, Point p, Point q)
	{
		Circle circ = makeDiameter(p, q);
		Circle left = null;
		Circle right = null;

		Point pq = q.subtract(p);
		for (Point r : points)
		{
			if (circ.isContained(r))
				continue;

			float cross = pq.cross(r.subtract(p));
			Circle c = makeCircumCircle(p, q, r);
			if (c == null)
				continue;
			else if (cross > 0
					&& (left == null || pq.cross(c.getPoint().subtract(p)) > pq.cross(left.getPoint().subtract(p))))
				left = c;
			else if (cross < 0
					&& (right == null || pq.cross(c.getPoint().subtract(p)) < pq.cross(right.getPoint().subtract(p))))
				right = c;
		}

		if (left == null && right == null)
			return circ;
		else if (left == null)
			return right;
		else if (right == null)
			return left;
		else
			return left.getRadius() <= right.getRadius() ? left : right;
	}

	static Circle makeDiameter(Point a, Point b)
	{
		Point c = new Point((a.x + b.x) / 2, (a.y + b.y) / 2);
		return new Circle(c, Math.max(c.distance(a), c.distance(b)));
	}

	static Circle makeCircumCircle(Point a, Point b, Point c)
	{

		float ox = (Math.min(Math.min(a.x, b.x), c.x) + Math.max(Math.max(a.x, b.x), c.x)) / 2;
		float oy = (Math.min(Math.min(a.y, b.y), c.y) + Math.max(Math.max(a.y, b.y), c.y)) / 2;
		float ax = a.x - ox, ay = a.y - oy;
		float bx = b.x - ox, by = b.y - oy;
		float cx = c.x - ox, cy = c.y - oy;
		float d = (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by)) * 2;
		if (d == 0)
			return null;
		float x = (((ax * ax + ay * ay) * (by - cy) + (bx * bx + by * by) * (cy - ay) + (cx * cx + cy * cy) * (ay - by))
				/ d);
		float y = (((ax * ax + ay * ay) * (cx - bx) + (bx * bx + by * by) * (ax - cx) + (cx * cx + cy * cy) * (bx - ax))
				/ d);
		Point p = new Point(ox + x, oy + y);
		float r = Math.max(Math.max(p.distance(a), p.distance(b)), p.distance(c));
		return new Circle(p, r);
	}

}
