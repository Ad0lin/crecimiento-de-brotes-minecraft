
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
     * Comprueba una propuesta de generacion.
     *
     * plannedBlocks:
     *   posiciones y bloques que el arbol quiere colocar.
     *
     * consumedSaplings:
     *   posiciones exactas de los brotes que se
     *   consumiran durante el crecimiento.
     *
     * No modifica el mundo.
     */
    public static ValidationResult validate(
            BlockView world,
            Map<BlockPos, BlockState> plannedBlocks,
            Set<BlockPos> consumedSaplings) {

        if (plannedBlocks == null || plannedBlocks.isEmpty()) {
            return ValidationResult.reject(
                null, "No hay bloques para generar"
            );
        }

        if (consumedSaplings == null) {
            return ValidationResult.reject(
                null, "No se indicaron los brotes"
            );
        }

        for (Map.Entry<BlockPos, BlockState> entry
                : plannedBlocks.entrySet()) {

            BlockPos pos = entry.getKey();
            BlockState proposed = entry.getValue();

            if (pos == null || proposed == null) {
                return ValidationResult.reject(
                    pos, "Posicion o bloque invalido"
                );
            }

            if (world.isOutOfHeightLimit(pos)) {
                return ValidationResult.reject(
                    pos, "Fuera del limite del mundo"
                );
            }

            // La generacion no debe destruir bloques
            // colocando aire.
            if (proposed.isAir()) {
                return ValidationResult.reject(
                    pos, "Intento de colocar aire"
                );
            }

            BlockState existing = world.getBlockState(pos);

            // No hay bloque que proteger.
            if (existing.isAir()) {
                continue;
            }

            // Unicamente se pueden consumir los brotes
            // pertenecientes a este crecimiento.
            if (consumedSaplings.contains(pos)
                    && existing.getBlock()
                    instanceof SaplingBlock) {
                continue;
            }

            // Cualquier otro bloque preexistente
            // provoca el rechazo de todo el arbol.
            return ValidationResult.reject(
                pos,
                "Bloque protegido: "
                    + existing.getBlock()
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
