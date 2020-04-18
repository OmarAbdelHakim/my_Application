package com.example.myapplication.ui.Comments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.Adapter.MyCommentAdapter;
import com.example.myapplication.CallBack.ICommentCallBackListner;
import com.example.myapplication.Model.CommentModel;
import com.example.myapplication.R;
import com.example.myapplication.common.common;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class CommentFragment extends BottomSheetDialogFragment implements ICommentCallBackListner {

    private CommentViewModel commentViewModel;
    private Unbinder unbinder;

    @BindView(R.id.recycler_comment)
    RecyclerView recycler_comment;

    AlertDialog dialog;
    ICommentCallBackListner listner;

    public CommentFragment() {
        listner =this;
    }

    private static CommentFragment instance;

    public static CommentFragment getInstance() {

        if(instance == null)
            instance  =new CommentFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View itemView = LayoutInflater.from(getContext())
                .inflate(R.layout.botton_sheet_comment_fragment , container ,false);

        unbinder = ButterKnife.bind(this , itemView);

        initView();
        loadCommentFromFirebase();

        commentViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), new Observer<List<CommentModel>>() {
            @Override
            public void onChanged(List<CommentModel> commentModelList) {

                MyCommentAdapter adapter = new MyCommentAdapter(getContext() , commentModelList);
                recycler_comment.setAdapter(adapter);

            }
        });
        return itemView;
    }

    private void loadCommentFromFirebase() {

        dialog.show();
        List<CommentModel>commentModels = new ArrayList<>();
        FirebaseDatabase .getInstance().getReference(common.COMMENT_REF)
                .child(common.selectedFood.getId())
                .orderByChild("commentTimeStamp")
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot commentSnapshot : dataSnapshot.getChildren()){

                            CommentModel commentModel = commentSnapshot.getValue(CommentModel.class);
                            commentModels.add(commentModel);


                        }
                        listner.onCommentLoadSuccess(commentModels);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        listner.onCommentLoadFailed(databaseError.getMessage());

                    }
                });


    }

    private void initView() {
        commentViewModel = ViewModelProviders.of(this).get(CommentViewModel.class);
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();


        recycler_comment.setHasFixedSize(true);
        LinearLayoutManager LayoutManager = new LinearLayoutManager(getContext() , RecyclerView.VERTICAL , true);
        recycler_comment.setLayoutManager(LayoutManager);
        recycler_comment.addItemDecoration(new DividerItemDecoration(getContext() ,LayoutManager.getOrientation()));


    }

    @Override
    public void onCommentLoadSuccess(List<CommentModel> commentModels) {

        dialog.dismiss();

        commentViewModel.setCommentList(commentModels);

    }

    @Override
    public void onCommentLoadFailed(String message) {

        dialog.dismiss();

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

    }
}
