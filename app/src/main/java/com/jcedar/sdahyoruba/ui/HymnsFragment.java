package com.jcedar.sdahyoruba.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jcedar.sdahyoruba.R;
import com.jcedar.sdahyoruba.adapter.AutoCompleteAdapter;
import com.jcedar.sdahyoruba.helper.AccountUtils;
import com.jcedar.sdahyoruba.helper.FileUtils;
import com.jcedar.sdahyoruba.helper.FontChangeCrawler;
import com.jcedar.sdahyoruba.provider.DataContract;
import com.jcedar.sdahyoruba.provider.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class HymnsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARGS_ALL_STUDENT_ID = "all_student_id";
    private static final String ARG_PARAM1 = "param1";
    private static final String TAG = HymnsFragment.class.getSimpleName();
    private static final String ARGS_HYMN_ID = "hymn_id";
    private static final String LAST_ITEM = "last_item";
    private static final String DATA_URI = "data_uri";
    private Handler handler;
    private  Uri dataUri;
    private TextView hymnTitle, hymnText, hymnEnglishVersion, hymnNumber;
    private long hymnId ;
    private String nameStr = "";
    MenuInflater inflater;
   private String presentId;
    private View rootView;
    ListAdapter adapter;
    private SearchView searchView;


    public HymnsFragment() {
    }


    public static HymnsFragment newInstance(long hymnId) {
        HymnsFragment fragment = new HymnsFragment();

        Bundle args = new Bundle();
        args.putLong(ARGS_HYMN_ID, hymnId);

        fragment.setArguments(args);

        return fragment;
    }
    public static HymnsFragment newInstance(int position,
                                                        NewDashBoardActivity hostActivity) {

        HymnsFragment fragment = new HymnsFragment();
        Bundle args = new Bundle();
        //long _id = hostActivity.fragments.get(position);
        args.putLong(ARGS_HYMN_ID, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        Bundle args = getArguments();
        if (args != null) {
            hymnId = args.getLong(ARGS_HYMN_ID);
            dataUri = DataContract.Hymns.buildHymnUri(hymnId);
            Log.e(TAG, dataUri + " uri inside ");
        }
        setHasOptionsMenu(true);
        //setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setHymnTextFont();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "present id onPause " + presentId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        outState.putLong(LAST_ITEM, Long.parseLong(presentId));
        Log.e(TAG, "present id saveInstant " + presentId);
        super.onSaveInstanceState(outState);

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * init loader should be here
         */
        getLoaderManager().initLoader(0, Bundle.EMPTY, this);
        //changing the fonts
        FontChangeCrawler fontChanger =
                new FontChangeCrawler(getActivity().getAssets(),
                        "fonts/proxima-nova-regular.ttf");
        fontChanger.replaceFonts((ViewGroup) this.getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if( rootView != null ){
            ((ViewGroup) rootView.getParent()).removeView(rootView);

            return rootView;
        }

        rootView =  inflater.inflate(R.layout.fragment_hymns, container, false);
        hymnText = (TextView) rootView.findViewById(R.id.tvHymnText);

        setHymnTextFont();
        hymnText.setTextIsSelectable(true);
        hymnText.setCustomSelectionActionModeCallback(new ActionProvider());

        hymnText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getActivity().startActionMode(new ActionProvider());
                return false;
            }
        });
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getActivity().setTitle("SDAH Yoruba");
            }
        });


        return rootView;
    }

    public void setHymnTextFont(){
        int fontSize = AccountUtils.getFontSettings(getActivity());
        hymnText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            return new CursorLoader(getActivity(), dataUri,
                DataContract.Hymns.PROJECTION_ALL, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }

        if(data != null && data.moveToFirst()) {

            String toDisplay = "";
            presentId = data.getString(
                    data.getColumnIndexOrThrow(DataContract.Hymns._ID));

             String numberStr = data.getString(
                    data.getColumnIndexOrThrow(DataContract.Hymns.SONG_ID));

            nameStr = data.getString(
                                data.getColumnIndexOrThrow(DataContract.Hymns.SONG_NAME));

            String formattedName = FileUtils.removeBrackets(nameStr).trim() ;
            String title = "<b>"+numberStr +" - "+formattedName+" </b> <br /><br />";


            String engVersionStr = data.getString(
                    data.getColumnIndexOrThrow(DataContract.Hymns.ENGLISH_VERSION));
            /*if(engVersionStr == null) hymnEnglishVersion.setVisibility(View.GONE);
            else  hymnEnglishVersion.setText(engVersionStr.trim());*/
            String engV = "";
            if(engVersionStr != null) engV = "<i>"+engVersionStr.trim()+"</i> <br /><br />";



            String text = data.getString(
                    data.getColumnIndexOrThrow(DataContract.Hymns.SONG_TEXT));

            String text1 = text.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");

            //Spanned toPresent = Html.fromHtml(text1.trim());
            //hymnText.setText(toPresent);
            toDisplay += title;
            toDisplay += engV ;
            toDisplay += text1.trim();

            hymnText.setText( Html.fromHtml(toDisplay));


            data.close();
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (!isAdded()) {
                return;
            }
            //getLoaderManager().restartLoader(0, null, HymnsFragment.this);
        }
    };



    public interface Listener{
         void onFragmentAttached(Fragment fragment);
         void onFragmentDetached(Fragment fragment);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //this.inflater = inflater;
        //menu.close();

        menu.clear();
        inflater.inflate(R.menu.menu_hymns_fragment, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search Hymn");

        searchView.setSuggestionsAdapter(new AutoCompleteAdapter(getActivity(), null));

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String songId = cursor.getString(
                        cursor.getColumnIndex( DataContract.Hymns.SONG_ID ));
                long id = Long.parseLong(songId);
                goToSelectedHymn(id);
                cursor.close();
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String songId = cursor.getString(
                        cursor.getColumnIndex( DataContract.Hymns.SONG_ID ));
                long id = Long.parseLong(songId);
                goToSelectedHymn(id);
                cursor.close();
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    Toast.makeText(getActivity(),
                            "Query == "+query, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Query == " + query);
                    new FetchDataFromDb().execute(query);
                } else{
                    searchView.getSuggestionsAdapter().changeCursor(null);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    new FetchDataFromDb().execute(newText);
                } else{
                    searchView.getSuggestionsAdapter().changeCursor(null);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case ( R.id.action_settings):
                 startActivity( new Intent(getActivity(), Settings.class));
                break;

            default:
                return super.onOptionsItemSelected(item);

        }
        //stopDialog();
        return true;
    }

    public int goToSelectedHymn(long hymnId) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if( getActivity().getSupportFragmentManager().findFragmentById(R.id.frame) != null){

            Log.e(TAG, "fragment isnt null");
            fm.beginTransaction()
                    .remove(getActivity().getSupportFragmentManager()
                            .findFragmentById(R.id.frame))
                    .commit();
        }

        FragmentTransaction ft1 = getActivity().getSupportFragmentManager().beginTransaction();
        ft1.add(R.id.frame, PagerFragment.newInstance(hymnId), "HOME")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        return 0;
    }

    public class FetchDataFromDb extends AsyncTask<String, Void, Cursor>{

        @Override
        protected Cursor doInBackground(String... params) {
            return new DatabaseHelper(getActivity()).getHymnsCursor(params[0]);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            searchView.getSuggestionsAdapter().changeCursor(cursor);
            super.onPostExecute(cursor);
        }
    }

    private class ActionProvider implements ActionMode.Callback{

        List<Integer> mOptionList = setOptionLists();

        private List<Integer> setOptionLists() {
            List<Integer> mOptionList = new ArrayList<>();
            mOptionList.add(R.id.action_copy);
            mOptionList.add(R.id.action_share);
            return mOptionList;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getActivity().getMenuInflater().inflate(R.menu.menu_copy_paste, menu);
            //mode.getMenuInflater().inflate(R.menu.menu_copy_paste, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            for (int i = 0; i < menu.size(); i++){
                MenuItem item = menu.getItem(i);
                if( !mOptionList.contains(item.getItemId()))
                    item.setVisible(false);
            }
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch ( item.getItemId() ){
                case R.id.action_copy:
                    //Toast.makeText(getActivity(), "Copy", Toast.LENGTH_SHORT).show();
                    copy(getSelectedString());
                    mode.finish();
                    break;

                case R.id.action_share:
                    //Toast.makeText(getActivity(), "Share", Toast.LENGTH_SHORT).show();
                    share(getSelectedString());
                    mode.finish();
                    break;

            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        private void share(String stringToShare){
            if(stringToShare.length() <= 0) {
                Toast.makeText(getActivity(), "Select at least one word", Toast.LENGTH_SHORT).show();
                return;
            }
            stringToShare +="\n\nSDAH Yoruba (c) 2016";


            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, stringToShare);

            startActivity(Intent.createChooser(intent,
                    getResources().getString(R.string.action_share_hymn)));
        }

        private void copy(String string){
            if(string.length() <= 0) {
                Toast.makeText(getActivity(), R.string.select_one, Toast.LENGTH_SHORT).show();
                return;
            }
                ClipboardManager manager = (ClipboardManager)
                        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("hymn", string);
                manager.setPrimaryClip(clip);
                Toast.makeText(getActivity(), R.string.copied, Toast.LENGTH_SHORT).show();

        }

        private String getSelectedString(){
            int min = 0;
            int max = hymnText.getText().length();

            if(hymnText.isFocused()){
                final int selStart = hymnText.getSelectionStart();
                final int selEnd = hymnText.getSelectionEnd();

                min = Math.max(0, Math.min(selStart, selEnd));
                max = Math.max(0, Math.max(selStart, selEnd));
            }
            return hymnText.getText().subSequence(min, max).toString();
        }
    }

}
