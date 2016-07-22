package com.android.samservice.provider;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;

import com.android.samservice.SamService;

public class DatabaseContext extends ContextWrapper {
	private Context dbContext;
	private String dbFolder;
	
	public DatabaseContext(Context base,String folder) {
		super(base);
		dbContext = base;
		dbFolder = folder;
    }

    @Override
    public File getDatabasePath(String name)
    {
        String filePath = dbContext.getCacheDir().getAbsolutePath();
        int index = filePath.lastIndexOf("cache");
        String packagePath = filePath.substring(0,index);

        String dbpath = packagePath+"databases"+File.separator+dbFolder;
        String dbfile = dbpath + File.separator + name;

        File path = new File(dbpath);
        if(!path.exists()){
            path.mkdirs();
        }

        File result = new File(dbfile);
        return result;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory)
    {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name),factory);
        return result;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory,
                                               DatabaseErrorHandler errorHandler) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name),factory);
        return result;
    }

};

