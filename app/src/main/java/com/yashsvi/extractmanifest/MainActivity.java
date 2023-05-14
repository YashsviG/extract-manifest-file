package com.yashsvi.extractmanifest;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 100;
    private static final int REQUEST_CODE_PICK_APK = 200;

    private Button selectApkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectApkButton = findViewById(R.id.select_apk_button);
        selectApkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (ContextCompat.checkSelfPermission(MainActivity.this,
//                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(MainActivity.this,
//                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                            REQUEST_CODE_PERMISSION);
//                } else {
                    pickApkFile();
//                }
            }
        });
    }

    private void pickApkFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.android.package-archive");
        startActivityForResult(intent, REQUEST_CODE_PICK_APK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_APK && resultCode == RESULT_OK && data != null) {
            Uri apkUri = data.getData();
            if (apkUri != null) {
                extractManifest(apkUri);
            }
        }
    }

    private void extractManifest(Uri contentUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(contentUri);
            if (inputStream != null) {
                ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                ZipEntry zipEntry;

                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (zipEntry.getName().equals("AndroidManifest.xml")) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int length;

                        while ((length = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }

                        String manifestContent = new String(outputStream.toByteArray());
                        System.out.println(manifestContent);
                        // Save manifestContent to a file
                        String fileName = "manifest.xml";
                        File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File outputFile = new File(outputDir, fileName);

                        FileWriter fileWriter = new FileWriter(outputFile);
                        fileWriter.write(manifestContent);
                        fileWriter.close();

                        // Display a Toast indicating the file creation status
                        if (outputFile.exists()) {
                            showToast("File created successfully!");
                        } else {
                            showToast("File creation failed.");
                        }

                        break;
                    }
                }

                zipInputStream.close();
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String filePath = null;

        // Check if the URI scheme is "content"
        if (ContentResolver.SCHEME_CONTENT.equals(contentUri.getScheme())) {
            // Try to obtain the DocumentFile from the content URI
            DocumentFile documentFile = DocumentFile.fromSingleUri(this, contentUri);
            if (documentFile != null && documentFile.isFile()) {
                // Retrieve the file path from the DocumentFile
                filePath = documentFile.getUri().getPath();
            }
        }

        return filePath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickApkFile();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

