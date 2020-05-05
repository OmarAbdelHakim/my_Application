    package com.example.myapplication.ui.menu;

    import com.example.myapplication.CallBack.ICategoryCallBackListner;
    import com.example.myapplication.Model.CategoryModel;
    import com.example.myapplication.common.common;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.ArrayList;
    import java.util.List;

    import androidx.annotation.NonNull;
    import androidx.lifecycle.LiveData;
    import androidx.lifecycle.MutableLiveData;
    import androidx.lifecycle.ViewModel;

    public class menuViewModel extends ViewModel implements ICategoryCallBackListner {

        private MutableLiveData<List<CategoryModel>> categoryListMultable ;
        private MutableLiveData<String> messageError = new MutableLiveData<>();
        private ICategoryCallBackListner categoryCallBackListner;



        public menuViewModel() {

            categoryCallBackListner = this;

        }

        public MutableLiveData<List<CategoryModel>> getCategoryListMultable() {
            if(categoryListMultable == null)
            {
                categoryListMultable = new MutableLiveData<>();
                messageError = new MutableLiveData<>();
                loadCategories();
            }

            return categoryListMultable;

        }

        public void loadCategories() {

            List<CategoryModel> templist = new ArrayList<>();
            DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(common.CATEGORY_REF);
            categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(DataSnapshot itemSnapshot : dataSnapshot.getChildren())
                    {

                        CategoryModel categoryModel = itemSnapshot.getValue(CategoryModel.class);
                        categoryModel.setMenu_id(itemSnapshot.getKey());
                        templist.add(categoryModel);
                    }
                    categoryCallBackListner.onCategoryLoadingSuccess(templist);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    categoryCallBackListner.onCategoryLoadingFailed(databaseError.getMessage());

                }
            });
        }

        public MutableLiveData<String> getMessageError() {
            return messageError;
        }

        @Override
        public void onCategoryLoadingSuccess(List<CategoryModel> categoryModels) {

            categoryListMultable.setValue(categoryModels);

        }

        @Override
        public void onCategoryLoadingFailed(String message) {
            messageError.setValue(message);

        }
    }