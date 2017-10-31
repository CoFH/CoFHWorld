package cofh.cofhworld.feature.distribution;

import cofh.cofhworld.feature.IDistribution;
import cofh.cofhworld.feature.IDistributionParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import com.typesafe.config.Config;
import net.minecraft.init.Blocks;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DecorationDist extends SurfaceDist {

    public DecorationDist(List<WeightedRandomBlock> matList, boolean useTopBlock) {
        super(matList, useTopBlock);
    }

    @Override
    public String defaultGenerator() {
        return "decoration";
    }

    public static class Parser implements IDistributionParser {

        @Override
        public IDistribution parse(String name, Config config, Logger log) {

            // The ONLY difference between surface and decoration is that the default materials
            // vary
            List<WeightedRandomBlock> defaultMats = Arrays.asList(new WeightedRandomBlock(Blocks.GRASS, -1));

            List<WeightedRandomBlock> matList = defaultMats;
            if (config.hasPath("material")) {
                matList = new ArrayList<>();
                if (!FeatureParser.parseResList(config.root().get("material"), matList, false)) {
                    log.warn("Invalid material list! Using default list.");
                    matList = defaultMats;
                }
            }
            // TODO: clarity on follow-terrain field
            boolean useTopBlock = (config.hasPath("follow-terrain") && config.getBoolean("follow-terrain"));
            return new DecorationDist(matList, useTopBlock);
        }
    }
}
