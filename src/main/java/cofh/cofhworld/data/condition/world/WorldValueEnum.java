package cofh.cofhworld.data.condition.world;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Random;

public enum WorldValueEnum {

	IS_AIR {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.isAirBlock(new BlockPos(data.getPosition()));
		}
	},
	IS_HIGH_HUMIDITY {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			return world.isBlockinHighHumidity(new BlockPos(data.getPosition()));
		}
	},
	IS_SPAWN_CHUNK {
		@Override
		public boolean getValue(World world, Random rand, DataHolder data) {

			Vec3i pos = data.getPosition();
			return world.isSpawnChunk(pos.getX() >> 4, pos.getZ() >> 4);
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
	};

	public abstract boolean getValue(World world, Random rand, DataHolder data);

}
