package top.rainj2013.knn;

import Jama.Matrix;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import top.rainj2013.exception.CalException;
import top.rainj2013.util.ArrayUtil;
import top.rainj2013.util.MatrixUtil;

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
     * @param testData
     * @param trainingData
     * @param k
     * @return
     */
    public <T> T classify(double[] testData, double[][] trainingData, T[] labels, int k) {
        //训练数据的数量
        int rowCount = trainingData.length;
        //训练数据属性个数+1，因为最后一列是该行数据的分类
        int columnCount = trainingData[0].length;
        //制造一个与训练数据一样行数的测试数据矩阵
        double[][] testArray = ArrayUtil.tile(testData, rowCount, 1);
        Matrix testMatrix = new Matrix(testArray);
        //制造一个训练集数据矩阵
        Matrix trainingMatrix = new Matrix(trainingData);
        //用欧氏距离计算公式
        Matrix calMatrix = testMatrix.minus(trainingMatrix);
        calMatrix = calMatrix.arrayTimes(calMatrix);
        double[][] calArray = calMatrix.getArray();
        //将测试数据与每一行训练数据的距离，以及该行训练数据对应的分类保存到一个list里
        List<Object[]> distanceAndClassList = Lists.newArrayListWithExpectedSize(rowCount);
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
            distanceAndClassList.add(new Object[] {distance , classification});
            index.getAndIncrement();
        });

        //按照距离排序
        distanceAndClassList.sort((o1, o2) -> {
            if ((double)o1[0] > (double)o2[0]) {
                return 1;
            } else if (o1[0] == o2[0]){
                return 0;
            } else {
                return -1;
            }
        });

        //取训练集中与测试数据距离最近的topK个数据，统计不同分类的个数
        Map<T, Integer> classCountMap = Maps.newHashMap();
        distanceAndClassList.subList(0, k).forEach(objects -> {
            T classification = (T)objects[1];
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
                AtomicInteger index = new AtomicInteger();
                Arrays.stream(tmp).forEach(field -> {
                    if (index.get() == tmp.length - 1) {
                        labelList.add(field);
                    } else {
                        line[index.get()] = Double.valueOf(field);
                    }
                    index.getAndIncrement();
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
            normData = MatrixUtil.norm(data);
        } catch (CalException e) {
            logger.error("normalization array fail", e);
            return;
        }

        List<double[]> trainingDataList = Lists.newArrayList();
        List<String> trainingDataLabelList = Lists.newArrayList();
        List<double[]> testDataList = Lists.newArrayList();
        List<String> testDataLabelList = Lists.newArrayList();
        AtomicInteger index = new AtomicInteger();
        normData.forEach(doubles -> {
            if (index.get() % 5 == 0) {
                testDataList.add(doubles);
                testDataLabelList.add(labelList.get(index.get()));
            } else {
                trainingDataList.add(doubles);
                trainingDataLabelList.add(labelList.get(index.get()));
            }
            index.getAndIncrement();
        });

        double[][] trainingData = new double[trainingDataList.size()][];
        index.set(0);
        trainingDataList.forEach( doubles -> {
            trainingData[index.get()] = doubles;
            index.incrementAndGet();
        });



        String[] labels = new String[trainingDataLabelList.size()];
        index.set(0);
        trainingDataLabelList.forEach(label -> {
            labels[index.get()] = label;
            index.incrementAndGet();
        });

        int k = 3;

        index.set(0);
        AtomicInteger trueCount = new AtomicInteger();
        testDataList.forEach(testData -> {
            String classification = knn.classify(testData, trainingData, labels, k);
            String label = testDataLabelList.get(index.get());
            logger.info("预测分类为: {}，实际分类为: {}", classification, label);
            if (classification.equals(label)) {
                trueCount.incrementAndGet();
            }
            index.incrementAndGet();
        });
        logger.info("预测准确率为: {}", trueCount.get() / (double)testDataList.size());
    }

}
