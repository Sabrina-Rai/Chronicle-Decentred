package net.openhft.chronicle.decentred.util;

import com.koloboke.compile.KolobokeMap;
import com.koloboke.function.IntObjConsumer;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.AbstractMarshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

/**
 * A long to Object Map.
 */
@KolobokeMap
public abstract class IntObjMap<V> extends AbstractMarshallable {
    Class<V> vClass;

    public static <V> IntObjMap<V> withExpectedSize(Class<V> vClass, int expectedSize) {
        if (expectedSize < 8)
            return new SmallIntObjMap<>(expectedSize + 1);
        KolobokeIntObjMap<V> map = new KolobokeIntObjMap<>(expectedSize);
        map.vClass = vClass;
        return map;
    }

    public abstract void justPut(int key, V value);

    public abstract V get(int key);

    public abstract int size();

    public abstract boolean containsKey(int key);

    public abstract void clear();

    public abstract void forEach(IntObjConsumer<? super V> longObjConsumer);

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        clear();
        while (wire.isNotEmptyAfterPadding()) {
            int k = (int) wire.readEventNumber();
            V v = wire.getValueIn().object(vClass);
            justPut(k, v);
        }
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        forEach((k, v) -> wire.writeEventId(k).object(vClass, v));
    }

    public abstract String toString();
}