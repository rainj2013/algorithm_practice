package top.rainj2013.util;

import top.rainj2013.exception.CalException;

import java.util.Arrays;
import java.util.OptionalDouble;

/**
 * Created by yangyujian
 * Time: 2017/12/10 21:00
 */
public class ArrayUtil {

    /**
     * 将一个一维数组重复 (rows, columns)次
     * 如[1, 2, 3]重复 (3, 2)次为
     * [
     *  [1, 2, 3, 1, 2, 3],
     *  [1, 2, 3, 1, 2, 3],
     *  [1, 2, 3, 1, 2, 3]
     * ]
     * @param input
     * @param rows
     * @param columns
     * @return
     */
    public static double[][] tile (double[] input, int rows, int columns) {
        int length = input.length;
        double[][] output = new double[rows][length * columns];
        for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < output[0].length; j++) {
                output[i][j] = input[j%length];
            }
        }
        return output;
    }

    /**
     * 截取矩阵(数组)
     * @param input 原数组
     * @param startPoint 截取的起始坐标
     * @param endPoint 截取的结束坐标
     * @return 截取后的数组
     */
    public static double[][] subArray (double[][] input, int[] startPoint, int[] endPoint) {
        int length = input.length;
        int startX = startPoint[0];
        int startY = startPoint[1];
        int endX = endPoint[0];
        int endY = endPoint[1];
        double[][] output = new double[endX - startX + 1][endY - startY + 1];
        int x = 0;
        for (int i = startX; i <= endX; i++) {
            int y = 0;
            for (int j = startY; j <= endY; j++) {
                output[x][y] = input[i][j];
                y++;
            }
            x++;
        }
        return output;
    }

    /**
     * 求一维数组中元素的平均数
     * @param input
     * @return
     * @throws CalException
     */
    public static double avg(double[] input) throws CalException {
        OptionalDouble optionalDouble = Arrays.stream(input).average();
        return optionalDouble.orElseThrow(CalException::new);
    }

}