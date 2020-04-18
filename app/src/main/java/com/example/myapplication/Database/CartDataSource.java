package com.example.myapplication.Database;

import java.util.List;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface CartDataSource {

    Flowable<List<CartItem>> getAllCart(String uid);


    Single<Integer> countItemInCart (String uid);

    Single<Long> sumPriceInCart (String uid);


    Single<CartItem> getItemInCart (String foodId ,String uid);


    Completable insertOrReplaceAll(CartItem... cartItems);


    Single<Integer> updateCartItems(CartItem cartItems);


    Single<Integer> deleteCartItem (CartItem cartItems);


    Single<Integer> cleanCart (String uid);

}
