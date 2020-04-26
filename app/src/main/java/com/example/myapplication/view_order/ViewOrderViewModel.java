package com.example.myapplication.view_order;

import com.example.myapplication.Model.FoodModel;
import com.example.myapplication.Model.order;
import com.example.myapplication.common.common;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewOrderViewModel extends ViewModel {

  private MutableLiveData<List<order>> mutableLiveDataOrderList;

    public ViewOrderViewModel() {
        mutableLiveDataOrderList = new MutableLiveData<>();
    }

    public MutableLiveData<List<order>> getMutableLiveDataOrderList() {
        return mutableLiveDataOrderList;
    }

    public void setMutableLiveDataOrderList(List<order> orderList) {
       mutableLiveDataOrderList.setValue(orderList);
    }
}