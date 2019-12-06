package com.sapuseven.noticap;

import com.sapuseven.noticap.utils.FilterRule;

import java.util.ArrayList;

public class TimeoutList {
    ArrayList<FilterRule> pendingTimeouts = new ArrayList<>();
    ArrayList<Long> curTimeoutTime = new ArrayList<>();

    public void Update(FilterRule fr){
        if(!pendingTimeouts.contains(fr)){
            pendingTimeouts.add(fr);
            curTimeoutTime.add(System.currentTimeMillis());
        }
        else{
            int index = pendingTimeouts.indexOf(fr);
            curTimeoutTime.set(index, System.currentTimeMillis());
        }
    }

    public boolean isInTimeout(FilterRule fr){
        if(!pendingTimeouts.contains(fr))
            return false;
        int index = pendingTimeouts.indexOf(fr);
        if(System.currentTimeMillis() - curTimeoutTime.get(index) >= fr.getminTimeDiff()){
            pendingTimeouts.remove(index);
            curTimeoutTime.remove(index);
            return false;
        }
        return true;
    }
}
