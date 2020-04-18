package com.example.myapplication.Database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

//  Cart Data Access Object (DOA)
@Dao

public interface CartDOA {

    @Query("SELECT * FROM Cart WHERE uid=:uid")
    Flowable<List<CartItem>> getAllCart(String uid);

    @Query("SELECT COUNT(*) FROM Cart WHERE uid=:uid ")
    Single<Integer> countItemInCart (String uid);

    @Query("SELECT SUM(foodPrice*foodQuantity) + (foodExtraPrice*foodQuantity) FROM Cart WHERE uid=:uid ")
    Single<Long> sumPriceInCart (String uid);

    @Query("SELECT * FROM Cart WHERE foodId=:foodId AND uid=:uid ")
    Single<CartItem> getItemInCart (String foodId ,String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrReplaceAll(CartItem... cartItems);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCartItems(CartItem cartItems);

    @Delete
    Single<Integer> deleteCartItem (CartItem cartItems);

    @Query("DELETE FROM Cart WHERE uid=:uid")
    Single<Integer> cleanCart (String uid);









}
