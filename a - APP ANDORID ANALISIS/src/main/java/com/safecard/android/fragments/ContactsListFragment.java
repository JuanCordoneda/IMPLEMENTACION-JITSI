package com.safecard.android.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.Settings;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.core.view.MenuItemCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.safecard.android.BuildConfig;
import com.safecard.android.R;
import com.safecard.android.utils.Contact;
import com.safecard.android.utils.ImageLoader;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class ContactsListFragment extends ListFragment implements
        AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final ArrayList<Contact> contact_selected = new ArrayList<>();
    private final ArrayList<String> contact_selected_aux = new ArrayList<>();

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    // Defines a tag for identifying log entries
    private static final String TAG = "ContactsListFragment";

    // Bundle key for saving previously selected search result item
    private static final String STATE_PREVIOUSLY_SELECTED_KEY =
            "com.safecard.android.activities.SELECTED_ITEM";

    private ContactsAdapter mAdapter; // The main query adapter
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread
    private String mSearchTerm; // Stores the current search query term

    // Contact selected listener that allows the activity holding this fragment to be notified of
    // a contact being selected
    private OnContactsInteractionListener mOnContactSelectedListener;

    // Stores the previously selected search item so that on a configuration change the same item
    // can be reselected again
    private int mPreviouslySelectedSearchItem = 0;

    // Whether or not the search query has changed since the last time the loader was refreshed
    private boolean mSearchQueryChanged;

    // Whether or not this is a search result view of this fragment, only used on pre-honeycomb
    // OS versions as search results are shown in-line via Action Bar search from honeycomb onward
    private boolean mIsSearchResultView = false;

    /**
     * Fragments require an empty constructor.
     */
    public ContactsListFragment() {}

    public void setSearchQuery(String query) {
        if (TextUtils.isEmpty(query)) {
            mIsSearchResultView = false;
        } else {
            mSearchTerm = query;
            mIsSearchResultView = true;
        }
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Snackbar snackbar = Snackbar.make(getListView(), R.string.fragment_contacts_list_contacts_access_request_message, Snackbar.LENGTH_LONG);
                    snackbar.setAction(getResources().getString(R.string.contacts_grant), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    });
                    snackbar.show();
                } else {
                    setHasOptionsMenu(true);
                    //if (getActivity().findViewById(R.id.btnInvitar) != null){
                    //    getActivity().findViewById(R.id.btnInvitar).setVisibility(View.VISIBLE);
                    //}

                    if (mPreviouslySelectedSearchItem == 0) {
                        // Initialize the loader, and create a loader identified by ContactsQuery.QUERY_ID
                        getLoaderManager().initLoader(ContactsQuery.QUERY_ID, null, this);
                    }
                }
                break;
        }
    }

    public void requestAppPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            Snackbar.make(getListView(), R.string.fragment_contacts_list_contacts_access_request_message2, Snackbar.LENGTH_LONG)
                    .setAction(getResources().getString(R.string.contacts_grant), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPermissions( new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            }).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the list fragment layout
        return inflater.inflate(R.layout.contact_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");

        // Create the main contacts adapter
        mAdapter = new ContactsAdapter(getActivity());

        if (savedInstanceState != null) {
            Log.i(TAG, "onCreateView, savedInstanceState not null");
            // If we're restoring state after this fragment was recreated then
            // retrieve previous search term and previously selected search
            // result.
            mSearchTerm = savedInstanceState.getString(SearchManager.QUERY);
            mPreviouslySelectedSearchItem =
                    savedInstanceState.getInt(STATE_PREVIOUSLY_SELECTED_KEY, 0);
        }

        mImageLoader = new ImageLoader(getActivity(), getListPreferredItemHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                // This gets called in a background thread and passed the data from
                // ImageLoader.loadImage().
                Bitmap bitmap = loadContactPhotoThumbnail((String) data, getImageSize());
                if(bitmap == null) {
                    if (getContext() != null) {
                        bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_avatar);
                    } else {
                        bitmap =  Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_4444);
                    }
                }
                return getRoundedBitmap(bitmap);
            }
        };

        // Set a placeholder loading image for the image loader
        mImageLoader.setLoadingImage(R.drawable.ic_avatar);

        // Add a cache to the image loader
        mImageLoader.addImageCache(getActivity().getSupportFragmentManager(), 0.1f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            setHasOptionsMenu(false);
        } else {
            setHasOptionsMenu(true);
        }


        // Set up ListView, assign adapter and set some listeners. The adapter was previously
        // created in onCreate().
        setListAdapter(mAdapter);

        //getListView().setItemsCanFocus(true);

        getListView().setOnItemClickListener(this);
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                hideKeyboard(getContext());
                // Pause image loader to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageLoader.setPauseWork(true);
                } else {
                    mImageLoader.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {}
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            requestAppPermission();
            return;
        }

        // If there's a previously selected search item from a saved state then don't bother
        // initializing the loader as it will be restarted later when the query is populated into
        // the action bar search view (see onQueryTextChange() in onCreateOptionsMenu()).
        if (mPreviouslySelectedSearchItem == 0) {
            // Initialize the loader, and create a loader identified by ContactsQuery.QUERY_ID
            getLoaderManager().initLoader(ContactsQuery.QUERY_ID, null, this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            // Assign callback listener which the holding activity must implement. This is used
            // so that when a contact item is interacted with (selected by the user) the holding
            // activity will be notified and can take further action such as populating the contact
            // detail pane (if in multi-pane layout) or starting a new activity with the contact
            // details (single pane layout).
            mOnContactSelectedListener = (OnContactsInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnContactsInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // In the case onPause() is called during a fling the image loader is
        // un-paused to let any remaining background work complete.
        mImageLoader.setPauseWork(false);
    }

    /**
     * Called when ListView selection is cleared, for example
     * when search mode is finished and the currently selected
     * contact should no longer be selected.
     */
    private void onSelectionCleared() {
        // Uses callback to notify activity this contains this fragment
        mOnContactSelectedListener.onSelectionCleared();

        // Clears currently checked item
        getListView().clearChoices();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu items
        inflater.inflate(R.menu.menu_contacts, menu);

        // Locate the search item
        MenuItem searchItem = menu.findItem(R.id.action_search);

        // In versions prior to Android 3.0, hides the search item to prevent additional
        // searches. In Android 3.0 and later, searching is done via a SearchView in the ActionBar.
        // Since the search doesn't create a new Activity to do the searching, the menu item
        // doesn't need to be turned off.
        if (mIsSearchResultView) {
            searchItem.setVisible(false);
        }

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);
        //searchView.setIconified(false);
        //searchView.onActionViewExpanded();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                // Nothing needs to happen when the user submits the search string
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "onQueryTextChange");
                if(!isAdded()){
                    return false;
                }
                Log.i(TAG, "newText" + newText);
                // Called when the action bar search text has changed.  Updates
                // the search filter, and restarts the loader to do a new query
                // using the new search string.
                String newFilter = !TextUtils.isEmpty(newText) ? newText : null;

                // Don't do anything if the filter is empty
                if (mSearchTerm == null && newFilter == null) {
                    return true;
                }

                // Don't do anything if the new filter is the same as the current filter
                if (mSearchTerm != null && mSearchTerm.equals(newFilter)) {
                    return true;
                }

                // Updates current filter to new filter
                mSearchTerm = newFilter;

                // Restarts the loader. This triggers onCreateLoader(), which builds the
                // necessary content Uri from mSearchTerm.
                mSearchQueryChanged = true;
                Log.i(TAG, "restartLoader");
                getLoaderManager().restartLoader(
                        ContactsQuery.QUERY_ID, null, ContactsListFragment.this);
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                // Nothing to do when the action item is expanded
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                // When the user collapses the SearchView the current search string is
                // cleared and the loader restarted.
                if (!TextUtils.isEmpty(mSearchTerm)) {
                    onSelectionCleared();
                }
                mSearchTerm = null;
                getLoaderManager().restartLoader(
                        ContactsQuery.QUERY_ID, null, ContactsListFragment.this);
                return true;
            }
        });

        if (mSearchTerm != null) {
            // If search term is already set here then this fragment is
            // being restored from a saved state and the search menu item
            // needs to be expanded and populated again.

            // Stores the search term (as it will be wiped out by
            // onQueryTextChange() when the menu item is expanded).
            final String savedSearchTerm = mSearchTerm;

            // Expands the search menu item
            //if (Utils.hasICS()) {
                searchItem.expandActionView();
            //}

            // Sets the SearchView to the previous search string
            searchView.setQuery(savedSearchTerm, false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // For platforms earlier than Android 3.0, triggers the search activity
            case R.id.action_search:
                if (!Utils.hasHoneycomb()) {
                    getActivity().onSearchRequested();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // Gets the Cursor object currently bound to the ListView
        final Cursor cursor = mAdapter.getCursor();

        hideKeyboard(getContext());

        // Moves to the Cursor row corresponding to the ListView item that was clicked
        cursor.moveToPosition(position);
        String phoneNumber = cursor.getString(ContactsQuery.NUMBER).replace(" ", "");//cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        String name = cursor.getString(ContactsQuery.DISPLAY_NAME);
        int contact_id = cursor.getInt(ContactsQuery.ID);

        if(contact_selected_aux.contains(phoneNumber)){
            for(int i = 0;i < contact_selected.size();i++){
                //Log.d(TAG, "Selected numbers: "+contact_selected.get(i).getMobile()+" "+phoneNumber);
                if(contact_selected.get(i).getMobile().equals(phoneNumber)){
                    //Log.d(TAG, "Selected id: "+i);
                    contact_selected.get(i).setSelected(0);
                    contact_selected_aux.remove(phoneNumber);
                }
            }

        } else {
            Boolean exist = false;
            for(int i = 0;i < contact_selected.size();i++){
                //Log.d(TAG, "Selected numbers: "+contact_selected.get(i).getMobile()+" "+phoneNumber);
                if(contact_selected.get(i).getMobile().equals(phoneNumber)){
                    contact_selected.get(i).setSelected(1);
                    contact_selected_aux.add(phoneNumber);
                    exist = true;
                    break;
                }
            }

            if(!exist){
                Contact data = new Contact(contact_id, name, phoneNumber,"",1);
                contact_selected.add(data);
                contact_selected_aux.add(phoneNumber);
            }

        }

        String phoneNumbers = "";
        String names = "";
        for (Contact contact : contact_selected) {
            //Log.d(TAG, "Selected getSelected: "+contact.getSelected());
            if (contact.getSelected() == 1){
                phoneNumbers = phoneNumbers+contact.getMobile()+",";
                names = names+contact.getName()+",";
            }
        }

        // Notifies the parent activity that the user selected a contact. In a two-pane layout, the
        // parent activity loads a ContactDetailFragment that displays the details for the selected
        // contact. In a single-pane layout, the parent activity starts a new activity that
        // displays contact details in its own Fragment.
        mOnContactSelectedListener.onContactSelected(phoneNumbers, names);
        //mOnContactSelectedListener.onContactSelected(contact_selected);
        mAdapter.notifyDataSetChanged();
    }

    public static void hideKeyboard( Context context ) {

        try {
            InputMethodManager inputManager = ( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE );

            View view = ( ( Activity ) context ).getCurrentFocus();
            if ( view != null ) {
                inputManager.hideSoftInputFromWindow( view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mSearchTerm)) {
            // Saves the current search string
            outState.putString(SearchManager.QUERY, mSearchTerm);

            // Saves the currently selected contact
            outState.putInt(STATE_PREVIOUSLY_SELECTED_KEY, getListView().getCheckedItemPosition());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "onCreateLoader");

        // If this is the loader for finding contacts in the Contacts Provider
        // (the only one supported)
        if (id == ContactsQuery.QUERY_ID) {
            Uri contentUri;

            // There are two types of searches, one which displays all contacts and
            // one which filters contacts by a search query. If mSearchTerm is set
            // then a search query has been entered and the latter should be used.

            if (mSearchTerm == null) {
                // Since there's no search string, use the content URI that searches the entire
                // Contacts table
                contentUri = ContactsQuery.CONTENT_URI;
            } else {
                // Since there's a search string, use the special content Uri that searches the
                // Contacts table. The URI consists of a base Uri and the search string.
                contentUri =
                        Uri.withAppendedPath(ContactsQuery.FILTER_URI, Uri.encode(mSearchTerm));
            }

            Loader<Cursor> loader_cursor = new CursorLoader(getActivity(),
                    contentUri,
                    ContactsQuery.PROJECTION,
                    ContactsQuery.SELECTION,
                    null,
                    ContactsQuery.SORT_ORDER);

            return loader_cursor;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i(TAG, "onLoadFinished");
        // This swaps the new cursor into the adapter.
        if (loader != null && loader.getId() == ContactsQuery.QUERY_ID && cursor != null) {
            cursor.moveToFirst();
            Log.i(TAG, "cursor.getCount(): " + cursor.getCount());
            MatrixCursor cursor_aux = new MatrixCursor(ContactsQuery.PROJECTION);
            String previuosPhone = "", actualPhone = "";
            Boolean flag_add, flag_continue;

            JSONObject countries_obj = Utils.getDefaultJSONObject("countries", getActivity());
            if(cursor.getCount() > 0) {
                if (countries_obj.has("countries")) {
                    try {
                        JSONArray countries = countries_obj.getJSONArray("countries");

                        do {
                            actualPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "");
                            Log.i(TAG, "actualPhone: " + actualPhone);

                            String actualPhoneAux = actualPhone.replace("+", "");
                            flag_continue = true;

                            if (!previuosPhone.contains(actualPhone) && actualPhoneAux.matches("^\\d{6,15}$")) {

                                for (int i = 0; i < countries.length() && flag_continue; i++) {

                                    if (countries.getJSONObject(i).getString("phone_validation_regex") != null && countries.getJSONObject(i).getString("phone_validation_regex") != "") {

                                        if (actualPhoneAux.substring(0, 4).equals(countries.getJSONObject(i).getString("calling_code")) &&
                                                actualPhoneAux.substring(4, actualPhoneAux.length()).matches(countries.getJSONObject(i).getString("phone_validation_regex"))) {

                                            flag_add = true;

                                        } else if (actualPhoneAux.substring(0, 3).equals(countries.getJSONObject(i).getString("calling_code")) &&
                                                actualPhoneAux.substring(3, actualPhoneAux.length()).matches(countries.getJSONObject(i).getString("phone_validation_regex"))) {

                                            flag_add = true;

                                        } else if (actualPhoneAux.substring(0, 2).equals(countries.getJSONObject(i).getString("calling_code")) &&
                                                actualPhoneAux.substring(2, actualPhoneAux.length()).matches(countries.getJSONObject(i).getString("phone_validation_regex"))) {

                                            flag_add = true;

                                        } else if (actualPhoneAux.substring(0, 1).equals(countries.getJSONObject(i).getString("calling_code")) &&
                                                actualPhoneAux.substring(1, actualPhoneAux.length()).matches(countries.getJSONObject(i).getString("phone_validation_regex"))) {
                                            flag_add = true;

                                        } else {
                                            flag_add = false;
                                        }


                                        if (flag_add) {
                                            Log.i(TAG, "added");
                                            flag_continue = false;
                                            cursor_aux.addRow(new Object[]{
                                                    cursor.getString(cursor.getColumnIndex(Contacts._ID)),
                                                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                                                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                                                    cursor.getString(cursor.getColumnIndex(Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                                                    cursor.getString(cursor.getColumnIndex(Utils.hasHoneycomb() ? Contacts.PHOTO_THUMBNAIL_URI : Contacts._ID))
                                            });
                                        }
                                    }
                                }
                                previuosPhone = previuosPhone + actualPhone + ";";
                            }

                        } while (cursor.moveToNext());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                    do {
                        actualPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "");

                        if (!previuosPhone.contains(actualPhone) && (actualPhone.matches("^\\+549\\d{8,12}$") || actualPhone.matches("^\\+569\\d{8}$") || actualPhone.matches("^\\+519\\d{8}$"))) {
                            cursor_aux.addRow(new Object[]{
                                    cursor.getString(cursor.getColumnIndex(Contacts._ID)),
                                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                                    cursor.getString(cursor.getColumnIndex(Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                                    cursor.getString(cursor.getColumnIndex(Utils.hasHoneycomb() ? Contacts.PHOTO_THUMBNAIL_URI : Contacts._ID))
                            });
                        }
                        previuosPhone = previuosPhone + actualPhone + ";";
                    } while (cursor.moveToNext());
                }
            }

            mAdapter.swapCursor(cursor_aux);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(TAG, "onLoaderReset");
        if (loader.getId() == ContactsQuery.QUERY_ID) {
            // When the loader is being reset, clear the cursor from the adapter. This allows the
            // cursor resources to be freed.
            mAdapter.swapCursor(null);
        }
    }

    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        // Resolve list item preferred height theme attribute into typedValue
        getActivity().getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);

        // Create a new DisplayMetrics object
        final DisplayMetrics metrics = new android.util.DisplayMetrics();

        // Populate the DisplayMetrics
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Return theme value based on DisplayMetrics
        return (int) typedValue.getDimension(metrics);
    }

    private Bitmap loadContactPhotoThumbnail(String photoData, int imageSize) {

        // Ensures the Fragment is still added to an activity. As this method is called in a
        // background thread, there's the possibility the Fragment is no longer attached and
        // added to an activity. If so, no need to spend resources loading the contact photo.
        if (!isAdded() || getActivity() == null) {
            return null;
        }

        // Instantiates an AssetFileDescriptor. Given a content Uri pointing to an image file, the
        // ContentResolver can return an AssetFileDescriptor for the file.
        AssetFileDescriptor afd = null;

        // This "try" block catches an Exception if the file descriptor returned from the Contacts
        // Provider doesn't point to an existing file.
        try {
            Uri thumbUri;
            // If Android 3.0 or later, converts the Uri passed as a string to a Uri object.
            if (Utils.hasHoneycomb()) {
                thumbUri = Uri.parse(photoData);
            } else {
                // For versions prior to Android 3.0, appends the string argument to the content
                // Uri for the Contacts table.
                final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_URI, photoData);

                // Appends the content Uri for the Contacts.Photo table to the previously
                // constructed contact Uri to yield a content URI for the thumbnail image
                thumbUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);
            }
            // Retrieves a file descriptor from the Contacts Provider. To learn more about this
            // feature, read the reference documentation for
            // ContentResolver#openAssetFileDescriptor.
            afd = getActivity().getContentResolver().openAssetFileDescriptor(thumbUri, "r");

            // Gets a FileDescriptor from the AssetFileDescriptor. A BitmapFactory object can
            // decode the contents of a file pointed to by a FileDescriptor into a Bitmap.
            FileDescriptor fileDescriptor = afd.getFileDescriptor();

            if (fileDescriptor != null) {
                // Decodes a Bitmap from the image pointed to by the FileDescriptor, and scales it
                // to the specified width and height
                return ImageLoader.decodeSampledBitmapFromDescriptor(fileDescriptor, imageSize, imageSize);
            }
        } catch (FileNotFoundException e) {
            // If the file pointed to by the thumbnail URI doesn't exist, or the file can't be
            // opened in "read" mode, ContentResolver.openAssetFileDescriptor throws a
            // FileNotFoundException.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Contact photo thumbnail not found for contact " + photoData
                        + ": " + e.toString());
            }
        } finally {
            // If an AssetFileDescriptor was returned, try to close it
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    // Closing a file descriptor might cause an IOException if the file is
                    // already closed. Nothing extra is needed to handle this.
                }
            }
        }

        // If the decoding failed, returns null
        return null;
    }

    private class ContactsAdapter extends CursorAdapter implements SectionIndexer {

        private LayoutInflater mInflater; // Stores the layout inflater
        private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
        private TextAppearanceSpan highlightTextSpan; // Stores the highlight text appearance style

        public ContactsAdapter(Context context) {
            super(context, null, 0);
            //ts = new TimeUtilites();
            // Stores inflater for use later
            mInflater = LayoutInflater.from(context);

            final String alphabet = context.getString(R.string.alphabet);

            mAlphabetIndexer = new AlphabetIndexer(null, ContactsQuery.SORT_KEY, alphabet);

        }

        private int indexOfSearchQuery(String displayName) {
            if (!TextUtils.isEmpty(mSearchTerm)) {
                return displayName.toLowerCase(Locale.getDefault()).indexOf(
                        mSearchTerm.toLowerCase(Locale.getDefault()));
            }
            return -1;
        }

        @Override
        public int getItemViewType(int position) {
            return IGNORE_ITEM_VIEW_TYPE;
        }

        /**
         * Overrides newView() to inflate the list item views.
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            // Inflates the list item layout.
            final View itemLayout =
                    mInflater.inflate(R.layout.contact_item, viewGroup, false);

            // Creates a new ViewHolder in which to store handles to each view resource. This
            // allows bindView() to retrieve stored references instead of calling findViewById for
            // each instance of the layout.
            final ViewHolder holder = new ViewHolder();
            holder.icon = (ImageView) itemLayout.findViewById(R.id.contactImage);
            holder.contact_name = (TextView) itemLayout.findViewById(R.id.resident_name);
            holder.contact_aux = (TextView) itemLayout.findViewById(R.id.contact_aux);
            holder.contact_mobile = (TextView) itemLayout.findViewById(R.id.contact_mobile);
            holder.tic_contact = (ImageView) itemLayout.findViewById(R.id.tic_contact);
            itemLayout.setTag(holder);

            // Returns the item layout view
            return itemLayout;
        }

        /**
         * Binds data from the Cursor to the provided view.
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Gets handles to individual view resources
            final ViewHolder holder = (ViewHolder) view.getTag();
            final String photoUri = cursor.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA);
            String displayName = cursor.getString(ContactsQuery.DISPLAY_NAME);
            final String displayNumber = cursor.getString(ContactsQuery.NUMBER).replace(" ", "");

            final int startIndex = indexOfSearchQuery(displayName);
            if (startIndex == -1) {
                // If the user didn't do a search, or the search string didn't match a display
                // name, show the display name without highlighting
                holder.contact_name.setText(displayName);
                holder.contact_mobile.setText(displayNumber);
                if (TextUtils.isEmpty(mSearchTerm)) {
                    // If the search search is empty, hide the second line of text
                    holder.contact_aux.setVisibility(View.GONE);
                } else {
                    // Shows a second line of text that indicates the search string matched
                    // something other than the display name
                    holder.contact_aux.setVisibility(View.VISIBLE);
                }
            } else {
                // Binds the SpannableString to the display name View object
                holder.contact_name.setText(displayName);
                holder.contact_mobile.setText(displayNumber);
                // Since the search string matched the name, this hides the secondary message
                holder.contact_aux.setVisibility(View.GONE);
            }

            // Generates the contact lookup Uri
            final Uri contactUri = Contacts.getLookupUri(
                    cursor.getLong(ContactsQuery.ID),
                    cursor.getString(ContactsQuery.ID));

            // Binds the contact's lookup Uri to the QuickContactBadge
            //holder.icon.assignContactUri(contactUri);

            // Loads the thumbnail image pointed to by photoUri into the QuickContactBadge in a
            // background worker thread
            mImageLoader.loadImage(photoUri, holder.icon);

            //Contact data = new Contact (contact_id, displayName, displayNumber, "", 1);
            if (!contact_selected_aux.contains(displayNumber)) {
                view.setBackgroundColor(Color.TRANSPARENT);
            } else {
                view.setBackgroundColor(Color.rgb(42, 46, 51));
                holder.tic_contact.setVisibility(View.VISIBLE);
            }
        }

        /**
         * Overrides swapCursor to move the new Cursor into the AlphabetIndex as well as the
         * CursorAdapter.
         */
        @Override
        public Cursor swapCursor(Cursor newCursor) {
            // Update the AlphabetIndexer with new cursor as well
            mAlphabetIndexer.setCursor(newCursor);
            return super.swapCursor(newCursor);
        }

        /**
         * An override of getCount that simplifies accessing the Cursor. If the Cursor is null,
         * getCount returns zero. As a result, no test for Cursor == null is needed.
         */
        @Override
        public int getCount() {
            if (getCursor() == null) {
                return 0;
            }
            return super.getCount();
        }

        /**
         * Defines the SectionIndexer.getSections() interface.
         */
        @Override
        public Object[] getSections() {
            return mAlphabetIndexer.getSections();
        }

        /**
         * Defines the SectionIndexer.getPositionForSection() interface.
         */
        @Override
        public int getPositionForSection(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getPositionForSection(i);
        }

        /**
         * Defines the SectionIndexer.getSectionForPosition() interface.
         */
        @Override
        public int getSectionForPosition(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getSectionForPosition(i);
        }

        /**
         * A class that defines fields for each resource ID in the list item layout. This allows
         * ContactsAdapter.newView() to store the IDs once, when it inflates the layout, instead of
         * calling findViewById in each iteration of bindView.
         */
        private class ViewHolder {
            TextView contact_name;
            TextView contact_aux;
            TextView contact_mobile;
            ImageView icon;
            ImageView tic_contact;
        }
    }

    public interface OnContactsInteractionListener {
        /**
         * Called when a contact is selected from the ListView.
         */
        void onContactSelected(String phoneNumbers, String names);

        /**
         * Called when the ListView selection is cleared like when
         * a contact search is taking place or is finishing.
         */
        void onSelectionCleared();
    }

    /**
     * This interface defines constants for the Cursor and CursorLoader, based on constants defined
     * in the {@link android.provider.ContactsContract.Contacts} class.
     */
    public interface ContactsQuery {

        int QUERY_ID = 1;

        Uri CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        //final static Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;

        Uri FILTER_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI;
        //final static Uri FILTER_URI = ContactsContract.Contacts.CONTENT_FILTER_URI;

        @SuppressLint("InlinedApi")
        String SELECTION =
                (Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) + "<>'' AND " +
                ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " = 1 AND "+
                //ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP + " = 1 AND " +
                "REPLACE(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ",' ','') LIKE '+%' ";

                //"REPLACE(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ",' ','') REGEXP '^+549\\d{8,12}$|^+569\\d{8}$|^+519\\d{8}'";
                /*"("+ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE '+569%' OR "+
                ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE '+56 9%') AND "+
                "LENGTH(REPLACE("+ContactsContract.CommonDataKinds.Phone.NUMBER+",' ','')) = 12";*/

        @SuppressLint("InlinedApi")
        String SORT_ORDER =
                Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

        @SuppressLint("InlinedApi")
        String[] PROJECTION = {
            Contacts._ID,
            //Contacts.LOOKUP_KEY,
            Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER, //ContactsContract.RawContacts.ACCOUNT_TYPE
            SORT_ORDER,
            Utils.hasHoneycomb() ? Contacts.PHOTO_THUMBNAIL_URI : Contacts._ID,
        };

        // The query column numbers which map to each value in the projection
        int ID = 0;
        //final static int LOOKUP_KEY = 1;
        int DISPLAY_NAME = 1;
        int NUMBER = 2;
        int SORT_KEY = 3;
        int PHOTO_THUMBNAIL_DATA = 4;
    }

}

