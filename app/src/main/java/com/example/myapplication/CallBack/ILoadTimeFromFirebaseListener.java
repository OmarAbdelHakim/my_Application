package com.example.myapplication.CallBack;

import com.example.myapplication.Model.orderModel;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(orderModel order , long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
