package tk.internet.praktikum.foursquare;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.UserCheckinInformation;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.foursquare.search.Utils;


public class LastHereAdapter extends RecyclerView.Adapter<LastHereAdapter.MyViewHolder> {
    private final static String LOG = LastHereAdapter.class.getSimpleName();
    private final Context context;
    private ArrayList<UserCheckinInformation> checkinInformations;
    private List<UserCheckinInformation> data;

    public LastHereAdapter(ArrayList<UserCheckinInformation> checkinInformations, Context context) {
        this.checkinInformations = checkinInformations;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.last_here_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        UserCheckinInformation info = checkinInformations.get(position);

        holder.date.setText(DateFormat.getFriendlyTime(context,info.getLastDate()));

        UserService us = ServiceFactory.createRetrofitService(UserService.class, VenueInDetailsNestedScrollView.URL);

        us.profileByID(info.getUserID())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((user) -> {
                    holder.name.setText(user.getDisplayName());
                    holder.name.setVisibility(View.VISIBLE);
                    if(user.getAvatar() != null) {
                        ImageCacheLoader icl = new ImageCacheLoader(context);
                        icl.loadBitmap(user.getAvatar(), ImageSize.SMALL)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe((bitmap) -> {holder.avatar.setImageBitmap(bitmap);
                                                        holder.avatar.setVisibility(View.VISIBLE);}
                                        ,
                                        (err) -> Log.d(LOG, err.toString(), err));
                    }
                    else{

                        holder.avatar.setDrawingCacheEnabled(true);
                        Bitmap bitmap= null;
                        try {
                            bitmap = Utils.decodeResourceImage(context,"no_avatar",35,35);
                            holder.avatar.setImageBitmap(bitmap);
                            holder.avatar.setVisibility(View.VISIBLE);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }, (err) -> Log.d(LOG, err.toString(), err));
    }

    @Override
    public int getItemCount() {
        return checkinInformations.size();
    }

    public void setData(List<UserCheckinInformation> data) {
        checkinInformations.clear();
        checkinInformations.addAll(data);
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, date;
        public ImageView avatar;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            date = (TextView) view.findViewById(R.id.date);
            avatar = (ImageView) view.findViewById(R.id.avatar);
        }
    }
}
