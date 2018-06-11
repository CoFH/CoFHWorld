package com.typesafe.config.impl;

import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigParseable;
import com.typesafe.config.ConfigSyntax;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Iterator;

// "do not use" then offer the features required
public class CoFHOrderedParsableFile extends Parseable {

	final private File input;

	public CoFHOrderedParsableFile(File input, ConfigParseOptions options) {

		this.input = input;
		postConstruct(options);
	}

	@Override
	protected Reader reader() throws IOException {

		if (ConfigImpl.traceLoadsEnabled())
			trace("Loading config from a file: " + input);
		InputStream stream = new FileInputStream(input);
		Reader reader = new InputStreamReader(stream, "UTF-8");
		return new BufferedReader(reader);
	}

	@Override
	ConfigSyntax guessSyntax() {

		return ConfigSyntax.CONF;
	}

	@Nullable
	@Override
	ConfigParseable relativeTo(String filename) {

		File sibling;
		if ((new File(filename)).isAbsolute()) {
			sibling = new File(filename);
		} else {
			// this may return null
			sibling = relativeTo(input, filename);
		}
		if (sibling == null)
			return null;
		if (sibling.exists()) {
			return new CoFHOrderedParsableFile(sibling, options().setOriginDescription(null));
		} else {
			return super.relativeTo(filename);
		}
	}

	@Override
	protected ConfigOrigin createOrigin() {

		return SimpleConfigOrigin.newFile(input.getPath());
	}

	@Override
	public String toString() {

		return getClass().getSimpleName() + "(" + input.getPath() + ")";
	}

	protected AbstractConfigValue rawParseValue(Reader reader, ConfigOrigin origin, ConfigParseOptions finalOptions) throws IOException {

		if (finalOptions.getSyntax() == ConfigSyntax.PROPERTIES) {
			return PropertiesParser.parse(reader, origin);
		} else {
			Iterator<Token> tokens = Tokenizer.tokenize(origin, reader, finalOptions.getSyntax());
			return CoFHWorldParser.parse(tokens, origin, finalOptions, includeContext());
		}
	}

}
