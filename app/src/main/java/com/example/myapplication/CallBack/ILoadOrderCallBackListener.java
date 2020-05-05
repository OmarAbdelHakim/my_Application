package com.example.myapplication.CallBack;

import com.example.myapplication.Model.orderModel;

import java.util.List;

public interface ILoadOrderCallBackListener {

    void onLoadOrderSucceeded(List<orderModel>orderList);
    void OnLoadOrderFailed(String message);
}
