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

public class WorldGenPlate extends WorldGen {

	private final List<WeightedBlock> resource;
	private final Material[] material;

	private final Shape2D shape;

	private final INumberProvider radius;
	private final INumberProvider height;

	private final ICondition slim;

	public WorldGenPlate(List<WeightedBlock> resource, List<Material> materials, Shape2D shape, INumberProvider radius, INumberProvider height, ICondition slim) {

		this.resource = resource;
		material = materials.toArray(new Material[0]);
		this.radius = radius;
		this.shape = shape;
		this.height = height;
		this.slim = slim;
	}

	@Override
	public boolean generate(IWorld world, Random rand, final DataHolder data) {

		int x = data.getPosition().getX();
		int y = data.getPosition().getY();
		int z = data.getPosition().getZ();

		final Shape2D shape = this.shape;
		final ShapeSettings2D settings = shape.getSettings(rand);

		++y;
		int size = radius.intValue(world, rand, data);
		int height = this.height.intValue(world, rand, data.setValue("radius", radius));
		data.setValue("height", height);

		boolean r = false;
		for (int posX = x - size; posX <= x + size; ++posX) {
			int areaX = posX - x;
			for (int posZ = z - size; posZ <= z + size; ++posZ) {
				int areaZ = posZ - z;

				if (shape.inArea(areaX, areaZ, size, settings)) {
					data.setValue("layer-x", areaX).setValue("layer-z", areaZ).setPosition(new BlockPos(posX, y, posZ));
					final boolean slim = this.slim.checkCondition(world, rand, data);
					for (int posY = y - height; slim ? posY < y + height : posY <= y + height; ++posY) {
						r |= generateBlock(world, rand, posX, posY, posZ, material, resource);
					}
				}
			}
		}

		return r;
	}

}
