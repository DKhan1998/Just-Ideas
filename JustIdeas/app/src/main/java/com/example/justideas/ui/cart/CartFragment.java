package com.example.justideas.ui.cart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justideas.ConfirmOrderActivity;
import com.example.justideas.Prevalent.Prevalent;
import com.example.justideas.ProductDetailsActivity;
import com.example.justideas.R;
import com.example.justideas.databinding.FragmentCartBinding;
import com.example.justideas.model.Basket;
import com.example.justideas.model.Products;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class CartFragment extends Fragment{

    private DatabaseReference BasketRef, productRef;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private CartViewModel holder;

    private Button Checkout;
    private TextView totalAmount;
    private int basketTotal = 0;

    private FragmentCartBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCartBinding.inflate(getLayoutInflater());

        /*
            This here is initializing the variables for the recycler view
         */
        recyclerView = binding.getRoot().findViewById(R.id.recycler_basket);
        layoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        // Reference to database and storage
        BasketRef = FirebaseDatabase.getInstance().getReference().child("Basket Items").child("User View");
        productRef = FirebaseDatabase.getInstance().getReference().child("Products");

        Checkout = binding.getRoot().findViewById(R.id.checkoutBtn);
        totalAmount = binding.getRoot().findViewById(R.id.basket_price);

        Checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ConfirmOrderActivity.class);
                intent.putExtra("Total Price", String.valueOf(basketTotal));
                startActivity(intent);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseRecyclerOptions<Basket> options =
                new FirebaseRecyclerOptions.Builder<Basket>()
                        .setQuery(BasketRef
                                .child(Prevalent.currentOnlineUser.getPhone())
                                .child("Products"), Basket.class).build();

        FirebaseRecyclerAdapter<Basket, CartViewModel> adapter =
                new FirebaseRecyclerAdapter<Basket, CartViewModel>(options) {

                    @NonNull
                    @Override
                    public CartViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.basket_item_layout, parent, false);
                        holder = new CartViewModel(view);
                        return holder;
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull CartViewModel cartViewModel, int i, @NonNull Basket basket)
                    {

                        // Assign values to the specific fields
                        holder.pTitle.setText(basket.getName());
                        holder.pCategory.setText(basket.getCategory());
                        holder.pSize.setText(basket.getSize());
                        holder.pColor.setText(basket.getColor());
                        holder.pPrice.setText(getString(R.string.priceTag) + basket.getPrice());
                        Picasso.get().load(basket.getImage()).placeholder(R.drawable.profile).into(holder.pImage);
                        holder.pQuantity.setText(basket.getQuantity());
                        int itemTotalPrice = ((Integer.parseInt(basket.getPrice()))) * Integer.parseInt(basket.getQuantity());
                        basketTotal = basketTotal + itemTotalPrice;
                        totalAmount.setText(getString(R.string.priceTag) + basketTotal);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]
                                        {
                                                "Edit",
                                                "Remove"
                                        };

                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                                dialogBuilder.setTitle("Options");

                                dialogBuilder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which == 0){
                                            Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                                            intent.putExtra("pid", basket.getPid());
                                            startActivity(intent);
                                        }
                                        if(which == 1)
                                        {
                                            BasketRef.child(Prevalent.currentOnlineUser.getPhone())
                                                    .child("Products")
                                                    .child(basket.getPid())
                                                    .removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful())
                                                            {
                                                                Toast.makeText(getActivity(), "Removed!", Toast.LENGTH_LONG).show();
                                                                int itemTotalPrice = ((Integer.parseInt(basket.getPrice()))) * Integer.parseInt(basket.getQuantity());
                                                                basketTotal = basketTotal + itemTotalPrice;
                                                                totalAmount.setText(getString(R.string.priceTag) + basketTotal);
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                                dialogBuilder.show();
                            }
                        });
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
}
