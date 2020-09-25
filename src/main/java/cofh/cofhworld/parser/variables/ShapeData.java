package cofh.cofhworld.parser.variables;

import cofh.cofhworld.data.shape.Mirror;
import cofh.cofhworld.data.shape.PlaneShape;
import cofh.cofhworld.data.shape.Rotation;
import cofh.cofhworld.data.shape.Shape2D;
import cofh.cofhworld.util.random.WeightedEnum;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static cofh.cofhworld.CoFHWorld.log;

public class ShapeData {

	@Nullable
	public static Shape2D parse2DShapeEntry(ConfigValue shapeValue) {

		List<WeightedEnum<PlaneShape>> shapes = new ArrayList<>(1);
		List<WeightedEnum<Rotation>> rotations = new ArrayList<>(0);
		List<WeightedEnum<Mirror>> mirrors = new ArrayList<>(0);

		switch (shapeValue.valueType()) {
			case OBJECT: {
				ConfigObject shapeObj = (ConfigObject) shapeValue;
				if (shapeObj.containsKey("name")) {
					if (!EnumData.parseEnumList(shapeObj.get("name"), shapes, PlaneShape.class)) {
						log.warn("Invalid shape entry on line {}! Partial values will be used.", shapeObj.get("name").origin().lineNumber());
					}
				}
				if (shapeObj.containsKey("rotations")) {
					if (!EnumData.parseEnumList(shapeObj.get("rotations"), rotations, Rotation.class)) {
						log.warn("Invalid rotation entry on line {}! Partial values will be used.", shapeObj.get("rotations").origin().lineNumber());
					}
				}
				if (shapeObj.containsKey("mirrors")) {
					if (!EnumData.parseEnumList(shapeObj.get("mirrors"), mirrors, Mirror.class)) {
						log.warn("Invalid mirror entry on line {}! Partial values will be used.", shapeObj.get("mirrors").origin().lineNumber());
					}
				}
				break;
			}
			case LIST: {
				if (!EnumData.parseEnumList(shapeValue, shapes, PlaneShape.class)) {
					log.warn("Invalid shape list on line {}! Partial values will be used.", shapeValue.origin().lineNumber());
				}
				break;
			}
			case STRING: {
				WeightedEnum<PlaneShape> val = EnumData.parseEnumEntry(shapeValue, PlaneShape.class);
				if (val != null)
					shapes.add(val);
				 else {
					log.error("Invalid shape entry on line {}!", shapeValue.origin().lineNumber());
					return null;
				}
				break;
			}
			default:
				log.error("Invalid shape entry type on line {}!", shapeValue.origin().lineNumber());
				return null;
		}

		if (shapes.size() == 0) {
			return null;
		}

		return new Shape2D(shapes, rotations, mirrors);
	}
}
