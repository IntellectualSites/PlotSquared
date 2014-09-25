package com.intellectualcrafters.plot;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Citymonstret on 2014-09-25.
 */
public class PlotCollection implements Collection {

    private Plot[] collection;

    public PlotCollection(int size) {
        this.collection = new Plot[size];
    }

    @Override
    public int size() {
        return this.collection.length;
    }

    @Override
    public boolean isEmpty() {
        return this.collection.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        if(!(o instanceof  Plot)) {
            throw new IllegalArgumentException("Object not instance of Plot.class");
        }
        Plot p = (Plot) o;
        for(Plot plot : this.collection) {
            if(plot.getId().toString().equals(p.getId().toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return this.collection;
    }

    @Override
    public boolean add(Object o) {
        Plot[] pre = this.collection;
        this.collection = new Plot[pre.length + 1];
        for(int x = 0; x < pre.length; x++) {
            this.collection[x] = pre[x];
        }
        this.collection[collection.length - 1] = (Plot) o;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        Plot[] pre = this.collection;
        this.collection = new Plot[pre.length - 1];
        int x = 0;
        for(Plot plot : pre) {
            if(plot != (Plot) o) {
                this.collection[x++] = plot;
            }
        }
        return false;
    }

    @Override
    public boolean addAll(Collection c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean retainAll(Collection c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection c) {
        return false;
    }

    @Override
    public boolean containsAll(Collection c) {
        return false;
    }

    @Override
    public T[] toArray(Object[] a) {
        return new T[0];
    }

    public
}
