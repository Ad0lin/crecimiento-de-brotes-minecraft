package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({
    net.minecraft.block.SaplingBlock.class,
    net.minecraft.block.MushroomPlantBlock.class,
    net.minecraft.block.FungusBlock.class
})
public class TreeGrowthMixin {

    @Inject(
        method = "grow",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void forceGrow(ServerWorld world, BlockPos pos, BlockState state, RandomGenerator random, CallbackInfo ci) {
        // Al interactuar con dispensadores o harina de hueso, 
        // permitimos que el proceso de crecimiento se ejecute libremente sin bloqueos espaciales.
    }
}
