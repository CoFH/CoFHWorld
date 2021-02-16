package cofh.cofhworld.command.helpers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.World;

public class CoordinateHelpers {

    public static MutableBoundingBox CoordinatesToBox(World world, int x1, int y1, int z1, int x2, int y2, int z2, boolean wholeChunks) {

        // TODO - in 1.17 this should be modified to use the getBottomY and getTopY methods of the HeightLimitView interface.
        // The assumption that y == 0 is the lower bound of the world is not valid in 1.17.
        int maxY = world.getHeight();

        // Clamp y values to the dimension world height build limits.
        // Do not use 256 here as it is variable in 1.17 and above.
        if (y1 < 0) {
            y1 = 0;
        } else if (y1 > maxY) {
            y1 = maxY;
        }

        if (y2 < 0) {
            y2 = 0;
        } else if (y2 > maxY) {
            y2 = maxY;
        }

        // This will select a minimal sized cuboid that will contain all of the chunks which the coordinate range fit within.
        if (wholeChunks) {
            ChunkPos cp1 = world.getChunkAt(new BlockPos(x1, y1, z1)).getPos();
            x1 = cp1.getXStart();
            y1 = 0;
            z1 = cp1.getZStart();

            ChunkPos cp2 = world.getChunkAt(new BlockPos(x2, y2, z2)).getPos();
            x2 = cp2.getXEnd();
            y2 = maxY;
            z2 = cp2.getZEnd();
        }

        return MutableBoundingBox.createProper(x1, y1, z1, x2, y2, z2);
    }

}
