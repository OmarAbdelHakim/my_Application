package com.example.myapplication.ui.home;

import com.example.myapplication.CallBack.IBestDealCallBackListner;
import com.example.myapplication.CallBack.IpopularCallBackListner;
import com.example.myapplication.Model.BestDealModel;
import com.example.myapplication.Model.PopularCategoryModel;
import com.example.myapplication.common.common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel implements IpopularCallBackListner, IBestDealCallBackListner {

    private MutableLiveData<List<PopularCategoryModel>> popularList;
    private MutableLiveData<List<BestDealModel>> bestdealList;
    private MutableLiveData<String> MessageError;

    private IpopularCallBackListner ipopularCallBackListner;
    private IBestDealCallBackListner iBestDealCallBackListner;






    public HomeViewModel() {

        ipopularCallBackListner = this;
        iBestDealCallBackListner = this;

    }

    public MutableLiveData<List<BestDealModel>> getBestdealList() {

        if(bestdealList == null)
        {
            bestdealList = new MutableLiveData<>();
            MessageError = new MutableLiveData<>();
            loadBestDealList();
        }
        return bestdealList;
    }

    private void loadBestDealList() {
        final List<BestDealModel> templist = new ArrayList<>();
        DatabaseReference bestDealRef= FirebaseDatabase.getInstance().getReference(common.BEST_DEAL_REF);

        bestDealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot itemSnapshot : dataSnapshot.getChildren())
                {
                    BestDealModel model = itemSnapshot.getValue(BestDealModel.class);
                    templist.add(model);
                }
                iBestDealCallBackListner.onBestDealLoadingSuccess(templist);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                iBestDealCallBackListner.onBestDealLoadingFailed(databaseError.getMessage());

            }
        });

        //hakml mn hena

    }

    public MutableLiveData<List<PopularCategoryModel>> getPopularList() {
      if(popularList == null)
      {
          popularList = new MutableLiveData<>();
          MessageError = new MutableLiveData<>();
          loadPopularList();
      }
      return popularList;
    }

    private void loadPopularList() {
        final List<PopularCategoryModel> templist = new ArrayList<>();
        DatabaseReference popularRef= FirebaseDatabase.getInstance().getReference(common.POPULAR_CATEGORY_REF);
        popularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot itemSnapshot : dataSnapshot.getChildren())
                {
                    PopularCategoryModel model = itemSnapshot.getValue(PopularCategoryModel.class);
                    templist.add(model);
                }
                ipopularCallBackListner.onPopularLoadingSuccess(templist);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                ipopularCallBackListner.onPopularLoadingFailed(databaseError.getMessage());

            }
        });

    }

    public MutableLiveData<String> getMessageError() {
        return MessageError;
    }

    @Override
    public void onPopularLoadingSuccess(List<PopularCategoryModel> popularCategoryModels) {

        popularList.setValue(popularCategoryModels);


    }

    @Override
    public void onPopularLoadingFailed(String message) {

        MessageError.setValue(message);

    }

    @Override
    public void onBestDealLoadingSuccess(List<BestDealModel> bestDealModels) {
        bestdealList.setValue(bestDealModels);

    }

    @Override
    public void onBestDealLoadingFailed(String message) {
        MessageError.setValue(message);

    }
}