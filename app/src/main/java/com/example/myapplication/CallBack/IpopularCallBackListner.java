package com.example.myapplication.CallBack;

import com.example.myapplication.Model.PopularCategoryModel;

import java.util.List;

public interface IpopularCallBackListner {
    void onPopularLoadingSuccess(List<PopularCategoryModel> popularCategoryModels);
    void onPopularLoadingFailed(String message);

}
