package de.ctoffer.util;

import de.ctoffer.util.funtional.ThrowingConsumer;
import de.ctoffer.util.funtional.ThrowingSupplier;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;

public enum LoopUtils {
    ;

    public static <T> void loopUntilNull(final Supplier<T> source, final Consumer<T> action) {
        T object = source.get();
        while(object != null){
            action.accept(object);
            object = source.get();
        }
    }

    public static <T, E extends Exception, F extends Exception>
    void tryLoopUntilNull(final ThrowingSupplier<T, E> source, final ThrowingConsumer<T, F> action) throws E, F {
        T object = source.get();
        while(object != null){
            action.accept(object);
            object = source.get();
        }
    }
}
