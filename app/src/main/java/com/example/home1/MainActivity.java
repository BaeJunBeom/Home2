package com.example.home1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private  static final int MY_PERMISSION_CAMERA = 1111;
    private  static final int REQUEST_TAKE_PHOTO = 2222;
    private  static final int REQUEST_TAKE_ALBUM = 3333;
    private  static final int REQUEST_IMAGE_CROP = 4444;

    private  boolean checkCameraHardware (Context context ){
        if ( context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }else{
            return  false;
        }
    }
    Button btn_capture, btn_album, btnRead;
    ImageView iv_view;
    EditText edtRaw;
    String mCurrentPhotoPath;

    Uri imageUri;
    Uri photoURI, albumURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_capture = (Button) findViewById(R.id.btn_capture);
        btn_album = (Button) findViewById(R.id.btn_album);
        iv_view = (ImageView) findViewById(R.id.iv_view);
        edtRaw = (EditText) findViewById(R.id.edtRaw);
        btnRead = (Button) findViewById(R.id.btnRead);

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputStream inputS = getResources().openRawResource(R.raw.asd);
                try {
                    byte[] temp = new byte[inputS.available()];
                    inputS.read(temp);
                    inputS.close();
                    edtRaw.setText(new String(temp));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });


        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureCamera();
            }

            private void captureCamera() {
            }
        });

        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlbum();
            }
        });
        checkPermission();
            }
            private  void captureCamera(){
        String state = Environment.getExternalStorageState();
        // 외장 메모리 검사
                if (Environment.MEDIA_MOUNTED.equals(state)){
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null){
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch  (IOException ex){
                            Log.e("captureCamera Error", ex.toString());
                        }
                        if (photoFile != null) {
                            // getUriForFile의 두번째 인자는 manifest provier의 authorites와 일치해야함
                            Uri providerURI = FileProvider.getUriForFile(this,getPackageName(),photoFile);
                            imageUri = providerURI;
                            // 인텐트에 전달할 떄는 FileProvier의 return값인 content: // 로만!!, providerURI의 값에 카메라 데이터를 넣어 보냄
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,providerURI);
                            startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);
                        }
                    }
        }else {
                    Toast.makeText(this,"저장공간이 접근 불가능한 기기입니다", Toast.LENGTH_SHORT).show();
                    return;
    }
            }
public File createImageFile() throws IOException{
    // Create an image file name
    String timeStamp = new SimpleDateFormat("YYYYmmDD_hhMMSS").format(new Date());
    String imageFileName = "JPEG_"+timeStamp + "jpg";
    File imageFile = null;
    File storageDir = new File(Environment.getExternalStorageDirectory()+"/Pictures","gyeom");

    if (!storageDir.exists()){
        Log.i("mCurrentPhotopath1", storageDir.toString());
        storageDir.mkdirs();
    }
        imageFile = new File(storageDir, imageFileName);
    mCurrentPhotoPath = imageFile.getAbsolutePath();
    return  imageFile;
    }
    private void getAlbum() {
        Log.i("getAlbum", "call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,REQUEST_TAKE_ALBUM);
    }
    public static final int sub = 1001;

    private void galleryAddPIc(){
        Log.i("galleryAddPic", " Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File (mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show();
            }
            public  void cropImage(){
        Log.i("cropImage", " call");
        Log.i("cropImage", " photoURI : "+ "photoURI : " + albumURI);

        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        // 50*50 픽셀미만은 편집할 수 없다는 문구 처리 + 갤러리, 포토 둘다 호환하는 방법
                cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cropIntent.setDataAndType(photoURI, "image/*");
                // cropIntent.putExtra("outputX", 200 ); // crop 한 이미지의 x축 크기 , 결과물 크기
                // cropIntent.putExtra("outputY", 200 ); // crop 한 이미지의 y축 크기
                cropIntent.putExtra("aspectX",1); //crop 박스의 x축 비율, 1&1이면 정사각형
                cropIntent.putExtra("aspectY",1); //crop 박스의 y축 비율
                cropIntent.putExtra("scale", true);
                cropIntent.putExtra("output", albumURI);
                startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
                }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK){
                    try {
                        Log.i("REQUEST_TAKE_PHOTO", "OK");
                        galleryAddPIc();

                        iv_view.setImageURI(imageUri);
                } catch (Exception e) {
                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
                    }
                    Toast.makeText(MainActivity.this, " 사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
        }
                break;
            case REQUEST_TAKE_ALBUM:
                if (resultCode == Activity.RESULT_OK){
                    if(data.getData()!=null){
                        try {
                            File albumFile = null;
                            albumFile = createImageFile();
                            photoURI = data.getData();
                            albumURI = Uri.fromFile(albumFile);
                            cropImage();
                        } catch (Exception e){
                            Log.e("TAKE_ALBUM_SINGLE ERROR", e.toString());
                        }
                    }
                }
                break;
            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK){
                    galleryAddPIc();
                    iv_view.setImageURI(albumURI);
                }
                break;
        }
    }
            private void checkPermission(){
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
        // 처음 호출시에는 IF()안의 부분은 False로 리턴이 되고,  elsl 로 요청이 넘어간다.
    if ((ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA))) {
        new AlertDialog.Builder(this)
                .setTitle("알림")
                .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다. ")
                .setNeutralButton("설정 ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:"+ getPackageName()));
                        startActivity(intent);
                    }
                    })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .create()
                .show();;
                }else {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_CAMERA:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] :
                    if (grantResults[i] < 0 ){
                        Toast.makeText(MainActivity.this, " 해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // 허용했다면 이 부분에서 가능
                break;
        }
    }
}
