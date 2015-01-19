package com.garlini.gastosgames.database;

import java.util.ArrayList;
import java.util.List;

import com.garlini.gastosgames.model.Categoria;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CategoriaDatabaseHandler {

	public static final String TABLE_CATEGORIAS = "categorias";
	
	public static final String KEY_ID = "id";
	public static final String KEY_NOME = "nome";
	private static final String KEY_VENDIVEL = "vendivel";
	
	public void createTableCategorias(SQLiteDatabase db)
	{
		String sql = "CREATE TABLE \"" + TABLE_CATEGORIAS + "\" (" + 
				"\"" + KEY_ID + "\" INTEGER PRIMARY KEY, " + 
				"\"" + KEY_NOME + "\" TEXT NOT NULL," +
				"\"" + KEY_VENDIVEL + "\" TINYINT NOT NULL" +
				");";
		db.execSQL(sql);
		
		//cria index na coluna nome
		sql = "CREATE INDEX \"categorias_nome_idx\" ON \"" + TABLE_CATEGORIAS + "\" (\"" + KEY_NOME + "\");";
		db.execSQL(sql);
		
		//insere as categorias iniciais
		ContentValues values = new ContentValues();
		values.put(KEY_NOME, "Console");
		values.put(KEY_VENDIVEL, 1);
		db.insert(TABLE_CATEGORIAS, null, values);
		
		values.put(KEY_NOME, "Computador");
		values.put(KEY_VENDIVEL, 1);
		db.insert(TABLE_CATEGORIAS, null, values);
		
		values.put(KEY_NOME, "Componente Computador");
		values.put(KEY_VENDIVEL, 1);
		db.insert(TABLE_CATEGORIAS, null, values);
		
		values.put(KEY_NOME, "Acessório");
		values.put(KEY_VENDIVEL, 1);
		db.insert(TABLE_CATEGORIAS, null, values);
		
		values.put(KEY_NOME, "Game");
		values.put(KEY_VENDIVEL, 1);
		db.insert(TABLE_CATEGORIAS, null, values);
		
		values.put(KEY_NOME, "Game Digital");
		values.put(KEY_VENDIVEL, 0);
		db.insert(TABLE_CATEGORIAS, null, values);
		
		values.put(KEY_NOME, "Assinatura Serviço");
		values.put(KEY_VENDIVEL, 0);
		db.insert(TABLE_CATEGORIAS, null, values);
		
		values.put(KEY_NOME, "Aluguel");
		values.put(KEY_VENDIVEL, 0);
		db.insert(TABLE_CATEGORIAS, null, values);
		
		values.put(KEY_NOME, "Conserto");
		values.put(KEY_VENDIVEL, 0);
		db.insert(TABLE_CATEGORIAS, null, values);
	}
	
	public Cursor createListCursor(SQLiteDatabase db)
	{
		String sql = 
				"SELECT p." + KEY_ID +  " as _id, p." + KEY_NOME + 
				" FROM " + TABLE_CATEGORIAS + " p" +
			    " ORDER BY " + KEY_ID;
		
		return db.rawQuery(sql, null);
	}
	
	public int deleteCategoria(SQLiteDatabase db, long id)
	{
		return db.delete(TABLE_CATEGORIAS, KEY_ID + " = ?", new String[] {String.valueOf(id)});
	}
	
	public Categoria getCategoria(SQLiteDatabase db, Long id)
	{
		Categoria categoria = null;
		
		Cursor cursor = db.query(TABLE_CATEGORIAS, 
				new String[] {KEY_ID, KEY_NOME, KEY_VENDIVEL}, 
				KEY_ID + " = ?", 
				new String[] {String.valueOf(id)}, 
				null, null, null);
		
		if (cursor.moveToFirst()) {
			categoria = createCategoriaFromCursor(cursor);			
		}
				
		cursor.close();
		
		return categoria;
	}
	
	public Categoria getCategoriaPeloNome(SQLiteDatabase db, String nome)
	{
		Categoria categoria = null;
		
		Cursor cursor = db.query(TABLE_CATEGORIAS, 
				new String[] {KEY_ID, KEY_NOME, KEY_VENDIVEL}, 
				KEY_NOME + " = ? COLLATE NOCASE", 
				new String[] {nome}, 
				null, null, null);
		
		if (cursor.moveToFirst()) {
			categoria = createCategoriaFromCursor(cursor);			
		}
				
		cursor.close();
		
		return categoria;
	}
	
	protected Categoria createCategoriaFromCursor(Cursor cursor)
	{
		Long id = cursor.getLong(0);
		String nome = cursor.getString(1);
		Boolean vendivel = cursor.getInt(2) != 0;
		
		return new Categoria(id, nome, vendivel);
	}
	
	public boolean insertCategoria(SQLiteDatabase db, Categoria categoria)
	{
		
		ContentValues values = new ContentValues();
		
		values.put(KEY_NOME, categoria.getNome());
		values.put(KEY_VENDIVEL, categoria.isVendivel() ? 1 : 0);
		
		long id = db.insertOrThrow(TABLE_CATEGORIAS, null, values);
		
		categoria.setId(id);
		
		return id >= 0;
	}
	
	public boolean updateCategoria(SQLiteDatabase db, Categoria categoria)
	{
		if (categoria.getId() == null) {
			throw new IllegalArgumentException("Categoria não possui um id.");
		}
		
		ContentValues values = new ContentValues();
		
		values.put(KEY_NOME, categoria.getNome());
		values.put(KEY_VENDIVEL, categoria.isVendivel() ? 1 : 0);
		
		return db.update(TABLE_CATEGORIAS, values, KEY_ID + " = ?", new String[] {String.valueOf(categoria.getId())}) > 0; 
	}
	
	public List<Categoria> getAllCategorias(SQLiteDatabase db) 
	{
		List<Categoria> lista = new ArrayList<>();
		
		String sql = 
				"SELECT c." + KEY_ID +  ", c." + KEY_NOME + ", c." + KEY_VENDIVEL +
				" FROM " + TABLE_CATEGORIAS + " c" +
			    " ORDER BY c." + KEY_ID;
		
		Cursor cursor = db.rawQuery(sql, null);
		
		if (cursor.moveToFirst()) {
			do {
				lista.add(createCategoriaFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		
		cursor.close();
		
		return lista;
	}
}
