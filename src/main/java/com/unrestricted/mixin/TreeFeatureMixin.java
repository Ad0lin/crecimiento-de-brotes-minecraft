
package com.unrestricted.mixin;

import com.unrestricted.TreePlacementRules;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(TreeFeature.class)
public abstract class TreeFeatureMixin {

    private static final AtomicInteger SPACE_TEST_COUNT =
            new AtomicInteger(0);

    private static final AtomicInteger BLOCKED_WRITE_COUNT =
            new AtomicInteger(0);

    private static final int MAX_LOGS = 30;

    /**
     * Mantiene la comprobacion conservadora.
     */
    @Inject(
        method = "canReplace(Lnet/minecraft/world/TestableWorld;Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void unrestricted$protectBlocks(
            TestableWorld world,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {

        boolean allowed = world.testBlockState(
            pos,
            TreePlacementRules::canReplace
        );

        if (!allowed) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Impide escrituras conflictivas mediante
     * el metodo de colocacion de TreeFeature.
     */
    @Inject(
        method = "setBlockState(Lnet/minecraft/world/ModifiableWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void unrestricted$protectNormalWrites(
            ModifiableWorld world,
            BlockPos pos,
            BlockState proposed,
            CallbackInfo ci) {

        if (!unrestricted$canWrite(world, pos, proposed)) {
            ci.cancel();
        }
    }

    /**
     * Protege tambien la colocacion silenciosa
     * que utiliza TreeFeature.
     */
    @Inject(
        method = "setBlockStateWithoutUpdatingNeighbors(Lnet/minecraft/world/ModifiableWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void unrestricted$protectSilentWrites(
            ModifiableWorld world,
            BlockPos pos,
            BlockState proposed,
            CallbackInfo ci) {

        if (!unrestricted$canWrite(world, pos, proposed)) {
            ci.cancel();
        }
    }

    private static boolean unrestricted$canWrite(
            ModifiableWorld world,
            BlockPos pos,
            BlockState proposed) {

        if (!(world instanceof StructureWorldAccess access)) {
            unrestricted$logBlocked(
                pos, "Mundo no verificable"
            );
            return false;
        }

        BlockState existing = access.getBlockState(pos);

        if (!TreePlacementRules.canReplace(existing)) {
            unrestricted$logBlocked(
                pos,
                "Existente: " + existing.getBlock()
                + " | Propuesto: " + proposed.getBlock()
            );
            return false;
        }

        return true;
    }

    private static void unrestricted$logBlocked(
            BlockPos pos,
            String reason) {

        int count = BLOCKED_WRITE_COUNT.incrementAndGet();

        if (count <= MAX_LOGS) {
            System.out.println(
                "[Unrestricted Trees WRITE BLOCKED]"
                + " | Posicion: " + pos.toShortString()
                + " | Motivo: " + reason
            );
        }
    }

    /**
     * Diagnostico original de espacio.
     * Todavia NO fuerza el crecimiento.
     */
    @Inject(
        method = "getTopPosition(Lnet/minecraft/world/TestableWorld;ILnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)I",
        at = @At("RETURN")
    )
    private void unrestricted$diagnoseSpace(
            TestableWorld world,
            int requestedHeight,
            BlockPos origin,
            TreeFeatureConfig config,
            CallbackInfoReturnable<Integer> cir) {

        int count = SPACE_TEST_COUNT.incrementAndGet();

        if (count > MAX_LOGS) {
            return;
        }

        int availableHeight = cir.getReturnValue();

        System.out.println(
            "[Unrestricted Trees SPACE TEST]"
            + " | Prueba: " + count
            + " | Origen: " + origin.toShortString()
            + " | Altura solicitada: " + requestedHeight
            + " | Altura disponible: " + availableHeight
        );
    }
}
