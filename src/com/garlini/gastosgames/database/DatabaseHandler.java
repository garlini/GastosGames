package com.garlini.gastosgames.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.garlini.gastosgames.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	 
    private static final String DATABASE_NAME = "gastos_games";
	
    private static DatabaseHandler instance = null;
    
	private DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static synchronized DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context);
        }
 
        return instance;
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		new PlataformaDatabaseHandler().createTablePlataformas(db);
		new CategoriaDatabaseHandler().createTableCategorias(db);
		new GastoDatabaseHandler().createTableGastos(db);
	}
		

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
	        // Enable foreign key constraints
	        db.execSQL("PRAGMA foreign_keys=ON;");
	    }
	}
	
	public boolean exportDB(Context context, StringBuilder local)
	{
		close();
		
		File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
                                
        if (sd.canWrite()) {
        	
        	String currentDBPath = "//data//" + context.getPackageName()
                    + "//databases//" + DATABASE_NAME;
        	
        	String backupDBPath = "//GastosGames//" + DATABASE_NAME + ".db";
        	
        	File currentDB = new File(data, currentDBPath);
        	
            File backupDB = new File(sd, backupDBPath);
            
            //retorna o local onde sera feito o backup
            local.append(backupDB.getAbsolutePath());
            
            try {
            	if (!backupDB.getParentFile().exists()) {
            		backupDB.getParentFile().mkdirs();
            	}
            	            	
            	        
            	FileInputStream srcStream = new FileInputStream(currentDB);
				FileChannel src = srcStream.getChannel();
				
				FileOutputStream dstStream = new FileOutputStream(backupDB);
				FileChannel dst = dstStream.getChannel();
				
				long srcSize = src.size();
				long transferred = dst.transferFrom(src, 0, src.size());
								
				src.close();
				srcStream.close();
				
	            dst.close();
	            dstStream.close();
				
	            return transferred == srcSize;
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
            
        }
		
		return false;
	}
		
	public boolean importDB(Context context, StringBuilder msgErro)
	{
		close();
		
		File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        
        if (sd.canWrite()) {
        	
        	String currentDBPath = "//data//" + context.getPackageName()
                    + "//databases//" + DATABASE_NAME;
        	
        	String backupDBPath = "//GastosGames//" + DATABASE_NAME + ".db";
        	
        	File backupDB = new File(data, currentDBPath);
        	
            File currentDB = new File(sd, backupDBPath);
            
            if (!currentDB.exists()) {
            	String msg = context.getString(R.string.error_arquivo_exportacao_nao_existe);
            	msg = msg.replace("__diretorio__", currentDB.getParentFile().getAbsolutePath());
            	msgErro.append(msg);
            	return false;
            }
            
            try {
            	FileInputStream srcStream = new FileInputStream(currentDB); 
				FileChannel src = srcStream.getChannel();
				
				FileOutputStream dstStream = new FileOutputStream(backupDB);
				FileChannel dst = dstStream.getChannel();
				
				long srcSize = src.size();
				long transferred = dst.transferFrom(src, 0, src.size());
	            
				src.close();
				srcStream.close();
				
				dst.close();
				dstStream.close();
				
				return transferred == srcSize;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
                    	
        }
		
		return false;
	}

}
