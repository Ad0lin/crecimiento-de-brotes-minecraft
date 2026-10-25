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
        // Verificamos de forma directa si la herramienta tiene el encantamiento de Toque de Seda
        if (tool != null && !tool.isEmpty()) {
            boolean hasSilkTouch = EnchantmentHelper.getLevel(
                world.getRegistryManager().getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                    .getOrThrow(Enchantments.SILK_TOUCH), 
                tool
            ) > 0;

            if (hasSilkTouch) {
                // Soltamos el bloque físico de hojas y cancelamos los drops predeterminados
                LeavesBlock.dropStack(world, pos, new ItemStack(state.getBlock().asItem()));
                ci.cancel();
            }
        }
    }
}
