package cofh.cofhworld.data;

public enum PlaneShape {

	SQUARE {
		@Override
		public boolean inArea(int x, int z, int radius) {

			return true;
		}
	}, CIRCLE {
		@Override
		public boolean inArea(int x, int z, int radius) {

			return x * x + z * z <= radius * radius;
		}
	};

	public abstract boolean inArea(int x, int z, int radius);
}
