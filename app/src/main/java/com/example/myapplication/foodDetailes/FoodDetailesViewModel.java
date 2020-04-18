package com.example.myapplication.foodDetailes;

import com.example.myapplication.Model.CommentModel;
import com.example.myapplication.Model.FoodModel;
import com.example.myapplication.common.common;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FoodDetailesViewModel extends ViewModel {

    private MutableLiveData<FoodModel>mutableLiveDataFood;

    private MutableLiveData<CommentModel> mutableLiveDataComment;

    public void setCommentModel(CommentModel commentModel)
    {
        if(mutableLiveDataComment != null)

            mutableLiveDataComment.setValue(commentModel);

    }

    public MutableLiveData<CommentModel> getMutableLiveDataComment() {
        return mutableLiveDataComment;
    }

    public FoodDetailesViewModel() {

        mutableLiveDataComment = new MutableLiveData<>();

    }
    public MutableLiveData<FoodModel> getMutableLiveDataFood() {

        if(mutableLiveDataFood == null)
            mutableLiveDataFood = new MutableLiveData<>();
        mutableLiveDataFood.setValue(common.selectedFood);
        return mutableLiveDataFood;
    }


    public void setFoodModel(FoodModel foodModel) {

        if(mutableLiveDataFood != null)

        mutableLiveDataFood.setValue(foodModel);
    }
}