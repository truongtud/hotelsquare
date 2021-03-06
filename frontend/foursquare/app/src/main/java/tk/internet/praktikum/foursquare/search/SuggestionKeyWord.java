package tk.internet.praktikum.foursquare.search;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;


@Entity(nameInDb = "suggestionKeyWord")

public class SuggestionKeyWord {
    @NotNull
    @Id
    private String uid;
    @NotNull
    private  String suggestionName;

    @Generated(hash = 103785274)
    public SuggestionKeyWord(@NotNull String uid, @NotNull String suggestionName) {
        this.uid = uid;
        this.suggestionName = suggestionName;
    }

    public SuggestionKeyWord() {
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSuggestionName() {
        return this.suggestionName;
    }

    public void setSuggestionName(String suggestionName) {
        this.suggestionName = suggestionName;
    }

}
