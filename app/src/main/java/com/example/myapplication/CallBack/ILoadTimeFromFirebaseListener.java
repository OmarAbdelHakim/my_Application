package com.example.myapplication.CallBack;

import com.example.myapplication.Model.order;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(order order , long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
