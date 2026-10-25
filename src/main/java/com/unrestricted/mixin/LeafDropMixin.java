package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
    private void forceSilkTouchDrops(BlockState state, ServerWorld world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> cir) {
        boolean hasSilkTouch = false;

        // 1. Si se usa una herramienta directamente con Toque de Seda
        if (stack != null && !stack.isEmpty()) {
            hasSilkTouch = hasSilkTouch(world, stack);
        }

        // 2. Si es decaimiento natural (sin herramienta o entidad minando), buscamos si hay un jugador cerca con Toque de Seda
        if (!hasSilkTouch && world instanceof ServerWorld serverWorld) {
            Box box = new Box(pos).expand(6.0); // Rango de 6 bloques alrededor de las hojas que decaen
            List<PlayerEntity> nearbyPlayers = serverWorld.getEntitiesByClass(PlayerEntity.class, box, player -> 
                hasSilkTouch(serverWorld, player.getMainHandStack())
            );

            if (!nearbyPlayers.isEmpty()) {
                hasSilkTouch = true;
            }
        }

        // Si se detecta Toque de Seda (por uso directo o por proximidad durante el decaimiento), devolvemos el bloque de hojas
        if (hasSilkTouch) {
            ItemStack leafStack = new ItemStack(((LeavesBlock)(Object)this).asItem());
            cir.setReturnValue(List.of(leafStack));
        }
    }

    private boolean hasSilkTouch(ServerWorld world, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;
        try {
            return world.getRegistryManager()
                .getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .getOptional(Enchantments.SILK_TOUCH)
                .map(entry -> EnchantmentHelper.getLevel(entry, itemStack) > 0)
                .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
