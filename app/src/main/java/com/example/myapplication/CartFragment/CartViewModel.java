package com.example.myapplication.CartFragment;

import android.content.Context;

import com.example.myapplication.Database.CartDataSource;
import com.example.myapplication.Database.CartItem;
import com.example.myapplication.Database.LocalCartDataSource;
import com.example.myapplication.Database.cartDatabase;
import com.example.myapplication.Model.CommentModel;
import com.example.myapplication.Model.FoodModel;
import com.example.myapplication.common.common;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {
    private CompositeDisposable compositeDisposable ;
    CartDataSource cartDataSource;
    private MutableLiveData<List<CartItem>> mutableLiveDataCartItem;


    public CartViewModel() {

        compositeDisposable = new CompositeDisposable();

    }

    public void initialCartDataSources(Context context){

        cartDataSource = new LocalCartDataSource(cartDatabase.getInstance(context).cartDOA());


    }

    public void onStop()
    {
        compositeDisposable.clear();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItem() {

        if(mutableLiveDataCartItem == null )
            mutableLiveDataCartItem = new MutableLiveData<>();
        getAllCartItems();
        return mutableLiveDataCartItem;
    }

    private void getAllCartItems() {

compositeDisposable.add(cartDataSource.getAllCart(common.currentUser.getUid())
.subscribeOn(Schedulers.io())
.observeOn(AndroidSchedulers.mainThread())
.subscribe(new Consumer<List<CartItem>>() {
    @Override
    public void accept(List<CartItem> cartItems) throws Exception {
        mutableLiveDataCartItem.setValue(cartItems);
    }
}, new Consumer<Throwable>() {
    @Override
    public void accept(Throwable throwable) throws Exception {
        mutableLiveDataCartItem.setValue(null);
    }
}));



    }
}