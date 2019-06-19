package com.android.messaging.privatebox.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.privatebox.PrivateBoxSettings;
import com.android.messaging.privatebox.ui.view.timepickerview.TimePickerView;
import com.android.messaging.privatebox.ui.view.timepickerview.WheelTime;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;

public class PrivateBoxLockQuestionActivity extends HSAppCompatActivity implements View.OnClickListener{
    public static final int REQUEST_FOR_SETTING_PASSWORD = 0x8765;

    public static final String INTENT_KEY_IS_SETTING_QUESTION = "INTENT_KEY_IS_SETTING_QUESTION";
    public static final String INTENT_KEY_IS_FIRST_SETTING_QUESTION = "INTENT_KEY_IS_FIRST_SETTING_QUESTION";
    public static final String PREF_KEY_SECURITY_QUESTION = "PREF_KEY_SECURITY_QUESTION";
    public static final String PREF_KEY_SECURITY_ANSWER = "PREF_KEY_SECURITY_ANSWER";
    private static final int POSITION_BIRTHDAY = 0;
    private static final int POSITION_TEACHER = 1;
    private static final int POSITION_SCHOOL = 2;
    private static final int POSITION_HERO = 3;
    private static final int DEFAULT_BIRTHDAY_MONTH = 7;
    private static final int DEFAULT_BIRTHDAY_DAY = 15;
    private static final String DEFAULT_BIRTHDAY = "7-15";

    private Spinner questionSpinner;
    private EditText answerEditText;
    private MenuItem menuItem;
    private WheelTime wheelTime;

    private Preferences prefs;
    private boolean isSettingQuestion;

    private View timePickerView;
    private LinearLayout answerLl;
    private TextView setOnlyBtn;

