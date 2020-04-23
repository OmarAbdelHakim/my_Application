package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.EventLog;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.example.myapplication.Database.CartDataSource;
import com.example.myapplication.Database.LocalCartDataSource;
import com.example.myapplication.Database.cartDatabase;
import com.example.myapplication.EventBus.BestDealItemClick;
import com.example.myapplication.EventBus.CounterCartEvent;
import com.example.myapplication.EventBus.FoodItemClick;
import com.example.myapplication.EventBus.HidFABCart;
import com.example.myapplication.EventBus.PopularCategoryClick;
import com.example.myapplication.EventBus.categoryClick;
import com.example.myapplication.Model.CategoryModel;
import com.example.myapplication.Model.FoodModel;
import com.example.myapplication.common.common;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActvity extends AppCompatActivity  {

    private AppBarConfiguration mAppBarConfiguration;
    DrawerLayout drawer;

    private CartDataSource cartDataSource;
    NavController navController;

    android.app.AlertDialog dialog;


    @BindView(R.id.fab)
    CounterFab fab;

    @Override
    protected void onResume() {
        super.onResume();
        counterCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_actvity);

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        ButterKnife.bind(this );

        cartDataSource = new LocalCartDataSource(cartDatabase.getInstance(this).cartDOA());



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                navController.navigate(R.id.nav_food_cart);
            }
        });
         drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);






        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_list , R.id.nav_food_detail , R.id.nav_food_cart , R.id.nav_sign_out )
                .setDrawerLayout(drawer)
                .build();
         navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
       NavigationUI.setupWithNavController(navigationView, navController);

       ///////////////////HERE >>>////////// OK/////////:D /// SIGN OUT FUNCTIONS IS HERE/////////////////////////  set On Click Listener ON navigation 2020

       navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
           @Override
           public boolean onNavigationItemSelected(@NonNull MenuItem item) {
               int id=item.getItemId();
               //it's possible to do more actions on several items, if there is a large amount of items I prefer switch(){case} instead of if()
               if (id==R.id.nav_sign_out){
                  signOut();
               }
               NavigationUI.onNavDestinationSelected(item,navController);
               //This is for closing the drawer after acting on it
               drawer.closeDrawer(GravityCompat.START);
               return true;
           }
       });
//////////////////////////////////////////////////////////////////////////////////////



       navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
           @Override
           public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
               if(destination.getId() == R.id.nav_sign_out){
                   Toast.makeText(HomeActvity.this, "sig out is good", Toast.LENGTH_SHORT).show();
               }
           }
       });


        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        common.setSpanString("Hey, " , common.currentUser.getName(),txt_user);





        counterCartItem();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_actvity, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //Event

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);
        super.onStop();

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCategorySelected(categoryClick event)
    {
        if(event.isSuccess())
        {
            //Toast.makeText(this, "Click to "+event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
          //  NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.navigate(R.id.nav_food_list);
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event)
    {
        if(event.isSuccess())
        {
            //Toast.makeText(this, "Click to "+event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.navigate(R.id.nav_food_detail);
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onHideFABEvent  (HidFABCart event)
    {
        if(event.isHidden())
        {
           fab.hide();
        }
        else
            fab.show();
    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCartCounter  (CounterCartEvent event)
    {
        if(event.isSuccess())
        {
            counterCartItem();
        }
    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick  (BestDealItemClick event)
    {
        if(event.getBestDealModel() !=null)
        {

            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists())
                            {

                                common.categorySelected=dataSnapshot.getValue(CategoryModel.class);
                                common.categorySelected.setMenu_id(dataSnapshot.getKey());

                                //Load Food
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getBestDealModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if(dataSnapshot.exists())
                                                {

                                                    for(DataSnapshot itemSnapShot : dataSnapshot.getChildren())
                                                    {
                                                        common.selectedFood = itemSnapShot.getValue(FoodModel.class);
                                                        common.selectedFood.setKey(itemSnapShot.getKey());
                                                    }

                                                    navController.navigate(R.id.nav_food_detail);

                                                }
                                                else
                                                {
                                                    Toast.makeText(HomeActvity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();

                                                }
                                                dialog.dismiss();

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                dialog.dismiss();
                                                Toast.makeText(HomeActvity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });


                            }
                            else
                            {
                                dialog .dismiss();
                                Toast.makeText(HomeActvity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            dialog.dismiss();
                            Toast.makeText(HomeActvity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onPopularItemClick  (PopularCategoryClick event)
    {
        if(event.getPopularCategoryModel() !=null)
        {

            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists())
                            {

                                common.categorySelected=dataSnapshot.getValue(CategoryModel.class);
                                common.categorySelected.setMenu_id(dataSnapshot.getKey());

                                //Load Food
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if(dataSnapshot.exists())
                                                {

                                                    for(DataSnapshot itemSnapShot : dataSnapshot.getChildren())
                                                    {
                                                        common.selectedFood = itemSnapShot.getValue(FoodModel.class);
                                                        common.selectedFood.setKey(itemSnapShot.getKey());
                                                    }

                                                    navController.navigate(R.id.nav_food_detail);

                                                }
                                                else
                                                {
                                                    Toast.makeText(HomeActvity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();

                                                }
                                                dialog.dismiss();

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                dialog.dismiss();
                                                Toast.makeText(HomeActvity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });


                            }
                            else
                            {
                                dialog .dismiss();
                                Toast.makeText(HomeActvity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            dialog.dismiss();
                            Toast.makeText(HomeActvity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }
    }

    private void counterCartItem() {

        cartDataSource.countItemInCart(common.currentUser.getUid()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Integer integer) {
                fab.setCount(integer);
            }

            @Override
            public void onError(Throwable e) {
                if(!e.getMessage().contains("Query returned empty "))
                {
                    Toast.makeText(HomeActvity.this, "[COUNT CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                else
                    fab.setCount(0);
            }
        });
    }




    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign out")
                .setMessage("Do you really want sign out ?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                common.selectedFood = null;
                common.categorySelected = null;
                common.currentUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActvity.this , MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
