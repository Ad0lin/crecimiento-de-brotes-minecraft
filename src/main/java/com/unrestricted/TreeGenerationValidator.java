
package com.unrestricted;

import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.Map;
import java.util.Set;

public final class TreeGenerationValidator {

    private TreeGenerationValidator() {
    }

    /**
     * Valida la propuesta final contra el mundo original.
     *
     * plannedBlocks: estado final propuesto del arbol.
     * consumedSaplings: brotes de esta generacion.
     *
     * El mundo recibido NO debe contener las
     * modificaciones provisionales del arbol.
     */
    public static ValidationResult validate(
            BlockView originalWorld,
            Map<BlockPos, BlockState> plannedBlocks,
            Set<BlockPos> consumedSaplings) {

        if (plannedBlocks == null || plannedBlocks.isEmpty()) {
            return ValidationResult.reject(
                null, "Propuesta vacia"
            );
        }

        if (consumedSaplings == null) {
            return ValidationResult.reject(
                null, "Brotes no definidos"
            );
        }

        for (Map.Entry<BlockPos, BlockState> entry
                : plannedBlocks.entrySet()) {

            BlockPos pos = entry.getKey();
            BlockState proposed = entry.getValue();

            if (pos == null || proposed == null) {
                return ValidationResult.reject(
                    pos, "Entrada invalida"
                );
            }

            if (originalWorld.isOutOfHeightLimit(pos)) {
                return ValidationResult.reject(
                    pos, "Fuera del mundo"
                );
            }

            BlockState original =
                originalWorld.getBlockState(pos);

            // No existe una modificacion real.
            if (original.equals(proposed)) {
                continue;
            }

            // Aceptar el espacio originalmente vacio.
            if (original.isAir()) {
                // Evitar que el arbol coloque aire
                // como una operacion de eliminacion.
                if (proposed.isAir()) {
                    continue;
                }

                continue;
            }

            // Permitir consumir solamente los brotes
            // que pertenecen a esta generacion.
            if (consumedSaplings.contains(pos)
                    && original.getBlock()
                    instanceof SaplingBlock) {
                continue;
            }

            // Cualquier otra modificacion de bloques
            // preexistentes queda prohibida.
            return ValidationResult.reject(
                pos,
                "Bloque protegido: "
                    + original.getBlock()
                        .getTranslationKey()
            );
        }

        return ValidationResult.allow();
    }

    public record ValidationResult(
            boolean allowed,
            BlockPos blockedPosition,
            String reason) {

        public static ValidationResult allow() {
            return new ValidationResult(
                true, null, "Generacion permitida"
            );
        }

        public static ValidationResult reject(
                BlockPos position,
                String reason) {

            return new ValidationResult(
                false,
                position == null
                    ? null
                    : position.toImmutable(),
                reason
            );
        }
    }
}
