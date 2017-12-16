package top.rainj2013.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import top.rainj2013.exception.CalException;

import java.util.*;

/**
 * Created by yangyujian
 * Time: 2017/12/10 21:00
 */
public class ArrayUtil {

    /**
     * 将一个一维数组重复 (rows, columns)次
     * 如[1, 2, 3]重复 (3, 2)次为
     * [
     * [1, 2, 3, 1, 2, 3],
     * [1, 2, 3, 1, 2, 3],
     * [1, 2, 3, 1, 2, 3]
     * ]
     *
     * @param input
     * @param rows
     * @param columns
     * @return
     */
    public static double[][] tile(double[] input, int rows, int columns) {
        int length = input.length;
        double[][] output = new double[rows][length * columns];
        for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < output[0].length; j++) {
                output[i][j] = input[j % length];
            }
        }
        return output;
    }

    /**
     * 截取矩阵(数组)
     *
     * @param input      原数组
     * @param startPoint 截取的起始坐标
     * @param endPoint   截取的结束坐标
     * @return 截取后的数组
     */
    public static <E> E[][] subArray(E[][] input, int[] startPoint, int[] endPoint) {
        int length = input.length;
        int startX = startPoint[0];
        int startY = startPoint[1];
        int endX = endPoint[0];
        int endY = endPoint[1];
        Object[][] output = new Object[endX - startX + 1][endY - startY + 1];
        int x = 0;
        for (int i = startX; i <= endX; i++) {
            int y = 0;
            for (int j = startY; j <= endY; j++) {
                output[x][y] = input[i][j];
                y++;
            }
            x++;
        }
        return (E[][]) output;
    }

    /**
     * 求一维数组中元素的平均数
     *
     * @param input
     * @return
     * @throws CalException
     */
    public static double avg(double[] input) throws CalException {
        OptionalDouble optionalDouble = Arrays.stream(input).average();
        return optionalDouble.orElseThrow(CalException::new);
    }

    /**
     * 求一维数组中元素的最大值
     *
     * @param input
     * @return
     * @throws CalException
     */
    public static double max(double[] input) throws CalException {
        OptionalDouble optionalDouble = Arrays.stream(input).max();
        return optionalDouble.orElseThrow(CalException::new);
    }

    /**
     * 求一维数组中元素的最小值
     *
     * @param input
     * @return
     * @throws CalException
     */
    public static double min(double[] input) throws CalException {
        OptionalDouble optionalDouble = Arrays.stream(input).min();
        return optionalDouble.orElseThrow(CalException::new);
    }

    public static <E> E[][] mergeCols(E[][]... arrays) {
        int row = arrays[0].length;
        int totalCol = 0;

        Map<Integer, List<E[]>> map = Maps.newHashMapWithExpectedSize(row);

        for (int i = 0; i < arrays.length; i++) {
            E[][] innerArray = arrays[i];
            totalCol += innerArray[0].length;
            for (int rowIndex = 0; rowIndex < innerArray.length; rowIndex++) {
                List<E[]> rowArrays = map.get(rowIndex);
                if (rowArrays != null) {
                    rowArrays.add(innerArray[rowIndex]);
                    continue;
                }
                E[] data = innerArray[rowIndex];
                rowArrays = new ArrayList<>();
                rowArrays.add(data);
                map.put(rowIndex, rowArrays);
            }
        }
        Object[][] output = new Object[row][];
        int finalTotalCol = totalCol;
        map.forEach((k, v) -> {
            Object[] rowArray = new Object[finalTotalCol];
            int i = 0;
            for (E[] ds : v) {
                for (E d : ds) {
                    rowArray[i++] = d;
                }
            }
            output[k] = rowArray;
        });
        return (E[][]) output;
    }

    public static double[][] mergeRows(double[][]... arrays) {
        List<double[]> list = Lists.newArrayList();
        for (double[][] innerArray : arrays) {
            list.addAll(Arrays.asList(innerArray));
        }
        double[][] output = new double[list.size()][];
        IterableUtil.forEach(list, (index, doubles) -> output[index] = doubles);
        return output;
    }

    public static <E> E[][] removeCol(E[][] input, int col) {
        int rows = input.length;
        int cols = input[0].length;
        if (col == 0) {
            return subArray(input, new int[]{0, 1}, new int[]{rows - 1, cols - 1});
        }
        if (col == cols - 1) {
            return subArray(input, new int[]{0, 0}, new int[]{rows - 1, cols - 2});
        }

        E[][] leftArray = subArray(input, new int[]{0, 0}, new int[]{rows - 1, col - 1});
        E[][] rightArray = subArray(input, new int[]{0, col + 1}, new int[]{rows - 1, cols - 1});

        return mergeCols(leftArray, rightArray);
    }

    public static <E> E[] removeCol(E[] input, int col) {
        int cols = input.length;
        Object[] output = new Object[cols - 1];
        IterableUtil.forEach(input, (index, aDouble) -> {
            if (index > col) {
                output[index - 1] = input[index];
            }else if(index < col){
                output[index] = input[index];
            }
        });
        return (E[]) output;
    }

    public static <T> T[] getCol(T[][] input, int col) {
        Object[] colData = new Object[input.length];
        IterableUtil.forEach(input, (index, t) -> colData[index] = t[col]);
        return (T[]) colData;
    }

    public static void main(String[] args) {
        Double[] a = {1d, 2d, 3d, 4d};
        System.out.println(Arrays.deepToString(removeCol(a, 3)));
    }
}