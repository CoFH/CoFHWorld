package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.operation.BinaryCondition;
import cofh.cofhworld.data.condition.operation.ComparisonCondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.CacheProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.operation.ConditionalProvider;
import cofh.cofhworld.data.numbers.operation.UnaryMathProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.data.numbers.world.WorldValueProvider;
import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;

import java.util.Random;

public class DistributionCave extends Distribution {

	private final static INumberProvider MAX_LEVEL = new ConditionalProvider(
			new ComparisonCondition(
					new CacheProvider("ground_level", new WorldValueProvider("GROUND_LEVEL")),
					new ConstantProvider(20),
					">="
			),
			new CacheProvider("ground_level", new WorldValueProvider("GROUND_LEVEL")),
			new WorldValueProvider("WORLD_HEIGHT")
	);
	private final static INumberProvider MIN_LEVEL = ConstantProvider.ZERO;
	private final static INumberProvider INTERMEDIATE_LEVEL = new UniformRandomProvider(
			new DataProvider("min-height"),
			new UnaryMathProvider(new DataProvider("max-height"), "INCREMENT")
	);
	private final static ICondition IS_AIR_OR_FLUID = new BinaryCondition(
			new WorldValueCondition("IS_AIR"),
			new WorldValueCondition("IS_BLOCK_FLUID"),
			"OR"
	);

	private final WorldGen worldGen;
	private final INumberProvider count;
	private ICondition caveCondition = IS_AIR_OR_FLUID;
	private INumberProvider maxHeight = MAX_LEVEL, avgHeight = INTERMEDIATE_LEVEL, minHeight = MIN_LEVEL;
	private final boolean ceiling;

	public DistributionCave(String name, WorldGen worldGen, boolean ceiling, INumberProvider count, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.ceiling = ceiling;
	}

	public void setMaxLevel(INumberProvider level) {

		this.maxHeight = level;
	}

	public void setIntermediateLevel(INumberProvider level) {

		this.avgHeight = level;
	}

	public void setMinLevel(INumberProvider level) {

		this.minHeight = level;
	}

	public void setCaveCondition(ICondition condition) {

		this.caveCondition = condition;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, ISeedReader world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		DataHolder data = new DataHolder(pos);

		final int count = this.count.intValue(world, random, data);
		data.setValue("cluster-count", count);

		worldGen.setDecorationDefaults();

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			data.setValue("cluster-current", i);
			int x = blockX + random.nextInt(16);
			int z = blockZ + random.nextInt(16);
			if (!canGenerateInBiome(world, x, z, random)) {
				continue;
			}
			final int maxHeight = this.maxHeight.intValue(world, random, data.setPosition(new BlockPos(x, 64, z))),
					minHeight = this.minHeight.intValue(world, random, data.setValue("max-height", maxHeight)),
					avgHeight = this.avgHeight.intValue(world, random, data.setValue("min-height", minHeight));
			data.setValue("avg-height", avgHeight);

			int y = avgHeight;
			while (!caveCondition.checkCondition(world, random, data.setPosition(new BlockPos(x, y, z)))) if (++y < maxHeight) break;

			if (y == maxHeight & avgHeight > minHeight) {
				y = minHeight;
				while (!caveCondition.checkCondition(world, random, data.setPosition(new BlockPos(x, y, z)))) if (++y < avgHeight) break;
				if (y >= avgHeight) {
					continue;
				}
			}

			if (ceiling) {
				final int stopHeight = y < avgHeight ? avgHeight + 1 : maxHeight;
				while (caveCondition.checkCondition(world, random, data.setPosition(new BlockPos(x, y, z)))) if (++y < stopHeight) break;
				if (y >= stopHeight) {
					continue;
				}
			} else if (caveCondition.checkCondition(world, random, data.setPosition(new BlockPos(x, --y, z)))) {
				--y;
				while (caveCondition.checkCondition(world, random, data.setPosition(new BlockPos(x, y, z)))) if (y-- > minHeight) break;
				if (y <= minHeight) {
					continue;
				}
			}

			generated |= worldGen.generate(world, random, new BlockPos(x, y, z));
		}
		return generated;
	}

}
