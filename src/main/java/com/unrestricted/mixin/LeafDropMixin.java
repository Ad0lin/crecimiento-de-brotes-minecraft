package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LeavesBlock.class)
public class LeafDropMixin {

    @Inject(
        method = "getDroppedStacks",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void forceSilkTouchLeafDrops(BlockState state, LootContextParameterSet.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        // Obtenemos la herramienta que se está utilizando en el contexto
        ItemStack tool = builder.get(net.minecraft.loot.context.LootContextParameters.TOOL);

        if (tool != null && !tool.isEmpty()) {
            try {
                // Verificamos si la herramienta tiene Toque de Seda
                ServerWorld world = builder.getWorld();
                boolean hasSilkTouch = world.getRegistryManager()
                    .getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                    .getOptional(Enchantments.SILK_TOUCH)
                    .map(entry -> EnchantmentHelper.getLevel(entry, tool) > 0)
                    .orElse(false);

                if (hasSilkTouch) {
                    // Si tiene Toque de Seda, anulamos el botín normal y devolvemos únicamente el bloque de hojas
                    ItemStack leafBlockStack = new ItemStack(((LeavesBlock)(Object)this).asItem());
                    cir.setReturnValue(List.of(leafBlockStack));
                }
            } catch (Exception e) {
                // Seguridad ante cualquier fallo de contexto
            }
        }
    }
}
