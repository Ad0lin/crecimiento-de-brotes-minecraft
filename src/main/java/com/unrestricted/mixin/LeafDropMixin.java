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
    private void forceSilkTouchLeaves(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        boolean hasSilkTouch = false;

        // 1. Verificamos si la herramienta pasada directamente tiene Toque de Seda
        if (tool != null && !tool.isEmpty()) {
            hasSilkTouch = world.getRegistryManager()
                .getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .getOptional(Enchantments.SILK_TOUCH)
                .map(entry -> EnchantmentHelper.getLevel(entry, tool) > 0)
                .orElse(false);
        }

        // 2. Respaldo para Tree Harvester: Si no hay herramienta pero hay un jugador cerca con Toque de Seda en la mano
        if (!hasSilkTouch && world instanceof ServerWorld serverWorld) {
            Box box = new Box(pos).expand(6.0); // Rango de 6 bloques alrededor de las hojas
            List<PlayerEntity> players = serverWorld.getEntitiesByClass(PlayerEntity.class, box, player -> {
                ItemStack mainHand = player.getMainHandStack();
                if (mainHand.isEmpty()) return false;
                return serverWorld.getRegistryManager()
                    .getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                    .getOptional(Enchantments.SILK_TOUCH)
                    .map(entry -> EnchantmentHelper.getLevel(entry, mainHand) > 0)
                    .orElse(false);
            });

            if (!players.isEmpty()) {
                hasSilkTouch = true;
            }
        }

        if (hasSilkTouch) {
            LeavesBlock.dropStack(world, pos, new ItemStack(((LeavesBlock)(Object)this).asItem()));
            ci.cancel();
        }
    }
}
