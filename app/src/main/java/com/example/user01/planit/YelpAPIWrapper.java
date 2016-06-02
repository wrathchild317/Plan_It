package com.example.user01.planit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import retrofit.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class YelpAPIWrapper extends AsyncTask<Void, Void, Void> {
    private Activity activity;
    private YelpAPI yelpAPI;
    private ArrayList<String> settings;
    private ArrayList<Event> morningEvents;
    private ArrayList<Event> afternoonEvents;
    private ArrayList<Event> eveningEvents;
    private ArrayList<Event> hikeEvents;
    private CircularProgressView cpv;

    YelpAPIWrapper(Activity activity, ArrayList<String> settings) {
        this.activity = activity;
        this.settings = settings;
        YelpAPIFactory yelpAPIFactory = new YelpAPIFactory(
                activity.getString(R.string.consumer_key),
                        activity.getString(R.string.consumer_secret),
                        activity.getString(R.string.token),
                        activity.getString(R.string.token_secret));
        yelpAPI = yelpAPIFactory.createAPI();
        morningEvents = new ArrayList<>();
        afternoonEvents = new ArrayList<>();
        eveningEvents = new ArrayList<>();
        hikeEvents = new ArrayList<>();
    }

    @Override
    protected void onPreExecute() {
        cpv = (CircularProgressView) this.activity.findViewById(R.id.progress_circle);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Map<String, String> mornparam = new HashMap<>();
            Map<String, String> noonparam = new HashMap<>();
            Map<String, String> evenparam = new HashMap<>();
            Map<String, String> hikeparam = new HashMap<>();
            mornparam.put("term", "Breakfast");
            noonparam.put("term", "Lunch");
            evenparam.put("term", "Dinner");
            hikeparam.put("term", "hike");
            Call<SearchResponse> call;

            ArrayList<Bitmap> bitmapsList = new ArrayList<>();
            Bitmap bitmap = BitmapFactory.decodeResource(
                    this.activity.getResources(),R.drawable.yelp_pin);
            bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
            bitmapsList.add(bitmap);

            bitmap = BitmapFactory.decodeResource(
                    this.activity.getResources(), R.drawable.outdoor_pin);
            bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
            bitmapsList.add(bitmap);

            bitmap = BitmapFactory.decodeResource(
                    this.activity.getResources(), R.drawable.movie_pin);
            bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
            bitmapsList.add(bitmap);

            bitmap = BitmapFactory.decodeResource(
                    this.activity.getResources(), R.drawable.music_pin);
            bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
            bitmapsList.add(bitmap);


            EventData.setBitmap(bitmapsList);
            // Bitmap ArrayList
            // Index 0: Restaurant Pin
            // Index 1: Outdoor Pin
            // Index 2: Movie Pin
            // Index 3: Music Pin


            ArrayList<Business> breakfast, lunch, dinner, hike;

            call = yelpAPI.search(settings.get(0), mornparam);
            SearchResponse morningResponse = call.execute().body();
            breakfast = morningResponse.businesses();

            call = yelpAPI.search(settings.get(0), noonparam);
            SearchResponse noonResponse = call.execute().body();
            lunch = noonResponse.businesses();

            call = yelpAPI.search(settings.get(0), evenparam);
            SearchResponse eveningResponse = call.execute().body();
            dinner = eveningResponse.businesses();

            call = yelpAPI.search(settings.get(0), hikeparam);
            SearchResponse hikeResponse = call.execute().body();
            hike = hikeResponse.businesses();


            long seed = System.nanoTime();
            Collections.shuffle(breakfast, new Random(seed));
            Collections.shuffle(lunch, new Random(seed));
            Collections.shuffle(dinner, new Random(seed));
            Collections.shuffle(hike, new Random(seed));



            Retrofit movieDatabaseClient = new Retrofit.Builder()
                    .baseUrl("http://api.themoviedb.org/3/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            MovieDatabaseAPI movieDatabaseAPI = movieDatabaseClient.create(MovieDatabaseAPI.class);
            retrofit2.Call<MovieDatabaseModel> movieCall = movieDatabaseAPI.getMovieDatabaseModel();
            ArrayList<Movie> movies = movieCall.execute().body().getMovies();
            Collections.shuffle(movies, new Random(seed));
            EventData.setMovies(movies);

            Retrofit client = new Retrofit
                    .Builder()
                    .baseUrl("http://api.eventful.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            EventfulAPI eventfulAPI = client.create(EventfulAPI.class);
            retrofit2.Call<EventfulModel> eventfulModelCall =
                    eventfulAPI.EventfulList("music", "San Diego");
            ArrayList<EventfulEvent> eventfulEvents = eventfulModelCall.execute().body().getEvents().getEvent();
            Collections.shuffle(eventfulEvents,new Random(seed));
            for (EventfulEvent event: eventfulEvents) {
                event.setEventVariables();
            }

            EventData.setMorningRestaurant(breakfast);
            EventData.setAfternoonRestaurant(lunch);
            EventData.setEveningRestaurant(dinner);
            EventData.setHikes(hike);
            EventData.setEvents(eventfulEvents);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Intent intent = new Intent(activity,RecyclerActivity.class);

        activity.startActivity(intent);
        cpv.stopAnimation();
        cpv.setThickness(0);
        cpv.setColor(ContextCompat.getColor(this.activity.getApplicationContext(), R.color.grey));
    }

}
