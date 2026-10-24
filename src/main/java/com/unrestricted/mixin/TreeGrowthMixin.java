package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({
    net.minecraft.block.SaplingBlock.class,
    net.minecraft.block.MushroomPlantBlock.class,
    net.minecraft.block.FungusBlock.class
})
public class TreeGrowthMixin {

    @Inject(
        method = "canPlaceAt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldView;Lnet/minecraft/block/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void bypassSpatialRestrictions(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
