package com.example.user.firebasephoto;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public Button btn,btn_take;
    private static final int RC_PHOTO_PICKER = 2;
    private static final int CAMERA_REQUEST_CODE=1;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN = 1;
    public FirebaseStorage firebaseStorage;
    public StorageReference storageReference;
    public ProgressDialog progressDialog;
    public ImageView mimage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        btn = (Button) findViewById(R.id.btn);
        btn_take=(Button)findViewById(R.id.btn_take);
        mimage=(ImageView)findViewById(R.id.mimage);
        progressDialog=new ProgressDialog(this);
        btn.setOnClickListener(this);
        btn_take.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                // intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent, RC_PHOTO_PICKER);
                break;
            case R.id.btn_take:
                Intent intent1=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent1,CAMERA_REQUEST_CODE);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            progressDialog.setMessage("Uploading....");
            progressDialog.show();
            Uri uri=data.getData();
            StorageReference filepath=storageReference.child("Photo").child(uri.getLastPathSegment());//資料夾....取圖片檔名作為存取檔名
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this,"Upload Done.",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
        if(requestCode==CAMERA_REQUEST_CODE&&resultCode==RESULT_OK)
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            progressDialog.setMessage("Uploading....");
            progressDialog.show();
            Uri uri1=data.getData();
            ContentResolver cr=this.getContentResolver();
            try
            {
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inSampleSize=4;
                Bitmap bitmap=BitmapFactory.decodeStream(cr.openInputStream(uri1),null,options);
                bitmap.compress(Bitmap.CompressFormat.JPEG,90,stream);
                mimage.setImageBitmap(bitmap);

            }catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            /*原圖壓縮1/4上傳*/
            StorageReference filepath1=storageReference.child("Photo").child(uri1.getLastPathSegment());
            filepath1.putBytes(stream.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this,"Upload Done.",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
            /*原圖上傳不經壓縮，使用Picasso: http://square.github.io/picasso/ */
           /* filepath1.putFile(uri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this,"Upload Done.",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    Uri downloaduri=taskSnapshot.getDownloadUrl();

                    //Picasso.with(MainActivity.this).load(downloaduri).fit().centerCrop().into(mimage);
                }
            });*/
        }
    }

}
