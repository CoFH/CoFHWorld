package cofh.cofhworld.data;

import cofh.cofhworld.util.random.WeightedBlock;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;

public class DataHolder {

	private final Object2ObjectOpenHashMap<String, Object> data = new Object2ObjectOpenHashMap<>(16);

	public DataHolder(BlockPos start) {

		setValue("start", start).setPosition(start);
		setValue("chunk", new Vec3i(start.getX() >> 4, 0, start.getZ() >> 4));
	}

	public boolean getBool(String key) {

		Object r = data.get(key);
		if (r instanceof Boolean) {
			return ((Boolean) r).booleanValue();
		} else {
			return false;
		}
	}

	public long getLong(String key) {

		Object r = data.get(key);
		if (r instanceof Number) {
			return ((Number) r).longValue();
		} else {
			return 0;
		}
	}

	public double getDouble(String key) {

		Object r = data.get(key);
		if (r instanceof Number) {
			return ((Number) r).doubleValue();
		} else {
			return 0;
		}
	}

	public WeightedBlock getBlock(String key) {

		Object r = data.get(key);
		if (r instanceof WeightedBlock) {
			return ((WeightedBlock) r);
		} else {
			return WeightedBlock.AIR;
		}
	}

	public WeightedBlock getBlock() {

		return getBlock("block");
	}

	public DataHolder setBlock(WeightedBlock block) {

		setValue("block", block);
		return this;
	}

	public BlockPos getPos(String key) {

		Object r = data.get(key);
		if (r instanceof BlockPos) {
			return (BlockPos) r;
		} else {
			return BlockPos.ZERO;
		}
	}

	public BlockPos getPosition() {

		return getPos("position");
	}

	public DataHolder setPosition(BlockPos pos) {

		setValue("position", pos);
		return this;
	}

	public DataHolder setValue(String key, Object value) {

		data.put(key, value);
		return this;
	}

	public DataHolder removeValue(String key) {

		data.remove(key);
		return this;
	}

	public boolean hasValue(String key) {

		return hasValue(key, null);
	}

	public boolean hasValue(String key, @Nullable Class<?> type) {

		return data.containsKey(key) && (type == null || type.isInstance(data.get(key)));
	}
}
