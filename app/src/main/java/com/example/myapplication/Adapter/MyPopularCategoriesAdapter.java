            package com.example.myapplication.Adapter;

            import android.content.Context;
            import android.view.LayoutInflater;
            import android.view.View;
            import android.view.ViewGroup;
            import android.widget.TextView;
            import android.widget.Toast;

            import com.bumptech.glide.Glide;
            import com.example.myapplication.CallBack.IRecycelerViewClickListner;
            import com.example.myapplication.EventBus.PopularCategoryClick;
            import com.example.myapplication.Model.PopularCategoryModel;
            import com.example.myapplication.R;

            import org.greenrobot.eventbus.EventBus;

            import java.util.List;

            import androidx.annotation.NonNull;
            import androidx.recyclerview.widget.RecyclerView;
            import butterknife.BindView;
            import butterknife.ButterKnife;
            import butterknife.Unbinder;
            import de.hdodenhof.circleimageview.CircleImageView;

            public class MyPopularCategoriesAdapter extends RecyclerView.Adapter<MyPopularCategoriesAdapter.MyViewHolder> {


                Context context;
                List<PopularCategoryModel> popularCategoryModelList;


                public MyPopularCategoriesAdapter(Context context, List<PopularCategoryModel> popularCategoryModelList) {
                    this.context = context;
                    this.popularCategoryModelList = popularCategoryModelList;
                }



                @NonNull
                @Override
                public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    return new MyViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.layout_popular_categories_items ,parent , false));
                }

                @Override
                public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

                    Glide.with(context).load(popularCategoryModelList.get(position).getImage())
                            .into(holder.category_image);

                    holder.txt_category_name.setText(popularCategoryModelList.get(position).getName());

                    holder.setListener(new IRecycelerViewClickListner() {
                        @Override
                        public void onItemclickListner(View view, int pos) {

                            EventBus.getDefault().postSticky(new PopularCategoryClick(popularCategoryModelList.get(pos)));

                        }
                    });


                }

                @Override
                public int getItemCount() {
                    return popularCategoryModelList.size();
                }

                public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                    Unbinder unbinder ;
                    @BindView(R.id.txt_category_name)
                    TextView txt_category_name;
                    @BindView(R.id.category_image)
                    CircleImageView category_image;

                    IRecycelerViewClickListner listener;

                    public void setListener(IRecycelerViewClickListner listener) {
                        this.listener = listener;
                    }

                    public MyViewHolder(@NonNull View itemView) {
                        super(itemView);

                        unbinder = ButterKnife.bind(this,itemView);
                        itemView.setOnClickListener(this);
                    }

                    @Override
                    public void onClick(View v) {
                        listener.onItemclickListner(v , getAdapterPosition());
                    }
                }
            }
