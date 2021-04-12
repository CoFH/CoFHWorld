package cofh.cofhworld.data.condition.world;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.operation.BinaryCondition.Operation;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

import java.util.Locale;
import java.util.Random;

public enum WorldValueCondition implements ICondition {

	BLOCK_CAN_PLACE {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			return data.getBlock().getState().isValidPosition(world, new BlockPos(data.getPosition()));
		}
	},
	CAN_BLOCK_BE_REPLACED_BY_LEAVES {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().canBeReplacedByLeaves(world.getBlockState(pos), world, pos);
		}
	},
	CAN_RESPAWN {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			return world.getDimensionType().doesBedWork();
		}
	},
	CAN_SEE_SKY {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			return world.canSeeSky(new BlockPos(data.getPosition()));
		}
	},
	CAN_SNOW_AT {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			Biome biome = world.getBiome(pos);
			return biome.doesSnowGenerate(world, pos);
		}
	},
	DOES_WATER_VAPORIZE {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			return world.getDimensionType().isUltrawarm();
		}
	},
	HAS_SKY_LIGHT {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			return world.getDimensionType().hasSkyLight();
		}
	},
	IS_BLOCK_LIT_FROM_SKY {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			return world.getLightFor(LightType.SKY, new BlockPos(data.getPosition()).up()) > 0;
		}
	},
	IS_BLOCK_AIR("IS_AIR") {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			return world.isAirBlock(new BlockPos(data.getPosition()));
		}
	},
	IS_BLOCK_FERTILE {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().isFertile(world.getBlockState(pos), world, pos);
		}
	},
	//	IS_BLOCK_FOLIAGE {
	//		@Override
	//		public boolean getValue(IWorld world, Random rand, DataHolder data) {
	//
	//			BlockPos pos = new BlockPos(data.getPosition());
	//			return world.getBlockState(pos).getBlock().isFoliage(world.getBlockState(pos), world, pos);
	//		}
	//	},
	IS_BLOCK_LEAVES {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return BlockTags.LEAVES.contains(world.getBlockState(pos).getBlock());
		}
	},
	IS_BLOCK_FLUID {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			return !world.getBlockState(new BlockPos(data.getPosition())).getFluidState().isEmpty();
		}
	},
	//	IS_BLOCK_PASSABLE {
	//		@Override
	//		public boolean getValue(IWorld world, Random rand, DataHolder data) {
	//
	//			BlockPos pos = new BlockPos(data.getPosition());
	//			return world.getBlockState(pos).getBlock().isPassable(world, pos);
	//		}
	//	},
	IS_BLOCK_LOG {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return BlockTags.LOGS.contains(world.getBlockState(pos).getBlock());
		}
	},
	//	IS_HIGH_HUMIDITY {
	//		@Override
	//		public boolean getValue(IWorld world, Random rand, DataHolder data) {
	//
	//			return world.isBlockinHighHumidity(new BlockPos(data.getPosition()));
	//		}
	//	},
	//	IS_SPAWN_CHUNK {
	//		@Override
	//		public boolean getValue(IWorld world, Random rand, DataHolder data) {
	//
	//			Vec3i pos = data.getPosition();
	//			Vec3i spawnPos = world.getDimension().getSpawnPoint();
	//			spawnPos = new Vec3i(spawnPos.getX() >> 4, 0, spawnPos.getZ() >> 4);
	//			return spawnPos.equals(new Vec3i(pos.getX() >> 4, 0, pos.getZ() >> 4));
	//		}
	//	},
	IS_SURFACE_WORLD {
		@Override
		public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

			return world.getDimensionType().isNatural();
		}
	},
	;

	private WorldValueCondition(String... alts) {
		putEntry(name(), this);
		for (String name : alts)
			putEntry(name, this);
	}

	public abstract boolean checkCondition(IWorld world, Random rand, DataHolder data);

	private static Object2ObjectArrayMap<String, WorldValueCondition> mappings;

	private static void putEntry(String name, WorldValueCondition value){

		if (mappings == null) {
			mappings = new Object2ObjectArrayMap<>();
		}
		mappings.put(name.toUpperCase(Locale.ROOT).replace('_', '-'), value);
	}

	public static WorldValueCondition getCondition(String type) {

		type = type.trim().toUpperCase(Locale.ROOT);
		if (!mappings.containsKey(type))
			throw new IllegalArgumentException("No enum constant " + Operation.class.getCanonicalName() + "." + type);
		return mappings.get(type);
	}
}
