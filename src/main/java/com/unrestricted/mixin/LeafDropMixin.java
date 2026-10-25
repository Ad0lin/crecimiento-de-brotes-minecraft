package com.unrestricted.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LeavesBlock.class)
public class LeafDropMixin {

    @Inject(
        method = "onBreak",
        at = @At("HEAD"),
        require = 0
    )
    private void interceptLeafBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            boolean hasSilkTouch = false;

            // 1. Si el juego provee al jugador directamente en el evento
            if (player != null) {
                ItemStack mainHand = player.getMainHandStack();
                hasSilkTouch = hasSilkTouch(serverWorld, mainHand);
            }

            // 2. Respaldo por si un mod de tala rápida (como Tree Harvester) rompe sin pasar el jugador directamente
            if (!hasSilkTouch) {
                Box box = new Box(pos).expand(5.0);
                List<PlayerEntity> nearbyPlayers = serverWorld.getEntitiesByClass(PlayerEntity.class, box, p -> 
                    hasSilkTouch(serverWorld, p.getMainHandStack())
                );

                if (!nearbyPlayers.isEmpty()) {
                    hasSilkTouch = true;
                }
            }

            // Si detectamos Toque de Seda, forzamos la caída del bloque físico de hojas
            if (hasSilkTouch) {
                Block.dropStack(serverWorld, pos, new ItemStack(((LeavesBlock)(Object)this).asItem()));
            }
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
