package top.rainj2013.util;

import Jama.Matrix;
import com.google.common.collect.Lists;
import top.rainj2013.exception.CalException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangyujian
 * Time: 2017/12/12 14:55
 */
public class MatrixUtil {

    /**
     * 对矩阵中的每一列进行归一化
     * @param input
     * @return
     */
    public static double[][] norm(double[][] input) throws CalException {

        double[][] output = new double[input.length][input[0].length];

        int rows = input.length;
        int columns = input[0].length;
        Matrix matrix = new Matrix(input);
        for (int col = 0; col < columns; col++) {
            double[] columnData = matrix.getColumnVector(col);
            double max = ArrayUtil.max(columnData);
            double min = ArrayUtil.min(columnData);
            double range = max - min;
            for (int row = 0; row < rows; row++) {
                double value = input[row][col];
                output[row][col] = (value - min)/range;
            }
        }
        return output;
    }

    public static List<double[]> norm(List<double[]> input) throws CalException {
        AtomicInteger index = new AtomicInteger();
        double[][] inputArray = new double[input.size()][];
        input.forEach(doubles -> {
            inputArray[index.get()] = doubles;
            index.getAndIncrement();
        });
        double[][] outputArray = norm(inputArray);
        List<double[]> outputList = Lists.newArrayListWithExpectedSize(outputArray.length);
        Arrays.stream(outputArray).forEach(doubles -> outputList.add(doubles));
        return outputList;
    }
}
