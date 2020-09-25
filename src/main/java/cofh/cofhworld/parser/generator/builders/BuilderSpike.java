package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.operation.BinaryCondition;
import cofh.cofhworld.data.condition.operation.ComparisonCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.operation.ConditionalProvider;
import cofh.cofhworld.data.numbers.operation.MathProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderSize;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenSpike;

import java.util.List;

public class BuilderSpike extends BuilderSize<WorldGenSpike> {

	private static final INumberProvider SEVEN = new ConstantProvider(7);
	private static final INumberProvider HALF_SEVEN = new MathProvider(SEVEN, new ConstantProvider(2), "DIVIDE");
	private static final INumberProvider SEVEN_PLUS_FOUR = new ConstantProvider(7 + 4);

	private static final INumberProvider HEIGHT = new UniformRandomProvider(SEVEN, SEVEN_PLUS_FOUR);

	private static final INumberProvider SIZE = new MathProvider(
			new MathProvider(
					new DataProvider("height"),
					HALF_SEVEN,
					"DIVIDE"),
			new UniformRandomProvider(0, 2),
			"ADD");

	private static final INumberProvider Y_VARIANCE = new ConditionalProvider(
			new ComparisonCondition(
					new DataProvider("size"),
					new ConstantProvider(1),
					"GREATER_THAN"),
			new UniformRandomProvider(-1, 3),
			new ConstantProvider(0));

	private static final ICondition LARGE_SPIKES = new BinaryCondition(
			new ComparisonCondition(new DataProvider("size"), new ConstantProvider(1), "GREATER_THAN"),
			new ComparisonCondition(new UniformRandomProvider(0, 60), new ConstantProvider(0), "EQUAL_TO"),
			"AND");

	private static final INumberProvider LARGE_SPIKE_HEIGHT_GAIN = new UniformRandomProvider(10, 40);

	private static final INumberProvider LAYER_SIZE = new ConditionalProvider(
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

	private INumberProvider height = HEIGHT;

	private INumberProvider yVariance = Y_VARIANCE;

	private ICondition largeSpikes = LARGE_SPIKES;

	private INumberProvider largeSpikeHeightGain = LARGE_SPIKE_HEIGHT_GAIN;

	private INumberProvider layerSize = LAYER_SIZE;

	public BuilderSpike(List<WeightedBlock> resource, List<Material> material) {

		super(resource, material);
		size = SIZE;
	}

	public void setHeight(INumberProvider height) {

		this.height = height;
	}

	public void setyVariance(INumberProvider yVariance) {

		this.yVariance = yVariance;
	}

	public void setLargeSpikes(ICondition largeSpikes) {

		this.largeSpikes = largeSpikes;
	}

	public void setLargeSpikeHeightGain(INumberProvider largeSpikeHeightGain) {

		this.largeSpikeHeightGain = largeSpikeHeightGain;
	}

	public void setLayerSize(INumberProvider layerSize) {

		this.layerSize = layerSize;
	}

	@Override
	public WorldGenSpike build() {

		return new WorldGenSpike(resource, material, height, size, yVariance, largeSpikes, largeSpikeHeightGain, layerSize);
	}
}
