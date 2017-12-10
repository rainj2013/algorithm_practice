package top.rainj2013.knn;

import Jama.Matrix;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.rainj2013.exception.CalException;
import top.rainj2013.util.ArrayUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangyujian
 * Time: 2017/12/10 20:39
 */
public class Knn {

    private Logger logger = LoggerFactory.getLogger(Knn.class);

    public double classify(double[] testData, double[][] trainingData, int k) {
        //训练数据的数量
        int rowCount = trainingData.length;
        int columnCount = trainingData[0].length;
        double[][] testArray = ArrayUtil.tile(testData, rowCount, 1);
        Matrix testMatrix = new Matrix(testArray);
        Matrix trainingMatrix = new Matrix(ArrayUtil.subArray(
                trainingData, new int[]{0, 0}, new int[]{rowCount - 1, columnCount - 2}));
        Matrix calMatrix = testMatrix.minus(trainingMatrix);
        calMatrix = calMatrix.arrayTimes(calMatrix);
        double[][] calArray = calMatrix.getArray();
        List<double[]> distanceAndClassList = Lists.newArrayListWithExpectedSize(rowCount);

        AtomicInteger index = new AtomicInteger();
        Arrays.stream(calArray).map(doubles -> {
            try {
                return ArrayUtil.avg(doubles);
            } catch (CalException e) {
                logger.error("计算过程中发生错误！", e);
            }
            return -1d;
        }).forEach(aDouble -> {
            double classification = trainingData[index.get()][columnCount - 1];
            double distance = aDouble;
            distanceAndClassList.add(new double[] {distance , classification});
            index.getAndIncrement();
        });

        distanceAndClassList.sort((o1, o2) -> {
            if (o1[0] > o2[0]) {
                return 1;
            } else if (o1[0] == o2[0]){
                return 0;
            } else {
                return -1;
            }
        });

        Map<Double, AtomicInteger> classCountMap = Maps.newHashMap();

        distanceAndClassList.subList(0, k + 1).forEach(doubles -> {
            double classification = doubles[1];
            AtomicInteger count = classCountMap.get(classification);
            if (count != null) {
                count.incrementAndGet();
            } else {
                classCountMap.put(classification, new AtomicInteger(1));
            }
        });

        List<Object[]> classAndCountList = Lists.newArrayList();
        classCountMap.forEach((key, value) -> classAndCountList.add(new Object[]{key, value}));

        classAndCountList.sort(Comparator.comparingInt(o -> ( -((AtomicInteger) o[1]).intValue())) );

        return (double) classAndCountList.get(0)[0];
    }


    public static void main(String[] args) {
        Knn knn = new Knn();
        double[] testData = new double[]{1d, 0d, 1d};
        double[][] trainingData = new double[][]{
                {0d, 0d, 0d, 0d},
                {0d, 1d, 0d, 1d},
                {0d, 0d, 1d, 1d},
                {0d, 1d, 1d, 2d},
                {1d, 1d, 0d, 2d},
                {1d, 1d, 1d, 3d},
        };
        System.out.println(knn.classify(testData, trainingData, 4));
    }

}
