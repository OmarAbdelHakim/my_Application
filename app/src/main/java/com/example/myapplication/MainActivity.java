package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.Model.UserModel;
import com.example.myapplication.common.common;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171; // any number
    private FirebaseAuth firebaseAuth ;
    private DatabaseReference userRef;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;

    List<AuthUI.IdpConfig> providers;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {

        if(listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inite();
    }

    private void inite() {

        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        userRef = FirebaseDatabase.getInstance().getReference(common.User_Refrences);
        firebaseAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();


        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null)

                {

                    //Aleardy login
                    CheckUserFromFirbase(user);

                }
                else
                {
                   PhoneLogin();



                }

            }
        };
    }



    private void CheckUserFromFirbase(final FirebaseUser user) {

        dialog.show();
        userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                            Toast.makeText(MainActivity.this, "You already register", Toast.LENGTH_SHORT).show();

                            UserModel userModel =dataSnapshot.getValue(UserModel.class);
                            gotoHomeActivity(userModel);



                        }else{

                            showregisterDialog(user);



                        }

                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();

                        Toast.makeText(MainActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void showregisterDialog(final FirebaseUser user) {

        androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("please fill information's");
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        final EditText ed_name = (EditText)itemView.findViewById(R.id.edt_name);
        final EditText ed_Address = (EditText)itemView.findViewById(R.id.edt_address);
        final EditText ed_phone = (EditText)itemView.findViewById(R.id.edt_phone);


        //set

        ed_phone.setText(user.getPhoneNumber());

       builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
           }
       });
       builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(final DialogInterface dialog, int which) {

               if(TextUtils.isEmpty(ed_name.getText().toString()))
               {
                   Toast.makeText(MainActivity.this, "Please enter your name ", Toast.LENGTH_SHORT).show();
                   return;
               } else  if(TextUtils.isEmpty(ed_Address.getText().toString()))
               {
                   Toast.makeText(MainActivity.this, "Please enter your Address ", Toast.LENGTH_SHORT).show();
                   return;
               }

               final UserModel userModel = new UserModel();
               userModel.setUid(user.getUid());
               userModel.setName(ed_name.getText().toString());
               userModel.setAddress(ed_Address.getText().toString());
               userModel.setPhone(ed_phone.getText().toString());

               userRef.child(user.getUid()).setValue(userModel)
                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {

                               if(task.isSuccessful()){

                                   dialog.dismiss();


                                   Toast.makeText(MainActivity.this, "Congratulations registered succeeded", Toast.LENGTH_SHORT).show();
                                   gotoHomeActivity(userModel);
                               }

                           }
                       });



           }
       });
        builder.setView(itemView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void gotoHomeActivity(UserModel userModel) {

        common.currentUser = userModel; // important you need to assigne value for it before use
        //start Actvity Home
        startActivity(new Intent(MainActivity.this , HomeActvity.class));
        finish();

    }

    private void PhoneLogin() {


        startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build() , APP_REQUEST_CODE);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == APP_REQUEST_CODE)
        {

            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else{
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
