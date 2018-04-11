package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Emojifier {
    public static final String TAG = Emojifier.class.getSimpleName();

    private static final float EMOJI_SCALE_FACTOR = .9f;
    private static final double SMILING_PROB_THRESHOLD = .15;
    private static final double EYE_OPEN_PROB_THRESHOLD = .5;

    public Emojifier() {

    }

    static Bitmap detectFacesAndOverlayEmoji(Context context, Bitmap img) {
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

        //The result bitmap to original picture
        Bitmap resultBitmap = img;

        //Toast a message if no faces are detected
        if (faces.size() == 0) {
            Toast.makeText(context, R.string.no_faces_message, Toast.LENGTH_SHORT).show();
        } else {
            //Iterate through the faces
            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);

                Bitmap emojiBitmap;
                switch (whichEmoji(face)) {
                    case SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.smile);
                        break;
                    case FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.frown);
                        break;
                    case LEFT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.leftwink);
                        break;
                    case RIGHT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.rightwink);
                        break;
                    case LEFT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.rightwinkfrown);
                        break;
                    case CLOSED_EYED_SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.closed_smile);
                        break;
                    case CLOSED_EYE_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.closed_frown);
                        break;
                    default:
                        emojiBitmap = null;
                        Toast.makeText(context, R.string.no_emoji, Toast.LENGTH_SHORT).show();
                }

                //Add the emojiBitmap to the proper position in the original image
                resultBitmap = addBitmapToFace(resultBitmap, emojiBitmap, face);
            }
        }

        //Release the detector
        detector.release();

        return resultBitmap;
    }

    /**
     * Determines the closest emoji to the expression on the face, based on the
     * odds that the person is smiling and has each eye open.
     *
     * @param face The face for which you pick an emoji.
     */

    private static Emoji whichEmoji(Face face) {
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

        return emoji;
    }

    /**
    * Combines the original picture with the emoji bitmaps
    *
    * @param backgroundBitmap The original picture
    * @param emojiBitmap      The chosen emoji
    * @param face             The detected face
    * @return The final bitmap, including the emojis over the faces
    */
    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {
        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(
                backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(),
                backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight()
                * newEmojiWidth / emojiBitmap.getWidth()
                * scaleFactor);

        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX = (face.getPosition().x + face.getWidth() / 2)
                - emojiBitmap.getWidth() / 2;
        float emojiPositionY = (face.getPosition().y + face.getHeight() / 2)
                - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
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
