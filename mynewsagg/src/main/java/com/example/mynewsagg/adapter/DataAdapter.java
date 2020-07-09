package com.example.mynewsagg.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mynewsagg.R;
import com.example.mynewsagg.model.ArticleStructure;
import com.example.mynewsagg.model.Constants;
import com.example.mynewsagg.view.WebViewActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private ArrayList<ArticleStructure> articles;
    private Context mContext;
    private int lastPosition = -1;
    public ArrayList<String> list_title;


    public DataAdapter(Context mContext, ArrayList<ArticleStructure> articles) {
        this.mContext = mContext;
        this.articles = articles;

    }

    /*
     ** inflating the cardView.
     **/
    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {

        String title = articles.get(position).getTitle();
         if(title.endsWith(" - Firstpost"))
             title = title.replace(" - Firstpost", "");


        //list_title.add(title);


        holder.tv_card_main_title.setText(title);
        //Log.d("Title:", title);
        //list_title.add(title);

        //list_title=new ArrayList<String>();

        /*final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mynewsagg/" );
        final File myFile = new File(dir, "eggZenbo1" + ".txt");
        try {
            if (!myFile.exists())
                myFile.createNewFile();
            FileWriter writer = new FileWriter(myFile);
            for(String str:list_title){
                writer.write(str);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println(list_title);*/


        Glide.with(mContext)
                .load(articles.get(position).getUrlToImage())
                .thumbnail(0.1f)
                .apply(RequestOptions.centerCropTransform())
                .into(holder.img_card_main);

        if (position > lastPosition) {
            lastPosition = position;
        }
    }

    /*
     ** Last parameter for binding the articles in OnBindViewHolder.
     **/
    @Override
    public int getItemCount() {
        return articles.size();
    }

    /*
     ** ViewHolder class which holds the different views in the recyclerView .
     **/
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tv_card_main_title;
        private ImageView img_card_main;
        private CardView cardView;

        public ViewHolder(View view) {
            super(view);
            tv_card_main_title = view.findViewById(R.id.tv_card_main_title);
            img_card_main = view.findViewById(R.id.img_card_main);
            cardView = view.findViewById(R.id.card_row);
            view.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {

            String URL = articles.get(getAdapterPosition()).getUrl();
            openWebViewActivity(URL);
        }

        private void openWebViewActivity(String URL) {
            Intent browserIntent = new Intent(mContext, WebViewActivity.class);
            browserIntent.putExtra(Constants.INTENT_URL, URL);
            mContext.startActivity(browserIntent);
        }
    }
}
