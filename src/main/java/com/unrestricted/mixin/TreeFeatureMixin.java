package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TreeFeature.class)
public class TreeFeatureMixin {

    @Inject(
        method = "canReplace",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private static void forceReplaceBlocks(TestableWorld world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = world.getBlockState(pos);
        
        // Protegemos el suelo y la tierra base para que el árbol NO rompa el piso ni se trague la tierra de abajo
        if (state.isOf(Blocks.DIRT) || state.isOf(Blocks.GRASS_BLOCK) || 
            state.isOf(Blocks.PODZOL) || state.isOf(Blocks.MYCELIUM) || 
            state.isOf(Blocks.ROOTED_DIRT) || state.isOf(Blocks.MUD)) {
            return; // Dejamos que Minecraft maneje el suelo normalmente para que el piso quede intacto
        }

        // Para paredes, techos y obstáculos circundantes, permitimos el crecimiento libre
        cir.setReturnValue(true);
    }
}
