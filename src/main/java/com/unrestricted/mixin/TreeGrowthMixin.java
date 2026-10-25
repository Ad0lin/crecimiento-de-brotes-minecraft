package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.random.RandomGenerator;

@Mixin({
    net.minecraft.block.SaplingBlock.class,
    net.minecraft.block.MushroomPlantBlock.class,
    net.minecraft.block.FungusBlock.class
})
public class TreeGrowthMixin {

    @Inject(
        method = "grow",
        at = @At("HEAD"),
        require = 0
    )
    private void forceGrow(ServerWorld world, BlockPos pos, BlockState state, RandomGenerator random, CallbackInfo ci) {
        // Permite que el proceso de crecimiento se ejecute libremente sin bloqueos espaciales.
    }
}
