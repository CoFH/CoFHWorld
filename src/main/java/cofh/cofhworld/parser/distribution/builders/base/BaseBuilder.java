package cofh.cofhworld.parser.distribution.builders.base;

import cofh.cofhworld.parser.IBuilder;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;

import java.util.function.Consumer;

public abstract class BaseBuilder<T extends IConfigurableFeatureGenerator> implements IBuilder<T> {

	protected String featureName;
	protected boolean retrogen;

	public void setFeatureName(String featureName) {

		this.featureName = featureName;
	}

	public void setRetrogen(Boolean retrogen) {

		this.retrogen = retrogen == Boolean.TRUE;
	}

	public static class FeatureNameData<T extends IConfigurableFeatureGenerator> implements Consumer<BaseBuilder<T>> {

		@SuppressWarnings({"unchecked", "rawtypes"})
		public static <T extends IConfigurableFeatureGenerator> Consumer<IBuilder<T>> of(String featureName) {

			return new FeatureNameData(featureName);
		}

		private final String featureName;

		private FeatureNameData(String featureName) {

			this.featureName = featureName;
		}

		@Override
		public void accept(BaseBuilder<T> baseBuilder) {

			baseBuilder.setFeatureName(featureName);
		}
	}
}
