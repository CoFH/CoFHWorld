package cofh.cofhworld.data.numbers;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Random;

public interface INumberProvider {

	default int intValue(World world, Random rand, DataHolder data) {

		return (int) longValue(world, rand, data);
	}

	long longValue(World world, Random rand, DataHolder pos);

	default float floatValue(World world, Random rand, DataHolder data) {

		return (float) doubleValue(world, rand, data);
	}

	default double doubleValue(World world, Random rand, DataHolder data) {

		return longValue(world, rand, data);
	}

	public class DataHolder {

		private final Object2ObjectOpenHashMap<String, Object> data = new Object2ObjectOpenHashMap<>(16);

		public DataHolder(BlockPos start) {

			setValue("start", start).setPosition(start);
			setValue("chunk", new Vec3i(start.getX() >> 4, 0, start.getZ() >> 4));
		}

		public Vec3i getPos(String key) {

			return (Vec3i) data.get(key);
		}

		public DataHolder setValue(String key, Object value) {

			data.put(key, value);
			return this;
		}

		public DataHolder setPosition(Vec3i pos) {

			setValue("position", pos);
			return this;
		}
	}

}
