package com.example.justideas.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justideas.HomeActivity;
import com.example.justideas.ProductDetailsActivity;
import com.example.justideas.R;
import com.example.justideas.databinding.FragmentHomeBinding;
import com.example.justideas.model.Products;
import com.example.justideas.ui.account.AccountViewModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {

    private DatabaseReference ProductRef;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private HomeViewModel holder;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(getLayoutInflater());

        /*
            This here is initializing the variables for the recycler view
         */
        recyclerView = binding.getRoot().findViewById(R.id.recycler_menu);
        layoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        ProductRef = FirebaseDatabase.getInstance().getReference().child("Products");

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseRecyclerOptions<Products> options =
                new FirebaseRecyclerOptions.Builder<Products>()
                        .setQuery(ProductRef, Products.class).build();

        FirebaseRecyclerAdapter<Products, HomeViewModel> adapter =
                new FirebaseRecyclerAdapter<Products, HomeViewModel>(options) {

                    @NonNull
                    @Override
                    public HomeViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout, parent, false);
                        HomeViewModel holder = new HomeViewModel(view);
                        return holder;
                    }
                    @Override
                    protected void onBindViewHolder(@NonNull HomeViewModel holder, int i, @NonNull Products products) {

                        holder.productName.setText(products.getName());
                        holder.productPrice.setText(getString(R.string.priceTag) + products.getPrice());
                        holder.productCategory.setText(products.getCategory());
                        Picasso.get().load(products.getImage()).into(holder.imageView);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent productDetails = new Intent(getActivity(), ProductDetailsActivity.class);
                                productDetails.putExtra("pid", products.getPid());
                                startActivity((productDetails));
                            }
                        });
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }
}
