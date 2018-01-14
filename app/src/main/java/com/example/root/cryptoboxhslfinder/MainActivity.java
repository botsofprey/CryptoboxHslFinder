package com.example.root.cryptoboxhslfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    SeekBar HueMin, HueMax, Saturation, Lightness;
    TextView HueMinValue, HueMaxValue, SaturationValue, LightnessValue;
    Button takePic;
    ImageView image;
    Bitmap bmpSrc = null;
    Bitmap bmp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HueMinValue = (TextView) findViewById(R.id.HueMinValue);
        HueMaxValue = (TextView) findViewById(R.id.HueMaxValue);
        SaturationValue = (TextView) findViewById(R.id.SaturationValue);
        LightnessValue = (TextView) findViewById(R.id.LightnessValue);

        takePic = (Button)findViewById(R.id.takePic);

        takePic.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                //take the pic
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                //intent.setDataAndType(Uri.fromFile(new File(mediaFile.filePath)), mediaFile.getExtension());
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
        HueMin = (SeekBar) findViewById(R.id.HueMin);
        HueMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                HueMinValue.setText("" + progress);
                findCryptobox();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        HueMax = (SeekBar) findViewById(R.id.HueMax);
        HueMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                HueMaxValue.setText("" + progress);
                findCryptobox();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        Saturation = (SeekBar) findViewById(R.id.Saturation);
        Saturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SaturationValue.setText("" + progress);
                findCryptobox();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Lightness = (SeekBar) findViewById(R.id.Lightness);
        Lightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                LightnessValue.setText("" + progress);
                findCryptobox();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        image = (ImageView)findViewById(R.id.imageView);
        bmp = loadImage();
        findCryptobox();

    }

    public Bitmap scaleImage(Bitmap bmp){
        Bitmap myB;
        myB = Bitmap.createScaledBitmap(bmp, 171, 224, true);
        myB = myB.copy(Bitmap.Config.ARGB_8888, true);
        return myB;
    }

    public Bitmap loadImage(){
        if(bmpSrc == null) {
            Bitmap b = null;
            try {
                b = BitmapFactory.decodeStream(getAssets().open("sample2.jpg"));
                Log.d("Den", " " + b.getDensity());
                Log.d("Cur Dim", "H:" + b.getHeight() + " W:" + b.getWidth());
                //bmp.setDensity(100);

            } catch (IOException e) {
                e.printStackTrace();
            }
           bmpSrc = scaleImage(b);
        }
        return bmpSrc.copy(Bitmap.Config.ARGB_8888,true);

    }

    public void findCryptobox(){
        bmp = loadImage();
        //bmp.reconfigure(224,171, Bitmap.Config.ARGB_8888);
        Log.d("New Dim", "H:" + bmp.getHeight() + " W:" + bmp.getWidth());
        long start = System.currentTimeMillis();
        //manipulateImage();
        findColumns();
        Log.d("Time taken", " " + (System.currentTimeMillis() - start));
        image.setImageBitmap(bmp);
    }

    final double minimumColumnColorFound = .1;
    final double minimumColumnWidths = 1;

    public void findColumns(){
        int width = bmp.getWidth(), height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        double [] blueFrequencyByColumn = collapseVerticallyByBluePercent(pixels,width,height);
        ArrayList<Integer> interestingColumns = new ArrayList<Integer>();
        //look for columns that have the minimum required % of a color
        for(int i = 0; i < blueFrequencyByColumn.length; i++){
            if(blueFrequencyByColumn[i] >= minimumColumnColorFound){
                interestingColumns.add(Integer.valueOf(i));
            }
        }


        //find bounds of the colors
        if(interestingColumns.size() > 0) {
            ArrayList<Integer> columnBounds = new ArrayList<Integer>();
            columnBounds.add(interestingColumns.get(0));
            for (int i = 1; i < interestingColumns.size(); i++) {
                if (i != 1) columnBounds.add(interestingColumns.get(i));
                while (i < interestingColumns.size() && interestingColumns.get(i).intValue() == interestingColumns.get(i - 1).intValue() + 1) {
                    i++;
                }
                columnBounds.add(interestingColumns.get(i - 1));
            }
            //look for columns to be at least ___ columns wide
            for (int i = 0; i < columnBounds.size() - 1; i += 2) {
                if (columnBounds.get(i + 1).intValue() - columnBounds.get(i).intValue() >= minimumColumnWidths) {
                } else {
                    columnBounds.remove(i);
                    columnBounds.remove(i);
                    i -= 2;
                }
            }
            for (int i = 0; i < columnBounds.size(); i++) {
                Log.d("Bound", columnBounds.get(i).toString());
            }

            //get average column locations
            ArrayList<Integer> columnCenters = new ArrayList<Integer>();
            for (int i = 0; i < columnBounds.size() / 2; i++) {
                columnCenters.add((int) (columnBounds.get(i * 2).intValue() + (columnBounds.get(i * 2 + 1).intValue() - columnBounds.get(i * 2).intValue()) / 2.0 + .5));
            }

            for (int i = 0; i < columnCenters.size(); i++) {
                Log.d("Centers", columnCenters.get(i).toString());
            }
            Log.d("# of Columns", "" + columnCenters.size());
            for (int i = 0; i < columnCenters.size(); i++) {
                for (int r = 0; r < height; r++) {
                    pixels[r * width + columnCenters.get(i).intValue()] = Color.RED;
                }
            }
        }
        else Log.d("# of Columns","None Found!");
        for (int c = 0; c < width; c++) {
            int numberOfBluePixels = 0;
            for (int r = 0; r < height; r++) {
                int color = pixels[r * width + c];
                int[] rgba = {Color.red(color), Color.blue(color), Color.green(color), Color.alpha(color)};
                float[] hsv = new float[3];
                Color.colorToHSV(color, hsv);
                //check for blue
                if (checkIfBlue(hsv)) {
                    rgba[0] = 0;
                    rgba[1] = 250;
                    rgba[2] = 250;
                    pixels[r * width + c] = Color.argb(rgba[3], rgba[0], rgba[1], rgba[2]);
                    numberOfBluePixels++;
                }
            }
        }
        bmp.setPixels(pixels,0,width,0,0,width,height);

        /*
        //algortithm to find local maximums
        //first collapse into smaller sections, ~50 boxes
        //int [] smallerArray = collapseIntoSmallerArray(blueFrequencyByColumn, 50);
        //go through every index. if the current index has more than x frequency of pixels,
        int [][] columnIndexes = new int [smallerArray.length/2][2]; //start, end
        int curColumnIndex = 0;
        for(int i = 0; i < smallerArray.length; i ++){
            //check if the start of a column
            if(smallerArray[i] > 30){
                columnIndexes[curColumnIndex][0] = i;
                while(i < smallerArray.length & smallerArray[i+1] > 30){
                    i ++;
                }
                columnIndexes[curColumnIndex][1] = i;
                curColumnIndex ++;
            }
        }
        //find the average indexes of the columns
        int [] columnCenters = new int[curColumnIndex --];
        for(int i = 0; i < curColumnIndex; i ++){
            columnCenters[i] = (int)((columnIndexes[i][0] + columnIndexes[i][1])/2.0 + .5);
        }
        //convert to our actual picture's column locations
        int [] actualCenters = new int[columnCenters.length];
        for(int i = 0; i < columnCenters.length; i ++){
            actualCenters[i] = columnCenters[i] * width/50;
        }
        //edit the picture to have a column of red at the spot
        for(int i = 0; i < actualCenters.length; i ++){
            for(int r = 0; r < height; r ++){
                pixels[r*width + actualCenters[i]] = Color.RED;
            }
        }
        bmp.setPixels(pixels,0,width,0,0,width,height);
        */
    }

    public int [] collapseIntoSmallerArray(int [] orig, int numBoxesWanted){
        int [] averagedArray = new int [numBoxesWanted];
        //initialize to 0
        for(int i = 0; i < numBoxesWanted; i ++){
            averagedArray[i] = 0;
        }
        for(int i = 1; i < numBoxesWanted; i ++){
            int indexToGoTo = (int)((double)orig.length/numBoxesWanted * i);
            int indexToStartAt = (int)((double)orig.length/numBoxesWanted * (i - 1));
            for(int p = indexToStartAt; p < indexToGoTo; p ++){
                averagedArray[i] += orig[p];
            }
            //average into int
            averagedArray[i] = (int)(averagedArray[i]/(indexToGoTo - indexToStartAt) + .5);
        }
        return averagedArray;
    }

    public void findCryptoboxColumns(){
        int width = bmp.getWidth(), height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
    }

    public void manipulateImage() {
        int width = bmp.getWidth(), height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] columnsWithBlue = new int[width];
        int numberOfColumnsWithBlue = 0;
        for (int c = 0; c < width; c++) {
            int numberOfBluePixels = 0;
            for (int r = 0; r < height; r++) {
                int color = pixels[r * width + c];
                int[] rgba = {Color.red(color), Color.blue(color), Color.green(color), Color.alpha(color)};
                float[] hsv = new float[3];
                Color.colorToHSV(color, hsv);
                //check for blue
                if(checkIfBlue(hsv)){
                    rgba[0] = 0;
                    rgba[1] = 250;
                    rgba[2] = 250;
                    pixels[r * width + c] = Color.argb(rgba[3], rgba[0], rgba[1], rgba[2]);
                    numberOfBluePixels++;
                }
            }
            if (numberOfBluePixels / (double) height > .3) {
                columnsWithBlue[numberOfColumnsWithBlue] = c;
                numberOfColumnsWithBlue++;
            }
        }
        Log.d("Blue Columns", " " + numberOfColumnsWithBlue);
        //go through and find the number of columns
        int [][] columnBounds = new int [numberOfColumnsWithBlue][2];
        columnBounds[0][0] = columnsWithBlue[0];
        int numberOfCryptoboxColumns = 1;
        for(int i = 0; i < numberOfColumnsWithBlue - 1; i ++) {
            if(columnsWithBlue[i + 1] - columnsWithBlue[i] > width/100.0 * 5) {
                columnBounds[numberOfCryptoboxColumns - 1][1] = columnsWithBlue[i];
                columnBounds[numberOfCryptoboxColumns][0] = columnsWithBlue[i + 1];
                numberOfCryptoboxColumns ++;
            }
        }
        columnBounds[numberOfCryptoboxColumns-1][1] = columnsWithBlue[numberOfColumnsWithBlue - 1];

        for(int i = 0; i < numberOfCryptoboxColumns; i ++){
            Log.d("Start Col:" + i, " " + columnBounds[i][0]);
            Log.d("End Col:" + i, " " + columnBounds[i][1]);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    public double [] collapseVerticallyByBluePercent(int [] pixels, int imageWidth, int imageHeight){
        //collapse into a single, frequency based
        double [] toReturn = new double[imageWidth];
        for(int c = 0; c < imageWidth; c ++){
            for(int r = 0; r < imageHeight; r ++){
                int color = pixels[r*imageWidth + c];
                if(checkIfBlue(color)){
                    toReturn[c] ++;
                }
            }
            toReturn[c] = toReturn[c]/imageHeight;
        }
        return toReturn;
    }

    public int [] collapseVerticallyAverageRGB(int [] pixels, int imageWidth, int imageHeight){
        //collapse into a single, rgba
        int [][] toReturn = new int[imageWidth][4];
        for(int i = 0; i < imageWidth; i ++){
            toReturn[i][0] = Color.red(pixels[i]);
            toReturn[i][1] = Color.blue(pixels[i]);
            toReturn[i][2] = Color.green(pixels[i]);
            toReturn[i][3] = Color.alpha(pixels[i]);
        }
        for(int c = 0; c < imageWidth; c ++){
            for(int r = 0; r < imageHeight; r ++){
                toReturn[c][0] = (int)((toReturn[c][0] + Color.red(pixels[c + r*imageWidth])) / 2.0 + .5);
                toReturn[c][1] = (int)((toReturn[c][1] + Color.blue(pixels[c + r*imageWidth])) / 2.0 + .5);
                toReturn[c][2] = (int)((toReturn[c][2] + Color.green(pixels[c + r*imageWidth])) / 2.0 + .5);
                toReturn[c][3] = (int)((toReturn[c][3] + Color.alpha(pixels[c + r*imageWidth])) / 2.0 + .5);
            }
        }
        int[] colors = new int[imageWidth];
        for(int i = 0; i < imageWidth; i ++){
            colors[i] = Color.argb(toReturn[i][3],toReturn[i][0],toReturn[i][1],toReturn[i][2]);
        }
        return colors;
    }

    public boolean checkIfBlue(float [] hsl){
        if (hsl[0] > HueMin.getProgress() && hsl[0] < HueMax.getProgress() || (HueMax.getProgress() < HueMin.getProgress()) && hsl[0] < HueMin.getProgress() || hsl[0] > HueMax.getProgress()) {
            //make sure it's not a white blue
            if (hsl[1] > Saturation.getProgress()/100.0) {
                //make sure it's not a black blue
                if (hsl[2] > Lightness.getProgress()/100.0) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean checkIfBlue(int color){
        float [] hsl = new float[3];
        Color.colorToHSV(color,hsl);
        return checkIfBlue(hsl);
    }


    public static final int PICK_IMAGE = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                Matrix matrix = new Matrix();

                matrix.postRotate(90);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(selectedImage,selectedImage.getWidth(),selectedImage.getHeight(),true);

                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
                bmpSrc = scaleImage(rotatedBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }
        }
    }
}


