package cofh.cofhworld.data;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public enum PlaneShape {

	SQUARE {
		@Override
		public boolean inArea(int x, int z, int radius) {

			return true;
		}
	}, CIRCLE {
		@Override
		public boolean inArea(int x, int z, int radius) {

			return x * x + z * z <= radius * radius;
		}
	},
	RIGHT_TRIANGLE(0, 0, 100, 100, 100, 0),
	TRIANGLE(0, 0, 50, 100, 100, 0),
	ARROWHEAD(15, 0, 50, 100, 85, 0, 50, 15),
	RHOMBUS(50, 0, 0, 50, 50, 100, 100, 50),
	HEXAGON(50, 0, 3, 24, 3, 76, 50, 100, 97, 76, 97, 24),
	OCTAGON(29, 0, 0, 29, 0, 71, 29, 100, 71, 100, 100, 71, 100, 29, 71, 0),
	PLUS(25, 0, 25, 25, 0, 25, 0, 75, 25, 75, 25, 100, 75, 100, 75, 75, 100, 75, 100, 25, 75, 25, 75, 0),
	;

	// layout: (x1, y1), (y2), (xDist: x2 - x1, yDist: y2 - y1)
	private float[] lineArray;

	private PlaneShape() {

		lineArray = null;
	}

	/**
	 * @param path flat array of xy pairs on a 100x100 grid defining a polygon, the final point will be automatically connected to the first
	 */
	private PlaneShape(int... path) {

		{
			assert (path.length & 1) == 1 : "Array of points contains an odd number of xy pairs."; // every x must have a y
			assert (path.length < 6) : "Array of points does not define a polygon."; // a single point does not make anything, a line does not make a polygon
			for (int i = 0, e = path.length; i < e; path[i++] -= 50); // shift everything over to center things around 0,0 and double our precision
		}
		final int length = path.length;
		final boolean selfConnected = path[0] == path[length - 2] && path[1] == path[length - 1];

		float[] points = lineArray = new float[(path.length >> 1) * 5 - (selfConnected ? 5 : 0)];
		int i = 0, j = 0;

		final float gridSize = 49.5f; // we lied, grid is 99x99; this helps hide rounding errors by slightly over-sizing the comparison
		for (int e = length - 2; i < e; i += 2) {
			float x1 = points[j++] = path[i] / gridSize;
			float y1 = points[j++] = path[i + 1] / gridSize;
			float x2 = path[i + 2] / gridSize;
			float y2 = points[j++] = path[i + 3] / gridSize;
			points[j++] = x2 - x1;
			points[j++] = y2 - y1;
		}
		if (!selfConnected) {
			float x1 = points[j++] = path[i] / gridSize;
			float y1 = points[j++] = path[i + 1] / gridSize;
			float x2 = path[0] / gridSize;
			float y2 = points[j++] = path[1] / gridSize;
			points[j++] = x2 - x1;
			points[j] = y2 - y1;
		}
	}

	final public boolean inArea(int x, int z, int radius, Rotation rot, Mirror mirror) {

		switch (mirror) {
			case LEFT_RIGHT:
				x = -x - 1;
				break;
			case FRONT_BACK:
				z = -z - 1;
				break;
		}

		switch (rot) {
			case CLOCKWISE_90: {
				int t = z;
				z = x;
				x = -t - 1;
				break;
			}
			case CLOCKWISE_180: {
				z = -z - 1;
				x = -x - 1;
				break;
			}
			case COUNTERCLOCKWISE_90: {
				int t = z;
				z = -x - 1;
				x = t;
				break;
			}
		}
		return inArea(x, z, radius);
	}

	public boolean inArea(int x, int z, int radius) {

		int wn = 0;
		float[] lines = lineArray;
		// might want to explode this to 7 points (H) and pass if >= 4 are valid
		float pX = (x + 0.5f) / radius, pY = (z + 0.5f) / radius;

		for (int i = 0, e = lines.length; i < e; i += 5) {
			float y = lines[i + 1];
			if (y <= pY) {
				if (lines[i + 2] > pY)
					if ((lines[i + 3] * (pY - y) - (pX - lines[i]) * lines[i + 4]) > 0)
						++wn;
			} else {
				if (lines[i + 2] <= pY)
					if ((lines[i + 3] * (pY - y) - (pX - lines[i]) * lines[i + 4]) < 0)
						--wn;
			}
		}

		return (wn & 1) == 1;
	}
}
