package com.unrestricted.mixin;

import net.minecraft.block.SaplingGenerator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SaplingGenerator.class)
public class SaplingGeneratorMixin {
    // Dejamos la clase preparada para interceptar el generador 
    // sin inyecciones agresivas que fallen en el bootstrap.
}
