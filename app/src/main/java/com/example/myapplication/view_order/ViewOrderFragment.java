package com.example.myapplication.view_order;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.Adapter.MyOrderAdapter;
import com.example.myapplication.CallBack.ILoadOrderCallBackListener;
import com.example.myapplication.Model.orderModel;
import com.example.myapplication.R;
import com.example.myapplication.common.common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class ViewOrderFragment extends Fragment implements ILoadOrderCallBackListener {

    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    AlertDialog dialog;

    private Unbinder unbinder;

    private ViewOrderViewModel viewOrderViewModel;

    private ILoadOrderCallBackListener listener;







    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        viewOrderViewModel =
                ViewModelProviders.of(this).get(ViewOrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_ordert, container, false);



        unbinder = ButterKnife.bind(this,root);

        initView (root);
        LoadOrderFromFirebase();

        viewOrderViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner() , orderList -> {

            MyOrderAdapter adapter = new MyOrderAdapter(getContext() , orderList);
            recycler_orders.setAdapter(adapter);

        });

        return root;
    }

    private void LoadOrderFromFirebase() {
        List<orderModel> orderList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot orderSnapShop : dataSnapshot.getChildren())
                        {

                            orderModel order = orderSnapShop.getValue(orderModel.class);
                            order.setOrderNumber(orderSnapShop.getKey());

                            orderList.add(order);

                        }
                        listener.onLoadOrderSucceeded(orderList);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        listener.OnLoadOrderFailed(databaseError.getMessage());

                    }
                });
    }

    private void initView(View root) {

        listener = this;
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(getContext() , layoutManager.getOrientation()));
    }


    @Override
    public void onLoadOrderSucceeded(List<orderModel> orderList) {
        dialog.dismiss();
        viewOrderViewModel.setMutableLiveDataOrderList(orderList);
        
    }

    @Override
    public void OnLoadOrderFailed(String message) {

        dialog.dismiss();

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

    }
}

