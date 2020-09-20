package cofh.cofhworld.data.condition.world;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFlower;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Random;

public enum WorldValueEnum {

	BLOCK_BUSH_CAN_STAY {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			WeightedBlock block = data.getBlock();
			if (block.block instanceof BlockBush) {
				return ((BlockBush) block.block).canBlockStay(world, new BlockPos(data.getPosition()), block.getState());
			}
			return false;
		}
	},
	BLOCK_FLOWER_CAN_STAY {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			WeightedBlock block = data.getBlock();
			if (block.block instanceof BlockFlower) {
				return ((BlockFlower) block.block).canBlockStay(world, new BlockPos(data.getPosition()), block.getState());
			}
			return false;
		}
	},
	BLOCK_CAN_PLACE {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return data.getBlock().block.canPlaceBlockAt(world, new BlockPos(data.getPosition()));
		}
	},
	CAN_BLOCK_BE_REPLACED_BY_LEAVES {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().canBeReplacedByLeaves(world.getBlockState(pos), world, pos);
		}
	},
	CAN_BLOCK_SUSTAIN_LEAVES {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().canSustainLeaves(world.getBlockState(pos), world, pos);
		}
	},
	CAN_RESPAWN {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.provider.canRespawnHere();
		}
	},
	CAN_SEE_SKY {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.canSeeSky(new BlockPos(data.getPosition()));
		}
	},
	CAN_SNOW_AT {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.canSnowAt(new BlockPos(data.getPosition()), false);
		}
	},
	DOES_WATER_VAPORIZE {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.provider.doesWaterVaporize();
		}
	},
	HAS_SKY_LIGHT {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.provider.hasSkyLight();
		}
	},
	IS_AIR {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.isAirBlock(new BlockPos(data.getPosition()));
		}
	},
	IS_BLOCK_FERTILE {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().isFertile(world, pos);
		}
	},
	IS_BLOCK_FOLIAGE {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().isFoliage(world, pos);
		}
	},
	IS_BLOCK_LEAVES {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().isLeaves(world.getBlockState(pos), world, pos);
		}
	},
	IS_BLOCK_LIQUID {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.getBlockState(new BlockPos(data.getPosition())).getMaterial().isLiquid();
		}
	},
	IS_BLOCK_OPAQUE {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.getBlockState(new BlockPos(data.getPosition())).getMaterial().isOpaque();
		}
	},
	IS_BLOCK_PASSABLE {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().isPassable(world, pos);
		}
	},
	IS_BLOCK_REPLACEABLE {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
		}
	},
	IS_BLOCK_SOLID {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.getBlockState(new BlockPos(data.getPosition())).getMaterial().isSolid();
		}
	},
	IS_BLOCK_WOOD {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			BlockPos pos = new BlockPos(data.getPosition());
			return world.getBlockState(pos).getBlock().isWood(world, pos);
		}
	},
	IS_HIGH_HUMIDITY {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.isBlockinHighHumidity(new BlockPos(data.getPosition()));
		}
	},
	IS_NETHER {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.provider.isNether();
		}
	},
	IS_SPAWN_CHUNK {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			Vec3i pos = data.getPosition();
			return world.isSpawnChunk(pos.getX() >> 4, pos.getZ() >> 4);
		}
	},
	IS_SURFACE_WORLD {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.provider.isSurfaceWorld();
		}
	};

	public abstract boolean getValue(World world, Random rand, DataHolder data);

}
