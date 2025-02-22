package com.it_tech613.skydark.ui.movies;

import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.gson.Gson;
import com.it_tech613.skydark.R;
import com.it_tech613.skydark.apps.Constants;
import com.it_tech613.skydark.apps.MyApp;
import com.it_tech613.skydark.models.MovieInfoModel;
import com.it_tech613.skydark.models.MovieModel;
import com.it_tech613.skydark.ui.TrailerActivity;
import com.it_tech613.skydark.ui.VideoExoPlayActivity;
import com.it_tech613.skydark.ui.VideoIjkPlayActivity;
import com.it_tech613.skydark.ui.VideoPlayActivity;
import com.it_tech613.skydark.utils.MyFragment;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FragmentAllMovie extends MyFragment implements View.OnClickListener {

//    private SimpleDraweeView image;
    private TextView title, subTitle, plot, cast, director, genre,score;
    private MovieModel showMovieModel;
    private Button addFav;
    private ImageView fav_icon;
    private SliderLayout mDemoSlider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_movie, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = view.findViewById(R.id.textView9);
        subTitle = view.findViewById(R.id.textView10);
        plot = view.findViewById(R.id.textView11);
        cast = view.findViewById(R.id.textView16);
        director = view.findViewById(R.id.textView18);
        genre = view.findViewById(R.id.textView20);
        score = view.findViewById(R.id.textView21);
        Button watchTrailer = view.findViewById(R.id.button2);
        Button watchMovie = view.findViewById(R.id.button3);
        addFav = (Button)view.findViewById(R.id.button4);
        fav_icon = (ImageView)view.findViewById(R.id.fav_icon);
        addFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFav();
            }
        });
