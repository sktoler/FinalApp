/*
 * Copyright 2015-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.demo.s3transferutility;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import com.amazonaws.mobile.auth.core.IdentityManager;

/*
 * This is the beginning screen that lets the user select if they want to upload or download
 */
public class MainActivity extends Activity {

    public void logout() {
        Button button2;
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent b = new Intent(MainActivity.this, AuthenticatorActivity.class);

                IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();
                identityManager.signOut();

                startActivity(b);
            }
        });
    }

//    public void graph() {
//        Button button3;
//        button3 = (Button) findViewById(R.id.button3);
//        button3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e("TAG", "HERE!");
//                Intent b1 = new Intent(MainActivity.this, GraphActivity.class);
//                startActivity(b1);
//
//            }
//        });
//    }

    private Button btnDownload;
    //private Button button2;
    private CalendarView calendarView;
    //private String selectedDate;


    public void initUI() {
        calendarView = (CalendarView) findViewById(R.id.simpleCalendarView);
        btnDownload = (Button) findViewById(R.id.buttonDownloadMain);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                int m = month+1;

                Log.e("TAG", "DAY: " + dayOfMonth);
                Log.e("TAG", "MONTH: " + 0+m);
                Log.e("TAG", "YEAR: " + year);

                final String selectedDate = "" + dayOfMonth+ 0+m + year;

                btnDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        Log.e("TAG", "DATE STRING: " + selectedDate);
                        Intent intent = new Intent(MainActivity.this, DownloadSelectionActivity.class);
                        intent.putExtra("SELECTED_DATE", selectedDate);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        logout();
        //graph();
    }

}
