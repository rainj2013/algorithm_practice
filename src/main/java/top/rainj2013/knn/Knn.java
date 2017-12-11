package top.rainj2013.knn;

import Jama.Matrix;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.rainj2013.exception.CalException;
import top.rainj2013.util.ArrayUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangyujian
 * Time: 2017/12/10 20:39
 */
public class Knn {

    private Logger logger = LoggerFactory.getLogger(Knn.class);

    /**
     * 输入测试数据、训练数据及K值，对测试数据进行分类
     * @param testData
     * @param trainingData
     * @param k
     * @return
     */
    public double classify(double[] testData, double[][] trainingData, int k) {
        //训练数据的数量
        int rowCount = trainingData.length;
        //训练数据属性个数+1，因为最后一列是该行数据的分类
        int columnCount = trainingData[0].length;
        //制造一个与训练数据一样行数的测试数据矩阵
        double[][] testArray = ArrayUtil.tile(testData, rowCount, 1);
        Matrix testMatrix = new Matrix(testArray);
        //把训练数据矩阵的分类那一列切掉，其他属性留下制造一个矩阵
        Matrix trainingMatrix = new Matrix(ArrayUtil.subArray(
                trainingData, new int[]{0, 0}, new int[]{rowCount - 1, columnCount - 2}));
        //用欧氏距离计算公式
        Matrix calMatrix = testMatrix.minus(trainingMatrix);
        calMatrix = calMatrix.arrayTimes(calMatrix);
        double[][] calArray = calMatrix.getArray();
        //将测试数据与每一行训练数据的距离，以及该行训练数据对应的分类保存到一个list里
        List<double[]> distanceAndClassList = Lists.newArrayListWithExpectedSize(rowCount);
        AtomicInteger index = new AtomicInteger();
        Arrays.stream(calArray).map(doubles -> {
            try {
                return Math.sqrt(ArrayUtil.avg(doubles));
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

        //按照距离排序
        distanceAndClassList.sort((o1, o2) -> {
            if (o1[0] > o2[0]) {
                return 1;
            } else if (o1[0] == o2[0]){
                return 0;
            } else {
                return -1;
            }
        });

        //取训练集中与测试数据距离最近的topK个数据，统计不同分类的个数
        Map<Double, Integer> classCountMap = Maps.newHashMap();
        distanceAndClassList.subList(0, k + 1).forEach(doubles -> {
            double classification = doubles[1];
            Integer count = classCountMap.get(classification);
            if (count != null) {
                classCountMap.put(classification, ++count);
            } else {
                classCountMap.put(classification, 1);
            }
        });

        //将出现次数最多的分类作为训练数据的分类
        int max = 0;
        double classification = -1d;
        for (Map.Entry<Double, Integer> entry : classCountMap.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                classification = entry.getKey();
            }

        }

        return classification;
    }


    public static void main(String[] args) {
        Knn knn = new Knn();
        double[][] trainingData = new double[][]{
                {1.0, 1.1, 1},
                {1.0, 1.0, 1},
                {0, 0, 0},
                {0, 0.1, 0}
        };

        System.out.println(knn.classify(new double[]{0.2, 0.2}, trainingData, 3));
    }

}
