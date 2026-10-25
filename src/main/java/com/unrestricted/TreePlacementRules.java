
package com.unrestricted;

import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;

public final class TreePlacementRules {

    private TreePlacementRules() {
    }

    /**
     * Politica conservadora de reemplazo.
     *
     * Solo admite aire y brotes.
     * No autoriza sustituir maquinaria,
     * bloques de construccion ni hojas existentes.
     */
    public static boolean canReplace(BlockState existing) {

        if (existing.isAir()) {
            return true;
        }

        if (existing.getBlock() instanceof SaplingBlock) {
            return true;
        }

        return false;
    }
}
