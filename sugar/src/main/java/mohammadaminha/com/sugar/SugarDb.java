package mohammadaminha.com.sugar;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import mohammadaminha.com.sugar.helper.ManifestHelper;

import static mohammadaminha.com.sugar.SugarContext.getDbConfiguration;
import static mohammadaminha.com.sugar.helper.ManifestHelper.getDatabaseVersion;

public class SugarDb extends SQLiteOpenHelper {
    private static final String LOG_TAG = "Sugar";

    private static SchemaGenerator schemaGenerator;
    private static SQLiteDatabase db;
    private int openedConnections = 0;

    public static Context context;
    public static String dbName;
    public static SQLiteDatabase writ;
    public static File dbaddress;
    public static int version = 1;

    //Prevent instantiation
    public SugarDb(Context context, String dbname, int version) {
        super(context, dbname, null, version);
        this.context = context;
        this.version = version;
        this.dbName = dbname;
        schemaGenerator = SchemaGenerator.getInstance();
    }

    public SugarDb(Context context) {
        super(context, ManifestHelper.getDatabaseName(), null, getDatabaseVersion());
        this.context = context;
        this.dbName = ManifestHelper.getDatabaseName();
        this.version = getDatabaseVersion();
        schemaGenerator = SchemaGenerator.getInstance();
    }

    public static SugarDb getInstance() {
        return new SugarDb(context);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //schemaGenerator.createDatabase(sqLiteDatabase);


    }

    public boolean database() {
        boolean res = false;
        if (checkdb()) {
            res = true;
            open();

            writ = getWritableDatabase();
            schemaGenerator.createDatabase(writ);
        } else {
            //getReadableDatabase();
            open();

            writ = getWritableDatabase();
            schemaGenerator.createDatabase(writ);

            return createDatabase();
        }
        return res;
    }

    public static boolean createDatabase() {
        return context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null) != null;
    }

    public static boolean checkdb() {
        SQLiteDatabase db = null;

        File file = null;

        try {
            dbaddress = context.getDatabasePath(dbName);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dbaddress.exists();
    }

    public static void open() {
        try {
            dbaddress = context.getDatabasePath(dbName);
            db = SQLiteDatabase.openDatabase(dbaddress.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLException e) {
            //Toast.makeText(context, "خطا در خواندن اطلاعات", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        final SugarDbConfiguration configuration = getDbConfiguration();

        if (null != configuration) {
            db.setLocale(configuration.getDatabaseLocale());
            db.setMaximumSize(configuration.getMaxSize());
            db.setPageSize(configuration.getPageSize());
        }

        super.onConfigure(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        schemaGenerator.doUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }

    public synchronized SQLiteDatabase getDB() {
        if (writ == null)
            if (database()) {
                return writ;
            } else {
                Toast.makeText(context, "خطا در بازیابی دیتابیس", Toast.LENGTH_LONG).show();
                return null;
            }
        else
            return writ;

    }

    public synchronized SQLiteDatabase getWrite() {
        if (writ == null)
            if (database()) {
                return writ;
            } else {
                Toast.makeText(context, "خطا در بازیابی دیتابیس", Toast.LENGTH_LONG).show();
                return null;
            }
        else
            return writ;
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (ManifestHelper.isDebugEnabled()) {
            Log.d(LOG_TAG, "getReadableDatabase");
        }
        openedConnections++;
        return super.getReadableDatabase();
    }

    @Override
    public synchronized void close() {
        if (ManifestHelper.isDebugEnabled()) {
            Log.d(LOG_TAG, "getReadableDatabase");
        }
        openedConnections--;
        if (openedConnections == 0) {
            if (ManifestHelper.isDebugEnabled()) {
                Log.d(LOG_TAG, "closing");
            }
            super.close();
        }
    }
}
