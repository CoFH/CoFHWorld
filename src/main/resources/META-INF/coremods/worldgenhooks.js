function initializeCoreMod() {

    var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
    var opcodes = Java.type('org.objectweb.asm.Opcodes')
    var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
    var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    var ChunkProvider$generate = ASMAPI.mapMethod("func_230351_a_");
    var Biome$addFeature = ASMAPI.mapMethod("func_242513_a");

    function wrapVanillaGenerator(method, stageName, typeName, wrapper) {

        if (!typeName)
            typeName = "net/minecraft/world/gen/GenerationStage$Decoration";
        if (!wrapper)
            wrapper = "wrapStandard";

        var foundOre = false;
        for (var isn = method.instructions.getFirst();
            isn != null;
            isn = isn.getNext()) {
            if (isn.getOpcode() == opcodes.GETSTATIC &&
                typeName.equals(isn.owner)) {
                foundOre = stageName.equals(isn.name); // ah, enums. so nice.
            } else if(foundOre && isn.getOpcode() == opcodes.GETSTATIC) {
                print("Wrapping: " + isn.name);
            }
            if (foundOre &&
                isn.getOpcode() == opcodes.INVOKEVIRTUAL &&
                "net/minecraft/world/biome/BiomeGenerationSettings$Builder".equals(isn.owner) &&
                "(Lnet/minecraft/world/gen/GenerationStage$Decoration;Lnet/minecraft/world/gen/feature/ConfiguredFeature;)Lnet/minecraft/world/biome/BiomeGenerationSettings$Builder;".equals(isn.desc) &&
                Biome$addFeature.equals(isn.name)) {
                method.instructions.insertBefore(isn, new MethodInsnNode(opcodes.INVOKESTATIC,
                    "cofh/cofhworld/wrapper/VanillaFeatureWrapper",
                    wrapper,
                    "(Lnet/minecraft/world/gen/feature/ConfiguredFeature;)Lnet/minecraft/world/gen/feature/ConfiguredFeature;",
                    false));
                foundOre = false;
            }
        }
    }

    return {
        'populate_hook': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.chunk.ChunkStatus'
            },
            'transformer': function(classNode) {

                for (var method in classNode.methods) {
                    method = classNode.methods[method]
                    if (!method || !method.instructions)
                        continue;
                    for (var isn = method.instructions.getFirst();
                        isn != null;
                        isn = isn.getNext()) {
                        if (isn.getOpcode() == opcodes.INVOKEVIRTUAL &&
                            "net/minecraft/world/gen/ChunkGenerator".equals(isn.owner) &&
                            "(Lnet/minecraft/world/gen/WorldGenRegion;Lnet/minecraft/world/gen/feature/structure/StructureManager;)V".equals(isn.desc) &&
                            ChunkProvider$generate.equals(isn.name)) {
                            method.instructions.insertBefore(isn, new InsnNode(opcodes.DUP2_X1));
                            method.instructions.insert(isn, new MethodInsnNode(opcodes.INVOKESTATIC,
                                "cofh/cofhworld/init/WorldHandler", "generate",
                                 "(Lnet/minecraft/world/gen/WorldGenRegion;Lnet/minecraft/world/gen/feature/structure/StructureManager;)V", false));
                            break; // we're done
                        }
                    }
                }
                return classNode;
            }
        },
        'standard_gen_hooks': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.biome.DefaultBiomeFeatures'
            },
            'transformer': function(classNode) {

                for (var method in classNode.methods) {
                    method = classNode.methods[method]
                    if (!method || !method.instructions)
                        continue;
                    wrapVanillaGenerator(method, "UNDERGROUND_ORES");
                }
                return classNode;
            }
        },
        'standard_gen_hook_silverfish': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.biome.DefaultBiomeFeatures',
                'methodName': 'func_243753_m', // addInfestedStone
                'methodDesc': '(Lnet/minecraft/world/biome/BiomeGenerationSettings$Builder;)V'
            },
            'transformer': function(method) {

                wrapVanillaGenerator(method, "UNDERGROUND_DECORATION");
                return method;
            }
        },
        'standard_gen_hook_nether_debris_ore': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.biome.DefaultBiomeFeatures',
                'methodName': 'func_243732_ap', // withDebrisOre
                'methodDesc': '(Lnet/minecraft/world/biome/BiomeGenerationSettings$Builder;)V'
            },
            'transformer': function(method) {

                wrapVanillaGenerator(method, "UNDERGROUND_DECORATION");
                return method;
            }
        },
        'standard_gen_hook_nether_common_blocks': {
             'target': {
                 'type': 'METHOD',
                 'class': 'net.minecraft.world.biome.DefaultBiomeFeatures',
                 'methodName': 'func_243731_ao', // withCommonNetherBlocks
                 'methodDesc': '(Lnet/minecraft/world/biome/BiomeGenerationSettings$Builder;)V'
             },
             'transformer': function(method) {

                 wrapVanillaGenerator(method, "UNDERGROUND_DECORATION"); // we capture gravel and blackstone in addition to gold ore and quartz ore because mojang can't types
                 return method;
             }
         }
    }
}
