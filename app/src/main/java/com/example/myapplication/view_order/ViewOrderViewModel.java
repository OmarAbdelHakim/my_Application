package com.example.myapplication.view_order;

import com.example.myapplication.Model.orderModel;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewOrderViewModel extends ViewModel {

  private MutableLiveData<List<orderModel>> mutableLiveDataOrderList;

    public ViewOrderViewModel() {
        mutableLiveDataOrderList = new MutableLiveData<>();
    }

    public MutableLiveData<List<orderModel>> getMutableLiveDataOrderList() {
        return mutableLiveDataOrderList;
    }

    public void setMutableLiveDataOrderList(List<orderModel> orderList) {
       mutableLiveDataOrderList.setValue(orderList);
    }
}