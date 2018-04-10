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

    private static final double SMILING_PROB_THRESHOLD = .15;
    private static final double EYE_OPEN_PROB_THRESHOLD = .5;

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
        } else {
            //Iterate through the faces
            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);
                //Get the appropriate emoji for each face
                whichEmoji(face);
            }
        }

        //Release the detector
        detector.release();
    }

    /**
     * Determines the closest emoji to the expression on the face, based on the
     * odds that the person is smiling and has each eye open.
     *
     * @param face The face for which you pick an emoji.
     */

    private static void whichEmoji(Face face) {
        //Log all the probabilities
        Log.d(TAG, "whichEmoji: smilingProb = "
                + face.getIsSmilingProbability());
        Log.d(TAG, "whichEmoji: leftEyeOpenProb = "
                + face.getIsLeftEyeOpenProbability());
        Log.d(TAG, "whichEmoji: rightEyeOpenProb = "
                + face.getIsRightEyeOpenProbability());

        boolean smiling = face.getIsSmilingProbability() > SMILING_PROB_THRESHOLD;
        boolean leftEyeClosed = face.getIsLeftEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;
        boolean rightEyeClosed = face.getIsRightEyeOpenProbability() > EYE_OPEN_PROB_THRESHOLD;

        //Determine and log the appropriate emoji
        Emoji emoji;
        if (smiling) {
            if (!leftEyeClosed && !rightEyeClosed) {
                emoji = Emoji.SMILE;
            } else if (leftEyeClosed && rightEyeClosed) {
                emoji = Emoji.CLOSED_EYED_SMILE;
            } else if (!leftEyeClosed) {
                emoji = Emoji.RIGHT_WINK;
            } else {
                emoji = Emoji.LEFT_WINK;
            }
        } else {
            if (!leftEyeClosed && !rightEyeClosed) {
                emoji = Emoji.FROWN;
            } else if (leftEyeClosed && rightEyeClosed) {
                emoji = Emoji.CLOSED_EYE_FROWN;
            } else if (!leftEyeClosed) {
                emoji = Emoji.RIGHT_WINK_FROWN;
            } else {
                emoji = Emoji.LEFT_WINK_FROWN;
            }
        }

        //Log the chosen emoji
        Log.d(TAG, "whichEmoji: " + emoji.name());
    }

    //Enum for all possible emojis
    private enum Emoji {
        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYED_SMILE,
        CLOSED_EYE_FROWN
    }
}
