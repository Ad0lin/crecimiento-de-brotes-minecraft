package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LeavesBlock.class)
public class LeafDropMixin {

    @Inject(
        method = "onStacksDropped",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void forceSilkTouchOrDecayLeaves(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        boolean hasSilkTouch = false;

        // 1. Si minas directamente con una herramienta que tiene Toque de Seda
        if (tool != null && !tool.isEmpty()) {
            hasSilkTouch = hasSilkTouch(world, tool);
        }

        // 2. Si es decaimiento natural (o ruptura sin herramienta), buscamos si hay un jugador cerca con Toque de Seda
        if (!hasSilkTouch) {
            Box box = new Box(pos).expand(6.0); // Rango de 6 bloques alrededor de la hoja que cae
            List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, box, player -> 
                hasSilkTouch(world, player.getMainHandStack())
            );

            if (!nearbyPlayers.isEmpty()) {
                hasSilkTouch = true;
            }
        }

        // Si se cumple cualquiera de las dos, soltamos el bloque de hojas y cancelamos el botín de brotes/manzanas
        if (hasSilkTouch) {
            LeavesBlock.dropStack(world, pos, new ItemStack(((LeavesBlock)(Object)this).asItem()));
            ci.cancel();
        }
    }

    private boolean hasSilkTouch(ServerWorld world, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        try {
            return world.getRegistryManager()
                .getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .getOptional(Enchantments.SILK_TOUCH)
                .map(entry -> EnchantmentHelper.getLevel(entry, stack) > 0)
                .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
