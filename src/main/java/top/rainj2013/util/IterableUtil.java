package top.rainj2013.util;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public static <T> Map<T, List<Integer>> getElementPositions(List<T> list) {
        Map<T, List<Integer>> positionsMap = Maps.newHashMap();
        IterableUtil.forEach(list, (index, t) -> positionsMap.computeIfAbsent(t, k -> new ArrayList<>()).add(index));
        return positionsMap;
    }
}
