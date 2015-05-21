package dk.shape.churchdesk.viewmodel;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

import dk.shape.churchdesk.R;
import dk.shape.churchdesk.entity.Site;
import dk.shape.churchdesk.entity.User;
import dk.shape.churchdesk.entity.resources.Category;
import dk.shape.churchdesk.entity.resources.Group;
import dk.shape.churchdesk.entity.resources.Resource;
import dk.shape.churchdesk.util.DatabaseUtils;
import dk.shape.churchdesk.view.NewEventView;
import dk.shape.churchdesk.view.SingleSelectDialog;
import dk.shape.churchdesk.view.SingleSelectListItemView;
import dk.shape.library.viewmodel.ViewModel;

/**
 * Created by Martin on 20/05/2015.
 */
public class NewEventViewModel extends ViewModel<NewEventView> {


    private Context mContext;
    private NewEventView mNewEventView;

    private final User mCurrentUser;
    private List<Group> mGroups;
    private List<String> mVisibilityChoices;
    private List<Category> mCategories;
    private List<Resource> mResources;

    private static Site mSelectedSite;
    private static Group mSelectedGroup;
    private static List<Category> mSelectedCategories;
    private static List<Resource> mSelectedResources;
    private static String mSelectedVisibility;

    public NewEventViewModel(User mCurrentUser) {
        this.mCurrentUser = mCurrentUser;
        this.mVisibilityChoices = new ArrayList<>();
        this.mVisibilityChoices.add("Visible on website");
        this.mVisibilityChoices.add("Visible only in group");


    }

    @Override
    public void bind(NewEventView newEventView) {
        mContext = newEventView.getContext();
        mNewEventView = newEventView;

        setDefaultText();

        mNewEventView.mTimeAlldayChosen.setOnCheckedChangeListener(mAllDaySwitchListener);
        mNewEventView.mTimeStart.setOnClickListener(mStartTimeClickListener);
        mNewEventView.mSiteParish.setOnClickListener(mSiteParishClickListener);
        mNewEventView.mSiteGroup.setOnClickListener(mGroupClickListener);
        mNewEventView.mSiteCategory.setOnClickListener(mCategoryClickListener);
        mNewEventView.mResources.setOnClickListener(mResourcesClickListener);
        mNewEventView.mVisibility.setOnClickListener(mVisibilityClickListener);
    }


    private void setDefaultText(){
        mSelectedSite = mCurrentUser.mSites.get(0);
        validateNewSiteParish(mSelectedSite);

        mNewEventView.mVisibilityChosen.setText("Visible only in group");
    }

