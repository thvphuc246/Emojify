package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Emojifier {
    public static final String TAG = Emojifier.class.getSimpleName();

    public Emojifier() {

    }

    static void detectFaces(Context context, Bitmap img) {
        //Create the face detector
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        //Build the frame
        Frame frame = new Frame.Builder().setBitmap(img).build();

        //Detect the faces
        SparseArray<Face> faces = detector.detect(frame);

        //Log the number of faces
        Log.d(TAG, "detectFaces: number of faces = " + faces.size());

        //Toast a message if no faces are detected
        if (faces.size() == 0) {
            Toast.makeText(context, R.string.no_faces_message, Toast.LENGTH_SHORT).show();
        }

        //Release the detector
        detector.release();
    }
}
