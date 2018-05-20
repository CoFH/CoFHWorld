package cofh.cofhworld.parser.generator.base;

import cofh.cofhworld.parser.IGeneratorParser;

public abstract class AbstractGenParserBlock implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

}
