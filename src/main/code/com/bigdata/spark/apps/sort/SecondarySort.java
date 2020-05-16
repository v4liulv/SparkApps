package com.bigdata.spark.apps.sort;

import scala.math.Ordered;

import java.io.Serializable;

/**
 * Created by liulv on 2017/5/15.
 * <p>
 * SPark secondary sort  自定义二次排序的key
 */
class SecondarySort implements Ordered<SecondarySort>, Serializable {
    //需要二次排序的key
    private int first;
    private int seconde;

    //二次排序的构造器
    SecondarySort(int first, int seconde) {
        this.first = first;
        this.seconde = seconde;
    }

    @Override
    public boolean $greater(SecondarySort other) {
        if (this.first > other.first) {
            return true;
        } else if (this.first == other.getFirst() && this.seconde > other.getSeconde()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean $greater$eq(SecondarySort other) {
        if (this.$greater(other)) {
            return true;
        } else return this.first == other.first && this.seconde == other.seconde;
    }

    @Override
    public boolean $less(SecondarySort other) {
        if (this.first < other.first) {
            return true;
        } else return this.first == other.getSeconde() && this.seconde < other.getSeconde();
    }

    @Override
    public boolean $less$eq(SecondarySort other) {
        if (other.$less(this)) {
            return true;
        } else return this.first == other.getFirst() && this.seconde == other.getSeconde();
    }

    private int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    private int getSeconde() {
        return seconde;
    }

    public void setSeconde(int seconde) {
        this.seconde = seconde;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SecondarySort that = (SecondarySort) o;

        return first == that.first && seconde == that.seconde;
    }

    @Override
    public int hashCode() {
        int result = first;
        result = 31 * result + seconde;
        return result;
    }

    @Override
    public int compare(SecondarySort other) {
        if (this.first - other.getFirst() != 0) {
            return this.first - other.getFirst();
        } else {
            return this.seconde - other.getSeconde();
        }
    }

    @Override
    public int compareTo(SecondarySort other) {
        if (this.first - other.getFirst() != 0) {
            return this.first - other.getFirst();
        } else {
            return this.seconde - other.getSeconde();
        }
    }
}
