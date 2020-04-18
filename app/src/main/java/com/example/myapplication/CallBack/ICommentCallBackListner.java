package com.example.myapplication.CallBack;

import com.example.myapplication.Model.CommentModel;

import java.util.List;

public interface ICommentCallBackListner {

    void onCommentLoadSuccess(List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);
}
