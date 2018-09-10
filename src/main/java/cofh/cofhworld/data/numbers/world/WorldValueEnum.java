package cofh.cofhworld.data.numbers.world;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.Utils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Random;

public enum WorldValueEnum {

	WORLD_HEIGHT {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			return world.getActualHeight();
		}
	}, SEA_LEVEL {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			return world.getSeaLevel();
		}
	}, GROUND_LEVEL {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			return world.provider.getAverageGroundLevel();
		}
	}, RAIN_HEIGHT {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			return world.getPrecipitationHeight(new BlockPos(data.getPos("position"))).getY();
		}
	}, HEIGHT_MAP {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			Vec3i pos = data.getPos("position");
			return world.getHeight(pos.getX(), pos.getZ());
		}
	}, HIGHEST_BLOCK {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			Vec3i pos = data.getPos("position");
			return Utils.getTopBlockY(world, pos.getX(), pos.getY());
		}
	}, SURFACE_BLOCK {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			Vec3i pos = data.getPos("position");
			return Utils.getSurfaceBlockY(world, pos.getX(), pos.getZ());
		}
	}, LOWEST_CHUNK_HORIZON {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			Vec3i pos = data.getPos("position");
			return world.getChunksLowestHorizon(pos.getX(), pos.getZ());
		}
	}, SPAWN_X {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			return world.getSpawnPoint().getX();
		}
	}, SPAWN_Y {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			return world.getSpawnPoint().getY();
		}
	}, SPAWN_Z {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			return world.getSpawnPoint().getZ();
		}
	}, CURRENT_X {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			Vec3i pos = data.getPos("position");
			return pos.getX();
		}
	}, CURRENT_Y {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			Vec3i pos = data.getPos("position");
			return pos.getY();
		}
	}, CURRENT_Z {
		@Override
		public long getValue(World world, Random rand, INumberProvider.DataHolder data) {

			Vec3i pos = data.getPos("position");
			return pos.getZ();
		}
	};

	public abstract long getValue(World world, Random rand, INumberProvider.DataHolder pos);

}
