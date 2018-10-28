package mohammadaminha.com.sugar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;

import mohammadaminha.com.sugar.annotation.Table;
import mohammadaminha.com.sugar.annotation.Unique;
import mohammadaminha.com.sugar.helper.ManifestHelper;
import mohammadaminha.com.sugar.helper.NamingHelper;
import mohammadaminha.com.sugar.inflater.EntityInflater;
import mohammadaminha.com.sugar.util.QueryBuilder;
import mohammadaminha.com.sugar.util.ReflectionUtil;
import mohammadaminha.com.sugar.util.SugarCursor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static mohammadaminha.com.sugar.SugarContext.getSugarContext;


public class SugarRecord {
    public static final String SUGAR = "Sugar";

    @Unique
    private Long id = null;

    public SugarRecord() {
        SchemaGenerator schemaGenerator = SchemaGenerator.getInstance();
        schemaGenerator.createDatabase(SugarDb.getInstance().getDB(), this.getClass());

    }

    public SugarRecord(Class clName) {
        SchemaGenerator schemaGenerator = SchemaGenerator.getInstance();
        schemaGenerator.createDatabase(SugarDb.getInstance().getDB(), clName);
    }

    
    public void ExecuteQuery(String query) {
        getSugarDataBase().execSQL(query);
    }

    public SQLiteDatabase db() {
        return getSugarContext().getSugarDb().getDB();
    }

    private static SQLiteDatabase getSugarDataBase() {
        return getSugarContext().getSugarDb().getDB();
    }

    public static <T> int deleteAll(Class type) {
        if (isExist(type))
            return deleteAll(type, null);
        else
            return 0;

    }

    public static <T> int deleteAll(Class type, String whereClause, String... whereArgs) {
        if (isExist(type))
            return getSugarDataBase().delete(NamingHelper.toTableName(type), whereClause, whereArgs);
        else
            return 0;
    }

    public static <T> Cursor getCursor(Class type, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit) {
        if (isExist(type)) {
            Cursor raw = getSugarDataBase().query(NamingHelper.toTableName(type), null, whereClause, whereArgs,
                    groupBy, null, orderBy, limit);
            return new SugarCursor(raw);
        } else
            return null;

    }

    @SuppressWarnings("deprecation")
    public static <T> void saveInTx(T... objects) {
        saveInTx(Arrays.asList(objects));
    }

