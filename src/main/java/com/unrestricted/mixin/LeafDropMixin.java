package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
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
        cancellable = true,
        require = 0
    )
    private void forceSilkTouchLeaves(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        try {
            // Verificamos si la herramienta utilizada tiene el encantamiento de Toque de Seda (Silk Touch)
            if (tool != null && !tool.isEmpty()) {
                var enchantmentRegistry = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
                var silkTouchEntry = enchantmentRegistry.getEntry(Enchantments.SILK_TOUCH).orElse(null);
                
                if (silkTouchEntry != null && EnchantmentHelper.getLevel(silkTouchEntry, tool) > 0) {
                    // Si tiene Toque de Seda, soltamos directamente el bloque físico de hojas y cancelamos el evento estándar
                    LeavesBlock.dropStack(world, pos, new ItemStack(state.getBlock().asItem()));
                    ci.cancel();
                }
            }
        } catch (Exception e) {
            // Protección por si ocurre cualquier imprevisto en tiempo de ejecución
        }
    }
}
