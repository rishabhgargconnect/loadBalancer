package com.cloud.loadBalancer.beans;

import java.util.*;


public class VMTasksMap {

    private Map<Integer, Object> tasks = new HashMap<>();

    public synchronized void addTask(Integer key, Object task) {
        tasks.put(key, task);
    }

    //TODO Deliberately not making it synchronized -> dont want overhead, okay with approximate val
    public int getSize() {
        return tasks.size();
    }

    public void removeTaskWithKey(int key) {
        tasks.remove(key);
    }

    public boolean containsKey(int key) {
        return tasks.containsKey(key);
    }

}
