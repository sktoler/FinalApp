package com.amazonaws.demo.s3transferutility;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * DownloadSelectionActivity displays a list of files in the bucket. Users can
 * select a file to download.
 */
public class DownloadSelectionActivity extends ListActivity {

    // The S3 client used for getting the list of objects in the bucket
    private AmazonS3Client s3;
    // An adapter to show the objects
    private SimpleAdapter simpleAdapter;

    private ArrayList<HashMap<String, Object>> transferRecordMaps;
    private List<TransferObserver> observer;

    private Util util;
    private String selectedDate;

    private TransferUtility transferUtility;

    TextView txtFile;
    ImageView imageView;
    //private ArrayList<String> fileNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_selection);
        txtFile = (TextView) findViewById(R.id.txtFile);
        imageView = (ImageView) findViewById(R.id.imageView);

        util = new Util();
        transferUtility = util.getTransferUtility(this);

        selectedDate = getIntent().getStringExtra("SELECTED_DATE");

        initData();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the file list.
        new GetFileListTask().execute();
    }

    private void initData() {
        // Gets the default S3 client.
        s3 = util.getS3Client(DownloadSelectionActivity.this);
        transferRecordMaps = new ArrayList<HashMap<String, Object>>();
    }

    private void initUI() {
        simpleAdapter = new SimpleAdapter(this, transferRecordMaps,
                R.layout.bucket_item, new String[]{
                "key"
        },
                new int[]{
                        R.id.key
                });
        simpleAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                switch (view.getId()) {
                    case R.id.key:
                        TextView fileName = (TextView) view;
                        fileName.setText((String) data);
                        return true;
                }
                return false;
            }
        });
        setListAdapter(simpleAdapter);

        // When an item is selected, finish the activity and pass back the S3
        // key associated with the object selected
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {


//                Intent intent = new Intent();
//                intent.putExtra("key", (String) transferRecordMaps.get(pos).get("key"));
//                setResult(RESULT_OK, intent);
//                finish();

                String fileInformation = (String) transferRecordMaps.get(pos).get("key");
//                Log.e("TAG", "TEST3");
                String extension = fileInformation.substring(fileInformation.lastIndexOf(".") + 1, fileInformation.lastIndexOf(".") + 4);

                Log.e("TAG", fileInformation);
                Log.e("TAG", extension);

                if (extension.equals("jpg")) {

                    String fileDirectory = Environment.getExternalStorageDirectory().toString() + "/" + fileInformation;
                    File imgFile = new File(fileDirectory);
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                }

                if (extension.equals("txt")) {

                    String fileDirectory = Environment.getExternalStorageDirectory().toString() + "/" + fileInformation;

                    Log.d("TAG", "FILE DIRECTORY: " + fileDirectory);

                    String text = "";
                    try {
                        File fl = new File(fileDirectory);
                        FileInputStream fin = new FileInputStream(fl);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        reader.close();
                        text = sb.toString();

                        ArrayList<String> temperatureList = new ArrayList<String>();
                        SharedPreferences mSharedPreference1 = getApplicationContext().getSharedPreferences("temperature", MODE_PRIVATE);
                        temperatureList.clear();
                        int size = mSharedPreference1.getInt("Status_size", 0);
                        for (int i = 0; i < size; i++) {
                            temperatureList.add(mSharedPreference1.getString("Status_" + i, null));
//                            Log.e("TAG", "GET Temperature");
//                            Log.e("TAG", temperatureList.get(i));
                        }

                        //INSERT GRAPH STUFF HERE

                    } catch (Exception e) {
                        Log.e("er0r", e.toString());
                    }


                    Log.e("TEXT IS: ", text);

                    txtFile.setText(text);
                    Toast.makeText(getApplicationContext(), "The file read operation is finished successfully.",
                            Toast.LENGTH_SHORT).show();
                    txtFile.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * This async task queries S3 for all files in the given bucket so that they
     * can be displayed on the screen
     */
    private class GetFileListTask extends AsyncTask<Void, Void, Void> {
        // The list of objects we find in the S3 bucket
        private List<S3ObjectSummary> s3ObjList;
        // A dialog to let the user know we are retrieving the files
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(DownloadSelectionActivity.this,
                    getString(R.string.refreshing),
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... inputs) {
            // Queries files in the bucket from S3.
            s3ObjList = s3.listObjects(Constants.BUCKET_NAME).getObjectSummaries();
            transferRecordMaps.clear();
            for (S3ObjectSummary summary : s3ObjList) {
                String fileName = summary.getKey();
                String fileDate = fileName.substring(fileName.lastIndexOf('_') + 1, fileName.lastIndexOf('.'));
                if (fileDate.equals(selectedDate)) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("key", summary.getKey());
                    transferRecordMaps.add(map);


                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + fileName);
                    // Initiate the download
                    TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, fileName, file);
//                    TransferListener listener = new DownloadSelectionActivity.DownloadListener();

                    Log.e("TAG", "TOLER: " + fileName);

//                    observer.setTransferListener(listener);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            simpleAdapter.notifyDataSetChanged();
        }
    }


//    }
    /*
 * A TransferListener class that can listen to a download task and be
 * notified when the status changes.
 */
    private class DownloadListener implements TransferListener {
        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            if (state == TransferState.COMPLETED) {
                Log.e("TAG", "STATE: " + state.toString());
                Log.e("TAG", "ID: " + id);
            }
        }
//            if (state == TransferState.COMPLETED) {
//                Log.e("TAG", "ID1 IS: " + id);
//
//
//                String fileInformation = get(id).getKey();
//                Log.e("TAG", "TEST3");
//                String extension = fileInformation.substring(fileInformation.lastIndexOf(".") + 1, fileInformation.lastIndexOf(".") + 4);
//
//                Log.e("TAG", fileInformation);
//                Log.e("TAG", extension);
//
//                if (extension.equals("txt")) {
//
//                    String fileDirectory = Environment.getExternalStorageDirectory().toString() + "/" + fileInformation;
//
//                    Log.d("TAG", "FILE DIRECTORY: " + fileDirectory);
//
//                    String text = "";
//                    try {
//                        File fl = new File(fileDirectory);
//                        FileInputStream fin = new FileInputStream(fl);
//
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
//                        StringBuilder sb = new StringBuilder();
//                        String line = null;
//                        while ((line = reader.readLine()) != null) {
//                            sb.append(line).append("\n");
//                        }
//                        reader.close();
//                        text = sb.toString();
//
//                        ArrayList<String> temperatureList = new ArrayList<String>();
//                        SharedPreferences mSharedPreference1 = getApplicationContext().getSharedPreferences("temperature", MODE_PRIVATE);
//                        temperatureList.clear();
//                        int size = mSharedPreference1.getInt("Status_size", 0);
//                        for (int i = 0; i < size; i++) {
//                            temperatureList.add(mSharedPreference1.getString("Status_" + i, null));
//                            Log.e("TAG", "GET Temperature");
//                            Log.e("TAG", temperatureList.get(i));
//                        }
//
//                        //INSERT GRAPH STUFF HERE
//
//                    } catch (Exception e) {
//                        Log.e("er0r", e.toString());
//                    }
//
//
//                    Log.d("TAG", text);
//
//                    txtFile.setText(text);
//                    Toast.makeText(getApplicationContext(), "The file read operation is finished successfully.",
//                            Toast.LENGTH_SHORT).show();
//                    txtFile.setVisibility(View.VISIBLE);
//
//
//                }
//            }
//        }
    }

}

