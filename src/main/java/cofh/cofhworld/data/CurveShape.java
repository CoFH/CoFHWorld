package cofh.cofhworld.data;

import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.util.Direction8;

public abstract class CurveShape {

	public abstract void step(float progress);

	public static class DIRECTED extends CurveShape {

		private final Direction8 direction;
		private final INumberProvider rate;

		public DIRECTED(Direction8 direction, INumberProvider rate) {

			this.direction = direction;
			this.rate = rate;
		}

		@Override
		public void step(float progress) {

		}
	}

	public static class SPAWN extends CurveShape {

		@Override
		public void step(float progress) {

		}
	}

	public static class BEZIER extends CurveShape{

		@Override
		public void step(float progress) {

		}
	}
}
