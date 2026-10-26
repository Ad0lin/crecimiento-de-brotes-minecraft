
package com.unrestricted;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TreeSimulationWorld
        implements InvocationHandler {

    private final StructureWorldAccess originalWorld;

    private final Map<BlockPos, BlockState> stagedBlocks =
            new LinkedHashMap<>();

    private final Map<BlockPos, BlockState> originalBlocks =
            new LinkedHashMap<>();

    private final StructureWorldAccess proxy;

    private int totalWrites = 0;

    public TreeSimulationWorld(
            StructureWorldAccess originalWorld) {

        if (originalWorld == null) {
            throw new IllegalArgumentException(
                "Mundo original nulo"
            );
        }

        this.originalWorld = originalWorld;

        this.proxy = (StructureWorldAccess)
                Proxy.newProxyInstance(
                    StructureWorldAccess.class.getClassLoader(),
                    new Class<?>[] {
                        StructureWorldAccess.class
                    },
                    this
                );
    }

    public StructureWorldAccess getWorld() {
        return proxy;
    }

    public Map<BlockPos, BlockState> getStagedBlocks() {
        return Collections.unmodifiableMap(stagedBlocks);
    }

    public Map<BlockPos, BlockState> getOriginalBlocks() {
        return Collections.unmodifiableMap(originalBlocks);
    }

    public int getTotalWrites() {
        return totalWrites;
    }

    public BlockState getBlockState(BlockPos pos) {
        BlockState staged = stagedBlocks.get(pos);

        if (staged != null) {
            return staged;
        }

        return originalWorld.getBlockState(pos);
    }

    public boolean stage(
            BlockPos pos,
            BlockState state) {

        if (pos == null || state == null) {
            throw new IllegalArgumentException(
                "Posicion o estado nulo"
            );
        }

        if (originalWorld.isOutOfHeightLimit(pos)) {
            return false;
        }

        BlockPos key = pos.toImmutable();

        originalBlocks.putIfAbsent(
            key,
            originalWorld.getBlockState(key)
        );

        stagedBlocks.put(key, state);
        totalWrites++;

        return true;
    }

    private static boolean isNamed(
            Method method,
            String named,
            String intermediary) {

        String actual = method.getName();

        return actual.equals(named)
                || actual.equals(intermediary);
    }

    private static boolean hasParameters(
            Method method,
            Class<?>... expected) {

        Class<?>[] actual = method.getParameterTypes();

        if (actual.length != expected.length) {
            return false;
        }

        for (int i = 0; i < actual.length; i++) {
            if (actual[i] != expected[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Object invoke(
            Object proxyObject,
            Method method,
            Object[] args) throws Throwable {

        String name = method.getName();

        if (method.getDeclaringClass() == Object.class) {
            return switch (name) {
                case "toString" -> "TreeSimulationWorld";
                case "hashCode" ->
                    System.identityHashCode(proxyObject);
                case "equals" ->
                    proxyObject == (
                        args == null ? null : args[0]
                    );
                default ->
                    throw new UnsupportedOperationException(name);
            };
        }

        // Escritura virtual:
        // setBlockState(pos, state, flags)
        if (isNamed(
                method,
                "setBlockState",
                "method_8652"
            )
            && hasParameters(
                method,
                BlockPos.class,
                BlockState.class,
                int.class
            )
            && method.getReturnType() == boolean.class) {

            return stage(
                (BlockPos) args[0],
                (BlockState) args[1]
            );
        }

        // Lectura del bloque provisional.
        if (isNamed(
                method,
                "getBlockState",
                "method_8320"
            )
            && hasParameters(method, BlockPos.class)
            && method.getReturnType() == BlockState.class) {

            return getBlockState((BlockPos) args[0]);
        }

        // Lectura de fluidos, respetando los
        // estados provisionales de los bloques.
        if (isNamed(
                method,
                "getFluidState",
                "method_8316"
            )
            && hasParameters(method, BlockPos.class)
            && method.getReturnType() == FluidState.class) {

            return getBlockState(
                (BlockPos) args[0]
            ).getFluidState();
        }

        // Seguridad: no delegar otros metodos.
        throw new UnsupportedOperationException(
            "Metodo no permitido en simulacion: "
            + name
            + " | Firma: "
            + method.toGenericString()
        );
    }
}