    private boolean isBirthdayQuestion = false;
    private String savedQuestion;
    private String savedAnswer;
    private CharSequence[] questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_question);

        timePickerView = findViewById(R.id.time_picker_ll);
        answerLl = findViewById(R.id.answer_ll);
        setOnlyBtn = findViewById(R.id.set_only_button);
        setOnlyBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(3.3f), true));
        setOnlyBtn.setOnClickListener(this);

        wheelTime = new WheelTime(timePickerView, TimePickerView.Type.MONTH_DAY);
        wheelTime.setPicker(0, DEFAULT_BIRTHDAY_MONTH, DEFAULT_BIRTHDAY_DAY, 0, 0);

        FrameLayout mainContainer = findViewById(R.id.security_question_container);
        mainContainer.setBackgroundColor(PrimaryColors.getPrimaryColor());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        UiUtils.setTitleBarBackground(toolbar, this);

        prefs = Preferences.getDefault();

        questionSpinner = findViewById(R.id.question_spinner);
        EditText questionEditText = findViewById(R.id.question_edit_text);
        answerEditText = findViewById(R.id.answer_edit_text);
        TextView questionPromptTextView = findViewById(R.id.question_prompt);

        isSettingQuestion = !PrivateBoxSettings.isSecurityQuestionSet()
                || getIntent().getBooleanExtra(INTENT_KEY_IS_SETTING_QUESTION, false);

        questions = getResources().getTextArray(R.array.question_spinner_content);
        savedQuestion = prefs.getString(PREF_KEY_SECURITY_QUESTION, questions[POSITION_BIRTHDAY].toString());
        savedAnswer = prefs.getString(PREF_KEY_SECURITY_ANSWER, "");
        isBirthdayQuestion = (POSITION_BIRTHDAY < questions.length && savedQuestion.equals(questions[POSITION_BIRTHDAY]));

        if (isSettingQuestion) {
            if(getIntent().getBooleanExtra(INTENT_KEY_IS_FIRST_SETTING_QUESTION, false)){
                BugleAnalytics.logEvent("PrivateBox_SecurityQuestion_Show", true,"from", "firstsetcode");
            } else {
                BugleAnalytics.logEvent("PrivateBox_SecurityQuestion_Show", true,"from", "settings");
            }
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.question_spinner_content,
                    R.layout.item_security_question_spinner);
            adapter.setDropDownViewResource(R.layout.item_security_question_spinner_dropdown);
            questionSpinner.setAdapter(adapter);
            if (prefs.contains(PREF_KEY_SECURITY_ANSWER) && prefs.contains(PREF_KEY_SECURITY_QUESTION)) {

                questionSpinner.setSelection(getIndex(prefs.getString(PREF_KEY_SECURITY_QUESTION, "Error")));
                if (!isBirthdayQuestion) {
                    answerEditText.setText(savedAnswer);
                }

                int month = DEFAULT_BIRTHDAY_MONTH;
                int day = DEFAULT_BIRTHDAY_DAY;
                if (!TextUtils.isEmpty(savedAnswer) && savedAnswer.contains("-")) {
                    try {
                        String[] timeArray = savedAnswer.split("-");
                        if (timeArray.length == 2) {
                            month = Integer.parseInt(timeArray[0]);
                            day = Integer.parseInt(timeArray[1]);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                wheelTime.setPicker(0, month, day, 0, 0);
            }
            questionEditText.setVisibility(View.GONE);
            questionPromptTextView.setText(getResources().getText(R.string.choose_question_prompt));
            getSupportActionBar().setTitle(getResources().getString(R.string.security_question));
            setOnlyBtn.setText(getString(R.string.set));
        } else {
            BugleAnalytics.logEvent("PrivateBox_ForgetPage_Show");
            questionEditText.setText(savedQuestion);
            questionEditText.setFocusable(false);
            questionSpinner.setVisibility(View.GONE);
            questionPromptTextView.setText(getResources().getText(R.string.answer_question_prompt));
            getSupportActionBar().setTitle(getResources().getString(R.string.forget_password));
            wheelTime.setPicker(0, DEFAULT_BIRTHDAY_MONTH, DEFAULT_BIRTHDAY_DAY, 0, 0);
            setOnlyBtn.setText(getString(R.string.submit));
        }

        setSelectTimePickerViewStatus(isBirthdayQuestion);

        answerEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (s.length() == 0) {
                        menuItem.setIcon(R.drawable.done_btn_disable);
                        menuItem.setEnabled(false);
                        if (null != answerLl && answerLl.getVisibility() == View.VISIBLE) {
                            setOnlyBtn.setEnabled(false);
                        } else {
                            setOnlyBtn.setEnabled(true);
                        }
                    } else {
                        menuItem.setIcon(R.drawable.done_btn);
                        menuItem.setEnabled(true);
                        setOnlyBtn.setEnabled(true);
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }

            }
        });

        showSoftKeyboard();

        questionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                showSoftKeyboard();
                setSelectTimePickerViewStatus((i == POSITION_BIRTHDAY));
                setAnswerStatus(questions[i].toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }

    private void showSoftKeyboard() {
        if (answerEditText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(answerEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private int getIndex(String valueStr) {
        int index = 0;
        for (int i = 0; i < questionSpinner.getCount(); ++i) {
            if (questionSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(valueStr)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FOR_SETTING_PASSWORD) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
            }
            finishAndNotifyLockFinished();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.input, menu);
        menuItem = menu.findItem(R.id.action_done);

        if (TextUtils.isEmpty(answerEditText.getText())) {
            menuItem.setIcon(R.drawable.done_btn_disable);
            menuItem.setEnabled(false);
        } else {
            menuItem.setIcon(R.drawable.done_btn);
            menuItem.setEnabled(true);
        }
        // 不使用，下面按钮代替
        menuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_done:
                actionDown();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void finishAndNotifyLockFinished() {
        setResult(RESULT_OK);
        finish();
        // TODO confirm below code
        //HSGlobalNotificationCenter.sendNotification(AppLockActivity.NOTIFICATION_PASSWORD_ACTIVITIES_FINISHED);
    }

    private void setSelectTimePickerViewStatus(boolean isBirthdayQuestion) {
        if (isBirthdayQuestion) {
            hideKeyBoard(answerEditText, this);
            answerLl.setVisibility(View.GONE);
            timePickerView.setVisibility(View.VISIBLE);
        } else {
            hideKeyBoard(answerEditText, this);
            answerLl.setVisibility(View.VISIBLE);
            timePickerView.setVisibility(View.GONE);
        }

        setOnlyBtn.setVisibility(View.VISIBLE);
    }

    private void hideKeyBoard(EditText et, Context context) {
        if (null == et || null == context) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(et.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
    private void setAnswerStatus(String selectQuestion) {
        if (!isBirthdayQuestion && !TextUtils.isEmpty(selectQuestion) && selectQuestion.equals(savedQuestion)) {
            answerEditText.setText(savedAnswer);
        } else {
            answerEditText.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_only_button:
                actionDown();
                break;
            default:
                break;
        }
    }

    private void actionDown() {
        if (isSettingQuestion) {
            // Remove leading and trailing spaces from answer
            String answer = answerEditText.getText().toString().trim();

            String flurryType;
            switch (questionSpinner.getSelectedItemPosition()) {
                case POSITION_BIRTHDAY:
                    answer = wheelTime.getSelectedTime();
                    String flurryResult;
                    if (DEFAULT_BIRTHDAY.equals(answer)) {
                        flurryResult = "same";
                    } else {
                        flurryResult = "different";
                    }
                    flurryType = "birth";
                    break;
                case POSITION_TEACHER:
                    flurryType = "teacher";
                    break;
                case POSITION_SCHOOL:
                    flurryType = "school";
                    break;
                case POSITION_HERO:
                    flurryType = "hero";
                    break;
                default:
                    flurryType = "birth";
                    break;
            }

            if (answer.isEmpty()) {
                Toast.makeText(PrivateBoxLockQuestionActivity.this, getResources().getString(R.string.security_question_answer), Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.putString(PREF_KEY_SECURITY_QUESTION, questionSpinner.getSelectedItem().toString());
            prefs.putString(PREF_KEY_SECURITY_ANSWER, answer);
            PrivateBoxSettings.setSecurityQuestionSet(true);
            finishAndNotifyLockFinished();
            if (getIntent().getBooleanExtra(INTENT_KEY_IS_FIRST_SETTING_QUESTION, false)) {
                BugleAnalytics.logEvent("PrivateBox_SecurityQuestion_Set_Click", true,"from", "firstsetcode");
                Intent intent = new Intent(PrivateBoxLockQuestionActivity.this, PrivateConversationListActivity.class);
                if (getIntent().hasExtra(ConversationListActivity.INTENT_KEY_PRIVATE_CONVERSATION_LIST)) {
                    intent.putExtra(ConversationListActivity.INTENT_KEY_PRIVATE_CONVERSATION_LIST,
                            getIntent().getStringArrayExtra(ConversationListActivity.INTENT_KEY_PRIVATE_CONVERSATION_LIST)
                    );
                }
                Navigations.startActivitySafely(PrivateBoxLockQuestionActivity.this, intent);
            } else {
                BugleAnalytics.logEvent("PrivateBox_SecurityQuestion_Set_Click", true,"from", "settings");
            }
        } else { // Is answering question
            String answer = answerEditText.getText().toString().trim();
            if (isBirthdayQuestion) {
                answer = wheelTime.getSelectedTime();
            }

            if (!TextUtils.isEmpty(answer) && TextUtils.equals(answer, savedAnswer)) {
                Intent intent = new Intent(PrivateBoxLockQuestionActivity.this, PrivateBoxSetPasswordActivity.class);
                intent.putExtra(PrivateBoxSetPasswordActivity.INTENT_EXTRA_FORGET_PASSWORD, true);
                startActivityForResult(intent, REQUEST_FOR_SETTING_PASSWORD);
            } else {
                Toast.makeText(PrivateBoxLockQuestionActivity.this, "Answer is incorrect", Toast.LENGTH_SHORT).show();
            }
            BugleAnalytics.logEvent("PrivateBox_ForgetPage_Submit");
        }
    }
}
