package com.unrestricted.mixin;

import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SaplingGenerator.class)
public class SaplingGeneratorMixin {

    @Inject(
        method = "canGenerateLargeTree",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private static void forceLargeTreeGeneration(BlockState state, ServerWorld world, BlockPos pos, int x, int z, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
