package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.world.DirectionalScanner;
import cofh.cofhworld.data.numbers.world.WorldValueProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenSpike extends WorldGen {

	private final List<WeightedBlock> resource;
	private final Material[] material;

	private final INumberProvider height;

	private final INumberProvider size;

	private final INumberProvider yVariance;

	private final ICondition largeSpikes;

	private final INumberProvider largeSpikeHeightGain;

	private final INumberProvider layerSize;

	public WorldGenSpike(List<WeightedBlock> resource, List<Material> materials, INumberProvider height, INumberProvider size,
			INumberProvider yVariance, ICondition largeSpikes, INumberProvider largeSpikeHeightGain, INumberProvider layerSize) {

		this.resource = resource;
		material = materials.toArray(new Material[0]);
		this.height = height;
		this.size = size;
		this.yVariance = yVariance;
		this.largeSpikes = largeSpikes;
		this.largeSpikeHeightGain = largeSpikeHeightGain;
		this.layerSize = layerSize;
		setOffsetY(new DirectionalScanner(WorldValueCondition.IS_BLOCK_AIR, Direction.DOWN, WorldValueProvider.CURRENT_Y));
	}

	@Override
	public boolean generate(IWorld world, Random rand, final DataHolder data) {

		int xStart = data.getPosition().getX();
		int yStart = data.getPosition().getY();
		int zStart = data.getPosition().getZ();

		if (!canGenerateInBlock(world, xStart, yStart, zStart, material)) {
			return false;
		}
		data.setPosition(new BlockPos(xStart, yStart, zStart));

		int height = this.height.intValue(world, rand, data), originalHeight = height;
		data.setValue("height", height).setValue("original-height", height);
		int size = this.size.intValue(world, rand, data);
		data.setValue("size", size);
		{
			int posVar = yVariance.intValue(world, rand, data);
			yStart += posVar;
			data.setValue("variance-y", posVar).setPosition(new BlockPos(xStart, yStart, zStart));
		}

		if (largeSpikes.checkCondition(world, rand, data)) {
			data.setValue("large-spikes", true);
			int heightGain = largeSpikeHeightGain.intValue(world, rand, data);
			height += heightGain;
			data.setValue("height-gain", heightGain).setValue("height", height);
		} else {
			data.setValue("large-spikes", false).setValue("height-gain", 0);
		}

		int offsetHeight = height - originalHeight;

		for (int y = 0; y < height; ++y) {
			float layerSize = this.layerSize.floatValue(world, rand, data.setValue("layer", y));
			// layerSize = y >= offsetHeight ? (1.0F - (float) (y - offsetHeight) / (float) originalHeight) * size : 1;
			int width = MathHelper.ceil(layerSize);

			for (int x = -width; x <= width; ++x) {
				float xDist = Math.abs(x) - 0.25F;

				for (int z = -width; z <= width; ++z) {
					float zDist = Math.abs(z) - 0.25F;

					if ((x == 0 && z == 0 || xDist * xDist + zDist * zDist <= layerSize * layerSize) && (x != -width && x != width && z != -width && z != width || rand.nextFloat() <= 0.75F)) {

						generateBlock(world, rand, xStart + x, yStart + y, zStart + z, material, resource);

						if (y != 0 && width > 1) {
							generateBlock(world, rand, xStart + x, yStart - y + offsetHeight, zStart + z, material, resource);
						}
					}
				}
			}
		}
		return true;
	}

}
