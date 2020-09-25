package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.operation.MathProvider;
import cofh.cofhworld.data.numbers.operation.UnaryMathProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.data.numbers.world.DirectionalScanner;
import cofh.cofhworld.data.numbers.world.WorldValueProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldGenBoulder extends WorldGen {

	private final List<WeightedBlock> resource;
	private final Material[] material;
	private final INumberProvider size;

	public List<WeightedBlock> filler = Collections.singletonList(WeightedBlock.AIR);
	public ICondition hollow = ConstantCondition.FALSE;
	public INumberProvider hollowAmt = new UniformRandomProvider(0, 0.1665f);

	public INumberProvider quantity = new ConstantProvider(3);

	public INumberProvider xVar = new MathProvider( // -radius-x / 2 to radius-x / 2 inclusive
			new UnaryMathProvider(new UnaryMathProvider(new DataProvider("radius-x"), "INCREMENT"), "NEGATE"),
			new UniformRandomProvider(ConstantProvider.ZERO, new UnaryMathProvider(new DataProvider("radius-x"), "DOUBLE")),
			"ADD"
	);
	public INumberProvider yVar = new UniformRandomProvider(-2, 0);
	public INumberProvider zVar = new MathProvider(// -radius-z / 2 to radius-z / 2 inclusive
			new UnaryMathProvider(new UnaryMathProvider(new DataProvider("radius-z"), "INCREMENT"), "NEGATE"),
			new UniformRandomProvider(ConstantProvider.ZERO, new UnaryMathProvider(new DataProvider("radius-z"), "DOUBLE")),
			"ADD"
	);

	// TODO: shapes? sphere, cube, ellipsoid? more?

	public WorldGenBoulder(List<WeightedBlock> resource, INumberProvider minSize, List<Material> materials) {

		this.resource = resource;
		size = minSize;
		material = materials.toArray(new Material[0]);
		setOffsetY(new DirectionalScanner(new WorldValueCondition("IS_AIR"), Direction.DOWN, new WorldValueProvider("CURRENT_Y")));
	}

	@Override
	public boolean generate(IWorld world, Random rand, final DataHolder data) {

		BlockPos pos = data.getPosition();

		int xCenter = pos.getX();
		int yCenter = pos.getY();
		int zCenter = pos.getZ();

		boolean r = false;
		int i = quantity.intValue(world, rand, data);
		data.setValue("quantity", i);
		while (i-- > 0) {
			data.setValue("quantity-current", i);

			int xWidth = size.intValue(world, rand, data);
			int zWidth = size.intValue(world, rand, data.setValue("radius-x", xWidth));
			int yWidth = size.intValue(world, rand, data.setValue("radius-z", zWidth));
			data.setValue("radius-y", yWidth);
			float maxDist = (xWidth + yWidth + zWidth) * 0.333F + 0.5F;
			maxDist *= maxDist;
			float minDist = hollow.checkCondition(world, rand, data) ? (xWidth + yWidth + zWidth) * hollowAmt.floatValue(world, rand, data) : 0;
			minDist *= minDist;

			for (int x = -xWidth; x <= xWidth; ++x) {
				final int xDist = x * x;

				for (int z = -zWidth; z <= zWidth; ++z) {
					final int xzDist = xDist + z * z;

					for (int y = -yWidth; y <= yWidth; ++y) {
						final int dist = xzDist + y * y;

						if (dist <= maxDist) {
							if (dist >= minDist) {
								r |= generateBlock(world, rand, xCenter + x, yCenter + y, zCenter + z, material, resource);
							} else {
								r |= generateBlock(world, rand, xCenter + x, yCenter + y, zCenter + z, material, filler);
							}
						}
					}
				}

			}

			{
				int xOff = xVar.intValue(world, rand, data);
				int zOff = zVar.intValue(world, rand, data.setPosition(pos.add(xOff, 0, 0)));
				int yOff = yVar.intValue(world, rand, data.setPosition(pos.add(xOff, 0, zOff)));
				xCenter += xOff;
				zCenter += zOff;
				yCenter += yOff;
				data.setPosition(pos = new BlockPos(xCenter, yCenter, zCenter));
			}
		}

		return r;
	}

}
