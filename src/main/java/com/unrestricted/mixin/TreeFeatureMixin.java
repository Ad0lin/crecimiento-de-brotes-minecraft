
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
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;

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
     * Conserva la politica de reemplazo de la 1.50.
     *
     * Nunca considera reemplazable un bloque protegido
     * solamente para facilitar el crecimiento.
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
     * Proteccion de escrituras normales.
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
     * Proteccion de escrituras sin actualizar vecinos.
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
     * Consulta el estado original almacenado
     * por TreeGenerationBuffer.
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
     * Comprueba las columnas indispensables del tronco.
     *
     * width = 1 para arboles rectos 1x1.
     * width = 2 para arboles gigantes 2x2.
     *
     * Los obstaculos laterales no forman parte
     * de esta comprobacion.
     */
    private static boolean unrestricted$trunkIsClear(
            TestableWorld world,
            BlockPos origin,
            int requestedHeight,
            int width) {

        BlockPos.Mutable cursor = new BlockPos.Mutable();

        for (int y = 1; y <= requestedHeight; y++) {

            for (int dx = 0; dx < width; dx++) {

                for (int dz = 0; dz < width; dz++) {

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
                                + " | Ancho: " + width
                        );

                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Relaja la comprobacion lateral de Vanilla.
     *
     * VERSION 1.51:
     *
     * - GiantTrunkPlacer: conserva el soporte 2x2.
     * - StraightTrunkPlacer: agrega soporte 1x1.
     *
     * No modifica otros tipos de colocadores.
     *
     * Se conserva la validacion del tronco central
     * y el requisito de tener snapshot activo.
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

        // Si Vanilla ya permite crecer, no intervenimos.
        if (vanillaHeight >= requestedHeight) {
            return;
        }

        int trunkWidth;
        String placerName;

        if (config.trunkPlacer instanceof GiantTrunkPlacer) {

            trunkWidth = 2;
            placerName = "GiantTrunkPlacer";

        } else if (
                config.trunkPlacer instanceof StraightTrunkPlacer
        ) {

            trunkWidth = 1;
            placerName = "StraightTrunkPlacer";

        } else {

            // Los generadores curvos y ramificados
            // conservan la comprobacion Vanilla.
            return;
        }

        TreeGenerationBuffer snapshot =
                TreeGenerationBuffer.active();

        if (snapshot == null) {
            return;
        }

        if (!unrestricted$trunkIsClear(
                world,
                origin,
                requestedHeight,
                trunkWidth
        )) {
            return;
        }

        // Solo se omite el rechazo lateral.
        // Las escrituras continuan protegidas
        // por los Mixins de la version 1.50.
        cir.setReturnValue(requestedHeight);

        int overrideNumber =
                SPACE_OVERRIDE_COUNT.incrementAndGet();

        if (overrideNumber <= MAX_LOGS) {
            System.out.println(
                    "[Unrestricted Trees SPACE OVERRIDE]"
                    + " | Origen: " + origin.toShortString()
                    + " | Vanilla: " + vanillaHeight
                    + " | Nueva altura: " + requestedHeight
                    + " | Generador: " + placerName
                    + " | Ancho tronco: " + trunkWidth
                    + " | Tronco central: LIBRE"
            );
        }
    }
}
