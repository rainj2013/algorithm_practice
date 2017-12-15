package top.rainj2013.dt;

import Jama.Matrix;
import com.google.common.collect.Lists;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.rainj2013.util.IterableUtil;
import top.rainj2013.util.MathUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 决策树
 * Created by yangyujian
 * Time: 2017/12/10 20:39
 */
public class DecisionTree {

    private Logger logger = LoggerFactory.getLogger(DecisionTree.class);

    public <T> void train(Double[][] trainingData, List<T> labels) {
        //TODO

    }

    private <T> int getBestNode(double[][] trainingData, List<T> labels){
        int rowCount = trainingData.length;
        int columnCount = trainingData[0].length;

        Matrix matrix = new Matrix(trainingData);

        List<Double> infos = Lists.newArrayListWithExpectedSize(columnCount);
        for (int i = 0; i < columnCount; i++) {
            double[] colData = matrix.getColumnVector(i);
            List<Double> colDataList = Lists.newArrayListWithExpectedSize(colData.length);
            Arrays.stream(colData).forEach(colDataList::add);

            List<Pair<Double, T>> fieldAndLabelList = Lists.newArrayListWithExpectedSize(colData.length);
            IterableUtil.forEach(colDataList, (index, field) ->
                    fieldAndLabelList.add(Pair.create(field, labels.get(index))));

            Map<Pair<Double, T>, List<Integer>> pairMap = IterableUtil.getElementPositions(fieldAndLabelList);

            Map<Double, List<Integer>> classMap = IterableUtil.getElementPositions(colDataList);
            AtomicReference<Double> info = new AtomicReference<>(0d);
            classMap.forEach((aDouble, integers) -> {
                double p = integers.size() / (double)rowCount;

                List<Integer> fieldClassList = Lists.newArrayList();
                pairMap.forEach((pair, list) -> {
                    if (pair.getKey().equals(aDouble)) {
                        fieldClassList.add(list.size());
                    }
                });

                int[] values = new int[fieldClassList.size()];
                IterableUtil.forEach(fieldClassList, (index, aInt) -> values[index] = aInt);

                info.updateAndGet(v -> (v + p * aInfo(values)));
            });
            infos.add(info.get());
        }

        return getMinIndex(infos);
    }

    private double aInfo(int ...values) {
        long total = Arrays.stream(values).sum();
        double result = 0;
        for (int value : values) {
            double p = value / (double)total;
            result -= p * MathUtil.log2(p);
        }
        return result;
    }

    private int getMinIndex(List<Double> infos) {
        int index = 0;
        double min = 0;
        for (int i = 0; i < infos.size(); i++) {
            double info = infos.get(i);
            if (info < min) {
                min = info;
                index = i;
            }
        }
        return index;
    }

    public static void main(String[] args) {
        DecisionTree dt = new DecisionTree();
        System.out.println(dt.getBestNode(new double[][]{
                {1, 1, 1, 0},
                {1, 1, 1, 1},
                {2, 1, 1, 0},
                {3, 2, 1, 0},
                {3, 3, 2, 0},
                {3, 3, 2, 1},
                {2, 3, 2, 1},
                {1, 2, 1, 0},
                {1, 3, 2, 0},
                {3, 2, 2, 0},
                {1, 2, 2, 1},
                {2, 2, 1, 1},
                {2, 1, 2, 0},
                {3, 2, 1, 1}}, Lists.newArrayList(0,0,1,1,1,0,1,0,1,1,1,1,1,0)));


    }

}
