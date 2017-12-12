package top.rainj2013.util;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 带下标的迭代器
 * Created by yangyujian
 * Time: 2017/12/12 21:15
 */
public class IterableUtil {
    public static <E> void forEach(Iterable<? extends E> elements, BiConsumer<Integer, ? super E> action) {
        Objects.requireNonNull(elements);
        Objects.requireNonNull(action);
        int index = 0;
        for (E element : elements) {
            action.accept(index++, element);
        }
    }

    public static <E> void forEach(E[] elements, BiConsumer<Integer, ? super E> action) {
        Objects.requireNonNull(elements);
        Objects.requireNonNull(action);
        int index = 0;
        for (E element : elements) {
            action.accept(index++, element);
        }
    }
}
