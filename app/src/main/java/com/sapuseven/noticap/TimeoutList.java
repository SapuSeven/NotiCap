package com.sapuseven.noticap;

import android.util.Log;

import com.sapuseven.noticap.utils.FilterRule;

import java.util.ArrayList;

public class TimeoutList {
    ArrayList<FilterRule> pendingDelays = new ArrayList<>();
    ArrayList<Long> curDelayTime = new ArrayList<>();

    public void Update(FilterRule fr){
        if(!pendingDelays.contains(fr)){
            pendingDelays.add(fr);
            curDelayTime.add(System.currentTimeMillis());
        }
        else{
            int index = pendingDelays.indexOf(fr);
            curDelayTime.set(index, System.currentTimeMillis());
        }
    }

    public boolean isInTimeout(FilterRule fr){
        if(!pendingDelays.contains(fr))
            return false;
        int index = pendingDelays.indexOf(fr);
        Log.i("NotificationListener", Long.toString(System.currentTimeMillis() - curDelayTime.get(index)));
        if(System.currentTimeMillis() - curDelayTime.get(index) >= fr.getMinNotiDelay()){
            pendingDelays.remove(index);
            curDelayTime.remove(index);
            return false;
        }
        return true;
    }
}
