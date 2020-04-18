package com.example.myapplication.ui.Comments;

import android.widget.LinearLayout;

import com.example.myapplication.Model.CommentModel;
import com.example.myapplication.Model.FoodModel;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CommentViewModel extends ViewModel {
    private MutableLiveData<List<CommentModel>> mutableLiveDataFoodList;

    public CommentViewModel() {
        mutableLiveDataFoodList = new MutableLiveData<>();
    }

        public MutableLiveData<List<CommentModel>> getMutableLiveDataFoodList() {
        return mutableLiveDataFoodList;
    }

    public void setCommentList(List<CommentModel> commentList){

        mutableLiveDataFoodList.setValue(commentList);
    }

}
