function initializeCoreMod() {

    var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
    var opcodes = Java.type('org.objectweb.asm.Opcodes')
    var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
    var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    var ChunkProvider$generate = ASMAPI.mapMethod("func_202092_b");
    var Biome$addFeature = ASMAPI.mapMethod("func_203611_a");

    function wrapVanillaGenerator(method, stageName, typeName) {

        if (!typeName)
            typeName = "net/minecraft/world/gen/GenerationStage$Decoration";

        var foundOre = false;
        for (var isn = method.instructions.getFirst();
            isn != null;
            isn = isn.getNext()) {
            if (isn.getOpcode() == opcodes.GETSTATIC &&
                typeName.equals(isn.owner)) {
                foundOre = stageName.equals(isn.name); // ah, enums. so nice.
            }
            if (foundOre &&
                isn.getOpcode() == opcodes.INVOKEVIRTUAL &&
                "net/minecraft/world/biome/Biome".equals(isn.owner) &&
                "(Lnet/minecraft/world/gen/GenerationStage$Decoration;Lnet/minecraft/world/gen/feature/ConfiguredFeature;)V".equals(isn.desc) &&
                Biome$addFeature.equals(isn.name)) {
                method.instructions.insertBefore(isn, new MethodInsnNode(opcodes.INVOKESTATIC,
                    "cofh/cofhworld/wrapper/VanillaFeatureWrapper",
                    "wrap",
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
                            "(Lnet/minecraft/world/gen/WorldGenRegion;)V".equals(isn.desc) &&
                            ChunkProvider$generate.equals(isn.name)) {
                            method.instructions.insertBefore(isn, new InsnNode(opcodes.DUP_X1));
                            method.instructions.insert(isn, new MethodInsnNode(opcodes.INVOKESTATIC,
                                "cofh/cofhworld/init/WorldHandler", "generate", "(Lnet/minecraft/world/gen/WorldGenRegion;)V", false));
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
                'methodName': 'func_222322_k', // addInfestedStone
                'methodDesc': '(Lnet/minecraft/world/biome/Biome;)V'
            },
            'transformer': function(method) {

                wrapVanillaGenerator(method, "UNDERGROUND_DECORATION");
                return method;
            }
        },
        'standard_gen_hook_nether_ore': {
            'target': {
                'type': 'METHOD',
                'class': 'net/minecraft/world/biome/NetherBiome',
                'methodName': '<init>',
                'methodDesc': '()V'
            },
            'transformer': function(method) {

                wrapVanillaGenerator(method, ASMAPI.mapField("field_202290_aj"), "net/minecraft/world/gen/feature/Feature");
                return method;
            }
        }
    }
}
