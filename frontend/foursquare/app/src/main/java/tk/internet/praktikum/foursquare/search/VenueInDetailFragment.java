package tk.internet.praktikum.foursquare.search;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.UploadHelper;
import tk.internet.praktikum.foursquare.api.bean.Comment;
import tk.internet.praktikum.foursquare.api.bean.Image;
import tk.internet.praktikum.foursquare.api.bean.ImageComment;
import tk.internet.praktikum.foursquare.api.bean.Location;
import tk.internet.praktikum.foursquare.api.bean.TextComment;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.service.VenueService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

import static android.app.Activity.RESULT_OK;

//import android.support.design.widget.FloatingActionButton;


public class VenueInDetailFragment extends Fragment implements OnMapReadyCallback {

    private final String URL = "https://dev.ip.stimi.ovh/";
    private final String LOG = VenueInDetailFragment.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private ViewGroup container;
    private String venueId;
    private View view;

    private ImageView imageVenueOne;
    private ImageView imageVenueTwo;
    private ImageView imageVenueThree;

    private TextView venueName;
    private TextView venueAddress;
    private TextView venueIsOpened;
    private TextView venueWebsite;
    private TextView venueWebsiteLabel;
    private List<tk.internet.praktikum.foursquare.api.bean.Image> images;
    private GoogleMap map;
    private ProgressDialog progressDialog;
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton venueTextCommentButton;
    private FloatingActionButton venueImageCommentButton;
    private FloatingActionButton venueCheckInButton;
    private FloatingActionButton venueImagesButton;
    private RecyclerView recyclerView;
    private AlertDialog venueTextCommentDialog;
    private AlertDialog venueImageCommentDialog;

    private Bitmap venueImageComment;
    private LinearLayoutManager linearLayoutManager;
    private int visibleItemCount;
    private int itemCount;
    private int lastVisibleItemPosition;
    private int currentPage;
    private ImageView selectedImageView;
    private TextView venueRating;
    private TextView venueCheckIn;
    private final int REQUEST_CAMERA = 0;
    private final int REQUEST_GALLERY = 1;
    private Venue currentVenue;
    private CommentVenueAdapter commentVenueAdapter;
    private Image userAvatar=null;
    private boolean reachedMaxVenues;
    private Fragment parent;
    private TabLayout tabLayout;
    public static VenueInDetailFragment newInstance(String param1, String param2) {
        VenueInDetailFragment fragment = new VenueInDetailFragment();

        return fragment;
    }

