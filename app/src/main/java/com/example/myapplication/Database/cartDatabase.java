package com.example.myapplication.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 1 , entities = CartItem.class , exportSchema = false)

public abstract class cartDatabase extends RoomDatabase {

    public abstract CartDOA cartDOA() ;
    private static cartDatabase instance;

    public static cartDatabase getInstance(Context context) {

        if(instance == null )
            instance = Room.databaseBuilder(context , cartDatabase.class , "EatItV2DB2").build();
        return instance;
    }
}
