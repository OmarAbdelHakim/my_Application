package com.example.myapplication.foodDetailes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.myapplication.Adapter.MyFoodListAdapter;
import com.example.myapplication.Database.CartDataSource;
import com.example.myapplication.Database.CartItem;
import com.example.myapplication.Database.LocalCartDataSource;
import com.example.myapplication.Database.cartDatabase;
import com.example.myapplication.EventBus.CounterCartEvent;
import com.example.myapplication.Model.AddonModel;
import com.example.myapplication.Model.CommentModel;
import com.example.myapplication.Model.FoodModel;
import com.example.myapplication.Model.SizeModel;
import com.example.myapplication.R;
import com.example.myapplication.common.common;
import com.example.myapplication.ui.Comments.CommentFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailesFragment extends Fragment implements TextWatcher {

    private FoodDetailesViewModel foodeshowViewModel;

    private CartDataSource cartDataSource;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

   private Unbinder unbinder;
   private android.app.AlertDialog waitingDialog;

   private BottomSheetDialog addonBottomSheetDialog;

   //View Need Inflate

    ChipGroup chip_group_addon;
    EditText edt_search;

   @BindView(R.id.img_food)
    ImageView img_food;

    @BindView(R.id.btnCard)
    CounterFab btnCard;

    @BindView(R.id.btn_rating)
    FloatingActionButton btn_rating;

    @BindView(R.id.food_name)
    TextView food_name;

    @BindView(R.id.food_description)
    TextView food_description;

    @BindView(R.id.food_price)
    TextView food_price;

    @BindView(R.id.number_button)
    ElegantNumberButton numberButton;

    @BindView(R.id.ratingBar)
    RatingBar ratingBar;

    @BindView(R.id.btnShowComment)
    Button btnShowComment;

    @BindView(R.id.rdi_group_size)
    RadioGroup rdi_group_size;

    @BindView(R.id.img_add_addon)
    ImageView img_add_on;

    @BindView(R.id.ship_Group_user_selected_addon)
    ChipGroup ship_Group_user_selected_addon;

    @OnClick(R.id.img_add_addon)
    void onAddonClick(){
        if(common.selectedFood.getAddon() != null)
        {
            displayAddonList(); // show All addon option
            addonBottomSheetDialog.show();
        }
    }

    @OnClick(R.id.btnCard)
    void onCartItemAdd()
    {
        CartItem cartItem = new CartItem();
        cartItem.setUid(common.currentUser.getUid());
        cartItem.setUserPhone(common.currentUser.getPhone());

        cartItem.setFoodId(common.selectedFood.getId());
        cartItem.setFoodName(common.selectedFood.getName());
        cartItem.setFoodImage(common.selectedFood.getImage());
        cartItem.setFoodPrice(Double.valueOf(String.valueOf(common.selectedFood.getPrice())));
        cartItem.setFoodQuantity(Integer.valueOf(numberButton.getNumber()));
        cartItem.setFoodExtraPrice(common.calculateExtraPrice(common.selectedFood.getUserSelectedSize() , common.selectedFood.getUserSelectedAddon())); // Bacause default we not choose size + addon so extra price 0
        //FoodAddon
        if(common.selectedFood.getUserSelectedAddon() != null)
            cartItem.setFoodAddon(new Gson().toJson(common.selectedFood.getUserSelectedAddon()));
        else
        cartItem.setFoodAddon("Default");

        //FoodSize

        if(common.selectedFood.getUserSelectedSize() != null)
            cartItem.setFoodSize(new Gson().toJson(common.selectedFood.getUserSelectedSize()));
        else
            cartItem.setFoodSize("Default");



        cartDataSource.getWithAllOptionItemInCart(common.currentUser.getUid() ,
                cartItem.getFoodId(),
                cartItem.getFoodSize(),
                cartItem.getFoodAddon())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(CartItem cartItemFromDB) {

                        if(cartItemFromDB.equals(cartItem))
                        {

                            //Already in database just update

                            cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                            cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                            cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                            cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity() + cartItem.getFoodQuantity());

                            cartDataSource.updateCartItems(cartItemFromDB)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {

                                            Toast.makeText(getContext(), "Update Cart success", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                            Toast.makeText(getContext(), "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });


                        }
                        else{

                            //Item not available in Cart before , insert new

                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Add to Cart success", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    } , throwable -> {

                                        Toast.makeText(getContext(), "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                    }));

                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                        if(e.getMessage().contains("empty"))

                        {
                            // Default , if Cart is Empty , this code well be fired

                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Add to Cart success", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    } , throwable -> {

                                        Toast.makeText(getContext(), "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                    }));



                        }
                        else
                            Toast.makeText(getContext(), "[GET CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }





    private void displayAddonList() {

        if(common.selectedFood.getAddon().size() >0)
        {
            chip_group_addon.clearCheck(); // clear check all views
            chip_group_addon.removeAllViews();

            edt_search.addTextChangedListener( this);

            //add all View
            for(AddonModel addonModel : common.selectedFood.getAddon())
            {


                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item , null);
                    chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                            .append(addonModel.getPrice()).append(")"));
                    chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if(b)
                            {
                                if(common.selectedFood.getUserSelectedAddon() == null)
                                    common.selectedFood.setUserSelectedAddon(new ArrayList<>());

                                common.selectedFood.getUserSelectedAddon().add(addonModel);
                            }

                        }
                    });

                    chip_group_addon.addView(chip);




            }


        }


    }


    @OnClick(R.id.btn_rating)
    void onRatingButtonClick()
    {
        showDialogRating();
    }

    @OnClick(R.id.btnShowComment)
    void onShowCommentButtonClick()
    {
        CommentFragment commentFragment = CommentFragment.getInstance();
        commentFragment.show(getActivity().getSupportFragmentManager() , "CommentFragment");
    }

    private void showDialogRating() {
        androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Rating Food");
        builder.setMessage("please fill information's");
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rating,null);

        RatingBar ratingBar = (RatingBar)itemView.findViewById(R.id.rating_bar);
        EditText ed_Comment = (EditText)itemView.findViewById(R.id.edt_comment);
        builder.setView(itemView);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                CommentModel commentModel = new CommentModel();
                commentModel.setName(common.currentUser.getName());
                commentModel.setUid(common.currentUser.getUid());
                commentModel.setComment(ed_Comment.getText().toString());
                commentModel.setRatingValue(ratingBar.getRating());
                Map<String , Object> serverTimeStamp = new HashMap<>();

                serverTimeStamp.put("timeStamp" , ServerValue.TIMESTAMP);
                commentModel.setCommentTimeStamp(serverTimeStamp);

                foodeshowViewModel.setCommentModel(commentModel);

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodeshowViewModel =
                ViewModelProviders.of(this).get(FoodDetailesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_detailesfood, container, false);
        unbinder = ButterKnife.bind(this , root);

        initView();



        foodeshowViewModel.getMutableLiveDataFood().observe(getViewLifecycleOwner(), new Observer<FoodModel>() {
            @Override
            public void onChanged(FoodModel foodModel) {
                displayinfo(foodModel);
            }
        });

        //Comment

        foodeshowViewModel.getMutableLiveDataComment().observe(getViewLifecycleOwner(), new Observer<CommentModel>() {
            @Override
            public void onChanged(CommentModel commentModel) {

                submitRatingToFirebase(commentModel);

            }
        });
        return root;
    }

    private void initView() {

        cartDataSource = new LocalCartDataSource(cartDatabase.getInstance(getContext()).cartDOA());

        waitingDialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        addonBottomSheetDialog = new BottomSheetDialog(getContext() , R.style.DialogStyle);

        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display ,null);

        chip_group_addon = (ChipGroup) layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = (EditText) layout_addon_display.findViewById(R.id.edt_search);

        addonBottomSheetDialog.setContentView(layout_addon_display);
        addonBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                displayUserSelectedAddon();
                calculateTotalPrice();
            }
        });

    }

    private void displayUserSelectedAddon() {

        if(common.selectedFood.getUserSelectedAddon() != null &&
        common.selectedFood.getUserSelectedAddon().size() >0)
        {

            ship_Group_user_selected_addon.removeAllViews(); // Clear all view already added

            for(AddonModel addonModel : common.selectedFood.getUserSelectedAddon()) // Add all available addon to list
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_chip_with_delate_icon , null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                .append(addonModel.getPrice()).append(")"));
                chip.setClickable(true);
                chip.setOnCloseIconClickListener(view -> {
                    //Remove when user select delete

                    ship_Group_user_selected_addon.removeView(view);
                    common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });

                ship_Group_user_selected_addon.addView(chip);



            }

        }
        else if(common.selectedFood.getUserSelectedAddon().size() == 0)

            ship_Group_user_selected_addon.removeAllViews();



    }

    private void submitRatingToFirebase(CommentModel commentModel) {

        waitingDialog.show();

        // we will sumbit the comment Ref
        FirebaseDatabase.getInstance()
                .getReference(common.COMMENT_REF)
                .child(common.selectedFood.getId())
                .push()
                .setValue(commentModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task .isSuccessful())
                        {

                            // after sumbit to commentRef we will update value aveger in Food
                            addRatingToFood(commentModel.getRatingValue());

                        }
                        waitingDialog.dismiss();

                    }
                });
    }

    private void addRatingToFood(float ratingValue) {

        FirebaseDatabase.getInstance()
                .getReference(common.CATEGORY_REF)
                .child(common.categorySelected.getMenu_id())// select category
                .child("foods") // select ArrayList food From This category
        .child(common.selectedFood.getKey())// because food item is array list so key is index of array list
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {

                    FoodModel foodModel = dataSnapshot.getValue(FoodModel.class);
                    foodModel.setKey(common.selectedFood.getKey()); // Don't Forget set it

                    //Apply Rating

                   if(foodModel.getRatingValue() == null)
                        foodModel.setRatingValue(0d);

                    if(foodModel.getRatingCount() == null)
                        foodModel.setRatingCount(0l);



                    double sumRating = foodModel.getRatingValue()+ratingValue;
                    Long ratingCount = foodModel.getRatingCount()+1;
                  //  double result = sumRating / ratingCount;

                    Map<String , Object> updateData = new HashMap<>();
                    updateData.put("ratingValue" , sumRating);
                    updateData.put("ratingCount" ,ratingCount );


                            //update data in variables
                    foodModel.setRatingValue(sumRating);
                    foodModel.setRatingCount(ratingCount);

                    dataSnapshot.getRef()
                            .updateChildren(updateData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    waitingDialog.dismiss();

                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(getContext(), "Thank you ! ", Toast.LENGTH_SHORT).show();
                                        common.selectedFood = foodModel;
                                        foodeshowViewModel.setFoodModel(foodModel);  // food refresh
                                    }

                                }
                            });


                }
                else
                    waitingDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                waitingDialog.dismiss();
                Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void displayinfo(FoodModel foodModel) {

        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));
            if(foodModel.getRatingValue() != null)
        ratingBar.setRating(foodModel.getRatingValue().floatValue() / foodModel.getRatingCount());

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(common.selectedFood.getName());

        //Size
        for(SizeModel sizeModel : common.selectedFood.getSize())
        {
            RadioButton radioButton = new RadioButton(getContext());

            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        common.selectedFood.setUserSelectedSize(sizeModel);
                    calculateTotalPrice();  // Update Price
                }
            });

            LinearLayout .LayoutParams params = new LinearLayout.LayoutParams(0 ,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ,1.0f);
            radioButton.setLayoutParams(params);
            radioButton.setText(sizeModel.getName());
            radioButton.setTag(sizeModel.getPrice());

            rdi_group_size.addView(radioButton);
        }

        if(rdi_group_size.getChildCount() > 0)
        {
            RadioButton radioButton = (RadioButton)rdi_group_size.getChildAt(0);
            radioButton.setChecked(true); // Default first Select
        }

        calculateTotalPrice();


    }

    private void calculateTotalPrice() {

        double totalPrice = Double.parseDouble(common.selectedFood.getPrice().toString()) , displayPrice=0.0;

        //Addon

        if(common.selectedFood.getUserSelectedAddon() != null && common.selectedFood.getUserSelectedAddon().size()>0)
            for(AddonModel addonModel : common.selectedFood.getUserSelectedAddon())

                totalPrice += Double.parseDouble(addonModel.getPrice().toString());


        //Size

        if(common.selectedFood.getUserSelectedSize() != null) // heta dy bta3t error rating

        totalPrice += Double.parseDouble(common.selectedFood.getUserSelectedSize().getPrice().toString());

        displayPrice = totalPrice * (Integer.parseInt(numberButton.getNumber()));
        displayPrice = Math.round(displayPrice*100.0/100.0);

        food_price.setText(new StringBuilder("").append(common.FormatPrice(displayPrice)).toString());


    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // Nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();

        for(AddonModel addonModel : common.selectedFood.getAddon())
        {
            if(addonModel.getName().toLowerCase().contains(charSequence.toString().toLowerCase()))
            {

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item , null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b)
                        {
                            if(common.selectedFood.getUserSelectedAddon() == null)
                                common.selectedFood.setUserSelectedAddon(new ArrayList<>());

                            common.selectedFood.getUserSelectedAddon().add(addonModel);
                        }

                    }
                });

                chip_group_addon.addView(chip);



            }
        }



    }

    @Override
    public void afterTextChanged(Editable editable) {

        // Nothing


    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}
