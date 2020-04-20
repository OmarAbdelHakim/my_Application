package com.example.myapplication.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.CallBack.IRecycelerViewClickListner;
import com.example.myapplication.Database.CartDataSource;
import com.example.myapplication.Database.CartItem;
import com.example.myapplication.Database.LocalCartDataSource;
import com.example.myapplication.Database.cartDatabase;
import com.example.myapplication.EventBus.CounterCartEvent;
import com.example.myapplication.EventBus.FoodItemClick;
import com.example.myapplication.Model.FoodModel;
import com.example.myapplication.R;
import com.example.myapplication.common.common;

import org.greenrobot.eventbus.EventBus;

import java.util.ConcurrentModificationException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder> {

    private Context context;
    private List<FoodModel> foodModelList;

    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;

    public MyFoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;
        this.compositeDisposable = new CompositeDisposable();
        this.cartDataSource = new LocalCartDataSource(cartDatabase.getInstance(context).cartDOA());


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_food_item , parent  ,false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.img_food_image);
        holder.txt_food_price.setText(new StringBuilder("$")
        .append(foodModelList.get(position).getPrice()));

        holder.txt_food_name.setText(new StringBuilder("")
        .append(foodModelList.get(position).getName()));

        //Event

        holder.setListner(new IRecycelerViewClickListner() {
            @Override
            public void onItemclickListner(View view, int pos) {
                common.selectedFood = foodModelList.get(pos);
                common.selectedFood.setKey(String.valueOf(pos));
                EventBus.getDefault().postSticky( new FoodItemClick(true , foodModelList.get(pos)));
            }
        });

        holder.img_quick_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CartItem cartItem = new CartItem();
                cartItem.setUid(common.currentUser.getUid());
                cartItem.setUserPhone(common.currentUser.getPhone());

                cartItem.setFoodId(foodModelList.get(position).getId());
                cartItem.setFoodName(foodModelList.get(position).getName());
                cartItem.setFoodImage(foodModelList.get(position).getImage());
                cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice())));
                cartItem.setFoodQuantity(1);
                cartItem.setFoodExtraPrice(0.0); // Bacause default we not choose size + addon so extra price 0
                cartItem.setFoodAddon("Default");
                cartItem.setFoodSize("Default");

             cartDataSource.getWithAllOptionItemInCart(common.currentUser.getUid() ,
                     cartItem.getFoodId(),
                     cartItem.getFoodSize(),
                     cartItem.getFoodAddon())
                     .subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribe(new SingleObserver<CartItem>() {
                         @Override
                         public void onSubscribe(Disposable d) {

                         }

                         @Override
                         public void onSuccess(CartItem cartItemFromDB) {

                             if(cartItemFromDB.equals(cartItem))
                             {

                                 //Already in database just update

                                 cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                                 cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                                 cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                                 cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity() + cartItem.getFoodQuantity());

                                 cartDataSource.updateCartItems(cartItemFromDB)
                                          .subscribeOn(Schedulers.io())
                                     .observeOn(AndroidSchedulers.mainThread())
                                     .subscribe(new SingleObserver<Integer>() {
                                         @Override
                                         public void onSubscribe(Disposable d) {

                                         }

                                         @Override
                                         public void onSuccess(Integer integer) {

                                             Toast.makeText(context, "Update Cart success", Toast.LENGTH_SHORT).show();
                                             EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                         }

                                         @Override
                                         public void onError(Throwable e) {

                                             Toast.makeText(context, "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();

                                         }
                                     });


                             }
                             else{

                                 //Item not available in Cart before , insert new

                                 compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                 .subscribeOn(Schedulers.io())
                                                 .observeOn(AndroidSchedulers.mainThread())
                                                 .subscribe(() -> {
                                                     Toast.makeText(context, "Add to Cart success", Toast.LENGTH_SHORT).show();
                                                     EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                 } , throwable -> {

                                                     Toast.makeText(context, "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                                 }));

                             }

                         }

                         @Override
                         public void onError(Throwable e) {

                             if(e.getMessage().contains("empty"))

                             {
                                // Default , if Cart is Empty , this code well be fired

                                 compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                         .subscribeOn(Schedulers.io())
                                         .observeOn(AndroidSchedulers.mainThread())
                                         .subscribe(() -> {
                                             Toast.makeText(context, "Add to Cart success", Toast.LENGTH_SHORT).show();
                                             EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                         } , throwable -> {

                                             Toast.makeText(context, "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                         }));



                             }
                            else
                                 Toast.makeText(context, "[GET CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();

                         }
                     });



            }
        });

    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Unbinder unbinder;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;

        @BindView(R.id.txt_food_price)
        TextView txt_food_price;

        @BindView(R.id.img_food_image)
        ImageView img_food_image;

        @BindView(R.id.img_fav)
        ImageView img_fav;

        @BindView(R.id.img_quick_cart)
        ImageView img_quick_cart;



        IRecycelerViewClickListner listner;

        public void setListner(IRecycelerViewClickListner listner) {
            this.listner = listner;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this , itemView);
            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View v) {
            listner.onItemclickListner(v ,getAdapterPosition());
        }
    }
}
