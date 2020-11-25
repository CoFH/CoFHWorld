package cofh.cofhworld.data.numbers.world;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.util.Utils;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.Heightmap.Type;

import java.util.Random;

public enum WorldValueEnum {

	WORLD_HEIGHT {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			return world.getDimensionType().getLogicalHeight();
		}
	}, SEA_LEVEL {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			return world.getSeaLevel();
		}
	}, GROUND_LEVEL {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return world.getHeight(Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
		}
	}, RAIN_HEIGHT {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return world.getHeight(Type.MOTION_BLOCKING, pos.getX(), pos.getZ());
		}
	}, HEIGHT_MAP {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return world.getHeight(Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
		}
	}, HIGHEST_BLOCK {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return world.getHeight(Type.WORLD_SURFACE, pos.getX(), pos.getZ());
		}
	}, SURFACE_BLOCK {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return Utils.getSurfaceBlockY(world, pos.getX(), pos.getZ());
		}
	}, LOWEST_CHUNK_HORIZON {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return world.getHeight(Type.OCEAN_FLOOR, pos.getX(), pos.getZ());
		}
	}, SPAWN_X {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			return world.getDimension().getSpawnPoint().getX();
		}
	}, SPAWN_Y {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			return world.getDimension().getSpawnPoint().getY();
		}
	}, SPAWN_Z {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			return world.getDimension().getSpawnPoint().getZ();
		}
	}, CURRENT_X {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return pos.getX();
		}
	}, CURRENT_Y {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return pos.getY();
		}
	}, CURRENT_Z {
		@Override
		public long getValue(IWorldReader world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return pos.getZ();
		}
	};

	public abstract long getValue(IWorldReader world, Random rand, DataHolder pos);

}
