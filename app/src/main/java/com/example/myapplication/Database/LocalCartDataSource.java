package com.example.myapplication.Database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class LocalCartDataSource implements CartDataSource {


    private CartDOA cartDOA;

    public LocalCartDataSource(CartDOA cartDOA) {
        this.cartDOA = cartDOA;
    }



    @Override
    public Flowable<List<CartItem>> getAllCart(String uid) {
        return cartDOA.getAllCart(uid);
    }

    @Override
    public Single<Integer> countItemInCart(String uid) {
        return cartDOA.countItemInCart(uid);
    }

    @Override
    public Single<Long> sumPriceInCart(String uid) {
        return cartDOA.sumPriceInCart(uid);
    }

    @Override
    public Single<CartItem> getItemInCart(String foodId, String uid) {
        return cartDOA.getItemInCart(foodId , uid);
    }

    @Override
    public Completable insertOrReplaceAll(CartItem... cartItems) {
        return cartDOA.insertOrReplaceAll(cartItems);
    }

    @Override
    public Single<Integer> updateCartItems(CartItem cartItems) {
        return cartDOA.updateCartItems(cartItems);
    }

    @Override
    public Single<Integer> deleteCartItem(CartItem cartItems) {
        return cartDOA.deleteCartItem(cartItems);
    }

    @Override
    public Single<Integer> cleanCart(String uid) {
        return cartDOA.cleanCart(uid);
    }
}
