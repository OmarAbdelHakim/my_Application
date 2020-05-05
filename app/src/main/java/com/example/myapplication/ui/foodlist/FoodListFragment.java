package com.example.myapplication.ui.foodlist;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.example.myapplication.Adapter.MyFoodListAdapter;
import com.example.myapplication.EventBus.MenuItemBack;
import com.example.myapplication.Model.CategoryModel;
import com.example.myapplication.Model.FoodModel;
import com.example.myapplication.R;
import com.example.myapplication.common.common;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class FoodListFragment extends Fragment {

    private FoodListViewModel foodListViewModel;

    Unbinder unbinder;

    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;

    LayoutAnimationController layoutAnimationController;

    MyFoodListAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                ViewModelProviders.of(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_foodlist, container, false);
        unbinder = ButterKnife.bind(this , root);
        initView();

        foodListViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), new Observer<List<FoodModel>>() {
            @Override
            public void onChanged(List<FoodModel> foodModels) {
                if(foodModels != null)
                {
                    adapter = new MyFoodListAdapter(getContext() , foodModels);
                    recycler_food_list.setAdapter(adapter);
                    recycler_food_list.setLayoutAnimation(layoutAnimationController);
                }

            }
        });
        return root;
    }

    private void initView() {

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(common.categorySelected.getName());

        setHasOptionsMenu(true);
        recycler_food_list.setHasFixedSize(true);
        recycler_food_list.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext() , R.anim.layout_items_from_left);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu,menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));


        //Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        //Clear text when click  to clear button on SearchView

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed = (EditText)searchView.findViewById(R.id.search_src_text);
                //Clear Text
                ed.setText("");
                //Clear Query
                searchView.setQuery("" , false);
                //Collapse the action view
                searchView.onActionViewCollapsed();
                //Collapse the search widget
                menuItem.collapseActionView();
                //Restore result to original
                foodListViewModel.getMutableLiveDataFoodList();

            }
        });
    }

    private void startSearch(String s) {
        List<FoodModel> resultList=new ArrayList<>();
        for(int i=0;i<common.categorySelected.getFoods().size();i++)
        {

            FoodModel foodModel = common.categorySelected.getFoods().get(i);
            if(foodModel.getName().toLowerCase().contains(s))
                resultList.add(foodModel);

        }
        foodListViewModel.getMutableLiveDataFoodList().setValue(resultList);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
