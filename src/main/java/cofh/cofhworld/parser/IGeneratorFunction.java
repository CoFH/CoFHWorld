package cofh.cofhworld.parser;

import cofh.cofhworld.parser.IGeneratorParser.InvalidGeneratorException;
import com.typesafe.config.ConfigValue;

@FunctionalInterface
public interface IGeneratorFunction<R> {

	R apply(ConfigValue t) throws InvalidGeneratorException;
}
