package com.binus.pekalongancityguide.Adapter;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Comments;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ListCommentBinding;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.HolderComment> {
    private final Context context;
    private final ArrayList<Comments> commentsArrayList;
    private ListCommentBinding binding;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public CommentAdapter(Context context, ArrayList<Comments> commentsArrayList) {
        this.context = context;
        this.commentsArrayList = commentsArrayList;
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ListCommentBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position){
        Comments comments = commentsArrayList.get(position);
        String comment = comments.getComment();
        String uid = comments.getUid();
        if(firebaseAuth.getCurrentUser()!=null && uid.equals(firebaseAuth.getUid())){
            holder.deleteBtn.setVisibility(View.VISIBLE);
        }
            holder.commentTv.setText(comment);
            loadCommentDetails(comments,holder);
            holder.deleteBtn.setOnClickListener(v -> {
            if(firebaseAuth.getCurrentUser()!=null && uid.equals(firebaseAuth.getUid())){
                deleteComment(comments,holder);
            }
        });
    }
    private String editComment="";

    private void deleteComment(Comments comments, HolderComment holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogTheme);
        builder.setTitle(R.string.delete_comment)
                .setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.delete_opt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
                        reference.child(comments.getDestiId())
                                .child("Comments")
                                .child(comments.getId())
                                .removeValue()
                                .addOnSuccessListener(unused -> Toast.makeText(context,R.string.comment_deleted, Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context,context.getString(R.string.failed_delete_comment)+e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton(R.string.cancel_opt, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadCommentDetails(Comments comments, HolderComment holder) {
        String uid = comments.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Users");
        reference.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = ""+snapshot.child("Username").getValue();
                        String userImg = ""+snapshot.child("profileImage").getValue();
                        holder.nameTv.setText(name);
                        try {
                            Glide.with(context)
                                    .load(userImg)
                                    .placeholder(R.drawable.user_icon)
                                    .into(holder.profileImg);
                        }catch(Exception e){
                            holder.profileImg.setImageResource(R.drawable.user_icon);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return commentsArrayList.size();
    }

    class HolderComment extends RecyclerView.ViewHolder{
        ShapeableImageView profileImg;
        ImageButton deleteBtn;
        TextView nameTv,commentTv;
        public HolderComment(@NonNull View itemView) {
            super(itemView);
            profileImg = binding.userProfile;
            nameTv = binding.userName;
            commentTv =  binding.userComment;
            deleteBtn = binding.deleteCommentBtn;
        }
    }
}