    private void validateNewSiteParish(Site site){
        mSelectedSite = site;
        mSelectedGroup = null;
        mSelectedCategories = null;
        mSelectedResources = null;
        mGroups = DatabaseUtils.getInstance().getGroupsBySiteId(mSelectedSite.mSiteUrl);
        mCategories = DatabaseUtils.getInstance().getCategoriesBySiteId(mSelectedSite.mSiteUrl);
        mResources = DatabaseUtils.getInstance().getResourcesBySiteId(mSelectedSite.mSiteUrl);

        if(mGroups == null || mGroups.isEmpty()){
            mNewEventView.mSiteGroupChosen.setText("None available");
            mNewEventView.mSiteGroupChosen.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            //Fjern pil
        }else if(mSelectedGroup == null){
            mNewEventView.mSiteGroupChosen.setText("");
            mNewEventView.mSiteGroupChosen.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(R.drawable.disclosure_arrow), null);
        } else {
            mNewEventView.mSiteGroupChosen.setText(mSelectedGroup.mName);
            mNewEventView.mSiteGroupChosen.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(R.drawable.disclosure_arrow), null);
        }

        if(mCategories == null || mCategories.isEmpty()){
            mNewEventView.mSiteCategoryChosen.setText("None available");
            mNewEventView.mSiteCategoryChosen.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            mNewEventView.mSiteCategoryChosen.setText("");
            mNewEventView.mSiteCategoryChosen.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(R.drawable.disclosure_arrow), null);
        }

        if(mResources == null || mResources.isEmpty()){
            mNewEventView.mResourcesChosen.setText("None available");
            mNewEventView.mResourcesChosen.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            mNewEventView.mResourcesChosen.setText("");
            mNewEventView.mResourcesChosen.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(R.drawable.disclosure_arrow), null);
        }

        mNewEventView.mAllowDoubleBooking.setVisibility(mSelectedSite.mPermissions.get("canDoubleBook") ? View.VISIBLE : View.INVISIBLE);

        mNewEventView.mSiteParishChosen.setText(mSelectedSite.mSiteName);

    }



    private SwitchCompat.OnCheckedChangeListener mAllDaySwitchListener = new SwitchCompat.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                mNewEventView.mTimeStartChosen.setText("15 September 2015");
                mNewEventView.mTimeEndChosen.setText("15 September 2015");
            } else {
                mNewEventView.mTimeStartChosen.setText("15 September 2015   09:15");
                mNewEventView.mTimeEndChosen.setText("15 September 2015   10:15");
            }
        }
    };

    private View.OnClickListener mStartTimeClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //This should start the datepicker and send the state of the all-day switch
            mNewEventView.mTimeEnd.setVisibility(View.VISIBLE);
        }
    };

    private View.OnClickListener mSiteParishClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //this should let you choose a parish
            final SingleSelectDialog dialog = new SingleSelectDialog(mContext,
                    new SiteListAdapter(), R.string.new_event_parish_chooser);
            dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialog.dismiss();
                    validateNewSiteParish(mCurrentUser.mSites.get(position));

                }
            });
            dialog.show();
        }
    };

    private View.OnClickListener mGroupClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //This should let you choose a group
            mGroups = DatabaseUtils.getInstance().getGroupsBySiteId(mSelectedSite.mSiteUrl);

            final SingleSelectDialog dialog = new SingleSelectDialog(mContext,
                    new GroupListAdapter(), R.string.new_event_group_chooser);
            dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialog.dismiss();
                    mSelectedGroup = mGroups.get(position);
                    mNewEventView.mSiteGroupChosen.setText(mSelectedGroup.mName);
                }
            });
            dialog.show();

        }
    };

    private View.OnClickListener mCategoryClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //This should let you choose a category



        }
    };

    private View.OnClickListener mResourcesClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //This should let you choose the used resources
        }
    };

    private View.OnClickListener mVisibilityClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //This should let you choose the visibility of the event

            final SingleSelectDialog dialog = new SingleSelectDialog(mContext,
                    new VisibilityListAdapter(), R.string.new_event_visibility_chooser);
            dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialog.dismiss();
                    mSelectedVisibility = mVisibilityChoices.get(position);
                    mNewEventView.mVisibilityChosen.setText(mSelectedVisibility);
                }
            });
            dialog.show();

        }
    };

    private class SiteListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCurrentUser.mSites.size();
        }

        @Override
        public Object getItem(int position) {
            return mCurrentUser.mSites.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Site site = mCurrentUser.mSites.get(position);

            SingleSelectListItemView view = new SingleSelectListItemView(mContext);
            view.mItemTitle.setText(site.mSiteName);
            view.mItemSelected.setVisibility(
                    mSelectedSite != null && site.equals(mSelectedSite.mSiteUrl)
                            ? View.VISIBLE
                            : View.GONE);
            return view;
        }
    }

    private class GroupListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mGroups != null ? mGroups.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mGroups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Group group = mGroups.get(position);

            SingleSelectListItemView view = new SingleSelectListItemView(mContext);
            view.mItemTitle.setText(group.mName);
            view.mItemSelected.setVisibility(
                    mSelectedGroup != null && group.equals(mSelectedGroup)
                            ? View.VISIBLE
                            : View.GONE);
            return view;
        }
    }

    private class VisibilityListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mVisibilityChoices != null ? mVisibilityChoices.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mVisibilityChoices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String choice = mVisibilityChoices.get(position);

            SingleSelectListItemView view = new SingleSelectListItemView(mContext);
            view.mItemTitle.setText(choice);
            view.mItemSelected.setVisibility(
                    mSelectedVisibility != null && choice.equals(mSelectedVisibility)
                            ? View.VISIBLE
                            : View.GONE);
            return view;
        }
    }

}