    @SuppressWarnings("deprecation")
    public static <T> void saveInTx(Collection<T> objects) {
        SQLiteDatabase sqLiteDatabase = getSugarDataBase();
        try {
            sqLiteDatabase.beginTransaction();
            sqLiteDatabase.setLockingEnabled(false);
            for (T object : objects) {
                save(object);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            if (ManifestHelper.isDebugEnabled()) {
                Log.i(SUGAR, "Error in saving in transaction " + e.getMessage());
            }
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.setLockingEnabled(true);
        }
    }

    @SuppressWarnings("deprecation")
    public static <T> void updateInTx(T... objects) {
        updateInTx(Arrays.asList(objects));
    }

    @SuppressWarnings("deprecation")
    public static <T> void updateInTx(Collection<T> objects) {
        SQLiteDatabase sqLiteDatabase = getSugarDataBase();
        try {
            sqLiteDatabase.beginTransaction();
            sqLiteDatabase.setLockingEnabled(false);
            for (T object : objects) {
                update(object);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            if (ManifestHelper.isDebugEnabled()) {
                Log.i(SUGAR, "Error in saving in transaction " + e.getMessage());
            }
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.setLockingEnabled(true);
        }
    }

    @SuppressWarnings("deprecation")
    public static <T> int deleteInTx(T... objects) {
        return deleteInTx(Arrays.asList(objects));
    }

    @SuppressWarnings("deprecation")
    public static <T> int deleteInTx(Collection<T> objects) {
        SQLiteDatabase sqLiteDatabase = getSugarDataBase();
        int deletedRows = 0;
        try {
            sqLiteDatabase.beginTransaction();
            sqLiteDatabase.setLockingEnabled(false);
            for (T object : objects) {
                if (delete(object)) {
                    ++deletedRows;
                }
            }
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            deletedRows = 0;
            if (ManifestHelper.isDebugEnabled()) {
                Log.i(SUGAR, "Error in deleting in transaction " + e.getMessage());
            }
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.setLockingEnabled(true);
        }
        return deletedRows;
    }

    public static <T> List<T> listAll(Class type) {
        if (isExist(type))
            return find(type, null, null, null, null, null);
        else
            return null;
    }

    public static <T> List<T> listAll(Class type, String orderBy) {
        if (isExist(type))
            return find(type, null, null, null, orderBy, null);
        else
            return null;
    }

    public static <T> T findById(Class type, Long id) {
        if (isExist(type)) {
            List<T> list = find(type, "id=?", new String[]{String.valueOf(id)}, null, null, "1");
            if (list.isEmpty()) return null;
            return list.get(0);
        } else
            return null;
    }

    public static <T> T findById(Class type, Integer id) {
        if (isExist(type))
            return findById(type, Long.valueOf(id));
        else
            return null;
    }

    public static <T> List<T> findById(Class type, String... ids) {
        if (isExist(type)) {
            String whereClause = "id IN (" + QueryBuilder.generatePlaceholders(ids.length) + ")";
            return find(type, whereClause, ids);
        } else
            return null;
    }

    public static <T> T first(Class type) {
        if (isExist(type)) {
            List<T> list = findWithQuery(type,
                    "SELECT * FROM " + NamingHelper.toTableName(type) + " ORDER BY ID ASC LIMIT 1");
            if (list.isEmpty()) {
                return null;
            }
            return list.get(0);
        } else
            return null;
    }

    public static <T> T last(Class type) {
        if (isExist(type)) {
            List<T> list = findWithQuery(type,
                    "SELECT * FROM " + NamingHelper.toTableName(type) + " ORDER BY ID DESC LIMIT 1");
            if (list.isEmpty()) {
                return null;
            }
            return list.get(0);
        } else
            return null;
    }

    public static <T> Iterator<T> findAll(Class type) {
        if (isExist(type))
            return findAsIterator(type, null, null, null, null, null);
        else
            return null;
    }

    public static <T> Iterator<T> findAsIterator(Class type, String whereClause, String... whereArgs) {
        if (isExist(type))
            return findAsIterator(type, whereClause, whereArgs, null, null, null);
        else
            return null;
    }

    public static <T> Iterator<T> findWithQueryAsIterator(Class type, String query, String... arguments) {
        if (isExist(type)) {
            Cursor cursor = getSugarDataBase().rawQuery(query, arguments);
            return new CursorIterator<>(type, cursor);
        } else return null;
    }

    public static <T> Iterator<T> findAsIterator(Class type, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit) {
        if (isExist(type)) {
            Cursor cursor = getSugarDataBase().query(NamingHelper.toTableName(type), null, whereClause, whereArgs,
                    groupBy, null, orderBy, limit);
            return new CursorIterator<>(type, cursor);
        } else
            return null;
    }

    public static <T> List<T> find(Class type, String whereClause, String... whereArgs) {
        if (isExist(type))
            try {
                return find(type, whereClause, whereArgs, null, null, null);
            } catch (Exception e) {
                if (e.getMessage() != null)
                    if (e.getMessage().contains("has no column") || e.getMessage().contains("no such column")) {
                        List<Field> columns = ReflectionUtil.getTableFields(type.getClass());
                        for (Field column : columns) {
                            existsColumnInTable(getSugarDataBase(), type.getClass(), column);
                        }
                        return find(type, whereClause, whereArgs, null, null, null);
                    }
            }
        return null;
    }

    public static <T> List<T> findWithQuery(Class type, String query, String... arguments) {
        if (isExist(type)) {
            Cursor cursor = getSugarDataBase().rawQuery(query, arguments);
            return getEntitiesFromCursor(cursor, type);
        } else return null;
    }

    public static Cursor executeQuery(String query, String... arguments) {
        return getSugarDataBase().rawQuery(query, arguments);
    }

    public static List find(Class type, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit) {
        if (isExist(type)) {
            try {
                String args[];
                args = (whereArgs == null) ? null : replaceArgs(whereArgs);

                Cursor cursor = getSugarDataBase().query(NamingHelper.toTableName(type), null, whereClause, args,
                        groupBy, null, orderBy, limit);

                return getEntitiesFromCursor(cursor, type);

            } catch (Exception e) {
                if (e.getMessage() != null)
                    if (e.getMessage().contains("has no column") || e.getMessage().contains("no such column")) {
                        List<Field> columns = ReflectionUtil.getTableFields(type.getClass());
                        for (Field column : columns) {
                            existsColumnInTable(getSugarDataBase(), type.getClass(), column);
                        }
                        find(type, whereClause, whereArgs, groupBy, orderBy, limit);
                    }
            }
        } else {
            new SugarRecord(type);
        }
        return null;

    }

    public static <T> List<T> findOneToMany(Class type, String relationFieldName, Object relationObject, Long relationObjectId) {
        if (isExist(type)) {
            try {
                String args[] = {String.valueOf(relationObjectId)};
                String whereClause = NamingHelper.toSQLNameDefault(relationFieldName) + " = ?";

                Cursor cursor = getSugarDataBase().query(NamingHelper.toTableName(type), null, whereClause, args,
                        null, null, null, null);

                return getEntitiesFromCursor(cursor, type, relationFieldName, relationObject);
            } catch (Exception e) {
                if (e.getMessage() != null)
                    if (e.getMessage().contains("has no column") || e.getMessage().contains("no such column")) {
                        List<Field> columns = ReflectionUtil.getTableFields(type.getClass());
                        for (Field column : columns) {
                            existsColumnInTable(getSugarDataBase(), type.getClass(), column);
                        }
                        findOneToMany(type, relationFieldName, relationObject, relationObjectId);
                    }
            }
        } else {
            new SugarRecord(type);
        }
        return null;

    }

    public static <T> List<T> getEntitiesFromCursor(Cursor cursor, Class type) {
        if (isExist(type))
            try {
                return getEntitiesFromCursor(cursor, type, null, null);
            } catch (Exception e) {
                if (e.getMessage() != null)
                    if (e.getMessage().contains("has no column") || e.getMessage().contains("no such column")) {
                        List<Field> columns = ReflectionUtil.getTableFields(type.getClass());
                        for (Field column : columns) {
                            existsColumnInTable(getSugarDataBase(), type.getClass(), column);
                        }
                        return getEntitiesFromCursor(cursor, type, null, null);
                    }
                return null;
            }
        else
            return null;
    }

    public static <T> List<T> getEntitiesFromCursor(Cursor cursor, Class type, String relationFieldName, Object relationObject) {
        if (isExist(type)) {
            T entity;
            List result = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    entity = (T) type.getDeclaredConstructor().newInstance();
                    new EntityInflater()
                            .withCursor(cursor)
                            .withObject(entity)
                            .withEntitiesMap(getSugarContext().getEntitiesMap())
                            .withRelationFieldName(relationFieldName)
                            .withRelationObject(relationObject)
                            .inflate();
                    result.add(entity);
                }
            } catch (Exception e) {
                if (e.getMessage() != null)
                    if (e.getMessage().contains("has no column") || e.getMessage().contains("no such column")) {
                        List<Field> columns = ReflectionUtil.getTableFields(type.getClass());
                        for (Field column : columns) {
                            existsColumnInTable(getSugarDataBase(), type.getClass(), column);
                        }
                        return getEntitiesFromCursor(cursor, type, relationFieldName, relationObject);
                    }
            } finally {
                cursor.close();
            }

            return result;
        } else
            return null;
    }

