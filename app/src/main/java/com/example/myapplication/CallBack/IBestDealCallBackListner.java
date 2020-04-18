package com.example.myapplication.CallBack;

import com.example.myapplication.Model.BestDealModel;
import com.example.myapplication.Model.PopularCategoryModel;

import java.util.List;

public interface IBestDealCallBackListner {

    void onBestDealLoadingSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadingFailed(String message);
}
