package com.example.myapplication.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.asksira.loopingviewpager.LoopingViewPager;
import com.example.myapplication.Adapter.MyBestAdapter;
import com.example.myapplication.Adapter.MyPopularCategoriesAdapter;
import com.example.myapplication.Model.BestDealModel;
import com.example.myapplication.Model.PopularCategoryModel;
import com.example.myapplication.R;

import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    Unbinder unbinder;
    @BindView(R.id.Recycler_Popular)
    RecyclerView Recycler_Popular;

    @BindView(R.id.viewpager)
    LoopingViewPager viewpager;

    LayoutAnimationController layoutAnimationController;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this , root);

        init();

        //MyPopularCategoriesAdapter
        homeViewModel.getPopularList().observe(getViewLifecycleOwner() , new Observer<List<PopularCategoryModel>>() {
            @Override
            public void onChanged(List<PopularCategoryModel> PopularCategoryModel) {

                MyPopularCategoriesAdapter adapter = new MyPopularCategoriesAdapter(getContext() , PopularCategoryModel);
                Recycler_Popular.setAdapter(adapter);

                Recycler_Popular.setLayoutAnimation(layoutAnimationController);


            }
        });

        //Best Deal

        homeViewModel.getBestdealList().observe(getViewLifecycleOwner(), new Observer<List<BestDealModel>>() {
            @Override
            public void onChanged(List<BestDealModel> bestDealModels) {
                MyBestAdapter adapter = new MyBestAdapter(getContext() , bestDealModels , true);

                viewpager.setAdapter(adapter);

            }
        });

        return root;
    }

    private void init() {

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_items_from_left);
        Recycler_Popular.setHasFixedSize(true);
        Recycler_Popular.setLayoutManager(new LinearLayoutManager(getContext() , RecyclerView.HORIZONTAL ,false));

    }

    @Override
    public void onResume() {
        super.onResume();
        viewpager.resumeAutoScroll();
    }

    @Override
    public void onPause() {

        viewpager.pauseAutoScroll();
        super.onPause();

    }
}
