package tk.internet.praktikum.foursquare.history;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.VenueInDetailsNestedScrollView;
import tk.internet.praktikum.foursquare.storage.LocalDataBaseManager;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {

    private final List<HistoryEntry> historyEntries;
    private Context context;

    public HistoryRecyclerViewAdapter(List<HistoryEntry> historyEntries) {
        this.historyEntries = historyEntries;

    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final HistoryViewHolder holder, int position) {
        holder.renderHistory(historyEntries.get(position));
        holder.historyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VenueInDetailsNestedScrollView.class);
                intent.putExtra("VENUE_ID", historyEntries.get(position).getReferenceVenueId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyEntries.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        public final View historyView;
        public final TextView historyVenueName;
        public final TextView historyVenueShortName;
        public final ImageView historyVenueImage;
        public final TextView historyVenueState;
        public final TextView historyDate;
        public final ImageView historyDelete;

        public HistoryViewHolder(View view) {
            super(view);
            historyView = view;
            historyVenueName = (TextView) view.findViewById(R.id.history_venue_name);
            historyVenueShortName = (TextView) view.findViewById(R.id.history_venue_short_name);
            historyVenueImage = (ImageView) view.findViewById(R.id.history_venue_image);
            historyVenueState = (TextView) view.findViewById(R.id.history_state);
            historyDate = (TextView) view.findViewById(R.id.history_date);
            historyDelete = (ImageView) view.findViewById(R.id.history_venue_delete);


        }

        public void renderHistory(HistoryEntry historyEntry) {
            this.historyVenueName.setText(historyEntry.getVenueName());
            this.historyVenueShortName.setText(historyEntry.getVenueName().substring(0, 1));
            SimpleDateFormat formatter = new SimpleDateFormat("EEE HH:mm:ss dd-MM-yyyy");
            String dateToString = formatter.format(historyEntry.getDate());
            this.historyDate.setText(dateToString);
            this.historyVenueState.setText(getHistoryState(historyEntry.getHistoryType()));

            historyDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocalDataBaseManager.getLocalDatabaseManager(context).getDaoSession().getHistoryEntryDao().delete(historyEntry);
                    historyEntries.remove(historyEntry);
                    notifyDataSetChanged();
                }
            });

        }

        public String getHistoryState(HistoryType historyType) {
            switch (historyType) {
                case CHECKIN:
                    return context.getResources().getString(R.string.history_state_check_in);
               /* case VISIT:
                    return context.getResources().getString(R.string.history_state_visit);*/
                case LIKECOMMENT:
                    return context.getResources().getString(R.string.history_state_like);
                case DISLIKE_COMMENT:
                    return context.getResources().getString(R.string.history_state_dislike);
                case TEXT_COMMENT:
                    return context.getResources().getString(R.string.history_state_text_comment);
                case  IMAGE_COMMENT:
                    return context.getResources().getString(R.string.history_state_image_comment);
                default:
                    return null;
            }
        }

    }

    public List<HistoryEntry> getHistoryEntries() {
        return historyEntries;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
