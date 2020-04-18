package com.example.myapplication.CallBack;

import com.example.myapplication.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallBackListner {
    void onCategoryLoadingSuccess(List<CategoryModel> categoryModels);
    void onCategoryLoadingFailed(String message);

}
