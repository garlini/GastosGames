package com.garlini.gastosgames.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.garlini.gastosgames.model.Plataforma;

public class PlataformaDatabaseHandler {
	
	public static final String TABLE_PLATAFORMAS = "plataformas";
	
	public static final String KEY_ID = "id";
	public static final String KEY_NOME = "nome";
	public static final String KEY_ATIVA = "ativa";
	

	public PlataformaDatabaseHandler() {
		super();
	}
	
	public void createTablePlataformas(SQLiteDatabase db)
	{
		String sql = "CREATE TABLE \"" + TABLE_PLATAFORMAS + "\" (" +
				"\"" + KEY_ID + "\" INTEGER PRIMARY KEY, " +
				"\"" + KEY_NOME + "\" TEXT NOT NULL," +
				"\"" + KEY_ATIVA + "\" TINYINT NOT NULL" +
				");";
		db.execSQL(sql);
		
		//cria index na coluna "nome"
		sql = "CREATE INDEX \"plataformas_nome_idx\" ON \"" + TABLE_PLATAFORMAS + "\" (\"" + KEY_NOME + "\");";
		db.execSQL(sql);
		
		//cria index na coluna "ativa"
		sql = "CREATE INDEX \"plataformas_ativa_idx\" ON \"" + TABLE_PLATAFORMAS + "\" (\"" + KEY_ATIVA + "\");";
		db.execSQL(sql);
		
		//Insere as plataformas iniciais
		ContentValues values = new ContentValues();
		values.put(KEY_NOME, "Wii U");
		values.put(KEY_ATIVA, 1);
		db.insert(TABLE_PLATAFORMAS, null, values);
		
		values.put(KEY_NOME, "PS4");
		values.put(KEY_ATIVA, 1);
		db.insert(TABLE_PLATAFORMAS, null, values);
		
		values.put(KEY_NOME, "Xbox One");
		values.put(KEY_ATIVA, 1);
		db.insert(TABLE_PLATAFORMAS, null, values);
		
		values.put(KEY_NOME, "PC");
		values.put(KEY_ATIVA, 1);
		db.insert(TABLE_PLATAFORMAS, null, values);
		
		values.put(KEY_NOME, "N3DS");
		values.put(KEY_ATIVA, 1);
		db.insert(TABLE_PLATAFORMAS, null, values);
		
		values.put(KEY_NOME, "PS Vita");
		values.put(KEY_ATIVA, 1);
		db.insert(TABLE_PLATAFORMAS, null, values);
		
	}
	
	public List<Plataforma> getAllPlataformas(SQLiteDatabase db) 
	{
		List<Plataforma> lista = new ArrayList<>();
		
		String sql = 
				"SELECT p." + KEY_ID +  ", p." + KEY_NOME + ", p." + KEY_ATIVA +
				" FROM " + TABLE_PLATAFORMAS + " p" +
			    " ORDER BY " + KEY_ID;
		
		Cursor cursor = db.rawQuery(sql, null);
		
		if (cursor.moveToFirst()) {
			do {
				lista.add(createPlataformaFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		
		cursor.close();
				
		return lista;
	}
	
	public List<Plataforma> getAllPlataformasAtivas(SQLiteDatabase db) 
	{
		List<Plataforma> lista = new ArrayList<>();
		
		String sql = 
				"SELECT p." + KEY_ID +  ", p." + KEY_NOME + ", p." + KEY_ATIVA +
				" FROM " + TABLE_PLATAFORMAS + " p" +
			    " WHERE p." + KEY_ATIVA + " = 1" +
			    " ORDER BY " + KEY_ID;
		
		Cursor cursor = db.rawQuery(sql, null);
		
		if (cursor.moveToFirst()) {
			do {
				lista.add(createPlataformaFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		
		cursor.close();
				
		return lista;
	}
		
	public Plataforma getPlataforma(SQLiteDatabase db, Long id)
	{
		Plataforma plataforma = null;
		
		Cursor cursor = db.query(TABLE_PLATAFORMAS, 
				new String[] {KEY_ID, KEY_NOME, KEY_ATIVA}, 
				KEY_ID + " = ?", 
				new String[] {String.valueOf(id)}, 
				null, null, null);
		
		if (cursor.moveToFirst()) {
			plataforma = createPlataformaFromCursor(cursor);			
		}
				
		cursor.close();
		
		return plataforma;
	}
	
	public Plataforma getPlataformaPeloNome(SQLiteDatabase db, String nome)
	{
		Plataforma plataforma = null;
		
		Cursor cursor = db.query(TABLE_PLATAFORMAS, 
				new String[] {KEY_ID, KEY_NOME, KEY_ATIVA}, 
				KEY_NOME + " = ? COLLATE NOCASE", 
				new String[] {nome}, 
				null, null, null);
		
		if (cursor.moveToFirst()) {
			plataforma = createPlataformaFromCursor(cursor);			
		}
				
		cursor.close();
		
		return plataforma;
	}
	
	public Cursor createListCursor(SQLiteDatabase db)
	{
		return createListCursor(db, false);
	}
	
	public Cursor createListCursor(SQLiteDatabase db, boolean apenasAtivas)
	{
		String sql = 
				"SELECT p." + KEY_ID +  " as _id, p." + KEY_NOME + 
				" FROM " + TABLE_PLATAFORMAS + " p";
			    
		if (apenasAtivas) {
			sql += " WHERE p." + KEY_ATIVA + " = 1";
		}
		
		sql += " ORDER BY " + KEY_ID;
		
		return db.rawQuery(sql, null);
	}
	
	public boolean insertPlataforma(SQLiteDatabase db, Plataforma plataforma)
	{
		
		ContentValues values = new ContentValues();
		
		values.put(KEY_NOME, plataforma.getNome());
		values.put(KEY_ATIVA, plataforma.getAtiva());
		
		long id = db.insertOrThrow(TABLE_PLATAFORMAS, null, values);
		
		plataforma.setId(id);
		
		return id >= 0;
	}
	
	public boolean updatePlataforma(SQLiteDatabase db, Plataforma plataforma)
	{
		if (plataforma.getId() == null) {
			throw new IllegalArgumentException("Plataforma não possui um id.");
		}
		
		ContentValues values = new ContentValues();
		
		values.put(KEY_NOME, plataforma.getNome());
		values.put(KEY_ATIVA, plataforma.getAtiva());
		
		return db.update(TABLE_PLATAFORMAS, values, KEY_ID + " = ?", new String[] {String.valueOf(plataforma.getId())}) > 0; 
	}
	
	public int deletePlataforma(SQLiteDatabase db, long id)
	{
		return db.delete(TABLE_PLATAFORMAS, KEY_ID + " = ?", new String[] {String.valueOf(id)});
	}
	
	protected Plataforma createPlataformaFromCursor(Cursor cursor)
	{
		Long id = cursor.getLong(0);
		String nome = cursor.getString(1);
		Boolean ativa = cursor.getInt(2) != 0;
		
		return new Plataforma(id, nome, ativa);
	}
}