    public static <T> long count(Class type) {
        if (isExist(type))
            return count(type, null, null, null, null, null);
        else return 0;
    }

    public static <T> long count(Class type, String whereClause, String... whereArgs) {
        if (isExist(type))
            return count(type, whereClause, whereArgs, null, null, null);
        else return 0;
    }

    public static <T> long count(Class type, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit) {
        if (isExist(type)) {
            long result = -1;
            String filter = (!TextUtils.isEmpty(whereClause)) ? " where " + whereClause : "";
            SQLiteStatement sqliteStatement;
            try {
                sqliteStatement = getSugarDataBase().compileStatement("SELECT count(*) FROM " + NamingHelper.toTableName(type) + filter);
            } catch (SQLiteException e) {
                e.printStackTrace();
                return result;
            }

            if (whereArgs != null) {
                for (int i = whereArgs.length; i != 0; i--) {
                    sqliteStatement.bindString(i, whereArgs[i - 1]);
                }
            }

            try {
                result = sqliteStatement.simpleQueryForLong();
            } finally {
                sqliteStatement.close();
            }

            return result;
        } else return 0;
    }

    public static <T> long sum(Class type, String field) {
        if (isExist(type))
            return sum(type, field, null, null);
        else return 0;
    }

    public static <T> long sum(Class type, String field, String whereClause, String... whereArgs) {
        if (isExist(type)) {
            long result = -1;
            String filter = (!TextUtils.isEmpty(whereClause)) ? " where " + whereClause : "";
            SQLiteStatement sqLiteStatement;
            try {
                sqLiteStatement = getSugarDataBase().compileStatement("SELECT sum(" + field + ") FROM " + NamingHelper.toTableName(type) + filter);
            } catch (SQLiteException e) {
                e.printStackTrace();
                return result;
            }

            if (whereArgs != null) {
                for (int i = whereArgs.length; i != 0; i--) {
                    sqLiteStatement.bindString(i, whereArgs[i - 1]);
                }
            }

            try {
                result = sqLiteStatement.simpleQueryForLong();
            } finally {
                sqLiteStatement.close();
            }

            return result;
        } else return 0;
    }

