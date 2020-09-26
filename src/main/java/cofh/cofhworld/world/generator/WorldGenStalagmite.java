package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.operation.MathProvider;
import cofh.cofhworld.data.numbers.world.DirectionalScanner;
import cofh.cofhworld.data.numbers.world.WorldValueProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.Direction;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenStalagmite extends WorldGen {

	protected final List<WeightedBlock> resource;
	protected final Material[] surface;
	protected final Material[] material;
	private final Direction direction;

	private final INumberProvider height;
	private final INumberProvider size;

	private final ICondition fat;
	private final ICondition smooth;
	private final ICondition altSinc;

	public WorldGenStalagmite(List<WeightedBlock> resource, List<Material> surface, List<Material> materials, Direction direction,
			INumberProvider height, INumberProvider size, ICondition fat, ICondition smooth, ICondition altSinc) {

		this.resource = resource;
		this.surface = surface == null ? null : surface.toArray(new Material[0]);
		material = materials.toArray(new Material[0]);
		this.direction = direction;
		this.height = height;
		this.size = size;
		this.fat = fat;
		this.smooth = smooth;
		this.altSinc = altSinc;
		setOffsetY(new DirectionalScanner(
				new WorldValueCondition("IS_AIR"),
				direction,
				direction == Direction.UP ? new MathProvider(
						new ConstantProvider(256),
						new WorldValueProvider("CURRENT_Y"),
						"SUBTRACT"
				) : new WorldValueProvider("CURRENT_Y")));
	}

	protected int getHeight(int x, int z, int size, Random rand, int height, boolean fat, boolean smooth, boolean altSinc) {

		if (smooth) {
			if ((x * x + z * z) * 4 >= size * size * 5) {
				return 0;
			}

			final double lim = (altSinc ? 600f : (fat ? 1f : .5f) * 400f) / size;
			final double pi = Math.PI;
			double r;
			r = Math.sqrt((r = ((x * lim) / pi)) * r + (r = ((z * lim) / pi)) * r) * pi / 180;
			if (altSinc && r < 1) {
				r = Math.sqrt((size * 2 * lim) / pi) * pi / 180;
			}
			if (r == 0) {
				return height;
			}
			if (!altSinc) {
				return (int) Math.round(height * (fat ? Math.sin(r) / r : Math.sin(r = r * pi) / r));
			}
			double sinc = (Math.sin(r) / r);
			return (int) Math.round(height * (sinc * 2 + (Math.sin(r = r * (pi * 4)) / r)) / 2 + rand.nextGaussian() * .75);
		} else {
			int absx = x < 0 ? -x : x, absz = (z < 0 ? -z : z);
			int dist = fat ? (absx < absz ? absz + absx / 2 : absx + absz / 2) : absx + absz;
			if (dist == 0) {
				return height;
			}
			int v = 1 + height / dist;
			return v > 1 ? rand.nextInt(v) : 0;
		}
	}

	@Override
	public boolean generate(IWorld world, Random rand, final DataHolder data) {

		final int yMod = direction.getYOffset();

		final int xStart = data.getPosition().getX();
		final int yStart = data.getPosition().getY() - yMod;
		final int zStart = data.getPosition().getZ();

		if (!canGenerateInBlock(world, xStart, yStart, zStart, surface)) {
			return false;
		}

		final boolean fat = this.fat.checkCondition(world, rand, data);
		final boolean smooth = this.smooth.checkCondition(world, rand, data.setValue("fat", fat));
		final boolean altSinc = this.altSinc.checkCondition(world, rand, data.setValue("smooth",smooth));

		final int maxHeight = height.intValue(world, rand, data.setValue("altSinc", altSinc));
		final int size = this.size.intValue(world, rand, data.setValue("height", maxHeight));

		boolean r = false;
		for (int x = -size; x <= size; ++x) {
			for (int z = -size; z <= size; ++z) {
				if (!canGenerateInBlock(world, xStart + x, yStart + yMod, zStart + z, surface)) {
					continue;
				}
				int height = getHeight(x, z, size, rand, maxHeight, fat, smooth, altSinc);
				for (int y = 0; y < height; ++y) {
					r |= generateBlock(world, rand, xStart + x, yStart + y * yMod, zStart + z, material, resource);
				}
			}
		}
		return r;
	}
}
