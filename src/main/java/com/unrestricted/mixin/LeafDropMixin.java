package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public class LeafDropMixin {

    @Inject(
        method = "onStacksDropped",
        at = @At("HEAD"),
        cancellable = true
    )
    private void forceSilkTouchLeaves(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        if (tool != null && !tool.isEmpty()) {
            boolean hasSilkTouch = world.getRegistryManager()
                .getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .getOptional(Enchantments.SILK_TOUCH)
                .map(entry -> EnchantmentHelper.getLevel(entry, tool) > 0)
                .orElse(false);

            if (hasSilkTouch) {
                LeavesBlock.dropStack(world, pos, new ItemStack(state.getBlock().asItem()));
                ci.cancel();
            }
        }
    }
}
