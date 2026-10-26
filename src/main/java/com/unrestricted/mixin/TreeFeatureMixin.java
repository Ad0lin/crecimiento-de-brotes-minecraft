
package com.unrestricted.mixin;

import com.unrestricted.TreeGenerationBuffer;
import com.unrestricted.TreePlacementRules;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.trunk.GiantTrunkPlacer;

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

    private static final AtomicInteger SPACE_OVERRIDE_COUNT =
            new AtomicInteger(0);

    private static final AtomicInteger BLOCKED_WRITE_COUNT =
            new AtomicInteger(0);

    private static final int MAX_LOGS = 30;

    /**
     * Mantiene la politica de reemplazo conservadora.
     *
     * Los bloques protegidos siguen sin considerarse
     * reemplazables por las comprobaciones Vanilla.
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
     * Protege las escrituras normales de TreeFeature.
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
     * Protege las escrituras sin actualizacion de vecinos.
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

    /**
     * Autoriza solamente posiciones cuyo estado original
     * permite una escritura segun el snapshot.
     */
    private static boolean unrestricted$canWrite(
            ModifiableWorld world,
            BlockPos pos,
            BlockState proposed) {

        if (!(world instanceof StructureWorldAccess access)) {
            unrestricted$logBlocked(
                    pos,
                    "Mundo no verificable"
            );
            return false;
        }

        TreeGenerationBuffer buffer =
                TreeGenerationBuffer.active();

        if (buffer == null) {
            unrestricted$logBlocked(
                    pos,
                    "Sin snapshot activo"
            );
            return false;
        }

        if (!buffer.mayWriteAt(pos)) {
            unrestricted$logBlocked(
                    pos,
                    "Original: " + buffer.originalDescription(pos)
                    + " | Actual: "
                    + access.getBlockState(pos).getBlock()
                    + " | Propuesto: "
                    + proposed.getBlock()
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
     * Verifica que el espacio indispensable para el
     * tronco 2x2 se encuentre libre.
     *
     * Las comprobaciones se hacen en las cuatro
     * columnas, desde un bloque encima del origen
     * hasta la altura solicitada, inclusive.
     *
     * No se consideran seguros los obstaculos
     * situados dentro de esas cuatro columnas.
     */
    private static boolean unrestricted$trunkIsClear(
            TestableWorld world,
            BlockPos origin,
            int requestedHeight) {

        BlockPos.Mutable cursor = new BlockPos.Mutable();

        for (int y = 1; y <= requestedHeight; y++) {

            for (int dx = 0; dx <= 1; dx++) {

                for (int dz = 0; dz <= 1; dz++) {

                    cursor.set(
                            origin.getX() + dx,
                            origin.getY() + y,
                            origin.getZ() + dz
                    );

                    boolean replaceable = world.testBlockState(
                            cursor,
                            TreePlacementRules::canReplace
                    );

                    if (!replaceable) {

                        System.out.println(
                                "[Unrestricted Trees TRUNK BLOCKED]"
                                + " | Posicion: "
                                + cursor.toShortString()
                        );

                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Permite ignorar restricciones de espacio
     * laterales exclusivamente en arboles gigantes
     * con tronco 2x2.
     *
     * Solo modifica el resultado de Vanilla si:
     *
     * 1. Vanilla redujo la altura disponible.
     * 2. Se utiliza GiantTrunkPlacer.
     * 3. Hay un snapshot de generacion activo.
     * 4. Las cuatro columnas del tronco estan libres.
     *
     * Las escrituras siguen pasando por las
     * protecciones de colocacion existentes.
     */
    @Inject(
        method = "getTopPosition(Lnet/minecraft/world/TestableWorld;ILnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)I",
        at = @At("RETURN"),
        cancellable = true
    )
    private void unrestricted$relaxLateralSpace(
            TestableWorld world,
            int requestedHeight,
            BlockPos origin,
            TreeFeatureConfig config,
            CallbackInfoReturnable<Integer> cir) {

        int vanillaHeight = cir.getReturnValue();

        int testNumber = SPACE_TEST_COUNT.incrementAndGet();

        if (testNumber <= MAX_LOGS) {
            System.out.println(
                    "[Unrestricted Trees SPACE TEST]"
                    + " | Prueba: " + testNumber
                    + " | Origen: " + origin.toShortString()
                    + " | Altura solicitada: " + requestedHeight
                    + " | Altura Vanilla: " + vanillaHeight
            );
        }

        // Si Vanilla ya permite toda la altura, no cambiamos nada.
        if (vanillaHeight >= requestedHeight) {
            return;
        }

        // Solo procesamos los arboles con tronco gigante.
        if (!(config.trunkPlacer instanceof GiantTrunkPlacer)) {
            return;
        }

        // Nunca forzamos una generacion sin captura inicial.
        TreeGenerationBuffer snapshot =
                TreeGenerationBuffer.active();

        if (snapshot == null) {
            return;
        }

        // La estructura principal del tronco debe estar libre.
        if (!unrestricted$trunkIsClear(
                world,
                origin,
                requestedHeight
        )) {
            return;
        }

        // La limitacion restante se trata como lateral.
        cir.setReturnValue(requestedHeight);

        int overrideNumber =
                SPACE_OVERRIDE_COUNT.incrementAndGet();

        if (overrideNumber <= MAX_LOGS) {
            System.out.println(
                    "[Unrestricted Trees SPACE OVERRIDE]"
                    + " | Origen: " + origin.toShortString()
                    + " | Vanilla: " + vanillaHeight
                    + " | Nueva altura: " + requestedHeight
                    + " | Tronco central: LIBRE"
                    + " | Obstaculos laterales: PERMITIDOS"
            );
        }
    }
}
