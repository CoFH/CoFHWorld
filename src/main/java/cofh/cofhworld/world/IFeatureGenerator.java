package cofh.cofhworld.world;

import net.minecraft.world.IWorld;

import java.util.Random;

/**
 * This interface should be implemented on classes which define a world feature to be generated. It is essentially
 * a more robust version of {@link net.minecraft.world.gen.feature.Feature}, and may include one or more WorldGenerators should you wish.
 *
 * @author King Lemming
 */
public interface IFeatureGenerator {

	/**
	 * Returns the name of the feature, used for unique identification in configs and retrogen.
	 */
	String getFeatureName();

	/**
	 * Generates the world feature.
	 *
	 * @param random     Random derived from the world seed.
	 * @param chunkX     Minimum X chunk-coordinate of the chunk. (x16 for block coordinate)
	 * @param chunkZ     Minimum Z chunk-coordinate of the chunk. (x16 for block coordinate)
	 * @param world      The world to generate in.
	 * @param hasVillage True if this chunk contains a village
	 * @param newGen     True on initial generation, false on retrogen.
	 * @return True if generation happened, false otherwise.
	 */
	boolean generateFeature(Random random, int chunkX, int chunkZ, IWorld world, boolean hasVillage, boolean newGen);

}
