package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.operation.MathProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderSize;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenStalagmite;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;
import java.util.List;

public class BuilderStalagmite extends BuilderSize<WorldGenStalagmite> {

	private static final INumberProvider HEIGHT = new UniformRandomProvider(7, 7 + 4);
	private static final INumberProvider SIZE = new MathProvider(
			new MathProvider(
					new DataProvider("height"),
					new ConstantProvider(5),
					"DIVIDE"),
			new UniformRandomProvider(0, 2),
			"ADD");

	private List<Material> surface;
	private Direction direction;

	private INumberProvider height = HEIGHT;

	private ICondition fat = ConstantCondition.TRUE;
	private ICondition smooth = ConstantCondition.FALSE;
	private ICondition altSinc = ConstantCondition.FALSE;

	public BuilderStalagmite(List<WeightedBlock> resource, List<Material> material) {

		super(resource, material);
		size = SIZE;
	}

	public void setSurface(List<Material> surface) {

		this.surface = surface;
	}

	public void setDirection(Direction direction) {

		this.direction = direction;
	}

	public void setHeight(INumberProvider height) {

		this.height = height;
	}

	public void setFat(ICondition fat) {

		this.fat = fat;
	}

	public void setSmooth(ICondition smooth) {

		this.smooth = smooth;
	}

	public void setAltSinc(ICondition altSinc) {

		this.altSinc = altSinc;
	}

	@Nonnull
	@Override
	public WorldGenStalagmite build() {

		return new WorldGenStalagmite(resource, surface, material, direction, height, size, fat, smooth, altSinc);
	}
}