    public VenueInDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(view==null) {
            // Inflate the layout for this fragment
            view = inflater.inflate(R.layout.fragment_venue_detail_new, container, false);
            // layoutInflater=inflater;
            //this.container=container;
            imageVenueOne = (ImageView) view.findViewById(R.id.image_venue_one);
           // imageVenueTwo = (ImageView) view.findViewById(R.id.image_venue_two);
           // imageVenueThree = (ImageView) view.findViewById(R.id.image_venue_three);

            venueName = (TextView) view.findViewById(R.id.venue_name);
            venueAddress = (TextView) view.findViewById(R.id.venue_address);
            venueIsOpened = (TextView) view.findViewById(R.id.venue_is_opened);
            venueWebsite = (TextView) view.findViewById(R.id.venue_website);
            venueWebsiteLabel = (TextView) view.findViewById(R.id.venue_website_label);
            venueRating = (TextView) view.findViewById(R.id.venue_rating);
            venueCheckIn = (TextView) view.findViewById(R.id.venue_checkinCount);
            tabLayout=(TabLayout) view.findViewById(R.id.venue_tabs);
            floatingActionMenu=(FloatingActionMenu)view.findViewById(R.id.floating_menu) ;
            venueTextCommentButton= (FloatingActionButton) floatingActionMenu.findViewById(R.id.venue_detail_text_comment_button);
            //venueTextCommentButton = (FloatingActionButton) view.findViewById(R.id.venue_detail_text_comment_button);
            venueTextCommentButton.setOnClickListener(v -> {
                showUpTextCommentDialog();
            });
            venueImageCommentButton=(FloatingActionButton)floatingActionMenu.findViewById(R.id.venue_detail_image_commnent_button);
            //venueImageCommentButton = (FloatingActionButton) view.findViewById(R.id.venue_detail_image_commnent_button);
            venueImageCommentButton.setOnClickListener(v -> {
                showUpImageCommentDialog();
            });
            venueCheckInButton=(FloatingActionButton)floatingActionMenu.findViewById(R.id.venue_checkin);
            //venueCheckInButton = (FloatingActionButton) view.findViewById(R.id.venue_checkin);
            venueCheckInButton.setOnClickListener(v -> venueCheckIn());
            //invisibleFloatingButton();
            venueImagesButton = (FloatingActionButton) view.findViewById(R.id.venue_detail_images);

            venueImagesButton.setOnClickListener(v -> venueImages());

            recyclerView = (RecyclerView) view.findViewById(R.id.comments_venue);
            linearLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            progressDialog = new ProgressDialog(getActivity(), 1);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Waiting for seeing venue details...");
            progressDialog.show();

            SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.venueDetails_mapView));
            mapFragment.getMapAsync(this);
            currentPage = 0;
            reachedMaxVenues=false;
            renderContent();
            recyclerViewOnScrollListener();

        }
        return view;
    }


    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }


    private void renderContent() {
        Log.d(LOG, "##### Venue Id: " + venueId);
        VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
        venueService.getDetails(venueId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(venue -> {
                            currentVenue=venue;
                            renderVenueInformation(venue);
                            Location location = venue.getLocation();
                            updateVenueLocation(location);
                            renderCommentVenue(venue);
                            images = venue.getImages();
                            Log.d(LOG, "all images size: " + images.size());
                            if (images.size() > 0) {
                                Log.d(LOG, "++++ get images");
                                Image image = images.get(0);
                                ImageCacheLoader imageCacheLoader = new ImageCacheLoader(this.getContext());
                                imageCacheLoader.loadBitmap(image, ImageSize.LARGE)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(bitmap -> {

                                            imageVenueOne.setMaxHeight(imageVenueOne.getWidth());
                                            //imageVenueTwo.setMaxHeight(imageVenueTwo.getWidth());
                                            //imageVenueThree.setMaxHeight(imageVenueThree.getWidth());
                                            Log.d(LOG,"imageView Width: "+imageVenueOne.getWidth());
                                            Log.d(LOG,"bitmap width: "+bitmap.getWidth());
                                            imageVenueOne.setImageBitmap(bitmap);
                                            //imageVenueTwo.setImageBitmap(bitmap);
                                            //imageVenueThree.setImageBitmap(bitmap);
                                        });
                            }
                            else{
                               /* Bitmap bitmap=Utils.decodeResourceImage(Resources.getSystem(),R.drawable.side_nav_bar,imageVenueOne.getWidth(),imageVenueOne.getHeight());
                                Log.d(LOG,"bitmap Resource: "+bitmap);
                                imageVenueOne.setImageBitmap(bitmap);*/
                            }

                            progressDialog.dismiss();
                        },
                        throwable -> {
                            //TODO
                            //handle exception
                            progressDialog.dismiss();
                            Log.d(LOG, "#### exception" + throwable.getCause());

                        }
                );
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        //map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(false);
        //map.getUiSettings().setMapToolbarEnabled(true);
    }

    public void updateVenueLocation(Location location) {
        LatLng venueLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("venue_location_marker", "mipmap", getContext().getPackageName()));

        map.addMarker(new MarkerOptions()
                .position(venueLocation))
                .setIcon(BitmapDescriptorFactory.fromBitmap(imageBitmap));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(venueLocation, 14));


    }

    public void renderVenueInformation(Venue venue) {
        this.venueName.setText(venue.getName());
        this.venueAddress.setText(venue.getVicinity());
        this.venueWebsiteLabel.setText(getString(R.string.websiteLabel));
        this.venueWebsite.setText(venue.getWebsite());

        if (venue.is_open())
            this.venueIsOpened.setText(getString(R.string.isOpened));
        else
            this.venueIsOpened.setText(getString(R.string.closed));

        this.venueRating.setText(String.valueOf(venue.getRating()));
        this.venueCheckIn.setText(String.valueOf(venue.getCheckInCount()));
    }

    public void renderCommentVenue(Venue venue) {
        Log.d(LOG,"renderCommentVenue");
        //CommentService commentService = ServiceFactory.createRetrofitService(CommentService.class, URL);
        VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
        venueService.getComments(venueId, currentPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                            updateRecyclerView(comments);
                        },
                        throwable -> {
                            Log.d(LOG, throwable.getMessage());
                        });

    }

    private void showUpTextCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(LocalStorage.getLocalStorageInstance(getContext()).isLoggedIn()) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View textCommentView = inflater.inflate(R.layout.venue_comment, null);
            //ImageView user_avatar=(ImageView) textCommentView.findViewById(R.id.user_avatar_text_comment);
            builder.setPositiveButton(R.string.sendComment, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    // call venueServices
                    System.out.println("*** comment text");
                    EditText textCommentContent = (EditText) textCommentView.findViewById(R.id.venue_text_comment_content);
                    String comment = textCommentContent.getText().toString().trim();
                    if (!comment.isEmpty()) {

                        TextComment textComment = new TextComment(comment);
                        textComment.setDate(new Date());

                        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getActivity().getApplicationContext());
                        User user = new User(sharedPreferences.getString(Constants.NAME, ""), sharedPreferences.getString(Constants.EMAIL, ""));
                        ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getContext());
                    /*getUserAvatar(user.getName());
                    if(userAvatar!=null) {
                        imageCacheLoader.loadBitmap(userAvatar, ImageSize.SMALL)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(bitmap -> {
                                    user_avatar.setImageBitmap(bitmap);
                                    user_avatar.setVisibility(View.VISIBLE);
                                });
                    }
                    else
                        user_avatar.setVisibility(GONE);*/

                        textComment.setAuthor(user);
                        Log.d(LOG, "author: " + user);
                        String token = sharedPreferences.getString(Constants.TOKEN, "");
                        Log.d(LOG, "token: " + token);
                        VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL, token);
                        Log.d(LOG, "#### running here");
                        venueService.addTextComment(textComment, venueId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(textComment1 -> {
                                            Log.d(LOG, "##### textcomment: " + textComment1.getId());
                                            addComment(textComment1);
                                        },
                                        throwable -> {
                                            Log.d(LOG, throwable.getMessage());
                                        });


                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    venueTextCommentDialog.dismiss();
                }
            });

            venueTextCommentDialog = builder.create();
            venueTextCommentDialog.setView(textCommentView);
            venueTextCommentDialog.show();

        }
        else{
            builder.setPositiveButton(R.string.loginToContinue, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                     // Todo
                    // calls loginActivity
                }
            });
            builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
    }

    private void showUpImageCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(LocalStorage.getLocalStorageInstance(getContext()).isLoggedIn()) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View venueImageCommentView = inflater.inflate(R.layout.venue_image_comment, null);
            // ImageView user_avatar=(ImageView) venueImageCommentView.findViewById(R.id.user_avatar_image_comment);
            builder.setPositiveButton(R.string.sendComment, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    //Todo
                    // call venueServices
                    MultipartBody.Part image = UploadHelper.createMultipartBodySync(venueImageComment, getContext(), true);
                    SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getActivity().getApplicationContext());
                    User user = new User(sharedPreferences.getString(Constants.NAME, ""), sharedPreferences.getString(Constants.EMAIL, ""));
                    ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getContext());
               /* getUserAvatar(user.getName());
                if(userAvatar!=null) {
                    imageCacheLoader.loadBitmap(userAvatar, ImageSize.SMALL)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(bitmap -> {
                                user_avatar.setImageBitmap(bitmap);
                                user_avatar.setVisibility(View.VISIBLE);
                            });
                }
                else
                    user_avatar.setVisibility(GONE);*/
                    ImageComment imageComment = new ImageComment();
                    imageComment.setAuthor(user);
                    imageComment.setDate(new Date());
                    Log.d(LOG, "author: " + user);
                    String token = sharedPreferences.getString(Constants.TOKEN, "");
                    Log.d(LOG, "token: " + token);
                    VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL, token);
                    venueService.uploadAvatar(image, venueId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(imageComment1 -> {
                                        Log.d(LOG, "##### imageComment: " + imageComment1.getId());
                                        addComment(imageComment1);
                                    },
                                    throwable -> {
                                        Log.d(LOG, throwable.getMessage());
                                    });

                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    venueImageCommentDialog.dismiss();
                }
            });

            venueImageCommentDialog = builder.create();

            venueImageCommentDialog.setView(venueImageCommentView);
            Button venuImageCommentButton = (Button) venueImageCommentView.findViewById(R.id.venue_image_comment_button);
            selectedImageView = (ImageView) venueImageCommentView.findViewById(R.id.venue_image_comment);
            venuImageCommentButton.setOnClickListener(v -> {
                uploadPicture();
            });
            venueImageCommentDialog.show();
        }
        else{
            builder.setPositiveButton(R.string.loginToContinue, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Todo
                    // calls loginActivity
                }
            });
            builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
    }


    private void venueCheckIn() {
        if(LocalStorage.getLocalStorageInstance(getContext()).isLoggedIn()) {
            SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getActivity().getApplicationContext());
            String token = sharedPreferences.getString(Constants.TOKEN, "");
            Log.d(LOG, "token: " + token);
            VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL, token);
            venueService.checkin(venueId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(checkInInformation -> {
                                Log.d(LOG, "checkIn Count: " + checkInInformation.getCount());
                                venueCheckIn.setText(String.valueOf(checkInInformation.getCount()));
                            },
                            throwable -> {

                            });
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.loginToContinue, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                     // Todo
                    // calls loginActivity
                }
            });
            builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
    }

    private void uploadPicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] options = {"Camera", "Gallery", "Cancel"};
        builder.setTitle("Select an option to choose your avatar.");
        builder.setItems(options, (dialog, option) -> {
            switch (options[option]) {
                case "Camera":
                    Log.d(LOG, "Camera");
                    cameraIntent();
                    break;
                case "Gallery":
                    Log.d(LOG, "gallery");
                    galleryIntent();
                    break;
                case "Cancel":
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    venueImageComment = (Bitmap) data.getExtras().get("data");
                    selectedImageView.setImageBitmap(venueImageComment);
                }
                break;

            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    try {
                        venueImageComment = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                        selectedImageView.setImageBitmap(venueImageComment);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void updateRecyclerView(List<Comment> comments) {
        Log.d(LOG, "***size :" + comments.size());
        if(comments.size()>0) {
            if (currentPage == 0) {
                commentVenueAdapter = new CommentVenueAdapter(comments, this);
                recyclerView.setAdapter(commentVenueAdapter);
                recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

            } else {
                commentVenueAdapter.addMoreCommentVenues(comments);
            }
        }
        else{
            reachedMaxVenues=true;
            currentPage=currentPage-1;
        }

    }

    public void addComment(Comment comment){
        commentVenueAdapter.addCommentVenue(comment);
    }

    public void recyclerViewOnScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = linearLayoutManager.getChildCount();
                itemCount = linearLayoutManager.getItemCount();
                lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                Log.d(LOG,"currentPage: "+currentPage);
                Log.d(LOG,"lastVisibleItemPosition "+lastVisibleItemPosition);
                Log.d(LOG,"visibleItemCount "+visibleItemCount);
                Log.d(LOG,"itemCount "+itemCount);
                if(dy>0&&(lastVisibleItemPosition+visibleItemCount)>=itemCount&&lastVisibleItemPosition%10==9&&!reachedMaxVenues){
                    currentPage+=1;

                    renderCommentVenue(currentVenue);
                }


            }
        });
    }

    private void venueImages() {
        System.out.println("I am here");
          VenueImagesFragment venueImagesFragment=new VenueImagesFragment();
          venueImagesFragment.setParent(this.getParent());
          venueImagesFragment.setImages(images);
          redirectToFragment(venueImagesFragment);

    }

    private void redirectToFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = this.getParent().getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    public Fragment getParent() {
        return parent;
    }

    public void setParent(Fragment parent) {
        this.parent = parent;
    }

   /* public void getUserAvatar(String userName){
        if(userAvatar==null) {
            UserService userService = ServiceFactory.createRetrofitService(UserService.class, URL);
            userService.detailsByName(userName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(user -> {
                                userAvatar = user.getAvatar();
                            },
                            throwable -> {
                                Log.d(LOG, throwable.getMessage());
                            });
        }
    }*/

 /*    public void invisibleFloatingButton(){
         SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getActivity().getApplicationContext());
         String token = sharedPreferences.getString(Constants.TOKEN, "");
         if(token==null || token.isEmpty()){
             //floatingActionMenu.setVisibility(View.INVISIBLE);
             venueTextCommentButton.setVisibility(View.INVISIBLE);
             venueImageCommentButton.setVisibility(View.INVISIBLE);
             venueCheckInButton.setVisibility(View.INVISIBLE);
         }
         else {
             venueTextCommentButton.setVisibility(View.VISIBLE);
             venueImageCommentButton.setVisibility(View.VISIBLE);
             venueCheckInButton.setVisibility(View.VISIBLE);
         }
     }*/

}
