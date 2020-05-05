package com.example.myapplication.ui.menu;

import android.app.AlertDialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

import com.example.myapplication.Adapter.MyCategoryItemAdapter;
import com.example.myapplication.EventBus.MenuItemBack;
import com.example.myapplication.Model.CategoryModel;
import com.example.myapplication.R;
import com.example.myapplication.common.SpaceItemDecoration;
import com.example.myapplication.common.common;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class menuFragment extends Fragment {

    private menuViewModel menuViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoryItemAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        menuViewModel =
                ViewModelProviders.of(this).get(menuViewModel.class);
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        unbinder= ButterKnife.bind(this, root);
        initView();
        menuViewModel.getMessageError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getContext(), ""+s, Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });
        menuViewModel.getCategoryListMultable().observe(getViewLifecycleOwner(), new Observer<List<CategoryModel>>() {
            @Override
            public void onChanged(List<CategoryModel> categoryModels) {
                dialog.dismiss();
                adapter = new MyCategoryItemAdapter(getContext() , categoryModels);
                recycler_menu.setAdapter(adapter);
                recycler_menu.setLayoutAnimation(layoutAnimationController);
            }
        });

        return root;
    }

    private void initView() {

        setHasOptionsMenu(true);

        dialog = new SpotsDialog. Builder().setContext(getContext()).setCancelable(false).build();
        dialog.show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext() , R.anim.layout_items_from_left);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext() , 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {

                if(adapter != null)
                {
                    switch (adapter.getItemViewType(position)){

                        case common.DEFAULT_COLUMN_COUNT : return 1;
                        case common.FULL_WIDTH_COLUMN : return 2;
                        default:return -1;


                    }
                }
                return -1;
            }
        });

        recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.addItemDecoration(new SpaceItemDecoration(8));

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
                menuViewModel.loadCategories();

            }
        });
    }

    private void startSearch(String s) {
        List<CategoryModel> resultList=new ArrayList<>();
        for(int i=0;i<adapter.getListCategory().size();i++)
        {

            CategoryModel categoryModel = adapter.getListCategory().get(i);
            if(categoryModel.getName().toLowerCase().contains(s))
                resultList.add(categoryModel);

        }
      menuViewModel.getCategoryListMultable().setValue(resultList);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
