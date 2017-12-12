package top.rainj2013.knn;

import Jama.Matrix;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import top.rainj2013.exception.CalException;
import top.rainj2013.util.ArrayUtil;
import top.rainj2013.util.IterableUtil;
import top.rainj2013.util.MathUtil;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangyujian
 * Time: 2017/12/10 20:39
 */
public class Knn {

    private static final Logger logger = LoggerFactory.getLogger(Knn.class);

    /**
     * 输入测试数据、训练数据及K值，对测试数据进行分类
     * @param testData 测试数据
     * @param trainingData 训练数据集
     * @param k K值
     * @return 测试数据的分类
     */
    public <T> T classify(double[] testData, double[][] trainingData, T[] labels, int k) {
        //训练数据的数量
        int rowCount = trainingData.length;

        //复制单行的测试数据n份，制造一个与训练数据一样行数的测试数据矩阵，其中n=训练集数据量
        double[][] testArray = ArrayUtil.tile(testData, rowCount, 1);
        Matrix testMatrix = new Matrix(testArray);

        //制造一个训练集数据矩阵
        Matrix trainingMatrix = new Matrix(trainingData);

        //用欧氏距离计算公式
        Matrix calMatrix = testMatrix.minus(trainingMatrix);
        calMatrix = calMatrix.arrayTimes(calMatrix);
        double[][] calArray = calMatrix.getArray();

        //将测试数据与每一行训练数据的距离，以及该行训练数据对应的分类保存到一个list里
        List<Pair<Double, T>> distanceAndClassList = Lists.newArrayListWithExpectedSize(rowCount);
        AtomicInteger index = new AtomicInteger();
        Arrays.stream(calArray).map(doubles -> {
            try {
                return Math.sqrt(ArrayUtil.avg(doubles));
            } catch (CalException e) {
                logger.error("计算过程中发生错误！", e);
            }
            return Double.MAX_VALUE;
        }).forEach(aDouble -> {
            T classification = labels[index.get()];
            double distance = aDouble;
            distanceAndClassList.add(new Pair<>(distance, classification));
            index.getAndIncrement();
        });

        //按照距离排序
        distanceAndClassList.sort((o1, o2) -> {
            if (o1.getKey() > o2.getKey()) {
                return 1;
            } else if (o1.getKey().equals(o2.getKey())){
                return 0;
            } else {
                return -1;
            }
        });

        //取训练集中与测试数据距离最近的topK个数据，统计不同分类的个数
        Map<T, Integer> classCountMap = Maps.newHashMap();
        distanceAndClassList.subList(0, k).forEach(pair -> {
            T classification = pair.getValue();
            Integer count = classCountMap.get(classification);
            if (count != null) {
                classCountMap.put(classification, ++count);
            } else {
                classCountMap.put(classification, 1);
            }
        });

        //将出现次数最多的分类作为训练数据的分类
        int max = 0;
        T classification = null;
        for (Map.Entry<T, Integer> entry : classCountMap.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                classification = entry.getKey();
            }

        }

        return classification;
    }


    public static void main(String[] args) {
        Knn knn = new Knn();
        String dataFile = "classpath:data/iris/bezdekIris.data";
        List<double[]> data = Lists.newArrayList();
        List<String> labelList = Lists.newArrayList();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(ResourceUtils.getFile(dataFile))))) {

            reader.lines().forEach(s -> {
                if (Strings.isNullOrEmpty(s)) {
                    return;
                }
                String[] tmp = s.split(",");
                double[] line = new double[tmp.length - 1];

                IterableUtil.forEach(tmp, (index, field) -> {
                    if (index == tmp.length - 1) {
                        labelList.add(field);
                    } else {
                        line[index] = Double.valueOf(field);
                    }
                });
                data.add(line);
            });

        } catch (FileNotFoundException e) {
            logger.error("file {} not found", dataFile);
        } catch (IOException e) {
            logger.error("read data from file:{} error", dataFile);
        }

        List<double[]> normData;
        try {
            normData = MathUtil.norm(data);
        } catch (CalException e) {
            logger.error("normalization array fail", e);
            return;
        }

        List<double[]> trainingDataList = Lists.newArrayList();
        List<String> trainingDataLabelList = Lists.newArrayList();
        List<double[]> testDataList = Lists.newArrayList();
        List<String> testDataLabelList = Lists.newArrayList();

        IterableUtil.forEach(normData, (index, doubles) -> {
            if (index % 5 == 0) {
                testDataList.add(doubles);
                testDataLabelList.add(labelList.get(index));
            } else {
                trainingDataList.add(doubles);
                trainingDataLabelList.add(labelList.get(index));
            }
        });

        double[][] trainingData = new double[trainingDataList.size()][];

        IterableUtil.forEach(trainingDataList, (index, doubles) -> trainingData[index] = doubles);

        String[] labels = new String[trainingDataLabelList.size()];
        IterableUtil.forEach(trainingDataLabelList, (index, label) -> labels[index] = label);

        int k = 3;

        AtomicInteger trueCount = new AtomicInteger();
        IterableUtil.forEach(testDataList, (index, testData) -> {
            String classification = knn.classify(testData, trainingData, labels, k);
            String label = testDataLabelList.get(index);
            logger.info("预测分类为: {}，实际分类为: {}", classification, label);
            if (classification.equals(label)) {
                trueCount.incrementAndGet();
            }
        });
        logger.info("预测准确率为: {}", trueCount.get() / (double)testDataList.size());
    }

}
