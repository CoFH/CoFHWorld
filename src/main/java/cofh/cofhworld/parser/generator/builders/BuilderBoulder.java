package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.operation.MathProvider;
import cofh.cofhworld.data.numbers.operation.UnaryMathProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderSize;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenBoulder;

import java.util.Collections;
import java.util.List;

public class BuilderBoulder extends BuilderSize<WorldGenBoulder> {

	private static final List<WeightedBlock> FILLER = Collections.singletonList(WeightedBlock.AIR_NORM);
	private static final INumberProvider HOLLOW_AMT = new UniformRandomProvider(0, 0.1665f);

	private static final INumberProvider QUANTITY = new ConstantProvider(3);

	private static final INumberProvider X_VAR = new MathProvider( // -radius-x / 2 to radius-x / 2 inclusive
			new UnaryMathProvider(new UnaryMathProvider(new DataProvider("radius-x"), "INCREMENT"), "NEGATE"),
			new UniformRandomProvider(ConstantProvider.ZERO, new UnaryMathProvider(new DataProvider("radius-x"), "DOUBLE")),
			"ADD"
	);
	private static final INumberProvider Y_VAR = new UniformRandomProvider(-2, 0);
	private static final INumberProvider Z_VAR = new MathProvider(// -radius-z / 2 to radius-z / 2 inclusive
			new UnaryMathProvider(new UnaryMathProvider(new DataProvider("radius-z"), "INCREMENT"), "NEGATE"),
			new UniformRandomProvider(ConstantProvider.ZERO, new UnaryMathProvider(new DataProvider("radius-z"), "DOUBLE")),
			"ADD"
	);

	private List<WeightedBlock> filler = FILLER;
	private ICondition hollow = ConstantCondition.FALSE;
	private INumberProvider hollowAmt = HOLLOW_AMT;

	private INumberProvider quantity = QUANTITY;

	private INumberProvider xVar = X_VAR;
	private INumberProvider yVar = Y_VAR;
	private INumberProvider zVar = Z_VAR;

	public BuilderBoulder(List<WeightedBlock> resource, List<Material> material) {

		super(resource, material);
	}

	public void setFiller(List<WeightedBlock> filler) {

		this.filler = filler;
	}

	public void setHollow(ICondition hollow) {

		this.hollow = hollow;
	}

	public void setHollowAmt(INumberProvider hollowAmt) {

		this.hollowAmt = hollowAmt;
	}

	public void setQuantity(INumberProvider quantity) {

		this.quantity = quantity;
	}

	public void setxVar(INumberProvider xVar) {

		this.xVar = xVar;
	}

	public void setyVar(INumberProvider yVar) {

		this.yVar = yVar;
	}

	public void setzVar(INumberProvider zVar) {

		this.zVar = zVar;
	}

	@Override
	public WorldGenBoulder build() {

		return new WorldGenBoulder(resource, size, material, filler, hollow, hollowAmt, quantity, xVar, yVar, zVar);
	}
}
