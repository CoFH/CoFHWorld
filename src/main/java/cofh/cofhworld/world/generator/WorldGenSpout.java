package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.shape.Shape2D;
import cofh.cofhworld.data.shape.Shape2D.ShapeSettings2D;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenSpout extends WorldGen {

	private final List<WeightedBlock> resource;
	private final Material[] material;

	private final Shape2D shape;

	private final INumberProvider radius;
	private final INumberProvider height;


	private final ICondition mirror;

	public WorldGenSpout(List<WeightedBlock> resource, List<Material> materials, Shape2D shape, INumberProvider radius, INumberProvider height, ICondition mirror) {

		this.resource = resource;
		material = materials.toArray(new Material[0]);
		this.shape = shape;
		this.radius = radius;
		this.height = height;
		this.mirror = mirror;
	}

	@Override
	public boolean generate(IWorld world, Random rand, final DataHolder data) {

		BlockPos pos = data.getPosition();

		final int xCenter = pos.getX();
		final int yCenter = pos.getY();
		final int zCenter = pos.getZ();

		final Shape2D shape = this.shape;
		final ShapeSettings2D settings = shape.getSettings(rand);

		final int height = this.height.intValue(world, rand, data);
		final boolean mirror = this.mirror.checkCondition(world, rand, data.setValue("height", height));
		boolean r = false;
		for (int y = mirror ? -height : 0; y < height; ++y) {
			int radius = this.radius.intValue(world, rand, data.setValue("layer", y).setPosition(pos.add(0, y, 0)));
			for (int x = -radius; x <= radius; ++x) {
				for (int z = -radius; z <= radius; ++z) {
					if (shape.inArea(x, z, radius, settings)) {
						r |= generateBlock(world, rand, xCenter + x, yCenter + y, zCenter + z, material, resource);
					}
				}
			}
		}

		return r;
	}

}
