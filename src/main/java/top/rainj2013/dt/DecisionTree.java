package top.rainj2013.dt;

import com.google.common.collect.Lists;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.rainj2013.util.ArrayUtil;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionTree.class);

    /**
     * 输入训练数据和对应的标签，构造决策树
     * @param trainingData 训练数据集
     * @param labels 标签列表
     * @param <E> 训练集数据属性的数据类型
     * @param <T> 标签的数据类型
     * @return 决策树的根节点
     */
    public <E,T> Node<E, T> buildDecisionTree(E[][] trainingData, List<T> labels) {
        Node<E, T> node =  getBestNode(trainingData, labels, null);
        LOGGER.info("build decision-tree success : {}", node);
        return node;
    }

    private <E,T> Node<E, T> getBestNode(E[][] trainingData, List<T> labels, E value){

        int rowCount = trainingData.length;
        int columnCount = trainingData[0].length;

        Map<T, List<Integer>> map = IterableUtil.getElementPositions(labels);
        //剩下的数据都是同一类时，即分类已经完成
        if (map.size() == 1) {
            Node<E,T> node = new Node<>();
            node.setLabel(labels.get(0));
            node.setValue(value);
            return node;
        }
        //没有更多属性支持分类，且数据标签不止一个时，取出现次数最多的标签作为分类标签
        if (columnCount == 0) {
            int maxLabelCount = 0;
            T label = null;
            for (Map.Entry<T, List<Integer>> entry : map.entrySet()) {
                if (entry.getValue().size() > maxLabelCount) {
                    maxLabelCount = entry.getValue().size();
                    label = entry.getKey();
                }
            }
            Node<E, T> node = new Node<>();
            node.setLabel(label);
            node.setValue(value);
            return node;
        }

        //迭代数据集中的属性，计算它们的信息熵
        List<Double> infos = Lists.newArrayListWithExpectedSize(columnCount);
        for (int i = 0; i < columnCount; i++) {

            E[] colData = ArrayUtil.getCol(trainingData, i);
            List<E> colDataList = Lists.newArrayListWithExpectedSize(colData.length);
            colDataList.addAll(Arrays.asList(colData));
            //当前属性下的所有<属性取值, 分类>
            List<Pair<E, T>> fieldAndLabelList = Lists.newArrayListWithExpectedSize(colData.length);
            IterableUtil.forEach(colData, (index, field) ->
                    fieldAndLabelList.add(Pair.create(field, labels.get(index))));
            Map<Pair<E, T>, List<Integer>> pairMap = IterableUtil.getElementPositions(fieldAndLabelList);
            //<属性取值, 该取值对应的行号列表>
            Map<E, List<Integer>> classMap = IterableUtil.getElementPositions(colDataList);
            AtomicReference<Double> info = new AtomicReference<>(0d);
            classMap.forEach((aDouble, integers) -> {
                //当前属性取值的数据行数在总数据中的占比
                double p = integers.size() / (double)rowCount;
                //当前属性取值下不同分类标签的数量
                List<Integer> fieldClassList = Lists.newArrayList();
                pairMap.forEach((pair, list) -> {
                    if (pair.getKey().equals(aDouble)) {
                        fieldClassList.add(list.size());
                    }
                });

                int[] values = new int[fieldClassList.size()];
                IterableUtil.forEach(fieldClassList, (index, aInt) -> values[index] = aInt);
                //计算当前属性的信息熵
                info.updateAndGet(v -> (v + p * aInfo(values)));
            });
            infos.add(info.get());
        }

        //获取最小的信息熵对应的属性下标，使用这个属性构造一个决策树节点
        int indexInArray = getMinIndex(infos);
        Node<E, T> root = new Node<>();
        root.setIndex(indexInArray);
        root.setValue(value);

        //根据分类属性的几个取值，可以分为几个子节点，构造对应的几个训练集，递归调用寻找最佳分类节点
        E[][] newTrainingData = ArrayUtil.removeCol(trainingData, indexInArray);
        E[] colData = ArrayUtil.getCol(trainingData, indexInArray);
        List<E> colDataList = Lists.newArrayList();
        colDataList.addAll(Arrays.asList(colData));
        Map<E, List<Integer>> fieldValueMap = IterableUtil.getElementPositions(colDataList);

        List<Node<E, T>> nexts = Lists.newArrayList();
        fieldValueMap.forEach((fieldValue, list) -> {
            List<T> newLabels = Lists.newArrayListWithExpectedSize(list.size());
            Object[][] nextInput = new Object[list.size()][];
            int index = 0;
            for (int labelIndex : list) {
                newLabels.add(labels.get(labelIndex));
                nextInput[index++] = newTrainingData[labelIndex];
            }
            nexts.add(getBestNode((E[][]) nextInput, newLabels, fieldValue));
        });

        root.setNexts(nexts);
        return root;
    }

    /**
     * 计算信息熵
     * @param values 属性可能的取值列表
     * @return 属性的信息熵
     */
    private double aInfo(int ...values) {
        long total = Arrays.stream(values).sum();
        double result = 0;
        for (int value : values) {
            double p = value / (double)total;
            result -= p * MathUtil.log2(p);
        }
        return result;
    }

    /**
     * 从一堆信息熵中找到最小的信息熵，返回它对应的属性的序号
     * @param infos 信息熵列表
     * @return 对应属性的序号
     */
    private int getMinIndex(List<Double> infos) {
        int index = 0;
        double min = infos.get(0);
        for (int i = 0; i < infos.size(); i++) {
            double info = infos.get(i);
            if (info < min) {
                min = info;
                index = i;
            }
        }
        return index;
    }

    /**
     * 输入测试数据，预测它的标签
     * @param root 决策树的根节点
     * @param testData 测试数据
     * @param <E> 属性的数据类型
     * @param <T> 标签的数据类型
     * @return 测试数据对应的标签
     */
    public <E, T> T predict(Node<E, T> root, E[] testData) {
        while (root.getLabel() == null) {
            E field = testData[root.getIndex()];
            for (Node<E, T> nextNode: root.getNexts()) {
                if (nextNode.getValue().equals(field)){
                    testData = ArrayUtil.removeCol(testData, root.getIndex());
                    root = nextNode;
                }
            }
        }
        return root.getLabel();
    }

    public static void main(String[] args) {
        DecisionTree dt = new DecisionTree();
        String[][] trainingData = new String[][]{
                {"sunny",   "hot",    "high",     "false"},
                {"sunny",   "hot",    "high",     "true"},
                {"overcast","hot",    "high",     "false"},
                {"rain",    "mid",    "high",     "false"},
                {"rain",    "cold",   "normal",   "false"},
                {"rain",    "cold",   "normal",   "true"},
                {"overcast","cold",   "normal",   "true"},
                {"sunny",   "mid",    "high",     "false"},
                {"sunny",   "cold",   "normal",   "false"},
                {"rain",    "mid",    "normal",   "false"},
                {"sunny",   "mid",    "normal",   "true"},
                {"overcast","mid",    "high",     "true"},
                {"overcast","hot",    "normal",   "false"},
                {"rain",    "mid",    "high",     "true"}};
        List<String> labels = Lists.newArrayList("no","no","yes","yes","yes","no","yes","no","yes","yes","yes","yes","yes","no");
        Node<String, String> tree = dt.buildDecisionTree(trainingData, labels);
        String[] testData = {"rain", "mid", "normal", "false"};

        LOGGER.info("test data: {}, predict label: {}", Arrays.toString(testData), dt.predict(tree, testData));
    }

}
