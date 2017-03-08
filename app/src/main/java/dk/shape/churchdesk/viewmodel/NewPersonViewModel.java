package dk.shape.churchdesk.viewmodel;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import com.roomorama.caldroid.CaldroidListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import dk.shape.churchdesk.R;
import dk.shape.churchdesk.entity.Person;
import dk.shape.churchdesk.entity.Tag;
import dk.shape.churchdesk.entity.User;
import dk.shape.churchdesk.request.CreatePersonRequest;
import dk.shape.churchdesk.util.CalendarUtils;
import dk.shape.churchdesk.util.DatabaseUtils;
import dk.shape.churchdesk.util.Validators;
import dk.shape.churchdesk.view.MultiSelectDialog;
import dk.shape.churchdesk.view.MultiSelectListItemView;
import dk.shape.churchdesk.view.NewPersonView;
import dk.shape.churchdesk.view.SingleSelectDialog;
import dk.shape.churchdesk.view.SingleSelectListItemView;
import dk.shape.churchdesk.view.TimePickerDialog;
import dk.shape.library.viewmodel.ViewModel;

/**
 * Created by chirag on 22/02/2017.
 */
public class NewPersonViewModel extends ViewModel<NewPersonView> {


    private Context mContext;
    private NewPersonView mNewPersonView;

    private final SendOkayListener mSendOkayListener;

    private List<String> mGenderChoices = new ArrayList<>();

    private static String mSelectedGender;
    private List<Integer> mSelectedTags = new ArrayList<>();;

    public List<Tag> tags;
    //timeEnd
    Calendar calBirth = Calendar.getInstance();

    public NewPersonViewModel(SendOkayListener listener) {
        this.mSendOkayListener = listener;

    }

    public interface SendOkayListener {
        void okay(boolean isOkay, CreatePersonRequest.PersonParameter parameter);
    }

    @Override
    public void bind(NewPersonView newPersonView) {
        mContext = newPersonView.getContext();
        mNewPersonView = newPersonView;

        this.mGenderChoices.add(mContext.getString(R.string.gender_male));
        this.mGenderChoices.add(mContext.getString(R.string.gender_female));

        mNewPersonView.mBirthday.setOnClickListener(mBirthdayClickListener);
        mNewPersonView.mGender.setOnClickListener(mGenderClickListener);
        mNewPersonView.mTags.setOnClickListener(mTagsClickListener);

        mNewPersonView.mFirstNameChosen.addTextChangedListener(mValidateTextWatcher);
        mNewPersonView.mLastNameChosen.addTextChangedListener(mValidateTextWatcher);
        mNewPersonView.mPersonEmailChosen.addTextChangedListener(mValidateTextWatcher);
        mNewPersonView.mMobilePhoneChosen.addTextChangedListener(mValidateTextWatcher);
        mNewPersonView.mHomePhoneChosen.addTextChangedListener(mValidateTextWatcher);
        mNewPersonView.mWorkPhoneChosen.addTextChangedListener(mValidateTextWatcher);
        mNewPersonView.mJobTitleChosen.addTextChangedListener(mValidateTextWatcher);
        mNewPersonView.mAddressChosen.addTextChangedListener(mValidateTextWatcher);
        mNewPersonView.mCityChosen.addTextChangedListener(mValidateTextWatcher);
        mNewPersonView.mPostalCodeChosen.addTextChangedListener(mValidateTextWatcher);

    }

