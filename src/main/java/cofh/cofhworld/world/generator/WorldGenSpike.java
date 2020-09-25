package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.operation.BinaryCondition;
import cofh.cofhworld.data.condition.operation.ComparisonCondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.operation.ConditionalProvider;
import cofh.cofhworld.data.numbers.operation.MathProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.data.numbers.world.DirectionalScanner;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenSpike extends WorldGen {

	private static final INumberProvider SEVEN = new ConstantProvider(7);
	private static final INumberProvider HALF_SEVEN = new MathProvider(SEVEN, new ConstantProvider(2), "DIVIDE");
	private static final INumberProvider SEVEN_PLUS_FOUR = new ConstantProvider(7 + 4);

	private final List<WeightedBlock> resource;
	private final Material[] material;

	public INumberProvider height = new UniformRandomProvider(SEVEN, SEVEN_PLUS_FOUR);

	public INumberProvider size = new MathProvider(
			new MathProvider(
					new DataProvider("height"),
					HALF_SEVEN,
					"DIVIDE"),
			new UniformRandomProvider(0, 2),
			"ADD");

	public INumberProvider yVariance = new ConditionalProvider(
			new ComparisonCondition(
					new DataProvider("size"),
					new ConstantProvider(1),
					"GREATER_THAN"),
			new UniformRandomProvider(-1, 3),
			new ConstantProvider(0));

	public ICondition largeSpikes = new BinaryCondition(
			new ComparisonCondition(new DataProvider("size"), new ConstantProvider(1), "GREATER_THAN"),
			new ComparisonCondition(new UniformRandomProvider(0, 60), new ConstantProvider(0), "EQUAL_TO"),
			"AND");

	public INumberProvider largeSpikeHeightGain = new UniformRandomProvider(10, 40);

	public INumberProvider layerSize = new ConditionalProvider(
			new ComparisonCondition(new DataProvider("layer"), new DataProvider("height-gain"), "GREATER_THAN_OR_EQUAL"),
			new MathProvider(
					new MathProvider(
							new ConstantProvider(1),
							new MathProvider(
									new MathProvider(
											new DataProvider("layer"),
											new DataProvider("height-gain"),
											"SUBTRACT"),
									new DataProvider("original-height"),
									"DIVIDE"),
							"SUBTRACT"),
					new DataProvider("size"),
					"MULTIPLY"),
			new ConstantProvider(1));

	public WorldGenSpike(List<WeightedBlock> resource, List<Material> materials) {

		this.resource = resource;
		material = materials.toArray(new Material[0]);
		setOffsetY(new DirectionalScanner(new WorldValueCondition("IS_AIR"), Direction.DOWN, new ConstantProvider(256)));
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
