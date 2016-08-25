package app.android.nino.myfilemanager;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SubMenu;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private File root;
    private boolean hasStoragePermission;
    private ArrayList<File> fileList = new ArrayList<File>();
    private ArrayList<File> dirList = new ArrayList<File>();
    private ArrayList<String> fileDirLlist = new ArrayList<String>();
    private ArrayList<String> fileSizeList = new ArrayList<String>();
    private ArrayList<Integer> iconFileDirLlist = new ArrayList<Integer>();
    private ArrayList<Date> fileModDate = new ArrayList<Date>();
    private ArrayList<Bitmap> thumbs=new ArrayList<Bitmap>();
    //private LinearLayout rootLayOut;
    private LinearLayout layoutToolbar;
    private HorizontalScrollView hScrollView;
    private RelativeLayout frame;
    private LinearLayout.LayoutParams lp;
    private SearchView search;
    //private LinearLayout mainLinearLayout;
    //private TextView pathBar;
    private CoordinatorLayout coLayout;
    //static int tvPadding = 10;
    private Toolbar toolbar;
    private static final String MYPATH = "pathKey";
    private static final String MYQUICKLINKS = "linkKey";
    private static final String MYVIEW = "viewKey";
    private static final String MyPREFERENCES = "MyPrefs" ;
    //private float textSize;
    private EditText editText;
    private InputMethodManager imm;
    private ListView lv;
    private GridView gv;
    private CustomAdapter myAdapter;
    Calendar calendar;
    private boolean selectionMode;
    private com.github.clans.fab.FloatingActionMenu fam_select;
    private com.github.clans.fab.FloatingActionMenu fam_nonselect;
    private  com.github.clans.fab.FloatingActionButton fab_fav;
    private  com.github.clans.fab.FloatingActionButton fab_newFolder;
    private  com.github.clans.fab.FloatingActionButton fab_sort;
    private  com.github.clans.fab.FloatingActionButton fab_rename;
    private  com.github.clans.fab.FloatingActionButton fab_del;
    private  com.github.clans.fab.FloatingActionButton fab_copy;
    private  com.github.clans.fab.FloatingActionButton fab_cut;
    private  LinearLayout llSelectAll;
    private CheckBox cbSelectAll;
    private Integer quickSortBy;
    private MyyFileList myFiles;
    private MyyFileList myDirs;
    private Integer displayStyle;
    private Boolean folderFirst;
    private Integer imgDisplayType;
    private Integer listViewType;
    private LruCache<String, Bitmap> mMemoryCache;
    private View currAttachedViewType;
    private SharedPreferences sharedpreferences;
    private NavigationView navigationView;
    private SubMenu subMenu;
    private String[] quickLinkTitle;
    private String[] quickLinkPath;

    private DrawerLayout mDrawerLayout;
    ExpandableListAdapter mMenuAdapter;
    ExpandableListView expandableList;
    List<ExpandedMenuModel> listDataHeader;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Bitmap cache
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        // Set initial values.
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        selectionMode=false;
        quickSortBy=1;
        displayStyle=1;
        folderFirst=true;
        imgDisplayType=1;
        listViewType= sharedpreferences.getInt(MYVIEW,1);

        // Initialise latyout objects
        calendar = Calendar.getInstance();                                                          // This is needed for getting the last modified date of file objects.


        //TEST
        final ActionBar ab = getSupportActionBar();
