package comma.khutimetablehelper;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;


public class MadeResultActivity extends AppCompatActivity {

    //시간표색상
    int[] colors = {Color.parseColor("#B8F3B8"),Color.parseColor("#FFA9B0"),Color.parseColor("#CCD1FF"),Color.parseColor("#FFDDA6"),Color.parseColor("#FFADC5")};
    int colorIndex = 0;

    ArrayList<Subject> needSubject;
    ArrayList<Subject> subSubject;
    ArrayList<Integer> spinStatus;
    ArrayList<Integer> spinValue;
    ArrayList<Subject> filteredSubSubject;
    boolean filteringNumber = false;

    ArrayList<ArrayList<Subject>> listData = new ArrayList<ArrayList<Subject>>();
    public TextView timeTable[][] = new TextView[140][5]; //시간표 각 칸
    int focusOn; // 저장될 시간표의 위치
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maderesult);

        Button btnSave = (Button) findViewById(R.id.maderesult_btn_save);
        Button btnMain = (Button) findViewById(R.id.maderesult_btn_main);
        setTextId();
        focusOn = -1;

        final ListView listView = (ListView) findViewById(R.id.maderesult_lv_List);
        final CustomLvAdapter adapter = new CustomLvAdapter(listData);
        listView.setAdapter(adapter);

        //저장, 메인버튼
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(focusOn < 0) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MadeResultActivity.this);
                    dialogBuilder.setTitle("저장할 시간표를 선택해주세요").setNegativeButton("취소", null).show();
                }else{
                    ShowSaveDialog(focusOn);
                }
            }
        });
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MadeResultActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // child목록을 클릭했을때 위 시간표에 뜨도록 이벤트설정
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ArrayList<Subject> selectedTimeTable = (ArrayList<Subject>) adapter.getItem(position);

                colorIndex = 0;
                for(int i = 0; i <selectedTimeTable.size();i++) {
                    ShowTimeTable(selectedTimeTable.get(i));
                }
                focusOn = position;
            }
        });

        needSubject = (ArrayList<Subject>) getIntent().getSerializableExtra("NeedSubject");
        subSubject = (ArrayList<Subject>) getIntent().getSerializableExtra("SubSubject");
        spinStatus = (ArrayList<Integer>) getIntent().getSerializableExtra("spinStatus");
        spinValue = (ArrayList<Integer>) getIntent().getSerializableExtra("spinValue");

        filteredSubSubject = subSubject;

        // 필수과목, 시작시간, 점심시간, 공강요일, 강의최대시간, 끝나는시간에 맞지않는과목 걸러내기
        for (int i = 0; i < filteredSubSubject.size(); i++)
        {
            filterStackNeedSubject(subSubject.get(i).cStart, subSubject.get(i).cEnd);
            if(spinStatus.get(1) == 0) {
                filterStartTime(spinValue.get(2), subSubject.get(i).cStart);
            }
            if(spinStatus.get(3) == 0) {
                filterBlankDay(spinValue.get(4), subSubject.get(i).cDay);
            }
            if(spinStatus.get(5) == 0) {
                filterLunchTime(spinValue.get(6), spinValue.get(7), subSubject.get(i).cStart, subSubject.get(i).cEnd);
            }
            if(spinStatus.get(6) == 0) {
                filterMaxLectureTime(subSubject.get(i).cStart, subSubject.get(i).cEnd);
            }
            if(spinStatus.get(9) == 0) {
                filterDayEndTime(subSubject.get(i).cDay, subSubject.get(i).cEnd);
            }
            if(filteringNumber){
                filteredSubSubject.remove(i);
                if(i!=0){
                    i--;
                }
            }
            filteringNumber = false;
        }

        calculateSubject();
        setData();

    }

    private void SaveTimeTable(int position, String timeTableName) {
        AppContext.timeTableList.add(AppContext.tempTimeTableList.get(position));
        AppContext.timeTableNameList.add(timeTableName);

        Log.d("tag", "minyoung good1");

        int[] classes = new int[42];
        classes[0] = 5;
        classes[1] = 150;
        classes[2] = 255;
        String fileName = "테스트용 시간표12.csv";
        UserInputData timeTable = new UserInputData(classes, fileName);
        int i = 0;


        try {
            //BufferedWriter fw = new BufferedWriter(new FileWriter(fileName, true));
            FileWriter fw = new FileWriter(fileName) ;

            Log.d("tag", "minyoung good2");
            while (timeTable.getcClasses()[i] != 0) {
                Log.d("tag", "minyoung good2-while start");
                fw.write("" + timeTable.getcClasses()[i]);
                Log.d("tag", "minyoung good2-while start2");
                if (timeTable.getcClasses()[i + 1] != 0) {
                    Log.d("tag", "minyoung good2-if start1");
                    fw.write(",");
                    Log.d("tag", "minyoung good2-if end");
                }
                i++;
            }
            Log.d("tag", "minyoung good2-while end");
            fw.flush();
            fw.close();

        } catch (Exception e) {
            Log.d("tag", "minyoung good3");
            e.printStackTrace();
        }
    }


    public void ShowTimeTable(Subject selected){
        TextView tv;
        int start = (int) ((selected.cStart - 9.0)*2.0);
        int end = (int)((selected.cEnd - 9.0)*2.0 - 1.0);

        for(int i = end; i >= start;i--){
            tv = timeTable[i][selected.cDay];
            tv.setBackgroundColor(colors[colorIndex]);
        }
        timeTable[start][selected.cDay].setText(selected.cName);
        colorIndex++;
    }

    //시간표 각 칸 변수지정
    private void setTextId() {
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 5; j++) {
                int resID = getResources().getIdentifier("maderesult_tv_" + i + "" + j, "id", "comma.khutimetablehelper");
                timeTable[i][j] = ((TextView) findViewById(resID));
            }
        }
    }

    //확장리스트뷰 목록 초기화
    private void setData() {

        int i = 0;

        // listData-시간표들의 목록
        for(i = 0; i< AppContext.tempTimeTableList.size();i++) {
            listData.add(AppContext.tempTimeTableList.get(i));
        }

    }

    //다이얼로그
    private void ShowSaveDialog(int position) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout saveLayout = (LinearLayout) inflater.inflate(R.layout.maderesult_savedialog, null);

        final EditText TimeTableTitle = (EditText) saveLayout.findViewById(R.id.dialog_edt_title);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("이 시간표를 저장하시겠습니까?").setView(saveLayout).setNegativeButton("취소", null)
                .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(TimeTableTitle.getText().length() == 0){
                            Toast.makeText(MadeResultActivity.this, "시간표이름을 입력해주세요", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(MadeResultActivity.this, TimeTableTitle.getText() + " 이 저장되었습니다", Toast.LENGTH_LONG).show();
                            SaveTimeTable(focusOn, String.valueOf(TimeTableTitle.getText()));
                        }
                    }
                });

        final AlertDialog dialog;
        dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false); //다이얼로그 밖 터치해도 안 꺼지도록
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
    }

    // 필수과목과 시간이 겹치는 후보과목 걸러내기
    public  void filterStackNeedSubject(double subSubjectStartTime, double subSubjectEndTime) {
        for(int i = 0; i < needSubject.size() ; i++){
            if(!(subSubjectStartTime > needSubject.get(i).cEnd || subSubjectEndTime < needSubject.get(i).cStart)){
                filteringNumber = true;
            }
        }
    }

    // 시작시간이 10시, 11시, 12시이전인 후보과목 걸러내기
    public void filterStartTime(int firstTimeSpinnerNum, double subSubjectStartTime){
        switch(firstTimeSpinnerNum) {
            case 1:
                if (subSubjectStartTime < 10) {
                    filteringNumber = true;
                }
                break;
            case 2:
                if (subSubjectStartTime < 11) {
                    filteringNumber = true;
                }
                break;
            case 3:
                if (subSubjectStartTime < 12) {
                    filteringNumber = true;
                }
                break;
        }
    }

    // 점심시간에 해당되는 후보과목 걸러내기
    public void filterLunchTime(int lunchStartTimeSpinnerNum, int lunchEndTimeSpinnerNum, double subSubjectStartTime, double subSubjectEndTime){
        double tmpLunchStartTime = 0.0;
        double tmpLunchEndTime = 0.0;
        switch(lunchStartTimeSpinnerNum) {
            case 0:
                tmpLunchStartTime = 11.5;
                break;
            case 1:
                tmpLunchStartTime = 12.0;
                break;
            case 2:
                tmpLunchStartTime = 12.5;
                break;
            case 3:
                tmpLunchStartTime = 13.0;
                break;
        }
        switch(lunchEndTimeSpinnerNum) {
            case 0:
                tmpLunchStartTime = 12.0;
                break;
            case 1:
                tmpLunchStartTime = 12.5;
                break;
            case 2:
                tmpLunchStartTime = 13.0;
                break;
            case 3:
                tmpLunchStartTime = 13.5;
                break;
            case 4:
                tmpLunchStartTime = 14.0;
                break;
        }
        if(!(subSubjectEndTime < tmpLunchStartTime || subSubjectStartTime > tmpLunchEndTime))
        {
            filteringNumber = true;
        }
    }

    // 공강요일 설정된 과목 걸러내기
    public void filterBlankDay(int daySpinnerNum, int subSubjectDay) {
        if(daySpinnerNum != subSubjectDay){
            filteringNumber = true;
        }
    }

    // 요일별 끝나는시간 걸러내기

    public void filterDayEndTime(int subSubjectDay, double subSubjectEndTime){ //첫번째 : subsubject의 요일

        switch (subSubjectDay) {
            case 0:
                if(subSubjectEndTime > spinValue.get(11) + 3) {
                    filteringNumber = true;
                }
                break;
            case 1:
                if(subSubjectEndTime > spinValue.get(12) + 3) {
                    filteringNumber = true;
                }
                break;
            case 2:
                if(subSubjectEndTime > spinValue.get(13) + 3) {
                    filteringNumber = true;
                }
                break;
            case 3:
                if(subSubjectEndTime > spinValue.get(14) + 3) {
                    filteringNumber = true;
                }
                break;
            case 4:
                if(subSubjectEndTime > spinValue.get(15) + 3) {
                    filteringNumber = true;
                }
                break;
        }
    }

    public void filterMaxLectureTime(double subSubjectStartTime, double subSubjectEndTime){
        double calculatedTime = subSubjectEndTime - subSubjectStartTime;
        switch (spinValue.get(8)) {
            case 0:
                if(calculatedTime > 1.5){
                    filteringNumber = true;
                }
                break;
            case 1:
                if(calculatedTime > 3.0){
                    filteringNumber = true;
                }
                break;
            case 2:
                if(calculatedTime > 4.5){
                    filteringNumber = true;
                }
                break;
        }
    }

    public void calculateSubject() {
        int[][] SubjectCell = new int[26][5];
        int[][] needSubjectCell = SubjectCell;
        int[][] Code = new int[10][21];

        ArrayList<Subject> tmpSubSubject = new ArrayList<>();
        ArrayList<ArrayList<Subject>> getUsedSubSubject = new ArrayList<ArrayList<Subject>>();
        ArrayList<Subject> OKSubSubject = new ArrayList<>();

        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 5; j++) {
                SubjectCell[i][j] = 0;
            }
        }
        boolean blankcheck = true;
        int getSettedMinCreditCount = spinValue.get(0) + 15;
        int getSettedMaxCreditCount = spinValue.get(1) + 15;
        int nowCreditCount = 0;
        int monDayClassCount = 0;
        int tuesDayClassCount = 0;
        int wednesDayClassCount = 0;
        int thursDayClassCount = 0;
        int friDayClassCount = 0;
        int dayCount;

        //먼저 필수과목 일단 담기

        double needSubjectStartTime;
        double needSubjectEndTime;


        for(int i = 0; i < needSubject.size() ; i++) {
            needSubjectStartTime = needSubject.get(i).cStart-9;
            needSubjectEndTime = needSubject.get(i).cEnd-9;

            for(int j = 0; j < (int) ((needSubjectEndTime - needSubjectStartTime)*2) ; j++){ // (spinValue.get(3)+1)*(1 - spinStatus.get(2))) 공강크기
                needSubjectCell[(int)needSubjectStartTime+j][needSubject.get(i).cDay] = needSubject.get(i).cRow;
            }
            switch (needSubject.get(i).cDay){
                case 0:
                    monDayClassCount++;
                    break;
                case 1:
                    tuesDayClassCount++;
                    break;
                case 2:
                    wednesDayClassCount++;
                    break;
                case 3:
                    thursDayClassCount++;
                    break;
                case 4:
                    friDayClassCount++;
                    break;
            }
            nowCreditCount = nowCreditCount + needSubject.get(i).cCredit;
        }
        int[] pluralChecker = new int[filteredSubSubject.size()];
        int[] pluralCount = new int[filteredSubSubject.size()];
        int[] triplePluralChecker = new int[filteredSubSubject.size()];

        for(int i = 0; i < filteredSubSubject.size() ; i++) {
            tmpSubSubject = filteredSubSubject;
            SubjectCell = needSubjectCell;
            monDayClassCount = 0;
            tuesDayClassCount = 0;
            wednesDayClassCount = 0;
            thursDayClassCount = 0;
            friDayClassCount = 0;

            for(int j = 0; j < tmpSubSubject.size() ; j++){
                pluralChecker[j] = 0;
                pluralCount[j] = 0;
                triplePluralChecker[j] = 0;
            }
            for(int k = 0; k < tmpSubSubject.size() - 1; k++){
                for(int l = k+1; l < tmpSubSubject.size(); l++) {
                    if(pluralCount[k] == 0) {
                        if (tmpSubSubject.get(k).cNum.equals(tmpSubSubject.get(l).cNum)) {
                            pluralChecker[k] = l;
                            pluralCount[k]++;
                            pluralCount[l] = 3;     // 이미 다른곳에서 중복된걸로 체크된경우
                        }
                    }
                    else if(pluralCount[k] == 1){
                        triplePluralChecker[k] = l;
                        pluralCount[k]++;
                    }
                }
            }
            for (int j = 0; j < tmpSubSubject.size(); j++) {
                int tmpJ;
                Log.d("tag","minyoung"+pluralCount);
                 switch(pluralCount[j]) {//중복횟수만큼
                     case 0:
                         if (nowCreditCount + tmpSubSubject.get(j).cCredit > getSettedMaxCreditCount) {     //학점이 되는지 체크
                             blankcheck = false;
                         }
                         for (int k = 0; k < (int) ((tmpSubSubject.get(j).cStart - tmpSubSubject.get(j).cEnd) * 2); k++) {
                             if (SubjectCell[(int) tmpSubSubject.get(j).cStart - 9 + k][tmpSubSubject.get(j).cDay] != 0) {
                                 blankcheck = false;
                                 break;
                             }
                         }
                         if (spinStatus.get(4) == 0) {        //요일수가 넘어가는지 체크
                             dayCount = 5;
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                             }
                             if (monDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (tuesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (wednesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (thursDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (friDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (dayCount > spinValue.get(5) + 3) {
                                 blankcheck = false;
                             }
                         }
                         if (spinStatus.get(7) == 0) {     // 요일별 최대강의수를 넘었는지 체크
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                             }
                         }
                         if(blankcheck){
                             switch (tmpSubSubject.get(j).cDay){
                                 case 0:
                                     monDayClassCount++;
                                     break;
                                 case 1:
                                     tuesDayClassCount++;
                                     break;
                                 case 2:
                                     wednesDayClassCount++;
                                     break;
                                 case 3:
                                     thursDayClassCount++;
                                     break;
                                 case 4:
                                     friDayClassCount++;
                                     break;
                             }
                         }
                         break;
                     case 1:
                         if (nowCreditCount + tmpSubSubject.get(j).cCredit > getSettedMaxCreditCount) {     //학점이 되는지 체크
                             blankcheck = false;
                         }
                         for (int k = 0; k < (int) ((tmpSubSubject.get(j).cStart - tmpSubSubject.get(j).cEnd) * 2); k++) {
                             if (SubjectCell[(int) tmpSubSubject.get(j).cStart - 9 + k][tmpSubSubject.get(j).cDay] != 0) {
                                 blankcheck = false;
                                 break;
                             }
                         }
                         if (spinStatus.get(4) == 0) {        //요일수가 넘어가는지 체크
                             dayCount = 5;
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                             }
                             if (monDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (tuesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (wednesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (thursDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (friDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (dayCount > spinValue.get(5) + 3) {
                                 blankcheck = false;
                             }
                         }
                         if (spinStatus.get(7) == 0) {     // 요일별 최대강의수를 넘었는지 체크
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                             }
                         }
                         //
                         tmpJ = j;
                         j = pluralChecker[tmpJ];
                         for (int k = 0; k < (int) ((tmpSubSubject.get(j).cStart - tmpSubSubject.get(j).cEnd) * 2); k++) {
                             if (SubjectCell[(int) tmpSubSubject.get(j).cStart - 9 + k][tmpSubSubject.get(j).cDay] != 0) {
                                 blankcheck = false;
                                 break;
                             }
                         }
                         if (spinStatus.get(4) == 0) {        //요일수가 넘어가는지 체크
                             dayCount = 5;
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                             }
                             if (monDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (tuesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (wednesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (thursDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (friDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (dayCount > spinValue.get(5) + 3) {
                                 blankcheck = false;
                             }
                         }
                         if (spinStatus.get(7) == 0) {     // 요일별 최대강의수를 넘었는지 체크
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                             }
                         }
                         if(blankcheck){
                             switch (tmpSubSubject.get(j).cDay){
                                 case 0:
                                     monDayClassCount++;
                                     break;
                                 case 1:
                                     tuesDayClassCount++;
                                     break;
                                 case 2:
                                     wednesDayClassCount++;
                                     break;
                                 case 3:
                                     thursDayClassCount++;
                                     break;
                                 case 4:
                                     friDayClassCount++;
                                     break;
                             }
                         }
                         j = tmpJ;
                         if(blankcheck){
                             switch (tmpSubSubject.get(j).cDay){
                                 case 0:
                                     monDayClassCount++;
                                     break;
                                 case 1:
                                     tuesDayClassCount++;
                                     break;
                                 case 2:
                                     wednesDayClassCount++;
                                     break;
                                 case 3:
                                     thursDayClassCount++;
                                     break;
                                 case 4:
                                     friDayClassCount++;
                                     break;
                             }
                         }
                         break;
                     case 2:
                         if (nowCreditCount + tmpSubSubject.get(j).cCredit > getSettedMaxCreditCount) {     //학점이 되는지 체크
                             blankcheck = false;
                         }
                         for (int k = 0; k < (int) ((tmpSubSubject.get(j).cStart - tmpSubSubject.get(j).cEnd) * 2); k++) {
                             if (SubjectCell[(int) tmpSubSubject.get(j).cStart - 9 + k][tmpSubSubject.get(j).cDay] != 0) {
                                 blankcheck = false;
                                 break;
                             }
                         }
                         if (spinStatus.get(4) == 0) {        //요일수가 넘어가는지 체크
                             dayCount = 5;
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                             }
                             if (monDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (tuesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (wednesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (thursDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (friDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (dayCount > spinValue.get(5) + 3) {
                                 blankcheck = false;
                             }
                         }
                         if (spinStatus.get(7) == 0) {     // 요일별 최대강의수를 넘었는지 체크
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                             }
                         }
                         //
                         tmpJ = j;
                         j = pluralChecker[tmpJ];
                         for (int k = 0; k < (int) ((tmpSubSubject.get(j).cStart - tmpSubSubject.get(j).cEnd) * 2); k++) {
                             if (SubjectCell[(int) tmpSubSubject.get(j).cStart - 9 + k][tmpSubSubject.get(j).cDay] != 0) {
                                 blankcheck = false;
                                 break;
                             }
                         }
                         if (spinStatus.get(4) == 0) {        //요일수가 넘어가는지 체크
                             dayCount = 5;
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                             }
                             if (monDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (tuesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (wednesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (thursDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (friDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (dayCount > spinValue.get(5) + 3) {
                                 blankcheck = false;
                             }
                         }

                         if (spinStatus.get(7) == 0) {     // 요일별 최대강의수를 넘었는지 체크
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                             }
                         }
                         j = tmpJ;
                         j = triplePluralChecker[tmpJ];
                         for (int k = 0; k < (int) ((tmpSubSubject.get(j).cStart - tmpSubSubject.get(j).cEnd) * 2); k++) {
                             if (SubjectCell[(int) tmpSubSubject.get(j).cStart - 9 + k][tmpSubSubject.get(j).cDay] != 0) {
                                 blankcheck = false;
                                 break;
                             }
                         }
                         if (spinStatus.get(4) == 0) {        //요일수가 넘어가는지 체크
                             dayCount = 5;
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount == 0) {
                                         dayCount--;
                                     }
                                     break;
                             }
                             if (monDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (tuesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (wednesDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (thursDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (friDayClassCount == 0) {
                                 dayCount--;
                             }
                             if (dayCount > spinValue.get(5) + 3) {
                                 blankcheck = false;
                             }
                         }
                         if (spinStatus.get(7) == 0) {     // 요일별 최대강의수를 넘었는지 체크
                             switch (tmpSubSubject.get(j).cDay) {
                                 case 0:
                                     if (monDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 1:
                                     if (tuesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 2:
                                     if (wednesDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 3:
                                     if (thursDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                                 case 4:
                                     if (friDayClassCount >= spinValue.get(9) + 2) {
                                         blankcheck = false;
                                     }
                                     break;
                             }
                         }
                         j = tmpJ;
                         if(blankcheck){
                             switch (tmpSubSubject.get(j).cDay){
                                 case 0:
                                     monDayClassCount++;
                                     break;
                                 case 1:
                                     tuesDayClassCount++;
                                     break;
                                 case 2:
                                     wednesDayClassCount++;
                                     break;
                                 case 3:
                                     thursDayClassCount++;
                                     break;
                                 case 4:
                                     friDayClassCount++;
                                     break;
                             }
                             switch (tmpSubSubject.get(pluralChecker[j]).cDay){
                                 case 0:
                                     monDayClassCount++;
                                     break;
                                 case 1:
                                     tuesDayClassCount++;
                                     break;
                                 case 2:
                                     wednesDayClassCount++;
                                     break;
                                 case 3:
                                     thursDayClassCount++;
                                     break;
                                 case 4:
                                     friDayClassCount++;
                                     break;
                             }
                             switch (tmpSubSubject.get(triplePluralChecker[j]).cDay){
                                 case 0:
                                     monDayClassCount++;
                                     break;
                                 case 1:
                                     tuesDayClassCount++;
                                     break;
                                 case 2:
                                     wednesDayClassCount++;
                                     break;
                                 case 3:
                                     thursDayClassCount++;
                                     break;
                                 case 4:
                                     friDayClassCount++;
                                     break;
                             }
                         }
                         break;
                     default:
                         break;
                 }
                if (blankcheck == true) {       // ok 면 시간표에 넣기
                    OKSubSubject.add(tmpSubSubject.get(j));
                    for (int k = 0; k < (int) ((tmpSubSubject.get(j).cStart - tmpSubSubject.get(j).cEnd) * 2); k++) {
                        SubjectCell[(int) tmpSubSubject.get(j).cStart - 9 + k][tmpSubSubject.get(j).cDay] = tmpSubSubject.get(j).cRow;
                    }
                    nowCreditCount = nowCreditCount + tmpSubSubject.get(j).cCredit;
                    tmpSubSubject.remove(j);
                    break;
                }
                if(spinStatus.get(2) == 0) {
                    for (int k = 0; k < 5; k++) {
                        for (int l = 0; k < 26; k++) {

                        }
                    }
                }
                if (nowCreditCount >= getSettedMinCreditCount) {
                    getUsedSubSubject.add(OKSubSubject);
                }
            }

            //조건 테스트하기전에 일단 되는경우 다넣기
            if(getUsedSubSubject.size() == 10){
                break;
            }
            // 조건테스트해야되는것 : 공강이 비었는가,
            OKSubSubject.clear();
        }
        // 조건테스트 1,3,5,6,9 이미됨 학점은 위에서 카운트할예정

        for(int i = 0; i < getUsedSubSubject.size(); i++) {
            for(int j = 0 ; j < needSubject.size() ; j++) {
                getUsedSubSubject.get(i).add(needSubject.get(j));
            }
        }

        //공강시간만큼 앞뒤로 더 크기를 늘림


        AppContext.tempTimeTableList.clear();
        AppContext.tempTimeTableList.addAll(getUsedSubSubject);
    }

}

//리스트뷰 어댑터
class CustomLvAdapter extends BaseAdapter{

    private ArrayList<ArrayList<Subject>> list = new ArrayList<ArrayList<Subject>>();
    LayoutInflater inflater = null;

    CustomLvAdapter(ArrayList<ArrayList<Subject>> list){ this.list = list; }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertview, ViewGroup parent) {
        if(convertview == null){
            Context context = parent.getContext();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertview = inflater.inflate(R.layout.maderesult_listitem, parent, false);
        }

        TextView tv = (TextView) convertview.findViewById(R.id.maderesult_lstv_name);

        tv.setText("시간표 " + (position + 1));
        return convertview;
    }
}

class UserInputData {
    int[] cClasses = new int[42]; // 저장되는 과목
    String cFileName; // 시간표 이름

    public UserInputData() {
    } // 기본생성자

    public UserInputData(int[] classes, String fileName) {
        for (int i = 0; i < 42; i++) {
            cClasses[i] = classes[i];
        }
        cFileName = fileName;
    }

    public String getFileName() {
        return cFileName;
    }

    public int[] getcClasses() {
        return cClasses;
    }


}



















