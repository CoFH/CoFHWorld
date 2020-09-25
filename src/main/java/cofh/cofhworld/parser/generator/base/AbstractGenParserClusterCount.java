package cofh.cofhworld.parser.generator.base;

import cofh.cofhworld.parser.IGeneratorParser;

public abstract class AbstractGenParserClusterCount implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block", "material", "cluster-size" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

}