//        image = view.findViewById(R.id.image);
        mDemoSlider = view.findViewById(R.id.slider_viewpager);
        List<MovieModel> movieModels = new ArrayList<>(MyApp.subMovieModels);
        Collections.sort(movieModels, (o1, o2) -> o2.getAdded().compareTo(o1.getAdded()));
        showMovieModel=movieModels.get(0);
        setAddFavText();
        if (showMovieModel.getMovieInfoModel()==null)
            new Thread(this::getMovieInfo).start();
        else setModelInfo();
        RecyclerView movies_recyclerview = view.findViewById(R.id.movies_recyclerview);
        movies_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        movies_recyclerview.setAdapter(new MovieListAdapter(movieModels, new Function2<MovieModel, Integer, Unit>() {
            @Override
            public Unit invoke(MovieModel movieModel, Integer integer) {
                showMovieModel = movieModel;
                setAddFavText();
                if (showMovieModel.getMovieInfoModel()==null)
                    new Thread(() -> getMovieInfo()).start();
                else setModelInfo();
                return null;
            }
        }));
        watchMovie.setOnClickListener(this);
        watchTrailer.requestFocus();
        watchTrailer.setOnClickListener(this);
    }

    private void setAddFavText(){
        if (showMovieModel.isIs_favorite()) {
            addFav.setText(getResources().getString(R.string.remove_favorites));
            fav_icon.setImageResource(R.drawable.heart_filled);
        }
        else {
            addFav.setText(getResources().getString(R.string.add_to_favorite));
            fav_icon.setImageResource(R.drawable.heart_unfilled);
        }
    }

    private void addToFav() {
        if (showMovieModel.isIs_favorite()) {
            showMovieModel.setIs_favorite(false);
            boolean is_exist = false;
            int pp = 0;
            for (int i = 0; i < Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels().size(); i++) {
                if (Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels().get(i).getName().equals(showMovieModel.getName())) {
                    is_exist = true;
                    pp = i;
                }
            }
            if (is_exist)
                Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels().remove(pp);
            //get favorite channel names list
            List<MovieModel> fav_movie_names = new ArrayList<>(Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels());
            //set
            MyApp.instance.getPreference().put(Constants.getFAV_VOD_MOVIES(), fav_movie_names);
            Log.e("ADD_FAV","removed");
        } else {
            showMovieModel.setIs_favorite(true);
            Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels().add(showMovieModel);
            //get favorite channel names list
            List<MovieModel> fav_channel_models = new ArrayList<>(Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels());
            //set
            MyApp.instance.getPreference().put(Constants.getFAV_VOD_MOVIES(), fav_channel_models);
            Log.e("LIVE_RATIO","added");
        }
        setAddFavText();
    }

    private void setModelInfo() {
//        image.setImageURI(Uri.parse(showMovieModel.getMovieInfoModel().getMovie_img()));
        mDemoSlider.removeAllSliders();
//        deleteCache(requireContext());
        if (showMovieModel.getMovieInfoModel()!=null && showMovieModel.getMovieInfoModel().getBackdrop_path()!=null){
            for(String url : showMovieModel.getMovieInfoModel().getBackdrop_path()){
                if (url==null || url.equalsIgnoreCase("")) continue;
                DefaultSliderView textSliderView = new DefaultSliderView (requireContext());
                // initialize a SliderLayout
                textSliderView
//                    .description(name)
                        .image(url)
                        .setScaleType(BaseSliderView.ScaleType.FitCenterCrop);

                //add your extra information
                //            textSliderView.bundle(new Bundle());
                //            textSliderView.getBundle()
                //                    .putString("extra",name);

                mDemoSlider.addSlider(textSliderView);
            }
            mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
            //        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            mDemoSlider.setCustomAnimation(new DescriptionAnimation());
            mDemoSlider.setDuration(showMovieModel.getMovieInfoModel().getBackdrop_path().size()* Constants.GetSlideTime(requireContext()));
        }
        title.setText(showMovieModel.getName());
        subTitle.setText(showMovieModel.getMovieInfoModel().getReleasedate().split("-")[0]+" | "+showMovieModel.getMovieInfoModel().getDuration()+" | "+showMovieModel.getMovieInfoModel().getAge());
        plot.setText(showMovieModel.getMovieInfoModel().getPlot());
        cast.setText(showMovieModel.getMovieInfoModel().getCast());
        director.setText(showMovieModel.getMovieInfoModel().getDirector());
        genre.setText(showMovieModel.getMovieInfoModel().getGenre());
        score.setText(showMovieModel.getMovieInfoModel().getRating()+"");
    }

    private void getMovieInfo(){
        try {
            String response = MyApp.instance.getIptvclient().getVodInfo(MyApp.user,MyApp.pass, showMovieModel.getStream_id());
            Log.e(getClass().getSimpleName(),response);
            JSONObject jsonObject = new JSONObject(response);
            MovieInfoModel movieInfoModel = new MovieInfoModel();
            try{
                JSONObject info_obj = jsonObject.getJSONObject("info");
                Gson gson = new Gson();
                try {
                    movieInfoModel = gson.fromJson(info_obj.toString(),MovieInfoModel.class);
                }catch (Exception e){
                    e.printStackTrace();
                    movieInfoModel.setMovie_img(info_obj.getString("movie_image"));
                    movieInfoModel.setGenre(info_obj.getString("genre"));
                    movieInfoModel.setPlot(info_obj.getString("plot"));
                    movieInfoModel.setCast(info_obj.getString("cast"));
                    try {
                        movieInfoModel.setRating(info_obj.getDouble("rating"));
                    }catch (Exception e1){
                        movieInfoModel.setRating(0.0);
                    }
                    try {
                        movieInfoModel.setYoutube(info_obj.getString("youtube_trailer"));
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    movieInfoModel.setDirector(info_obj.getString("director"));
                    movieInfoModel.setDuration(info_obj.getString("duration"));
                    try {
                        movieInfoModel.setActors(info_obj.getString("actors"));
                        movieInfoModel.setDescription(info_obj.getString("description"));
                        movieInfoModel.setAge(info_obj.getString("age"));
                        movieInfoModel.setCountry(info_obj.getString("country"));
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    JSONArray jsonArray = info_obj.getJSONArray("backdrop_path");
                    List<String> stringList = new ArrayList<>();
                    for (int i=0;i<jsonArray.length();i++){
                        stringList.add(jsonArray.getString(i));
                    }
                    movieInfoModel.setBackdrop_path(stringList);
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.e("error","info_parse_error");
            }
            showMovieModel.setMovieInfoModel(movieInfoModel);
            requireActivity().runOnUiThread(this::setModelInfo);
        } catch (Exception e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(()->{
                DefaultSliderView textSliderView = new DefaultSliderView (requireContext());
                // initialize a SliderLayout
                textSliderView
//                    .description(name)
                        .image(showMovieModel.getStream_icon())
                        .setScaleType(BaseSliderView.ScaleType.FitCenterCrop);

                //add your extra information
                //            textSliderView.bundle(new Bundle());
                //            textSliderView.getBundle()
                //                    .putString("extra",name);

                mDemoSlider.addSlider(textSliderView);
                mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                //        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                mDemoSlider.setDuration(Integer.MAX_VALUE);
            });
        }
    }

    @Override
    public boolean myOnKeyDown(KeyEvent event){
        //do whatever you want here
        switch (event.getKeyCode()){
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                addFav.requestFocus();
                break;
        }
        return super.myOnKeyDown(event);
    }

    @Override
    public void onClick(View v) {
        if (showMovieModel==null) return;
        switch (v.getId()){
            case R.id.button2:
                //watchTrailer
                String content_Uri = showMovieModel.getMovieInfoModel().getYoutube();
                if(content_Uri.isEmpty()){
                    Toast.makeText(requireContext(),"This movie do not have trailer",Toast.LENGTH_LONG).show();
                }else {
                    String newstr = content_Uri;
                    if(content_Uri.contains("=")){
                        int endIndex = content_Uri.lastIndexOf("=");
                        if (endIndex != -1) {
                            newstr = content_Uri.substring(endIndex+1); // not forgot to put check if(endIndex != -1)
                        }
                    }else {
                        int endIndex = content_Uri.lastIndexOf("/");
                        if (endIndex != -1)
                        {
                            newstr = content_Uri.substring(endIndex+1); // not forgot to put check if(endIndex != -1)
                        }
                    }
                    Intent intent1 = new Intent(requireActivity(), TrailerActivity.class);
                    intent1.putExtra("content_Uri",newstr);
                    startActivity(intent1);
                }
                break;
            case R.id.button3:
                //add recent movie
                checkAddedRecent(showMovieModel);
                Constants.getRecentCatetory(MyApp.vod_categories).getMovieModels().add(0,showMovieModel);
                //get recent channel names list
                List<MovieModel> recent_channel_models = new ArrayList<>(Constants.getRecentCatetory(MyApp.vod_categories).getMovieModels());
                //set
                MyApp.instance.getPreference().put(Constants.getRecentMovies(), recent_channel_models);
                Log.e(getClass().getSimpleName(),"added");
                //watchMovie
                String vod_url = MyApp.instance.getIptvclient().buildMovieStreamURL(MyApp.user,MyApp.pass,showMovieModel.getStream_id(),showMovieModel.getExtension());
                Log.e(getClass().getSimpleName(),vod_url);

                int current_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
                Intent intent;
                switch (current_player){
                    case 0:
                    default:
                        intent = new Intent(requireContext(), VideoPlayActivity.class);
                        break;
                    case 1:
                        intent = new Intent(requireContext(), VideoIjkPlayActivity.class);
                        break;
                    case 2:
                        intent = new Intent(requireContext(), VideoExoPlayActivity.class);
                        break;
                }
                MyApp.vod_model = showMovieModel;
                intent.putExtra("title",showMovieModel.getName());
                intent.putExtra("img",showMovieModel.getStream_icon());
                intent.putExtra("url",vod_url);
                startActivity(intent);
                break;
        }
    }

    private void checkAddedRecent(MovieModel showMovieModel) {
        Iterator<MovieModel> iter = Constants.getRecentCatetory(MyApp.vod_categories).getMovieModels().iterator();
        while(iter.hasNext()){
            MovieModel movieModel = iter.next();
            if (movieModel.getName().equals(showMovieModel.getName()))
                iter.remove();
        }
    }
}
