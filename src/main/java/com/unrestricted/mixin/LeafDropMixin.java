package com.unrestricted.mixin;

import net.minecraft.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public class LeafDropMixin {
    // Clase reservada y neutralizada para evitar interferencias con datapacks de tala masiva como Timber.
}