    public static long save(Object object) {
        return save(getSugarDataBase(), object);
    }

    static long save(SQLiteDatabase db, Object object) {
        long id = 0;
        try {
            Map<Object, Long> entitiesMap = getSugarContext().getEntitiesMap();
            List<Field> columns = ReflectionUtil.getTableFields(object.getClass());
            ContentValues values = new ContentValues(columns.size());
            Field idField = null;
            for (Field column : columns) {
                ReflectionUtil.addFieldValueToColumn(values, column, object, entitiesMap);
                if (column.getName().equals("ID")) {
                    idField = column;
                }
            }

            boolean isSugarEntity = isSugarEntity(object.getClass());
            if (isSugarEntity && entitiesMap.containsKey(object)) {
                values.put("ID", entitiesMap.get(object));
            }

            id = db.insertWithOnConflict(NamingHelper.toTableName(object.getClass()), null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);

            if (object.getClass().isAnnotationPresent(Table.class)) {
                if (idField != null) {
                    idField.setAccessible(true);
                    try {
                        idField.set(object, id);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    entitiesMap.put(object, id);
                }
            } else if (SugarRecord.class.isAssignableFrom(object.getClass())) {
                ((SugarRecord) object).setId(id);
            }

            if (ManifestHelper.isDebugEnabled()) {
                Log.i(SUGAR, object.getClass().getSimpleName() + " saved : " + id);
            }
        } catch (SQLiteException e) {
            if (e.getMessage() != null)
                if (e.getMessage().contains("has no column") || e.getMessage().contains("no such column")) {
                    List<Field> columns = ReflectionUtil.getTableFields(object.getClass());
                    for (Field column : columns) {
                        existsColumnInTable(db, object.getClass(), column);
                    }
                    save(db, object);
                }
        }

        return id;
    }


    public static long update(Object object) {
        return update(getSugarDataBase(), object);
    }

    static long update(SQLiteDatabase db, Object object) {
        long rowsEffected = 0;
        try {
            Map<Object, Long> entitiesMap = getSugarContext().getEntitiesMap();
            List<Field> columns = ReflectionUtil.getTableFields(object.getClass());
            ContentValues values = new ContentValues(columns.size());

            StringBuilder whereClause = new StringBuilder();
            List<String> whereArgs = new ArrayList<>();

            for (Field column : columns) {
                String columnName = NamingHelper.toColumnName(column);
                if (column.isAnnotationPresent(Unique.class)) {
                    try {
                        column.setAccessible(true);
                        Object columnValue = column.get(object);

                        whereClause.append(columnName).append(" = ?");
                        whereArgs.add(String.valueOf(columnValue));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else if (!columnName.equals("ID")) {
                    ReflectionUtil.addFieldValueToColumn(values, column, object, entitiesMap);
                }

            }

            String[] whereArgsArray = whereArgs.toArray(new String[whereArgs.size()]);
            // Get SugarRecord based on Unique values
            rowsEffected = db.update(NamingHelper.toTableName(object.getClass()), values, whereClause.toString(), whereArgsArray);
        } catch (SQLiteException e) {
            if (e.getMessage() != null)
                if (e.getMessage().contains("has no column") || e.getMessage().contains("no such column") || e.getMessage().contains("no such column")) {
                    List<Field> columns = ReflectionUtil.getTableFields(object.getClass());
                    for (Field column : columns) {
                        existsColumnInTable(db, object.getClass(), column);
                    }
                    update(db, object);
                }
        }
        if (rowsEffected == 0) {
            return update(object);
        } else {
            return rowsEffected;
        }
    }

    public static boolean isSugarEntity(Class<?> objectClass) {
        return objectClass.isAnnotationPresent(Table.class) || SugarRecord.class.isAssignableFrom(objectClass);
    }

    public boolean delete() {
        if (isExist(getClass())) {
            Long id = getId();
            Class<?> type = getClass();
            if (id != null && id > 0L) {
                if (ManifestHelper.isDebugEnabled()) {
                    Log.i(SUGAR, type.getSimpleName() + " deleted : " + id);
                }
                return getSugarDataBase().delete(NamingHelper.toTableName(type), "Id=?", new String[]{id.toString()}) == 1;
            } else {
                if (ManifestHelper.isDebugEnabled()) {
                    Log.i(SUGAR, "Cannot delete object: " + type.getSimpleName() + " - object has not been saved");
                }
                return false;
            }
        } else return false;
    }

    public static boolean delete(Object object) {
        if (isExist(object.getClass())) {
            Class<?> type = object.getClass();
            if (type.isAnnotationPresent(Table.class)) {
                try {
                    Field field = type.getDeclaredField("ID");
                    field.setAccessible(true);
                    Long id = (Long) field.get(object);
                    if (id != null && id > 0L) {
                        boolean deleted = getSugarDataBase().delete(NamingHelper.toTableName(type), "Id=?", new String[]{id.toString()}) == 1;
                        if (ManifestHelper.isDebugEnabled()) {
                            Log.i(SUGAR, type.getSimpleName() + " deleted : " + id);
                        }
                        return deleted;
                    } else {
                        if (ManifestHelper.isDebugEnabled()) {
                            Log.i(SUGAR, "Cannot delete object: " + object.getClass().getSimpleName() + " - object has not been saved");
                        }
                        return false;
                    }
                } catch (NoSuchFieldException e) {
                    if (ManifestHelper.isDebugEnabled()) {
                        Log.i(SUGAR, "Cannot delete object: " + object.getClass().getSimpleName() + " - annotated object has no id");
                    }
                    return false;
                } catch (IllegalAccessException e) {
                    if (ManifestHelper.isDebugEnabled()) {
                        Log.i(SUGAR, "Cannot delete object: " + object.getClass().getSimpleName() + " - can't access id");
                    }
                    return false;
                }
            } else if (SugarRecord.class.isAssignableFrom(type)) {
                return ((SugarRecord) object).delete();
            } else {
                if (ManifestHelper.isDebugEnabled()) {
                    Log.i(SUGAR, "Cannot delete object: " + object.getClass().getSimpleName() + " - not persisted");
                }
                return false;
            }
        } else
            return false;
    }

    public long save() {
        return save(getSugarDataBase(), this);
    }

    public long update() {
        return update(getSugarDataBase(), this);
    }

    @SuppressWarnings("unchecked")
    void inflate(Cursor cursor) {
        new EntityInflater()
                .withCursor(cursor)
                .withObject(this)
                .withEntitiesMap(getSugarContext().getEntitiesMap())
                .inflate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    static class CursorIterator<E> implements Iterator<E> {
        Class<E> type;
        Cursor cursor;

        public CursorIterator(Class<E> type, Cursor cursor) {
            this.type = type;
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return cursor != null && !cursor.isClosed() && !cursor.isAfterLast();
        }

        @Override
        public E next() {
            E entity = null;
            if (cursor == null || cursor.isAfterLast()) {
                throw new NoSuchElementException();
            }

            if (cursor.isBeforeFirst()) {
                cursor.moveToFirst();
            }

            try {
                entity = type.getDeclaredConstructor().newInstance();
                new EntityInflater()
                        .withCursor(cursor)
                        .withObject(entity)
                        .withEntitiesMap(getSugarContext().getEntitiesMap())
                        .inflate();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.moveToNext();
                if (cursor.isAfterLast()) {
                    cursor.close();
                }
            }

            return entity;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static String[] replaceArgs(String[] args) {

        String[] replace = new String[args.length];
        for (int i = 0; i < args.length; i++) {

            replace[i] = (args[i].equals("true")) ? replace[i] = "1" : (args[i].equals("false")) ? replace[i] = "0" : args[i];

        }

        return replace;

    }

    public static Boolean isExist(Class cl) {
        try {
            SchemaGenerator schemaGenerator = SchemaGenerator.getInstance();
            if (schemaGenerator.isTableExists(cl, SugarDb.getInstance().getDB()))
                return true;
            else
                new SugarRecord(cl);
        } catch (Exception e) {
            new SugarRecord(cl);
            SchemaGenerator schemaGenerator = SchemaGenerator.getInstance();
            if (schemaGenerator.isTableExists(cl, SugarDb.getInstance().getDB()))
                return true;
        }
        return true;
    }

    static void existsColumnInTable(SQLiteDatabase db, Class inTable, Field columnToCheck) {
        Cursor mCursor = null;
        try {
            // Query 1 row
            mCursor = db.rawQuery("SELECT * FROM " + NamingHelper.toTableName(inTable) + " LIMIT 0", null);

            String colName = NamingHelper.toColumnName(columnToCheck);
            int i = mCursor.getColumnIndex(colName);// getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
            String query = "ALTER TABLE " + NamingHelper.toTableName(inTable) + " ADD COLUMN " + colName;
            if (i == -1)//alter table add column
            {
                String type = QueryBuilder.getColumnType(columnToCheck.getType());

                query += " " + type + " ";
                switch (type) {
                    case "INTEGER":
                    case "FLOAT":
                        query += " default 0 NULL";
                        break;
                    case "BOOLEAN":
                        query += " default true NULL";
                        break;
                    case "TEXT":
                        query += " default '' NULL";
                        break;
                }

                db.execSQL(query);
            }

        } catch (Exception Exp) {
            // Something went wrong. Missing the database? The table?
            Exp.printStackTrace();
            if (Exp.getMessage().contains("no such table"))
                new SugarRecord(inTable);
        } finally {
            if (mCursor != null) mCursor.close();
        }
    }

}
