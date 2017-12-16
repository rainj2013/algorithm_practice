package top.rainj2013.dt;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * Created by yangyujian
 * Time: 2017/12/16 10:22
 */
public class Node<E, T> {
    //当前节点决策属性在当前训练集中的序号
    private int index;
    //父节点决策属性的值
    private E value;
    //子节点列表
    private List<Node<E, T>> nexts;
    //分类节点的标签(此属性非空的节点不是决策节点，而是代表一个分类，例如节点n的label属性为A，则节点n的父节点是决策树的叶子节点，对应的分类为A)
    private T label;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }

    public List<Node<E, T>> getNexts() {
        return nexts;
    }

    public void setNexts(List<Node<E, T>> nexts) {
        this.nexts = nexts;
    }

    public T getLabel() {
        return label;
    }

    public void setLabel(T label) {
        this.label = label;
    }


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