//                        String lastReading = "";
//                        if (temperatureList.size() > 0) {
//                            lastReading = temperatureList.get(temperatureList.size() - 1);
//
//                            text = text.trim();
//                            lastReading = lastReading.trim();
//                        }
//
//                        if (!text.equals(lastReading)) {
//                            temperatureList.add(text); //this adds an element to the list.
//
//                            SharedPreferences sp = getApplicationContext().getSharedPreferences("temperature", MODE_PRIVATE);
//                            SharedPreferences.Editor mEdit1 = sp.edit();
//
//                            mEdit1.putInt("Status_size", temperatureList.size());
//
//                            for (int i = 0; i < temperatureList.size(); i++) {
//                                mEdit1.remove("Status_" + i);
//                                mEdit1.putString("Status_" + i, temperatureList.get(i));
//                            }
//
//                            mEdit1.commit();
//                        }
//
//                        Log.e("TAG", "Something");

//                        int firstIndex = Math.max(0, temperatureList.size() - 7);
//                        List<String> lastReadings = temperatureList.subList(firstIndex, temperatureList.size());
//
//                        DataPoint[] temperatureValues = new DataPoint[lastReadings.size()];
//                        for (int i = 0; i < lastReadings.size(); i++) {
//                            String tempString = lastReadings.get(i);
//
//                            Float f = Float.valueOf(tempString.substring(
//                                    tempString.lastIndexOf('=') + 1,
//                                    tempString.lastIndexOf('*')));
//                            Log.e("TAG", "Getting DataPoint");
//                            Log.e("TAG", f.toString());
//
//                            temperatureValues[i] = new DataPoint(i, f);
//                        }
//
//                        GraphView graph = (GraphView) findViewById(R.id.graph);
//                        graph.removeAllSeries();
//                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(temperatureValues);
//                        graph.addSeries(series);