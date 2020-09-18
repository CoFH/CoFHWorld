package cofh.cofhworld.data;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class DataHolder {

	private final Object2ObjectOpenHashMap<String, Object> data = new Object2ObjectOpenHashMap<>(16);

	public DataHolder(BlockPos start) {

		setValue("start", start).setPosition(start);
		setValue("chunk", new Vec3i(start.getX() >> 4, 0, start.getZ() >> 4));
	}

	public long getLong(String key) {

		Object r = data.get(key);
		if (r instanceof Number) {
			return ((Number) r).longValue();
		} else {
			return 0;
		}
	}

	public Vec3i getPos(String key) {

		Object r = data.get(key);
		if (r instanceof Vec3i) {
			return (Vec3i) r;
		} else {
			return Vec3i.NULL_VECTOR;
		}
	}

	public Vec3i getPosition() {

		return getPos("position");
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
