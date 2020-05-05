package com.example.myapplication.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Model.AddonModel;
import com.example.myapplication.Model.CategoryModel;
import com.example.myapplication.Model.FoodModel;
import com.example.myapplication.Model.SizeModel;
import com.example.myapplication.Model.TokenModel;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.R;
import com.example.myapplication.Services.MyFCMServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class common {
    public static final String POPULAR_CATEGORY_REF = "MostPopular";
    public static final String BEST_DEAL_REF = "BestDeals";
    public static final int DEFAULT_COLUMN_COUNT =0 ;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String CATEGORY_REF = "Category";
    public static final String COMMENT_REF = "Comments";
    public static final String ORDER_REF = "Orders";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "content";
    private static final String TOKEN_REF ="Tokens" ;
    public static String User_Refrences = "Users";

    public static UserModel currentUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;

    public static String  FormatPrice(double price) {

        if(price != 0 )
        {
            DecimalFormat df = new DecimalFormat("#,#0.00");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(df.format(price)).toString();

            return finalPrice.replace("." , ",");

        }

        else
        return "0.00";


    }

    public static Double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {

        Double result =0.0;
        if(userSelectedSize == null && userSelectedAddon == null)

            return 0.0;

        else if(userSelectedSize == null)
        {
            // if User Selected Addon != null , we need to sum Price
            for(AddonModel addonModel : userSelectedAddon)

                result += addonModel.getPrice();
                return result;

        }
        else if(userSelectedAddon == null)
        {
            return  userSelectedSize.getPrice()*1.0;

        }
        else {
            // if both size and is selected
            result = userSelectedSize.getPrice()*1.0;
            for(AddonModel addonModel : userSelectedAddon)
                result += addonModel.getPrice();
            return result;
             }

    }

    public static void setSpanString(String welcome, String name, TextView textView) {

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan , 0 , name.length() , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder , TextView.BufferType.SPANNABLE);

    }

    public static String createOrderNumber() {
        return new StringBuilder()
                .append(System.currentTimeMillis())  // Get current time in milliSecond
                .append(Math.abs(new Random().nextInt())) // Add Random number to block same order at same time
                .toString();
    }

    public static String  getDateOfWeek(int i) {

        switch (i)
        {

            case 1 :
                return "Monday";
            case 2 :
                return "Tuesday";
            case 3 :
                return "Wednesday";
            case 4 :
                return "Thursday";
            case 5 :
                return "Friday";
            case 6 :
                return "Saturday";
            case 7 :
                return "Sunday";
            default:
                return "Unk";



        }
    }

    public static String convertStatusToText(int orderStatus) {

       switch (orderStatus)
       {
           case 0 :
               return "Placed";
           case 1 :
               return "Shipping";
           case 2 :
               return "Shipped";
           case -1 :
               return "Canceled";
           default:
               return "Unk";


       }



    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent =null;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(context ,id , intent , PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "edmt_dev_eat_it_v2";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID ,
                    "Eat It V2" , NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(" Eat It V2 ");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[] {0,1000,500,1000});
            notificationChannel.enableVibration(true);


            notificationManager.createNotificationChannel(notificationChannel);

        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context , NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_restaurant_menu_black_24dp));
        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notificationManager.notify(id , notification);


    }

    public static void UpdateToken(Context context, String newToken) {

        FirebaseDatabase.getInstance()
                .getReference(common.TOKEN_REF)
                .child(common.currentUser.getUid())
                .setValue(new TokenModel(common.currentUser.getPhone() , newToken))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    public static String CreateTopicOrder() {
        return new StringBuilder("/topics/new_order").toString();
    }
}
