package com.example.myapplication.CartFragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.MyCartAdapter;
import com.example.myapplication.Database.CartDOA;
import com.example.myapplication.Database.CartDataSource;
import com.example.myapplication.Database.CartItem;
import com.example.myapplication.Database.LocalCartDataSource;
import com.example.myapplication.Database.cartDatabase;
import com.example.myapplication.EventBus.CounterCartEvent;
import com.example.myapplication.EventBus.HidFABCart;
import com.example.myapplication.EventBus.UpdateItemInCart;
import com.example.myapplication.Model.CommentModel;
import com.example.myapplication.R;
import com.example.myapplication.common.MySwipeHelper;
import com.example.myapplication.common.common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.OnConflictStrategy;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.observers.LambdaObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment {

    private Parcelable recyclerViewState;

    private CartViewModel cartViewModel;

    private CartDataSource cartDataSource;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;

    @BindView(R.id.txt_total_price)
    TextView txt_total_price;

    @BindView(R.id.group_place_holder)
    CardView group_place_holder;

    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;

    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("One more Step!");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order,null);

        EditText edt_address = (EditText) view.findViewById(R.id.edt_address);
        EditText edt_comment = (EditText) view.findViewById(R.id.edt_comment);
        TextView txt_address = (TextView) view.findViewById(R.id.txt_address_detail);
        RadioButton rdi_home = (RadioButton) view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = (RadioButton) view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_to_this = (RadioButton) view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = (RadioButton) view.findViewById(R.id.rdi_code);
        RadioButton rdi_braintree = (RadioButton) view.findViewById(R.id.rdi_braintree);

        //Data

        edt_address.setText(common.currentUser.getAddress()); // By default we select home address , so users address will display

        //Event

        rdi_home.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {

                if(b)
                {
                    edt_address.setText(common.currentUser.getAddress());
                    txt_address.setVisibility(View.GONE);
                }

            }
        });
        rdi_other_address.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {

                if(b)
                {
                    edt_address.setText(""); // Clear
                    edt_address.setHint("Enter your Address");
                    txt_address.setVisibility(View.GONE);

                }

            }
        });
        rdi_ship_to_this.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {

                if(b)
                {
                    fusedLocationProviderClient.getLastLocation()
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    txt_address.setVisibility(View.GONE);

                                }
                            }).addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                           String coordinates = new StringBuilder()
                                   .append(task.getResult().getLatitude())
                                   .append("/")
                                   .append(task.getResult().getLongitude()).toString();

                            Single<String> singleAddress = Single.just(getAddressFromLatlag(task.getResult().getLatitude() ,
                                    task.getResult().getLongitude()));

                            Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>(){
                                @Override
                                public void onSuccess(String s) {

                                    edt_address.setText(coordinates);
                                    txt_address.setText(s);
                                    txt_address.setVisibility(View.VISIBLE);

                                }

                                @Override
                                public void onError(Throwable e) {
                                    edt_address.setText(coordinates);
                                    txt_address.setText(e.getMessage());
                                    txt_address.setVisibility(View.VISIBLE);

                                }
                            });


                        }
                    });

                }

            }
        });


        builder.setView(view);
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                dialogInterface.dismiss();

            }
        }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                Toast.makeText(getContext(), "Implement late!", Toast.LENGTH_SHORT).show();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private String  getAddressFromLatlag(double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(getContext() , Locale.getDefault());
        String result= "";
        try{
            List<Address> addressList = geocoder.getFromLocation(latitude , longitude , 1);

            if(addressList != null && addressList.size() > 0)
            {
                Address address = addressList.get(0); // always get first item
                StringBuilder sb = new StringBuilder(address.getAddressLine(0));
                result = sb.toString();
            }

            else
                result = "Address not found";



        } catch (IOException e) {
            e.printStackTrace();
            result=e.getMessage();
        }


        return result;
    }

    private MyCartAdapter adapter;

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
            adapter = new MyCartAdapter(getContext()  , cartItems);
            recycler_cart.setAdapter(adapter);
        }
    }
});

                unbinder = ButterKnife.bind(this , root);
        initView();

        initLocation();
        return root;
    }

    private void initLocation() {

        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient  = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());

    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }


    private void initView() {


        setHasOptionsMenu(true);

        cartDataSource = new LocalCartDataSource(cartDatabase.getInstance(getContext()).cartDOA());

        EventBus.getDefault().postSticky(new HidFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext() , layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext() , recycler_cart , 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(getContext() , "Delete" , 30 , 0, Color.parseColor("#FF3C30"),
                        pos -> {
                                    CartItem cartItem = adapter.getItemAtPostions(pos);
                                    cartDataSource.deleteCartItem(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new SingleObserver<Integer>() {
                                                @Override
                                                public void onSubscribe(Disposable d) {

                                                }

                                                @Override
                                                public void onSuccess(Integer integer) {
                                                    adapter.notifyItemRemoved(pos);
                                                    sumAllItemsInCart(); // update total price
                                                    EventBus.getDefault().postSticky(new CounterCartEvent(true)); //Update FAB
                                                    Toast.makeText(getContext(), "Delete item from Cart successful", Toast.LENGTH_SHORT).show();


                                                }

                                                @Override
                                                public void onError(Throwable e) {

                                                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                            });
                        }));

            }
        };

        sumAllItemsInCart();

    }

    private void sumAllItemsInCart() {

        cartDataSource.sumPriceInCart(common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {

                        txt_total_price.setText(new StringBuilder("Total: $").append(aDouble));

                    }

                    @Override
                    public void onError(Throwable e) {

                        if(!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false); // Hide Home Menu already inflate
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_clear_cart)
        {
            cartDataSource.cleanCart(common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {

                            Toast.makeText(getContext(), "Clear Cart Success", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));

                        }

                        @Override
                        public void onError(Throwable e) {

                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper());
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

                        txt_total_price.setText(new StringBuilder("Total: $ ")

                        .append(common.FormatPrice(price)));


                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(getContext(), "[SUM CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }
}
