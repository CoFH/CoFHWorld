package cofh.cofhworld.parser;

public interface IBuilder<T> {

	/**
	 * Builds the object after all configuration has been set. This will use default values for any
	 * unspecified attributes for the object.
	 *
	 * @return the configured instance.
	 */
	T build();
}
