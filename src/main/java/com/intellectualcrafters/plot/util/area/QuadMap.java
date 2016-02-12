package com.intellectualcrafters.plot.util.area;

import java.util.HashSet;
import java.util.Set;

import com.intellectualcrafters.plot.object.RegionWrapper;

public class QuadMap<T> {
    public final int size;
    public final int x;
    public final int z;

    public HashSet<T> objects;

    public QuadMap<T> one;
    public QuadMap<T> two;
    public QuadMap<T> three;
    public QuadMap<T> four;
    
    public QuadMap<T> skip;
    
    private final int newsize;
    
    private final int min;

    public QuadMap(int size, int x, int z) {
        this.size = size;
        this.x = x;
        this.z = z;
        this.newsize = size >> 1;
        min = 512;
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
        if (objects != null) {
            size += objects.size();
        }
        return size;
    }
    
    public Set<T> getAll() {
        HashSet<T> all = new HashSet<>();
        if (objects != null) {
            all.addAll(objects);
        }
        if (skip != null) {
            all.addAll(skip.getAll());
            return all;
        }
        if (one != null) {
            all.addAll(one.getAll());
        }
        if (two != null) {
            all.addAll(two.getAll());
        }
        if (three != null) {
            all.addAll(three.getAll());
        }
        if (four != null) {
            all.addAll(four.getAll());
        }
        return all;
    }

    public int countCurrent() {
        return objects == null ? 0 : objects.size();
    }

    public int countBelow() {
        int size = 0;
        if (one != null) {
            size += one.count();
        }
        if (two != null) {
            size += two.count();
        }
        if (three != null) {
            size += three.count();
        }
        if (four != null) {
            size += four.count();
        }
        return size;
    }

    public void add(T area) {
        if (size <= min) {
            if (this.objects == null) {
                objects = new HashSet<>();
            }
            this.objects.add(area);
            return;
        }
        RegionWrapper region = getRegion(area);
        if (region.minX >= x) {
            if (region.minZ >= z) {
                if (one == null) {
                    one = newInstance(newsize, x + newsize, z + newsize, min);
                }
                one.add(area);
                recalculateSkip();
                return;
            } else if (region.maxZ < z) {
                if (two == null) {
                    two = newInstance(newsize, x + newsize, z - newsize, min);
                }
                two.add(area);
                recalculateSkip();
                return;
            }
        } else if (region.maxX < x) {
            if (region.minZ >= z) {
                if (four == null) {
                    four = newInstance(newsize, x - newsize, z + newsize, min);
                }
                four.add(area);
                recalculateSkip();
                return;
            } else if (region.maxZ < z) {
                if (three == null) {
                    three = newInstance(newsize, x - newsize, z - newsize, min);
                }
                three.add(area);
                recalculateSkip();
                return;
            }
        }
        if (this.objects == null) {
            objects = new HashSet<>();
        }
        this.objects.add(area);
    }
    
    public RegionWrapper getRegion(T value) {
        return null;
    }
    
    public QuadMap<T> newInstance(int newsize, int x, int z, int min) {
        try {
            return new QuadMap<T>(newsize, x, z, min) {
                @Override
                public RegionWrapper getRegion(T value) {
                    return QuadMap.this.getRegion(value);
                }
            };
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean remove(T area) {
        if (objects != null) {
            if (objects.remove(area)) {
                return objects.size() == 0;
            }
        }
        if (skip != null) {
            if (skip.remove(area)) {
                skip = null;
            }
        } else {
            RegionWrapper region = getRegion(area);
            if (region.minX >= this.x) {
                if (region.minZ >= this.z) {
                    if (one != null) {
                        if (one.remove(area)) {
                            one = null;
                        }
                        return countCurrent() == 0;
                    }
                } else {
                    if (two != null) {
                        if (two.remove(area)) {
                            two = null;
                        }
                        return countCurrent() == 0;
                    }
                }
            } else {
                if (region.minZ >= this.z) {
                    if (four != null) {
                        if (four.remove(area)) {
                            four = null;
                        }
                        return countCurrent() == 0;
                    }
                } else {
                    if (three != null) {
                        if (three.remove(area)) {
                            three = null;
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
        for (QuadMap<T> current : new QuadMap[] { one, two, three, four }) {
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
        if (objects != null) {
            for (T obj : objects) {
                if (getRegion(obj).intersects(region)) {
                    set.add(obj);
                }
            }
        }
        if (skip != null) {
            if (skip.intersects(region)) {
                set.addAll(skip.get(region));
            }
        } else {
            if (one != null && one.intersects(region)) {
                set.addAll(one.get(region));
            }
            if (two != null && two.intersects(region)) {
                set.addAll(two.get(region));
            }
            if (three != null && three.intersects(region)) {
                set.addAll(three.get(region));
            }
            if (four != null && four.intersects(region)) {
                set.addAll(four.get(region));
            }
        }
        return set;
    }
    
    public boolean intersects(RegionWrapper other) {
        return (other.minX <= this.x + size) && (other.maxX >= this.x - size) && (other.minZ <= this.z + size) && (other.maxZ >= this.z - size);
    }

    public T get(int x, int z) {
        if (objects != null) {
            for (T obj : objects) {
                if (getRegion(obj).isIn(x, z)) {
                    return obj;
                }
            }
        }
        if (skip != null) {
            return skip.get(x, z);
        } else {
            if (x >= this.x) {
                if (z >= this.z) {
                    if (one != null) {
                        return one.get(x, z);
                    }
                } else {
                    if (two != null) {
                        return two.get(x, z);
                    }
                }
            } else {
                if (z >= this.z) {
                    if (four != null) {
                        return four.get(x, z);
                    }
                } else {
                    if (three != null) {
                        return three.get(x, z);
                    }
                }
            }
        }
        return null;
    }
}
