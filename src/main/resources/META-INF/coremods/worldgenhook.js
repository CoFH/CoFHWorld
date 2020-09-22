function initializeCoreMod() {
    var opcodes = Java.type('org.objectweb.asm.Opcodes')
    var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
    var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    var ChunkProvider$generate = Java.type("net.minecraftforge.coremod.api.ASMAPI").mapMethod("func_202092_b");
    return {
        'worldgenhook': {
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
                            ChunkProvider$generate.equals(isn.name) &&
                            "(Lnet/minecraft/world/gen/WorldGenRegion;)V".equals(isn.desc)) {
                            method.instructions.insertBefore(isn, new InsnNode(opcodes.DUP_X1));
                            method.instructions.insert(isn, new MethodInsnNode(opcodes.INVOKESTATIC,
                                "cofh/cofhworld/init/WorldHandler", "generate", "(Lnet/minecraft/world/gen/WorldGenRegion;)V", false));
                            break; // we're done
                        }
                    }
                }
                return classNode;
            }
        }
    }
}
