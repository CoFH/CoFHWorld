package cofh.cofhworld.data.shape;

import cofh.cofhworld.util.random.WeightedEnum;
import net.minecraft.util.WeightedRandom;

import java.util.List;
import java.util.Random;

public class Shape2D {

	private final List<WeightedEnum<PlaneShape>> shapes; // TODO: convert to shape with unique rotations, each?
	private final List<WeightedEnum<Rotation>> rotations;
	private final List<WeightedEnum<Mirror>> mirrors;

	private final int shapesWeight, rotationsWeight, mirrorsWeight;

	public Shape2D(List<WeightedEnum<PlaneShape>> shapes, List<WeightedEnum<Rotation>> rotations, List<WeightedEnum<Mirror>> mirrors) {

		this.shapes = shapes;
		this.rotations = rotations;
		this.mirrors = mirrors;

		shapesWeight = WeightedRandom.getTotalWeight(shapes);
		rotationsWeight = WeightedRandom.getTotalWeight(rotations);
		mirrorsWeight = WeightedRandom.getTotalWeight(mirrors);
	}

	public ShapeSettings2D getSettings(Random rand) {

		return new ShapeSettings2D(getRandomShape(rand), getRandomRotation(rand), getRandomMirror(rand));
	}

	public boolean inArea(int x, int y, int radius, ShapeSettings2D settings) {

		switch (settings.mirror) {
			case LEFT_RIGHT:
				x = -x - 1;
				break;
			case FRONT_BACK:
				y = -y - 1;
				break;
		}

		switch (settings.rotation) {
			case CLOCKWISE_90: {
				int t = y;
				y = x;
				x = -t - 1;
				break;
			}
			case CLOCKWISE_180: {
				y = -y - 1;
				x = -x - 1;
				break;
			}
			case COUNTERCLOCKWISE_90: {
				int t = y;
				y = -x - 1;
				x = t;
				break;
			}
		}

		return settings.shape.inArea(x, y, radius);
	}

	private PlaneShape getRandomShape(Random rand) {

		switch (shapes.size()) {
			case 0:
				return PlaneShape.CIRCLE;
			case 1:
				return shapes.get(0).value;
			default :
				return WeightedRandom.getRandomItem(rand, shapes, shapesWeight).value;
		}
	}

	private Rotation getRandomRotation(Random rand) {

		switch (shapes.size()) {
			case 0:
				return Rotation.NONE;
			case 1:
				return rotations.get(0).value;
			default :
				return WeightedRandom.getRandomItem(rand, rotations, rotationsWeight).value;
		}
	}

	private Mirror getRandomMirror(Random rand) {

		switch (shapes.size()) {
			case 0:
				return Mirror.NONE;
			case 1:
				return mirrors.get(0).value;
			default :
				return WeightedRandom.getRandomItem(rand, mirrors, mirrorsWeight).value;
		}
	}

	public static class ShapeSettings2D {

		public final PlaneShape shape;
		public final Rotation rotation;
		public final Mirror mirror;

		public ShapeSettings2D(PlaneShape shape, Rotation rotation, Mirror mirror) {

			this.shape = shape;

			this.rotation = rotation;
			this.mirror = mirror;
		}
	}

}
