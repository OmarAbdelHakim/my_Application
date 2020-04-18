package com.example.myapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.CallBack.IRecycelerViewClickListner;
import com.example.myapplication.EventBus.categoryClick;
import com.example.myapplication.Model.CategoryModel;
import com.example.myapplication.R;
import com.example.myapplication.common.common;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.microedition.khronos.opengles.GL;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCategoryItemAdapter extends RecyclerView.Adapter<MyCategoryItemAdapter.MyViewHolder> {

    Context context;
    List<CategoryModel> categoryModelList;

    public MyCategoryItemAdapter(Context context, List<CategoryModel> categoryModelList) {
        this.context = context;
        this.categoryModelList = categoryModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_category_item , parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(context).load(categoryModelList.get(position).getImage())
                .into(holder.Category_image);
        holder.category_name.setText(new StringBuilder(categoryModelList.get(position).getName()));

        //Event
        holder.setListner(new IRecycelerViewClickListner() {
            @Override
            public void onItemclickListner(View view, int pos) {

                common.categorySelected = categoryModelList.get(pos);

                EventBus.getDefault().postSticky(new categoryClick(true  , categoryModelList.get(pos)));

            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Unbinder unbinder;
        @BindView(R.id.img_category)
        ImageView Category_image;
        @BindView(R.id.txt_category)
        TextView category_name;


        IRecycelerViewClickListner listner;

        public void setListner(IRecycelerViewClickListner listner) {
            this.listner = listner;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listner.onItemclickListner(v , getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(categoryModelList.size()==1)
            return common.DEFAULT_COLUMN_COUNT;

        else
        {
            if(categoryModelList.size() %2 == 0)
                return common.DEFAULT_COLUMN_COUNT;

            else
                return (position >1 && position == categoryModelList.size() -1) ? common.FULL_WIDTH_COLUMN : common.DEFAULT_COLUMN_COUNT;


        }


    }
}
