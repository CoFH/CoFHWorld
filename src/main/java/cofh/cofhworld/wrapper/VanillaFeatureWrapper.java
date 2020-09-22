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

public class VanillaFeatureWrapper<Config extends IFeatureConfig, FeatureC extends Feature<Config>> extends ConfiguredFeature<Config, FeatureC> {

	public static ConfiguredFeature<?, ?> wrap(ConfiguredFeature<?, ?> input) {

		return new VanillaFeatureWrapper<>(input);
	}

	private VanillaFeatureWrapper(ConfiguredFeature<Config, FeatureC> wrap) {

		super(wrap.feature, wrap.config);
	}

	public ConfiguredFeature<?, ?> withPlacement(ConfiguredPlacement<?> placement) {

		return new VanillaFeatureWrapper<>(super.withPlacement(placement));
	}

	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos) {

		if (WorldProps.replaceStandardGeneration)
			return false;
		return super.place(world, generator, rand, pos);
	}
}