//        ab.setHomeAsUpIndicator(android.R.drawable.ic_menu_add);
   //     ab.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        expandableList = (ExpandableListView) findViewById(R.id.navigationmenu);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        prepareListData();
        mMenuAdapter = new ExpandableListAdapter(MainActivity.this, listDataHeader, listDataChild, expandableList);

        // setting list adapter
        expandableList.setAdapter(mMenuAdapter);

        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                //Log.d("DEBUG", "submenu item clicked");
                return false;
            }
        });
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                //Log.d("DEBUG", "heading clicked");
                return false;
            }
        });
        // TEST

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        coLayout=(CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        frame=(RelativeLayout) findViewById(R.id.Frame_Container);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(null);
        // mainLinearLayout= (LinearLayout) findViewById(R.id.mianLinearLayout);
        editText= (EditText) findViewById(R.id.editText);
        layoutToolbar= (LinearLayout) toolbar.findViewById(R.id.toolbar_item_container);
        hScrollView=(HorizontalScrollView) toolbar.findViewById(R.id.header_scrollview);
        search = (SearchView) findViewById(R.id.searchView_main);                                   // Searchview in the toolbar
        lp = (LinearLayout.LayoutParams) search.getLayoutParams();                                  // Layout params of of Searchview. Get this so that it can be restored later.

        // set list view
        setViewType(null);

        // Initialise floating action menu (FAM) objects
        fam_select=(com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fam_select);
        fam_nonselect=(com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fam_nonselect);
        fam_select.setClosedOnTouchOutside(true);
        fam_nonselect.setClosedOnTouchOutside(true);

        // SET FLOATING ACTION BUTTON LISTENERS

        fab_fav= (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_fav);
        fab_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Actions when add to favourites option is selected.

                Set<String> quickLinkspref= sharedpreferences.getStringSet(MYQUICKLINKS,null);
                Set<String> quickLinks;
                if (quickLinkspref==null){
                    quickLinks= new HashSet<String>();
                } else {
                     quickLinks=quickLinkspref;
                }

                SharedPreferences.Editor editor = sharedpreferences.edit();
                String currPath=sharedpreferences.getString(MYPATH,null);

                if (currPath.equals(Environment.getExternalStorageDirectory().toString())) {
                    Snackbar.make(view, "No need to add this to Quick Links. Use the Home shortcut instead",  Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {
                    if (!quickLinks.contains(currPath)){
                        quickLinks.add(currPath);
                        editor.putStringSet(MYQUICKLINKS,quickLinks);
                        editor.apply();
                        prepareListData();
                        mMenuAdapter.refreshExpadableList(MainActivity.this, listDataHeader, listDataChild, expandableList);
                        Snackbar.make(view, "Current location added to quick links.",  Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    } else {
                        Snackbar.make(view, "Current location is already in your quick links" ,  Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    }
                }


                updateFab(0);

            }
        });

        fab_newFolder= (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_folderadd);
        fab_newFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Actions when add new folder option is selected.
                updateFab(0);
                Snackbar.make(view, "New folder added.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        fab_sort= (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_sort);
        fab_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Actions when quick sort option is selected.
                updateFab(0);

                // 1  name asc - DEFAULT
                // 2  name desc
                // 3  size asc
                // 4  size desc
                // 5  date asc
                // 6  date desc

                final RadioGroup group = new RadioGroup(MainActivity.this);
                group.setOrientation(RadioGroup.VERTICAL);
                group.setPadding(30,30,0,0);
                RadioButton btn1 = new RadioButton(MainActivity.this);
                btn1.setText("Ascending by file name");
                group.addView(btn1);
                RadioButton btn2 = new RadioButton(MainActivity.this);
                btn2.setText("Descending by file name");
                group.addView(btn2);
                RadioButton btn3 = new RadioButton(MainActivity.this);
                btn3.setText("Ascending by file size");
                group.addView(btn3);
                RadioButton btn4 = new RadioButton(MainActivity.this);
                btn4.setText("Descending by file size");
                group.addView(btn4);
                RadioButton btn5 = new RadioButton(MainActivity.this);
                btn5.setText("Ascending by last modified date");
                group.addView(btn5);
                RadioButton btn6 = new RadioButton(MainActivity.this);
                btn6.setText("Descending by last modified date");
                group.addView(btn6);

                switch (quickSortBy){
                    case 6: group.check(btn6.getId());
                        break;
                    case 5: group.check(btn5.getId());
                        break;
                    case 4: group.check(btn4.getId());
                        break;
                   case 3: group.check(btn3.getId());
                           break;
                    case 2: group.check(btn2.getId());
                             break;
                    case 1:group.check(btn1.getId());
                        break;
                    default:group.check(btn1.getId());
                        break;
                }

                final Integer btn1ID=btn1.getId();
                final Integer btn2ID=btn2.getId();
                final Integer btn3ID=btn3.getId();
                final Integer btn4ID=btn4.getId();
                final Integer btn5ID=btn5.getId();
                final Integer btn6ID=btn6.getId();

                group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId==btn1ID) {
                            quickSortBy=1;
                        } else if (checkedId==btn2ID)  {
                            quickSortBy=2;
                        } else if (checkedId==btn3ID) {
                            quickSortBy=3;
                        } else if (checkedId==btn4ID) {
                            quickSortBy=4;
                        } else if (checkedId==btn5ID) {
                            quickSortBy=5;
                        } else if (checkedId==btn6ID) {
                            quickSortBy=6;
                        } else {
                            quickSortBy=1;
                        }
                    }



                });



                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle(R.string.sort_dialog_title );
                builder.setView(group);
                builder.setPositiveButton(R.string.continue_response_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                                final File currPath = new File(sharedpreferences.getString(MYPATH, null));
                                displayStyle=quickSortBy;
                                populateScreen(currPath);
                                 dialog.dismiss();
                            }
                        });
                builder.setNegativeButton(R.string.cancel_response_button, null);
                builder.show();
            }
        });

        fab_rename= (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_rename);
        fab_rename.setOnClickListener(new View.OnClickListener() {
            // Actions when rename option is selected.
            @Override
            public void onClick(View view) {
                updateFab(0);

            }
        });

        fab_del= (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_delete);
        fab_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Actions when delete option is selected.
                updateFab(0);

                // Initialise and design delete dialog elements.
                ScrollView diagScroll=new ScrollView((MainActivity.this));
                LinearLayout diagLayout=new LinearLayout(MainActivity.this);
                diagLayout.setOrientation(LinearLayout.VERTICAL);
                diagLayout.setPadding(0,60,0,0);

                if (myAdapter.getCheckedCOunt()>0) {

                    boolean[] checkedItems=myAdapter.getSelection();
                    for (int i=0;i<checkedItems.length;i++){
                        if (checkedItems[i]) {
                            TextView textView = new TextView(MainActivity.this);
                            textView.setText(" " + dirList.get(i).getName());
                            textView.setSingleLine();
                            textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
                            llp.setMargins(60, 0, 60, 20); // llp.setMargins(left, top, right, bottom);
                            textView.setLayoutParams(llp);
                            textView.setCompoundDrawablesWithIntrinsicBounds(getFileIcon(dirList.get(i)),0, 0,  0);
                            diagLayout.addView(textView);
                         }
                    }

                    diagScroll.addView(diagLayout);

                    // Create delete dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                    builder.setTitle(R.string.delete_dialog_title );
                    builder.setView(diagScroll);
                    builder.setPositiveButton(R.string.positive_response_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Snackbar.make(coLayout, "Deleted!", Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null).show();

                                    onBackPressed();
                                    dialog.dismiss();
                                }
                            });
                    builder.setNegativeButton(R.string.negative_response_button, null);
                    builder.show();

                } else {

                    Snackbar.make(view, "You have not selected anything.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }

            }
        });


        fab_copy= (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_copy);
        fab_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Actions when copy option is selected.
                updateFab(0);
                Snackbar.make(view, "Copy.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        fab_cut= (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_cut);
        fab_cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Actions when cut option is selected.
                updateFab(0);
                Snackbar.make(view, "Cut.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });


        // Set action listeners for the searchview in the toolbar.
        search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the search icon is clicked.
                search.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                hScrollView.setVisibility(View.GONE);
            }
        });

        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // When the searchview is closed.
                hScrollView.setVisibility(View.VISIBLE);
                search.setLayoutParams(lp);
                return false;
            }
        });


        // Check if app has permission to read external storage
        if (canMakeSmores()) {

            // Check if app already has permission.
            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE);

            // If app has no storage permission, request for it.
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        225);
            } else {
                hasStoragePermission=true;
            }
        } else {
            hasStoragePermission=true;

        }

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());

        if (savedInstanceState == null) {

            if (hasStoragePermission) {

                populateScreen(root);
            }

        } else { // savedInstanceState has saved values

           // myInt = savedInstanceState.getInt("MyIntKey");
            // restore display style
            displayStyle=savedInstanceState.getInt("displayStyle");
            String currPathStr =savedInstanceState.getString("currPathKey");
            populateScreen(new File(currPathStr));

            // Restore selection mode
            selectionMode=savedInstanceState.getBoolean("selectionModeKey");
            if (selectionMode) {
                selectionMode(null);
                myAdapter.setSelection(savedInstanceState.getBooleanArray("selectionKey"));
                setMenuType();
                if (savedInstanceState.getBoolean("famSelectOpen")) fam_select.open(true);
            } else {
                if (savedInstanceState.getBoolean("famNoSelectOpen")) fam_nonselect.open(true);
            }
        }

    }


    private void prepareListData() {
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();

        Log.d("METHODCALLS","prepareListDate");

        ExpandedMenuModel item1 = new ExpandedMenuModel();
        item1.setIconName("This Phone");
        item1.setIconImg(R.drawable.phone_dark);
        // Adding data header
        listDataHeader.add(item1);

        ExpandedMenuModel item2 = new ExpandedMenuModel();
        item2.setIconName("Cloud Storage");
        item2.setIconImg(R.drawable.cloud_dark);
        listDataHeader.add(item2);

        ExpandedMenuModel item3 = new ExpandedMenuModel();
        item3.setIconName("Quick Links");
        item3.setIconImg(R.drawable.link_dark);
        listDataHeader.add(item3);


        // Adding child data
        List<String> heading1 = new ArrayList<String>();
        heading1.add("Internal Storage");

        if (isExternalStorageAvailable()) heading1.add("External SD Card");

        List<String> heading2 = new ArrayList<String>();
        heading2.add("Google Drive");
        heading2.add("Drop Box");

        List<String> heading3 = new ArrayList<String>();
        Set<String> quickLinkList=sharedpreferences.getStringSet(MYQUICKLINKS,null);
        if (quickLinkList!=null) {
            quickLinkTitle=null;
            quickLinkPath=null;
            quickLinkTitle=new String[quickLinkList.size()];
            quickLinkPath=new String[quickLinkList.size()];
            Integer i=0;
            for (Iterator<String> it = quickLinkList.iterator(); it.hasNext(); ) {
                String f = it.next();
                String folderName=new File(f).getName();
                heading3.add(folderName);
                quickLinkTitle[i]=folderName;
                quickLinkPath[i]=f;
                i++;
            }
        }

        listDataChild.put(listDataHeader.get(0), heading1);// Header, Child data
        listDataChild.put(listDataHeader.get(1), heading2);
        listDataChild.put(listDataHeader.get(2), heading3);

    }

    private void setupDrawerContent(NavigationView navigationView) {
        //revision: this don't works, use setOnChildClickListener() and setOnGroupClickListener() above instead
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save current path
        //SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final File currPath = new File(sharedpreferences.getString(MYPATH, null));
        savedInstanceState.putString("currPathKey", currPath.toString());

        // Save selection mode
        savedInstanceState.putBoolean("selectionModeKey",selectionMode);
        if (selectionMode) {
            boolean[] currSelect=myAdapter.getSelection();
            savedInstanceState.putBooleanArray("selectionKey",currSelect);
        }

        // Save fab/fam state
        savedInstanceState.putBoolean("famSelectOpen",fam_select.isOpened());
        savedInstanceState.putBoolean("famNoSelectOpen",fam_nonselect.isOpened());

        // Save display style
        savedInstanceState.putInt("displayStyle",displayStyle);

        super.onSaveInstanceState(savedInstanceState);
    }

    private boolean canMakeSmores(){
        // Check if device OS is MM.
        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 225: {
                hasStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (hasStoragePermission)  {
                    root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                    populateScreen(root);
                } else {
                    ActivityCompat.finishAffinity(this);
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request

        }
    }

    public void setMenuType() {

        // Depending on the number of file obejects selected, this will enable or disable certain
        // fab buttons. Triggered by click on checkbox in main listview.
        if(myAdapter.getCheckedCOunt()>1) {
              if (fam_select.getChildCount()==10) fam_select.removeMenuButton(fab_rename);
        } else {
            if (fam_select.getChildCount()!=10)  fam_select.addMenuButton(fab_rename,0);
        }
    }

    public void selectionMode(Integer position) {

        // show select all checkbox
        if (position!=null) myAdapter.selectSpecificItem(position);
        showSelectAll(true);
        selectionMode = true;
        myAdapter.hideCheckboxes(false);
        updateFab(2);

    }

    public void showSelectAll(Boolean isvisible) {

        if (isvisible) {
            llSelectAll.setVisibility(View.VISIBLE);
        } else {
            llSelectAll.setVisibility(View.GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void populateScreen(File dir) {

        showSelectAll(false);
        setPathbar(dir.toString());
        getfile(dir);

        dirList.addAll(fileList);

        fileDirLlist.clear();
        iconFileDirLlist.clear();
        fileSizeList.clear();
        thumbs.clear();

        // 1  name asc - DEFAULT
        // 2  name desc
        // 3  size asc
        // 4  size desc
        // 5  date asc
        // 6  date desc

        switch (displayStyle) {

            case 1: myFiles.sortByFileName(myFiles.FILE_NAME_ASC);
                myDirs.sortByFileName(myFiles.FILE_NAME_ASC);
                break;
            case 2: myFiles.sortByFileName(myFiles.FILE_NAME_DESC);
                myDirs.sortByFileName(myFiles.FILE_NAME_DESC);
                break;
            case 3: myFiles.sortByFileSize(myFiles.FILE_NAME_ASC);
                myDirs.sortByFileSize(myFiles.FILE_NAME_ASC);
                break;
            case 4: myFiles.sortByFileSize(myFiles.FILE_NAME_DESC);
                myDirs.sortByFileSize(myFiles.FILE_NAME_DESC);
                break;
            case 5: myFiles.sortByFileDate(myFiles.FILE_NAME_ASC);
                myDirs.sortByFileDate(myFiles.FILE_NAME_ASC);
                break;
            case 6: myFiles.sortByFileDate(myFiles.FILE_NAME_DESC);
                myDirs.sortByFileDate(myFiles.FILE_NAME_DESC);
                break;
        }

        if (folderFirst ) {

            for (Integer i=0;i<=myDirs.size();i++){
                fileDirLlist.add(myDirs.getFile(i).getName());
                iconFileDirLlist.add(myDirs.getIcon(i));
                fileSizeList.add(myDirs.getSize(i));
                fileModDate.add(myDirs.getDate(i));

            }

            for (Integer i=0;i<=myFiles.size();i++){
                Log.e("FETCHED_DATE", "Date at " + i + " is:" + myFiles.getDate(i).toString());
                fileDirLlist.add(myFiles.getFile(i).getName());
                iconFileDirLlist.add(myFiles.getIcon(i));
                fileSizeList.add(myFiles.getSize(i));
                fileModDate.add(myFiles.getDate(i));
            }

        } else  {

            for (Integer i=0;i<=myFiles.size();i++){
                Log.e("FETCHED_DATE", "Date at " + i + " is:" + myFiles.getDate(i).toString());
                fileDirLlist.add(myFiles.getFile(i).getName());
                iconFileDirLlist.add(myFiles.getIcon(i));
                fileSizeList.add(myFiles.getSize(i));
                fileModDate.add(myFiles.getDate(i));
            }
            for (Integer i=0;i<=myDirs.size();i++){
                fileDirLlist.add(myDirs.getFile(i).getName());
                iconFileDirLlist.add(myDirs.getIcon(i));
                fileSizeList.add(myDirs.getSize(i));
                fileModDate.add(myDirs.getDate(i));

            }
        }

        //thumbs=generateThumbList(fileDirLlist);

            if (myAdapter!=null) {

                //myAdapter.refreshEvents(this,fileDirLlist,iconFileDirLlist,fileModDate,fileSizeList,thumbs,dir.toString(),imgDisplayType);
                myAdapter.refreshEvents(listViewType,mMemoryCache,this,fileDirLlist,iconFileDirLlist,fileModDate,fileSizeList,dir.toString(),imgDisplayType);
                updateFab(0);
                selectionMode=false;
               // myAdapter.hideCheckboxes(false);

            } else {
               // myAdapter=new CustomAdapter(this, fileDirLlist,iconFileDirLlist,fileModDate,fileSizeList,thumbs,dir.toString(),imgDisplayType);
                myAdapter=new CustomAdapter(listViewType,mMemoryCache,this, fileDirLlist,iconFileDirLlist,fileModDate,fileSizeList,dir.toString(),imgDisplayType);
                if (listViewType.equals(1)) {
                    lv.setAdapter(myAdapter);
                } else {
                    gv.setAdapter(myAdapter);
                }


             //   lv.setDivider(null);
            }
        }

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }


    public Integer getFileIcon(File dir) {

        Integer returnVal;

        if (dir.isDirectory()) {
            returnVal=R.drawable.folder;

        } else
        {

            String extension1="xxx";
            String[] fileNameArray;

            if (dir.getName().contains(".")) {
                fileNameArray=dir.getName().split("\\.");
                extension1=fileNameArray[fileNameArray.length-1];
            } else {
                extension1="1";
            }

            String extension="blank";
            if (!isInteger(extension1)) {
                switch (extension1) {
                    case "class":
                        extension = "xclass";
                        break;
                    case "7z":
                        extension = "x7z";
                        break;
                    case "3gp":
                        extension = "x3gp";
                        break;
                    case "3g2":
                        extension = "x3g2";
                        break;
                    case "3ds":
                        extension = "x3ds";
                        break;
                    case "3dm":
                        extension = "x3dm";
                        break;
                    case "1":
                        extension = "blank";
                        break;
                    default:
                        extension = extension1;
                        break;
                }
            }

            int checkExistence = getResources().getIdentifier(extension, "drawable", this.getPackageName());
            final Integer resID;
            if (checkExistence != 0) {  // the resouce exists...
                resID=getResources().getIdentifier(extension, "drawable", this.getPackageName());

            } else {  // checkExistence == 0  // the resouce does NOT exist!!
                resID=getResources().getIdentifier(extension, "blank", this.getPackageName());
            }

            returnVal= resID;

        }

        return returnVal;

    }

    public static String getFileSize(final File file)
    {
        float fileSize;
        String value;


            fileSize=file.length();
            if (fileSize<=1048576) {
                value= String.format("%.2f", fileSize/1048576) + " kb";
            }else if (fileSize<=1073741824) {
                value=String.format("%.2f", ((fileSize)/1024)/1024) + " mb";

            } else {
                value=String.format("%.2f", (((fileSize)/1024)/1024)/1024) + " gb";
            }

        return value ;

    }


    public void getfile(File dir) {

        dirList.clear();
        fileList.clear();

        Log.d("SHORTCUTCHECK",dir.getAbsolutePath().toString());

        File listFile[] = dir.listFiles();
        File currFile;

       Arrays.sort(listFile); // Sorts list alphabetically by default

        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

                currFile=listFile[i];

                if (currFile.isFile()) {
                        fileList.add(currFile);


                    } else {

                        dirList.add(currFile);

                }
            }
        }

       // return fileList;

        myDirs=new MyyFileList(MainActivity.this,dirList);
        myFiles=new MyyFileList(MainActivity.this,fileList);
    }

    public void updateFab (Integer x) {


        // x = 0 no change, hide menu if open
        // x =1 show add fam_nonselect
        // x =2 show add fam_select



        switch (x) {
            case 0: if (fam_nonselect.getVisibility() != View.GONE) {
                if (fam_nonselect.isOpened()) {
                    fam_nonselect.close(true);
                }
            }
                if (fam_select.getVisibility() != View.GONE) {
                    if (fam_select.isOpened()) {
                        fam_select.close(true);
                    }
                }
                break;
            case 1:

                if(fam_select.isOpened()){
                    fam_select.close(true);
                    new CountDownTimer(500, 100) {
                        public void onFinish() {
                            // When timer is finished
                            // Execute your code here
                            fam_select.setVisibility(View.GONE);
                            fam_nonselect.setVisibility(View.VISIBLE);
                        }
                        public void onTick(long millisUntilFinished) {
                            // millisUntilFinished    The amount of time until finished.
                        }
                    }.start();
                } else {
                    fam_select.setVisibility(View.GONE);
                    fam_nonselect.setVisibility(View.VISIBLE);
                }
                break;
            case 2:
                if(fam_nonselect.isOpened()){
                    fam_nonselect.close(true);
                    new CountDownTimer(500, 100) {
                        public void onFinish() {
                            // When timer is finished
                            // Execute your code here
                            fam_nonselect.setVisibility(View.GONE);
                            fam_select.setVisibility(View.VISIBLE);
                        }
                        public void onTick(long millisUntilFinished) {
                            // millisUntilFinished    The amount of time until finished.
                        }
                    }.start();
                } else {
                    fam_nonselect.setVisibility(View.GONE);
                    fam_select.setVisibility(View.VISIBLE);
                }

                    break;
        }


    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (selectionMode) {
                updateFab(1);
                myAdapter.removeSelection();
                myAdapter.hideCheckboxes(true);
                cbSelectAll.setChecked(false);
                showSelectAll(false);
                selectionMode=false;
            } else {

                if ( fam_nonselect.isOpened()) {
                    updateFab(0);
                } else {
                    //SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                    final File currPath = new File(sharedpreferences.getString(MYPATH, null));

                    if (root.equals(currPath)) {

                        Snackbar.make(coLayout, "This action requires root access.", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    } else {
                        populateScreen(currPath.getParentFile());
                    }
                }

            }
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //MenuItem item= menu.findItem(R.id.action_settings);
        //item.setVisible(false);
        MenuItem item=menu.getItem(0);
        if (listViewType!=1){
            item.setTitle(R.string.action_list_view);
        } else {
            item.setTitle(R.string.action_grid_view);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Here is just a good place to update item
        MenuItem item=menu.getItem(0);
        if (listViewType!=1){
            item.setTitle(R.string.action_list_view);
        } else {
            item.setTitle(R.string.action_grid_view);
        }
        return true;
    }


    public void setViewType(File currFile){
        //ViewGroup parent = (ViewGroup) currAttachedViewType.getParent();
        frame.removeAllViews();
        if (listViewType.equals(1)){
            currAttachedViewType = getLayoutInflater().inflate(R.layout.content_main_detail, null);
            frame.addView(currAttachedViewType);
            lv=(ListView) findViewById(R.id.mainListView);
            llSelectAll=(LinearLayout) findViewById(R.id.selectAllLayout);
            showSelectAll(false);                                                                          // hide select all view until needed during file ops
            cbSelectAll =(CheckBox) findViewById(R.id.selectAll) ;
            cbSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // update your model (or other business logic) based on isChecked
                    if (isChecked) {
                        myAdapter.selectAll();
                    } else {
                        myAdapter.removeSelection();
                    }
                    setMenuType();
                }
            });
        } else {
            currAttachedViewType = getLayoutInflater().inflate(R.layout.content_main_grid, null);
            frame.addView(currAttachedViewType);
            gv=(GridView) findViewById(R.id.mainGridView);
            llSelectAll=(LinearLayout) findViewById(R.id.selectAllLayout_grid);
            showSelectAll(false);                                                                          // hide select all view until needed during file ops
            cbSelectAll =(CheckBox) findViewById(R.id.selectAll_grid) ;
            cbSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // update your model (or other business logic) based on isChecked
                    if (isChecked) {
                        myAdapter.selectAll();
                    } else {
                        myAdapter.removeSelection();
                    }
                    setMenuType();
                }
            });
        }
      //
        if (currFile!=null) {
            myAdapter=null;
            populateScreen(currFile);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_view) {

            //SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedpreferences.edit();
            final Integer action_view = sharedpreferences.getInt(MYVIEW,0);
            final String currPath =sharedpreferences.getString(MYPATH, null);
            final File currFile;

            if (currPath.equals(null)){
                currFile=Environment.getExternalStorageDirectory();
            } else {
                currFile=new File(currPath);
            }

                if (action_view.equals(1)) {
                editor.putInt(MYVIEW, 2);
                editor.apply();
                listViewType=2;

                setViewType(currFile);



            } else if (action_view.equals(2)) {
                editor.putInt(MYVIEW, 1);
                editor.apply();
                listViewType=1;

                setViewType(currFile);


            } else {
                editor.putInt(MYVIEW, listViewType);
                editor.apply();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        //int id = item.getItemId();

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    public void onCustomNavigationItemSelected(String folder) {

        // Create new file object with new path.
        Integer index = getShortcutIndex(folder);
        if (index!=-1) {
            File nextPath=new File(quickLinkPath[index]);
            populateScreen(nextPath);
        }


       // Close drawer.
        mDrawerLayout.closeDrawer(GravityCompat.START);

    }

    public Integer getShortcutIndex(String quickPath){
        Integer index=-1;
        for (int i=0;i<quickLinkTitle.length;i++) {
            if (quickPath.equals(quickLinkTitle[i])) index=i;
        }
        return index;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setPathbar(String path) {

        //final SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(MYPATH, path);
        editor.apply();

        search.setVisibility(View.VISIBLE);
        hScrollView.setVisibility(View.VISIBLE);
        editText.setVisibility(View.GONE);

        if (layoutToolbar.getChildCount() > 0) {
            layoutToolbar.removeAllViews();
        }

        final String[] splitCurrPath = path.split("/");
        for (int i = 0; i < splitCurrPath.length ; i++) {

            final TextView textView = new TextView(this);
           // textSize=textView.getTextSize();
            textView.setText(splitCurrPath[i]);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (i < splitCurrPath.length -1){
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_right_black_24dp, 0);
            }
            textView.setGravity(Gravity.CENTER_VERTICAL);

            textView.setOnClickListener(new DoubleClickListener() {

                @Override
                public void onSingleClick(View v) {
                    File newDir;
                    String newPath="/";
                    int tag=0;
                    for (int i = 0; i < splitCurrPath.length ; i++) {
                        if(tag==0){
                            newPath=newPath + splitCurrPath[i] + "/";
                            if (textView.getText().equals(splitCurrPath[i])) {
                                tag=1;
                            }
                        }
                    }
                    newPath=newPath.substring(1,newPath.length()-1);

                    final String[] storagePath = Environment.getExternalStorageDirectory().getAbsolutePath().split("/");
                    String newPath2="";
                    int tag2=0;
                    for (int i = 0; i < storagePath.length -1; i++) {
                        newPath2=newPath2 + storagePath[i] + "/";
                        if (newPath.concat("/").equals(newPath2)) {
                            tag2=1;
                        }
                    }

                    if (tag2==0) {
                        newDir = new File(newPath);
                        populateScreen(newDir);
                    } else {
                        Snackbar.make(hScrollView, "This action requires root access.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                      }

                @Override
                public void onDoubleClick(View v) {

                }

            });

            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                   // SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                   final String currPath =sharedpreferences.getString(MYPATH, null);
                    search.setVisibility(View.GONE);
                    hScrollView.setVisibility(View.GONE);
                    editText.setVisibility(View.VISIBLE);
                    editText.setText(currPath);
                    editText.requestFocusFromTouch();
                    imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, 0);
                    editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                File dir = new File(editText.getText().toString());
                                if(dir.exists() && dir.isDirectory()) {
                                    setPathbar(editText.getText().toString());
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                } else {
                                    Snackbar.make(coLayout, "You entered and invalid path.", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                    setPathbar(currPath);
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                }

                                return true;
                            }
                            return false;
                        }
                    });


                    return true;
                }
            });

            textView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.mainText));

            layoutToolbar.addView(textView);

        }

        hScrollView.post(new Runnable() {
            public void run() {
                hScrollView.scrollTo(hScrollView.getWidth(),0);
            }
        });

    }

    private String getExtension(File currFile) {
        String[] fileNameArray;
        String extension;

        if (currFile.getName().contains(".")) {
            fileNameArray=currFile.getName().split("\\.");
            extension=fileNameArray[fileNameArray.length-1];
        } else {
            extension="xxx";
        }
        return extension;
    }

    private boolean isExternalStorageAvailable() {

        String state = Environment.getExternalStorageState();
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (mExternalStorageAvailable == true
                && mExternalStorageWriteable == true) {
            return true;
        } else {
            return false;
        }
    }




}
