package cofh.cofhworld.data.condition.world;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public enum WorldValueEnum {

	BLOCK_CAN_PLACE {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return data.getBlock().getState().isValidPosition(world, new BlockPos(data.getPosition()));
		}
	},
	CAN_BLOCK_BE_REPLACED_BY_LEAVES {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().canBeReplacedByLeaves(world.getBlockState(pos), world, pos);
		}
	},
	CAN_RESPAWN {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return world.getDimensionType().doesBedWork();
		}
	},
	CAN_SEE_SKY {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return world.canSeeSky(new BlockPos(data.getPosition()));
		}
	},
	CAN_SNOW_AT {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			Biome biome = world.getBiome(pos);
			return biome.doesSnowGenerate(world, pos);
		}
	},
	DOES_WATER_VAPORIZE {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return world.getDimensionType().isUltrawarm();
		}
	},
	HAS_SKY_LIGHT {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return world.getDimensionType().hasSkyLight();
		}
	},
	IS_BLOCK_LIT_FROM_SKY {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return world.getLightFor(LightType.SKY, new BlockPos(data.getPosition()).up()) > 0;
		}
	},
	IS_AIR {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return world.isAirBlock(new BlockPos(data.getPosition()));
		}
	},
	IS_BLOCK_FERTILE {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

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
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return BlockTags.LEAVES.contains(world.getBlockState(pos).getBlock());
		}
	},
	IS_BLOCK_LIQUID {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return world.getBlockState(new BlockPos(data.getPosition())).getMaterial().isLiquid();
		}
	},
	IS_BLOCK_OPAQUE {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return world.getBlockState(new BlockPos(data.getPosition())).getMaterial().isOpaque();
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
	IS_BLOCK_REPLACEABLE {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getMaterial().isReplaceable();
		}
	},
	IS_BLOCK_SOLID {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return world.getBlockState(new BlockPos(data.getPosition())).getMaterial().isSolid();
		}
	},
	IS_BLOCK_LOG {
		@Override
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

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
		public boolean getValue(IWorld world, Random rand, DataHolder data) {

			return world.getDimensionType().isNatural();
		}
	};

	public abstract boolean getValue(IWorld world, Random rand, DataHolder data);

}
