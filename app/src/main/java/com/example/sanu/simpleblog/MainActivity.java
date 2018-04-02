package com.example.sanu.simpleblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private boolean mProcessLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser()==null){

                    Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");

        mDatabase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);

        mBlogList = (RecyclerView)findViewById(R.id.blog_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(layoutManager);

        checkUserExist();

    }

    @Override
    protected void onStart() {
        super.onStart();

        //checkUserExist();

        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<Blog, BlogViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(

                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, final Blog model, int position) {

                final String post_key = getRef(position).getKey();

               viewHolder.setTitle(model.getTitle());
               viewHolder.setDesc(model.getDesc());
               viewHolder.setImage(getApplicationContext(), model.getImage());
               viewHolder.setUsername(model.getUsername());

               viewHolder.setLikeBtn(post_key);

               viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                      // Toast.makeText(MainActivity.this, post_key,Toast.LENGTH_LONG).show();

                       Intent singleBlogIntent = new Intent(MainActivity.this,BlogSingleActivity.class);
                       singleBlogIntent.putExtra("blog_id", post_key);
                       startActivity(singleBlogIntent);

                   }
               });

               viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {

                       mProcessLike = true;



                           mDatabaseLike.addValueEventListener(new ValueEventListener() {
                               @Override
                               public void onDataChange(DataSnapshot dataSnapshot) {

                                   if(mProcessLike) {

                                       if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                           mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();

                                           mProcessLike = false;

                                       } else {

                                           mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("RandomValue");

                                           mProcessLike = false;
                                       }
                                   }

                               }

                               @Override
                               public void onCancelled(DatabaseError databaseError) {

                               }
                           });

                   }
               });
            }
        };

        mBlogList.setAdapter(firebaseRecyclerAdapter);
    }

    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null){
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(user_id)){

                        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageButton mLikeButton;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {

            super(itemView);

            mView = itemView;

            mLikeButton = (ImageButton)mView.findViewById(R.id.like_button);

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();

            mDatabaseLike.keepSynced(true);

        }

        public void setLikeBtn(final String post_key){

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){

                        mLikeButton.setImageResource(R.mipmap.like_blue);

                    }else {

                        mLikeButton.setImageResource(R.mipmap.like_gray);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title){

            TextView post_title = (TextView)mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setDesc(String desc){

            TextView post_desc = (TextView)mView.findViewById(R.id.post_text);
            post_desc.setText(desc);
        }

        public void setUsername(String username){

            TextView post_username = (TextView)mView.findViewById(R.id.post_username);
            post_username.setText(username);
        }


        public void setImage(Context ctx, String image){

            ImageView post_image = (ImageView)mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(image).into(post_image);
        }
    }

    @Override
    //add Add icon to main menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //open postActivity
        if (item.getItemId()==R.id.action_add){
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }

        //logout activity
        if (item.getItemId()==R.id.action_logout){
            logout();
        }

        //Open settings
        if (item.getItemId()==R.id.action_settings){
            startActivity(new Intent(MainActivity.this, Settings.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

}