    public void setDataToEdit(Person person){
        mNewPersonView.mFirstNameChosen.setText(person.mFirstName);
        mNewPersonView.mLastNameChosen.setText(person.mLastName);
        mNewPersonView.mPersonEmailChosen.setText(person.mEmail);
        mNewPersonView.mMobilePhoneChosen.setText(person.mContact.get("phone"));
        mNewPersonView.mHomePhoneChosen.setText(person.mContact.get("homePhone"));
        mNewPersonView.mWorkPhoneChosen.setText(person.mContact.get("workPhone"));
        mNewPersonView.mJobTitleChosen.setText(person.mContact.get("occupation"));
        mNewPersonView.mAddressChosen.setText(person.mContact.get("street"));
        mNewPersonView.mCityChosen.setText(person.mContact.get("city"));
        mNewPersonView.mPostalCodeChosen.setText(person.mContact.get("zipcode"));

        if (person.mGender!= null && !person.mGender.isEmpty())
        {
            if (person.mGender.equals("male"))
                mSelectedGender = mContext.getString(R.string.gender_male);
            else
                mSelectedGender = mContext.getString(R.string.gender_female);
            mNewPersonView.mGenderChosen.setText(mSelectedGender);
        }
        if (person.mBirthday != null) {
            TimeZone tz = TimeZone.getDefault();
            // Make sure that we are dealing with the timezones.
            calBirth.setTimeInMillis(person.mBirthday.getTime() + tz.getOffset(person.mBirthday.getTime()));
            setTime(true);
        }
        if (person.mTags!= null){
            for (int tagIndex = 0; tagIndex < person.mTags.size(); tagIndex++) {
                mSelectedTags.add(person.mTags.get(tagIndex).mTagId);
            }
            setTagsText();
        }
        validate();
    }

