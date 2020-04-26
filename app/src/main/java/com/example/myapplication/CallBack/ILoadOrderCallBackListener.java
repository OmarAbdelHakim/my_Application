package com.example.myapplication.CallBack;

import com.example.myapplication.Model.order;

import java.util.List;

public interface ILoadOrderCallBackListener {

    void onLoadOrderSucceeded(List<order>orderList);
    void OnLoadOrderFailed(String message);
}
