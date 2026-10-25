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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerWorld.class)
public class LeafDropMixin {

    @Inject(
        method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
        at = @At("HEAD"),
        require = 0
    )
    private void interceptLeafDeletion(BlockPos pos, BlockState newState, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld world = (ServerWorld) (Object) this;
        
        // Verificamos si el bloque que va a ser borrado es una hoja
        BlockState currentState = world.getBlockState(pos);
        if (currentState.getBlock() instanceof LeavesBlock) {
            // Si el nuevo estado es aire (o sea, el bloque está siendo eliminado por Tree Harvester o decaimiento)
            if (newState.isAir()) {
                // Buscamos si hay algún jugador cerca (en un radio de 6 bloques) con Toque de Seda
                Box box = new Box(pos).expand(6.0);
                List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, box, player -> {
                    ItemStack mainHand = player.getMainHandStack();
                    if (mainHand.isEmpty()) return false;
                    try {
                        return world.getRegistryManager()
                            .getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                            .getOptional(Enchantments.SILK_TOUCH)
                            .map(entry -> EnchantmentHelper.getLevel(entry, mainHand) > 0)
                            .orElse(false);
                    } catch (Exception e) {
                        return false;
                    }
                });

                if (!players.isEmpty()) {
                    // Forzamos la caída del bloque físico exacto de hojas (roble, abedul, abeto, etc.)
                    Block.dropStack(world, pos, new ItemStack(currentState.getBlock().asItem()));
                }
            }
        }
    }
}