    TextWatcher mValidateTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validate();
        }
        @Override
        public void afterTextChanged(Editable s) {}
    };

    CompoundButton.OnCheckedChangeListener mValidateCheckedChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            validate();
        }
    };

    private void validate(){
        boolean isOkay = true;
        String firstName = "" + mNewPersonView.mFirstNameChosen.getText().toString().trim();
        String lastName = mNewPersonView.mLastNameChosen.getText().toString().trim();
        String email  = mNewPersonView.mPersonEmailChosen.getText().toString().trim();
        String mobilePhone = mNewPersonView.mMobilePhoneChosen.getText().toString().trim();
        String homePhone = mNewPersonView.mHomePhoneChosen.getText().toString().trim();
        String workPhone = mNewPersonView.mWorkPhoneChosen.getText().toString().trim();
        String jobTitle = mNewPersonView.mJobTitleChosen.getText().toString().trim();
        String address = mNewPersonView.mAddressChosen.getText().toString().trim();
        String city = mNewPersonView.mCityChosen.getText().toString().trim();
        String postalCode = mNewPersonView.mPostalCodeChosen.getText().toString().trim();

        if(mSelectedGender == null &&
                firstName.isEmpty()
                && lastName.isEmpty() &&
                calBirth == null &&
                email.isEmpty() &&
                mobilePhone.isEmpty() &&
                homePhone.isEmpty() &&
                workPhone.isEmpty() &&
                jobTitle.isEmpty() &&
                address.isEmpty() &&
                city.isEmpty() &&
                postalCode.isEmpty()
                && mSelectedTags.size() ==0
                ){
            isOkay = false;
        }

        //Lav request parameter
        if(isOkay) {
            String gender = "";
            if (mSelectedGender != null && mSelectedGender.equals(mContext.getString(R.string.gender_male)))
                gender = "male";
            else if (mSelectedGender != null && mSelectedGender.equals(mContext.getString(R.string.gender_female)))
                gender = "female";
                if (calBirth != null){
                calBirth.set(Calendar.HOUR_OF_DAY, 23);
                calBirth.set(Calendar.MINUTE, 59);
                calBirth.set(Calendar.SECOND, 59);
            }

            List<Tag> tagParam = new ArrayList<>();
            if (mSelectedTags != null && tags != null) {
                for (int selectedTagIndex = 0; selectedTagIndex < mSelectedTags.size(); selectedTagIndex++) {
                    Tag tempTag = new Tag();
                    tempTag.mTagId = mSelectedTags.get(selectedTagIndex);
                    for (int tagIndex = 0; tagIndex < tags.size(); tagIndex++) {
                        if (tempTag.mTagId == tags.get(tagIndex).mTagId)
                            tempTag.mTagName = tags.get(tagIndex).mTagName;
                    }
                    tagParam.add(tempTag);
                }
            }
            CreatePersonRequest.PersonParameter mEventParameter = new CreatePersonRequest.PersonParameter(
                    firstName, lastName, email, mobilePhone, homePhone, workPhone, jobTitle, calBirth.getTime(), gender, address, city, postalCode, tagParam);
            mSendOkayListener.okay(isOkay, mEventParameter);
        }
    }


    private View.OnClickListener mBirthdayClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //This should start the datepicker and send the state of the all-day switch

            FragmentTransaction ft = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
            final TimePickerDialog timePickerDialog = new TimePickerDialog();
            Bundle b = new Bundle();
            b.putLong("date", calBirth.getTimeInMillis());
            b.putBoolean("allDay", true);
            timePickerDialog.setArguments(b);
            timePickerDialog.setOnSelectDateListener(new CaldroidListener() {
                @Override
                public void onSelectDate(Calendar date, View view) {
                    mNewPersonView.mBirthday.setVisibility(View.VISIBLE);
                    if (timePickerDialog.caldroidFragment.isDateSelected(timePickerDialog.convertDateToDateTime(date))) {
                        timePickerDialog.caldroidFragment.deselectDate(date);
                        calBirth.setTimeInMillis(System.currentTimeMillis());
                        setTime(true);
                    } else {
                            timePickerDialog.caldroidFragment.clearSelectedDates();
                            timePickerDialog.caldroidFragment.selectDate(date);
                            //Select date
                            calBirth.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
                            setTime(true);
                    }
                }
            });

            timePickerDialog.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    calBirth.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calBirth.set(Calendar.MINUTE, minute);
                    calBirth.setTime(calBirth.getTime());
                    calBirth.add(Calendar.HOUR_OF_DAY, 1);
                    setTime(true);
                }
            });

            timePickerDialog.show(ft, "dialog");
        }
    };

    private void setTime(boolean isChecked){
        String[] months = mContext.getResources().getStringArray(R.array.months);
        String dateBirth = calBirth.get(Calendar.DATE) + " " + months[calBirth.get(Calendar.MONTH)] +" " + calBirth.get(Calendar.YEAR);
            mNewPersonView.mBirthdayChosen.setText(dateBirth);
        validate();
    }

    private void setTagsText(){
        mNewPersonView.mTagsChosen.setText(String.valueOf(mSelectedTags.size()));
    }

    private View.OnClickListener mGenderClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //This should let you choose the visibility of the event

            final SingleSelectDialog dialog = new SingleSelectDialog(mContext,
                    new GenderListAdapter(), R.string.person_select_gender);
            dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mSelectedGender = mGenderChoices.get(position);
                    mNewPersonView.mGenderChosen.setText(mSelectedGender);
                    validate();
                    dialog.dismiss();
                }
            });
            dialog.show();

        }
    };

    private class GenderListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mGenderChoices != null ? mGenderChoices.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mGenderChoices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String gender = mGenderChoices.get(position);

            SingleSelectListItemView view = new SingleSelectListItemView(mContext);
            view.mItemTitle.setText(gender);
            view.mItemSelected.setVisibility(
                    mSelectedGender != null && gender.equals(mSelectedGender)
                            ? View.VISIBLE
                            : View.GONE);
            return view;
        }
    }

    private View.OnClickListener mTagsClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //This should let you choose the visibility of the event

            final MultiSelectDialog dialog = new MultiSelectDialog(mContext,
                    new TagsListAdapter(), R.string.person_select_tags);
            dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(mSelectedTags == null){
                        mSelectedTags = new ArrayList<>();
                    }
                    if(mSelectedTags.contains(tags.get(position).mTagId)){
                        mSelectedTags.remove((Integer) tags.get(position).mTagId);
                    } else {
                        mSelectedTags.add(tags.get(position).mTagId);
                    }

                    ((MultiSelectListItemView)view).mItemSelected.setVisibility(
                            mSelectedTags != null && mSelectedTags.contains(tags.get(position).mTagId)
                                    ? View.VISIBLE
                                    : View.GONE);
                    validate();
                }
            });
            dialog.showCancelButton(false);
            dialog.setOnOKClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTagsText();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    };

    private class TagsListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return tags != null ? tags.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return tags.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Tag tag = tags.get(position);

            MultiSelectListItemView view = new MultiSelectListItemView(mContext);
            view.mItemTitle.setText(tag.mTagName);
            view.mItemSelected.setVisibility(
                    mSelectedTags != null && mSelectedTags.contains(tag.mTagId)
                            ? View.VISIBLE
                            : View.GONE);
            return view;
        }
    }
}