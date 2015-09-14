package com.example.rdubey.samplelogin;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    private TextView mTextDetails;
    private CallbackManager mCallbackManager;
    private ProfileTracker mProfileTracker;
    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {

        Profile mProfile;
        @Override
        public void onSuccess(LoginResult loginResult) {

            LoginManager.getInstance().logInWithReadPermissions(getActivity(),
                    Arrays.asList("user_friends","user_location","user_birthday","user_likes","user_photos"));
            //AccessToken accesstoken = loginResult.getAccessToken();
            GraphRequest request = GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            // Application code
                            String name = object.optString("name");
                            String id = object.optString("id");
                            String link = object.optString("link");
                            String ul = object.optJSONObject("location").optString("name");
                            String birthday = object.optString("birthday");
                            String friend = object.optString("friends");

                            mTextDetails.setText(name);
                            Log.d("Name:", name);
                            Log.d("Id:", id);
                            Log.d("Link:", link);
                            Log.d("user location", ul);
                            Log.d("Birthday:", birthday);
                            Log.d("Friends:", friend);
                            TextView mBirthday = (TextView)getView().findViewById(R.id.birthday);
                            mBirthday.setText(birthday);

                            TextView mhometown = (TextView)getView().findViewById(R.id.hometown);
                            mhometown.setText(ul);

                            TextView mUid = (TextView)getView().findViewById(R.id.userid);
                            mUid.setText(id);


                            ProfilePictureView pictureView = (ProfilePictureView) getView().findViewById(R.id.picture);
                            pictureView.setProfileId(id);

                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link, birthday, location, friends");
            request.setParameters(parameters);
            request.executeAsync();

            //mTextDetails.setText("In CalBack OnSuccess");

            GraphRequestBatch batch = new GraphRequestBatch(
                    GraphRequest.newMeRequest(
                            AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject jsonObject,
                                        GraphResponse response) {
                                    // Application code for user
                                }
                            }),
                    GraphRequest.newMyFriendsRequest(
                            AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONArrayCallback() {
                                @Override
                                public void onCompleted(
                                        JSONArray jsonArray,
                                        GraphResponse response) {
                                    // Application code for users friends
                                    JSONObject object = response.getJSONObject();
                                    JSONObject summary = object.optJSONObject("summary");
                                    Log.d("Num Friends:", summary.optString("total_count"));
                                }
                            })
            );
            batch.addCallback(new GraphRequestBatch.Callback() {
                @Override
                public void onBatchCompleted(GraphRequestBatch graphRequests) {
                    // Application code for when the batch finishes
                }
            });
            batch.executeAsync();
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException e) {

        }
    };

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        mProfileTracker = new ProfileTracker() {
        @Override
        protected void onCurrentProfileChanged(Profile profile, Profile profile1) {
            Profile.setCurrentProfile(profile1);
        }
    };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextDetails = (TextView) view.findViewById(R.id.text_details);
        mTextDetails.setText("Welcome in OnViewCreated");

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, mCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
