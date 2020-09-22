package cofh.cofhworld.wrapper;

import cofh.cofhworld.init.WorldProps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.ConfiguredPlacement;

import java.util.Random;
import java.util.function.BooleanSupplier;

public class VanillaFeatureWrapper<Config extends IFeatureConfig, FeatureC extends Feature<Config>> extends ConfiguredFeature<Config, FeatureC> {

	public static ConfiguredFeature<?, ?> wrapStandard(ConfiguredFeature<?, ?> input) {

		return new VanillaFeatureWrapper<>(input, () -> WorldProps.replaceStandardGeneration);
	}

	public static ConfiguredFeature<?, ?> wrapLakes(ConfiguredFeature<?, ?> input) {

		return new VanillaFeatureWrapper<>(input, () -> true);
	}

	public static ConfiguredFeature<?, ?> wrapVines(ConfiguredFeature<?, ?> input) {

		return new VanillaFeatureWrapper<>(input, () -> true);
	}

	final private BooleanSupplier test;

	private VanillaFeatureWrapper(ConfiguredFeature<Config, FeatureC> wrap, BooleanSupplier cannotRun) {

		super(wrap.feature, wrap.config);
		test = cannotRun;
	}

	public ConfiguredFeature<?, ?> withPlacement(ConfiguredPlacement<?> placement) {

		return new VanillaFeatureWrapper<>(super.withPlacement(placement), test);
	}

	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos) {

		if (test.getAsBoolean())
			return false;
		return super.place(world, generator, rand, pos);
	}
}
