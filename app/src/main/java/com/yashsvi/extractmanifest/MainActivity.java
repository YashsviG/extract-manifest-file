package com.yashsvi.extractmanifest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/*
 * These two imports are the libraries
 * used extract the contents of the XML file.
 */
import org.xmlpull.v1.XmlPullParser;
import brut.androlib.res.decoder.AXmlResourceParser;

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
                /* THIS IF CONDITION DOESN'T WOK ON EMULATOR PROPERLY */
                /* PLEASE COMMENT THIS TO TEST ON EMULATOR */

                /* Gets and checks for permission to read external storage */
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSION);
                } else {
                    pickApkFile();
                }
            }
        });
    }

    /**
     * Picks the APK file from the file explorer
     */
    private void pickApkFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.android.package-archive");
        startActivityForResult(intent, REQUEST_CODE_PICK_APK);
    }

    /**
     * Runs once the pickApkFile is completed
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_APK && resultCode == RESULT_OK && data != null) {
            Uri apkUri = data.getData();

            /* if apkUri has data in it, calls the extractManifest function */
            if (apkUri != null) {
                /* This is the actual file path */
                String filePath = PathUtil.getPath(apkUri, this.getApplicationContext());
                try {
                    extractManifestXml(apkUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * This utilizes apktool library to make the extracted manifest Content readable
     */
    private String extractManifestXml(Uri uri) throws IOException {
        StringBuilder manifestXml = new StringBuilder();
        InputStream inputStream = getContentResolver().openInputStream(uri);
        try {
            /*
            Using the apktool AXMLResourceParser to parse
            through the content of the AndroidManifest.xml
             */
            AXmlResourceParser parser = new AXmlResourceParser();
            parser.open(inputStream);

            while (true) {
                int eventType = parser.next();
                if (eventType == XmlPullParser.END_DOCUMENT) {
                    break;
                }

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tagName = parser.getName();
                        manifestXml.append("<").append(tagName);

                        int namespaceCount = parser.getNamespaceCount(parser.getDepth());
                        for (int i = 0; i < namespaceCount; i++) {
                            String namespacePrefix = parser.getNamespacePrefix(i);
                            String namespaceUri = parser.getNamespaceUri(i);
                            manifestXml.append(" xmlns");
                            if (namespacePrefix != null && !namespacePrefix.isEmpty()) {
                                manifestXml.append(":").append(namespacePrefix);
                            }
                            manifestXml.append("=\"").append(namespaceUri).append("\"");
                        }

                        int attributeCount = parser.getAttributeCount();
                        for (int i = 0; i < attributeCount; i++) {
                            String attributeName = parser.getAttributeName(i);
                            String attributeValue = parser.getAttributeValue(i);
                            manifestXml.append(" ").append(attributeName).append("=\"").append(attributeValue).append("\"");
                        }

                        manifestXml.append(">");
                        break;
                    case XmlPullParser.END_TAG:
                        manifestXml.append("</").append(parser.getName()).append(">");
                        break;
                    case XmlPullParser.TEXT:
                        manifestXml.append(parser.getText());
                        break;
                }
            }

            parser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        Saving the content now in the manifest.xml
        file in the Downloads directory
        */
        String fileName = "manifest.xml";
        File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outputFile = new File(outputDir, fileName);

        FileWriter fileWriter = new FileWriter(outputFile);
        fileWriter.write(manifestXml.toString());
        fileWriter.close();

        /* Display a Toast indicating the file creation status */
        if (outputFile.exists()) {
            showToast("File created successfully!");
        } else {
            showToast("File creation failed.");
        }

        return manifestXml.toString();
    }

    /**
     * Displays the toast based on the file creation status
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the permissions are set by the user, i.e. allowed or denied
     */
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

