package ml.dogboy.yanius;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UploadedItemsDatabase extends SQLiteOpenHelper {

    public UploadedItemsDatabase(Context context) {
        super(context, "YaniusUploadedItems", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE uploadeditems (filename TEXT, size INT, succeeded BOOLEAN, url TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
}
