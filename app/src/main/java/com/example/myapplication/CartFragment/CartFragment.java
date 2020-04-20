package com.example.myapplication.CartFragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.MyCartAdapter;
import com.example.myapplication.Database.CartDOA;
import com.example.myapplication.Database.CartDataSource;
import com.example.myapplication.Database.CartItem;
import com.example.myapplication.Database.LocalCartDataSource;
import com.example.myapplication.Database.cartDatabase;
import com.example.myapplication.EventBus.HidFABCart;
import com.example.myapplication.EventBus.UpdateItemInCart;
import com.example.myapplication.Model.CommentModel;
import com.example.myapplication.R;
import com.example.myapplication.common.common;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment {

    private Parcelable recyclerViewState;

    private CartViewModel cartViewModel;

    private CartDataSource cartDataSource;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;

    @BindView(R.id.txt_total_price)
    TextView txt_total_price;

    @BindView(R.id.group_place_holder)
    CardView group_place_holder;

    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;

    private Unbinder unbinder;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel =
                ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cardfood, container, false);

cartViewModel.initialCartDataSources(getContext());
cartViewModel.getMutableLiveDataCartItem().observe(getViewLifecycleOwner(), new Observer<List<CartItem>>() {
    @Override
    public void onChanged(List<CartItem> cartItems) {
        if(cartItems == null || cartItems.isEmpty()  )
        {
            recycler_cart.setVisibility(View.GONE);
            group_place_holder.setVisibility(View.GONE);
            txt_empty_cart.setVisibility(View.VISIBLE);


        }
        else
        {
            recycler_cart.setVisibility(View.VISIBLE);
            group_place_holder.setVisibility(View.VISIBLE);
            txt_empty_cart.setVisibility(View.GONE);
            MyCartAdapter adapter = new MyCartAdapter(getContext()  , cartItems);
            recycler_cart.setAdapter(adapter);
        }
    }
});

                unbinder = ButterKnife.bind(this , root);
        intiatView();
        return root;
    }

    private void intiatView() {

        cartDataSource = new LocalCartDataSource(cartDatabase.getInstance(getContext()).cartDOA());

        EventBus.getDefault().postSticky(new HidFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext() , layoutManager.getOrientation()));

    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this))

            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HidFABCart(false));
        cartViewModel.onStop();
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Subscribe(sticky = true , threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event)
    {
       if(event.getCartItem() != null)
       {

           //First , Save Statue in RecyelrView

           recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
           cartDataSource.updateCartItems(event.getCartItem())
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(new SingleObserver<Integer>() {
                       @Override
                       public void onSubscribe(Disposable d) {

                       }

                       @Override
                       public void onSuccess(Integer integer) {

                           calculateTotalPrice();
                           recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState); // fix error refresh recycler view After Update



                       }

                       @Override
                       public void onError(Throwable e) {

                           Toast.makeText(getContext(), "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();

                       }
                   });


       }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {

                        txt_total_price.setText(new StringBuilder("Total: ")

                        .append(common.FormatPrice(price)));


                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(getContext(), "[SUM CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }
}
