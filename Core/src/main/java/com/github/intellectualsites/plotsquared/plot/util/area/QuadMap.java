package com.github.intellectualsites.plotsquared.plot.util.area;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;

import java.util.HashSet;
import java.util.Set;

public class QuadMap<T> {

    public final int size;
    public final int x;
    public final int z;
    private final int newsize;
    private final int min;
    public HashSet<T> objects;
    public QuadMap<T> one;
    public QuadMap<T> two;
    public QuadMap<T> three;
    public QuadMap<T> four;
    public QuadMap<T> skip;

    public QuadMap(int size, int x, int z) {
        this.size = size;
        this.x = x;
        this.z = z;
        this.newsize = size >> 1;
        this.min = 512;
    }

    public QuadMap(int size, int x, int z, int min) {
        this.size = size;
        this.x = x;
        this.z = z;
        this.newsize = size >> 1;
        this.min = min;
    }

    public int count() {
        int size = countBelow();
        if (this.objects != null) {
            size += this.objects.size();
        }
        return size;
    }

    public Set<T> getAll() {
        HashSet<T> all = new HashSet<>();
        if (this.objects != null) {
            all.addAll(this.objects);
        }
        if (this.skip != null) {
            all.addAll(this.skip.getAll());
            return all;
        }
        if (this.one != null) {
            all.addAll(this.one.getAll());
        }
        if (this.two != null) {
            all.addAll(this.two.getAll());
        }
        if (this.three != null) {
            all.addAll(this.three.getAll());
        }
        if (this.four != null) {
            all.addAll(this.four.getAll());
        }
        return all;
    }

    public int countCurrent() {
        return this.objects == null ? 0 : this.objects.size();
    }

    public int countBelow() {
        int size = 0;
        if (this.one != null) {
            size += this.one.count();
        }
        if (this.two != null) {
            size += this.two.count();
        }
        if (this.three != null) {
            size += this.three.count();
        }
        if (this.four != null) {
            size += this.four.count();
        }
        return size;
    }

    public void add(T area) {
        if (this.size <= this.min) {
            if (this.objects == null) {
                this.objects = new HashSet<>();
            }
            this.objects.add(area);
            return;
        }
        RegionWrapper region = getRegion(area);
        if (region.minX >= this.x) {
            if (region.minZ >= this.z) {
                if (this.one == null) {
                    this.one =
                        newInstance(this.newsize, this.x + this.newsize, this.z + this.newsize,
                            this.min);
                }
                this.one.add(area);
                recalculateSkip();
                return;
            } else if (region.maxZ < this.z) {
                if (this.two == null) {
                    this.two =
                        newInstance(this.newsize, this.x + this.newsize, this.z - this.newsize,
                            this.min);
                }
                this.two.add(area);
                recalculateSkip();
                return;
            }
        } else if (region.maxX < this.x) {
            if (region.minZ >= this.z) {
                if (this.four == null) {
                    this.four =
                        newInstance(this.newsize, this.x - this.newsize, this.z + this.newsize,
                            this.min);
                }
                this.four.add(area);
                recalculateSkip();
                return;
            } else if (region.maxZ < this.z) {
                if (this.three == null) {
                    this.three =
                        newInstance(this.newsize, this.x - this.newsize, this.z - this.newsize,
                            this.min);
                }
                this.three.add(area);
                recalculateSkip();
                return;
            }
        }
        if (this.objects == null) {
            this.objects = new HashSet<>();
        }
        this.objects.add(area);
    }

    public RegionWrapper getRegion(T value) {
        return null;
    }

    public QuadMap<T> newInstance(int newsize, int x, int z, int min) {
        try {
            return new QuadMap<T>(newsize, x, z, min) {
                @Override public RegionWrapper getRegion(T value) {
                    return QuadMap.this.getRegion(value);
                }
            };
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean remove(T area) {
        if (this.objects != null) {
            if (this.objects.remove(area)) {
                return this.objects.isEmpty();
            }
        }
        if (this.skip != null) {
            if (this.skip.remove(area)) {
                this.skip = null;
            }
        } else {
            RegionWrapper region = getRegion(area);
            if (region.minX >= this.x) {
                if (region.minZ >= this.z) {
                    if (this.one != null) {
                        if (this.one.remove(area)) {
                            this.one = null;
                        }
                        return countCurrent() == 0;
                    }
                } else {
                    if (this.two != null) {
                        if (this.two.remove(area)) {
                            this.two = null;
                        }
                        return countCurrent() == 0;
                    }
                }
            } else {
                if (region.minZ >= this.z) {
                    if (this.four != null) {
                        if (this.four.remove(area)) {
                            this.four = null;
                        }
                        return countCurrent() == 0;
                    }
                } else {
                    if (this.three != null) {
                        if (this.three.remove(area)) {
                            this.three = null;
                        }
                        return countCurrent() == 0;
                    }
                }
            }
        }
        return false;
    }

    public void recalculateSkip() {
        QuadMap<T> map = null;
        for (QuadMap<T> current : new QuadMap[] {this.one, this.two, this.three, this.four}) {
            if (current != null) {
                if (map != null) {
                    this.skip = null;
                    return;
                }
                map = current;
            }
        }
        this.skip = map.skip == null ? map : map.skip;
    }

    public Set<T> get(RegionWrapper region) {
        HashSet<T> set = new HashSet<>();
        if (this.objects != null) {
            for (T obj : this.objects) {
                if (getRegion(obj).intersects(region)) {
                    set.add(obj);
                }
            }
        }
        if (this.skip != null) {
            if (this.skip.intersects(region)) {
                set.addAll(this.skip.get(region));
            }
        } else {
            if (this.one != null && this.one.intersects(region)) {
                set.addAll(this.one.get(region));
            }
            if (this.two != null && this.two.intersects(region)) {
                set.addAll(this.two.get(region));
            }
            if (this.three != null && this.three.intersects(region)) {
                set.addAll(this.three.get(region));
            }
            if (this.four != null && this.four.intersects(region)) {
                set.addAll(this.four.get(region));
            }
        }
        return set;
    }

    public boolean intersects(RegionWrapper other) {
        return (other.minX <= this.x + this.size) && (other.maxX >= this.x - this.size) && (
            other.minZ <= this.z + this.size) && (other.maxZ >= this.z - this.size);
    }

    public T get(int x, int z) {
        if (this.objects != null) {
            for (T obj : this.objects) {
                if (getRegion(obj).isIn(x, z)) {
                    return obj;
                }
            }
        }
        if (this.skip != null) {
            return this.skip.get(x, z);
        } else {
            if (x >= this.x) {
                if (z >= this.z) {
                    if (this.one != null) {
                        return this.one.get(x, z);
                    }
                } else {
                    if (this.two != null) {
                        return this.two.get(x, z);
                    }
                }
            } else {
                if (z >= this.z) {
                    if (this.four != null) {
                        return this.four.get(x, z);
                    }
                } else {
                    if (this.three != null) {
                        return this.three.get(x, z);
                    }
                }
            }
        }
        return null;
    }
}
