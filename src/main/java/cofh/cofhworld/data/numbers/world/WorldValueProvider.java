package cofh.cofhworld.data.numbers.world;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.Heightmap.Type;

import java.util.Locale;
import java.util.Random;

public enum WorldValueProvider implements INumberProvider {
	WORLD_HEIGHT {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			return world.getDimensionType().getLogicalHeight();
		}
	},
	SEA_LEVEL {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			return world.getSeaLevel();
		}
	},
	GROUND_LEVEL {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return world.getHeight(Type.WORLD_SURFACE, pos.getX(), pos.getZ());
		}
	},
	RAIN_HEIGHT {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return world.getHeight(Type.MOTION_BLOCKING, pos.getX(), pos.getZ());
		}
	},
	HEIGHT_MAP {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return world.getHeight(Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
		}
	},
	HIGHEST_BLOCK {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return world.getHeight(Type.WORLD_SURFACE, pos.getX(), pos.getZ());
		}
	},
	SURFACE_BLOCK {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return Utils.getSurfaceBlockY(world, pos.getX(), pos.getZ());
		}
	},
	LOWEST_CHUNK_HORIZON {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return world.getHeight(Type.OCEAN_FLOOR, pos.getX(), pos.getZ());
		}
	},
	SPAWN_X {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			return world.getWorldInfo().getSpawnX();
		}
	},
	SPAWN_Y {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			return world.getWorldInfo().getSpawnY();
		}
	},
	SPAWN_Z {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			return world.getWorldInfo().getSpawnZ();
		}
	},
	CURRENT_X {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return pos.getX();
		}
	},
	CURRENT_Y {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return pos.getY();
		}
	},
	CURRENT_Z {
		@Override
		public long longValue(IWorld world, Random rand, DataHolder data) {

			Vector3i pos = data.getPosition();
			return pos.getZ();
		}
	},
	;

	private WorldValueProvider(String... alts) {

		putEntry(name(), this);
		for (String name : alts)
			putEntry(name, this);
	}

	public abstract long longValue(IWorld world, Random rand, DataHolder pos);

	private static Object2ObjectArrayMap<String, WorldValueProvider> mappings;

	private static void putEntry(String name, WorldValueProvider value) {

		if (mappings == null) {
			mappings = new Object2ObjectArrayMap<>();
		}
		mappings.put(name.toUpperCase(Locale.ROOT).replace('_', '-'), value);
	}

	public static WorldValueProvider getProvider(String type) {

		type = type.trim().toUpperCase(Locale.ROOT);
		if (!mappings.containsKey(type))
			throw new IllegalArgumentException("No enum constant " + WorldValueProvider.class.getCanonicalName() + "." + type);
		return mappings.get(type);
	}

}
