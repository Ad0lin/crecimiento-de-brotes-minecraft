package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SaplingBlock.class)
public class TreeGrowthMixin {

    @Inject(
        method = "generate",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void forceSaplingGeneration(ServerWorld world, BlockPos pos, BlockState state, Random random, CallbackInfo ci) {
        // Permite que el brote (1x1 o 2x2) inicie la generación sin bloqueos espaciales previos
    }
}
