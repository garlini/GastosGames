package com.garlini.gastosgames.database;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.garlini.gastosgames.model.Categoria;
import com.garlini.gastosgames.model.Gasto;
import com.garlini.gastosgames.model.Plataforma;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GastoDatabaseHandler {
	
	private static final String TABLE_GASTOS = "gastos";
	
	private static final String KEY_ID = "id";
	private static final String KEY_DESCRICAO = "descricao";
	private static final String KEY_VALOR = "valor";
	private static final String KEY_DATA = "data";
	private static final String KEY_VENDIVEL = "vendivel";
	private static final String KEY_PLATAFORMA_ID = "plataforma_id";
	private static final String KEY_CATEGORIA_ID = "categoria_id";
	
	public void createTableGastos(SQLiteDatabase db)
	{
		String sql = "CREATE TABLE \"" + TABLE_GASTOS + "\" (" +
				"\"" + KEY_ID + "\" INTEGER PRIMARY KEY," +
				"\"" + KEY_DESCRICAO + "\" TEXT NOT NULL," +
				"\"" + KEY_VALOR + "\" DECIMAL(19,4) NOT NULL," +
				"\"" + KEY_DATA + "\" DATE NOT NULL," +
				"\"" + KEY_VENDIVEL + "\" TINYINT NOT NULL," +
				"\"" + KEY_PLATAFORMA_ID + "\" INT NOT NULL REFERENCES \"" + PlataformaDatabaseHandler.TABLE_PLATAFORMAS + "\"(\"" + PlataformaDatabaseHandler.KEY_ID + "\") ON DELETE CASCADE," +
				"\"" + KEY_CATEGORIA_ID + "\" INT REFERENCES \"" + CategoriaDatabaseHandler.TABLE_CATEGORIAS + "\"(\"" + CategoriaDatabaseHandler.KEY_ID + "\") ON DELETE SET NULL);";
				
		db.execSQL(sql);
		
		//insereDadosTeste(db);
				
	}
	
	/*private void insereDadosTeste(SQLiteDatabase db)
	{
		final int totalRegistrosPlataforma = 10000;
		int countGasto = 0;
		
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		
		for (Plataforma plat : new PlataformaDatabaseHandler().getAllPlataformas(db)) {
			int i = 0;
			for (i = 1; i <= totalRegistrosPlataforma; i++) {
				countGasto++;
				String descricao = "Gasto " + countGasto;
				boolean vendivel = (i % 2) == 0;
				Gasto gasto = new Gasto(null, descricao, new BigDecimal("1.00"), cal.getTime(), vendivel, plat.getId(), null);
				insertGasto(db, gasto);
				
				cal.add(Calendar.DATE, 1);
			}
		}
	}*/
	
	public Gasto getGasto(SQLiteDatabase db, Long id)
	{
		Gasto gasto = null;
		
		Cursor cursor = db.query(TABLE_GASTOS, 
				new String[] {KEY_ID, KEY_DESCRICAO, KEY_VALOR, KEY_DATA, KEY_VENDIVEL, KEY_PLATAFORMA_ID, KEY_CATEGORIA_ID}, 
				KEY_ID + " = ?", 
				new String[] {String.valueOf(id)}, 
				null, null, null);
		
		if (cursor.moveToFirst()) {
			gasto = createGastoFromCursor(cursor);			
		}
				
		cursor.close();
		
		
		return gasto;
	}
	
	
	
	protected Gasto createGastoFromCursor(Cursor cursor)
	{
		int columnIndex = 0;
		
		Long id = cursor.getLong(columnIndex++);
		String descricao = cursor.getString(columnIndex++);
		BigDecimal valor = new BigDecimal(cursor.getDouble(columnIndex++));
		Date data = new Date(cursor.getLong(columnIndex++));
		Boolean vendivel = cursor.getInt(columnIndex++) != 0;
		Long plataformaId = cursor.getLong(columnIndex++);
		Long categoriaId = cursor.getLong(columnIndex++);
		
		return new Gasto(id, descricao, valor, data, vendivel, plataformaId, categoriaId);
	}
	
	public boolean insertGasto(SQLiteDatabase db, Gasto gasto)
	{
		ContentValues values = new ContentValues();
				
		putValues(gasto, values);
		
		long id = db.insertOrThrow(TABLE_GASTOS, null, values);
		
		gasto.setId(id);
		
		return id >= 0;
	}
	
	public boolean updateGasto(SQLiteDatabase db, Gasto gasto)
	{
		if (gasto.getId() == null) {
			throw new IllegalArgumentException("Gasto não possui um id.");
		}
		
		ContentValues values = new ContentValues();
		
		putValues(gasto, values);
				
		return db.update(TABLE_GASTOS, values, KEY_ID + " = ?", new String[] {String.valueOf(gasto.getId())}) > 0; 
	}
	
	protected void putValues(Gasto gasto, ContentValues values)
	{
		values.put(KEY_DESCRICAO, gasto.getDescricao());
		values.put(KEY_DATA, gasto.getData().getTime());
		values.put(KEY_VALOR, gasto.getValor().toPlainString());
		values.put(KEY_VENDIVEL, gasto.getVendivel());
		
		Long categoriaId = null;
		Categoria categoria = gasto.getCategoria();
		if (null != categoria) {
			categoriaId = categoria.getId();
		} else {
			categoriaId = gasto.getCategoriaId();
		}
		values.put(KEY_CATEGORIA_ID, categoriaId);
		
		Long plataformaId = null;
		Plataforma plataforma = gasto.getPlataforma();
		if (null != plataforma) {
			plataformaId = plataforma.getId();
		} else {
			plataformaId = gasto.getPlataformaId();
		}
		if (null == plataformaId) {
			throw new IllegalArgumentException("Gasto não possui id da plataforma.");
		}
		values.put(KEY_PLATAFORMA_ID, plataformaId);
	}
	
	
	public Cursor createListCursor(SQLiteDatabase db, String query)
	{
		List<String> parametros = new ArrayList<>();
		
		String sql = 
				"SELECT g." + KEY_ID +  " as _id, g." + KEY_DESCRICAO + " as descricao, g." + KEY_VALOR + " as valor," +
				" p." + PlataformaDatabaseHandler.KEY_NOME + " as plataforma" +
				" FROM " + TABLE_GASTOS + " g" +
				" INNER JOIN " + PlataformaDatabaseHandler.TABLE_PLATAFORMAS + " p" +
				" ON g." + KEY_PLATAFORMA_ID + " = p." + PlataformaDatabaseHandler.KEY_ID;
		
		if (query != null && query.length() > 0) {
			query = "%" + query + "%";
			
			sql += " WHERE (g." + KEY_DESCRICAO + " LIKE ? COLLATE NOCASE";
			parametros.add(query);
			
			sql += " OR p." + PlataformaDatabaseHandler.KEY_NOME +" LIKE ? COLLATE NOCASE)";
			parametros.add(query);
		}
		
		
		sql += " ORDER BY g." + KEY_DATA + " DESC";
				
		return db.rawQuery(sql, parametros.toArray(new String[parametros.size()]));
	}
	
	public int deleteGasto(SQLiteDatabase db, long id)
	{
		return db.delete(TABLE_GASTOS, KEY_ID + " = ?", new String[] {String.valueOf(id)});
	}
	
	public Double calculaTotalGastosPlataforma(SQLiteDatabase db, long plataformaId)
	{
		Double total = null;
				
		String sql = 
				"SELECT SUM(g." + KEY_VALOR + ")" +
				" FROM " + TABLE_GASTOS + " g" +
				" WHERE g." + KEY_PLATAFORMA_ID + " = ?";
		
		Cursor cursor = db.rawQuery(sql, new String[] {String.valueOf(plataformaId)});
		if (cursor != null) {
			
			if (cursor.moveToFirst()) {
				total = cursor.getDouble(0);
			}
			
			cursor.close();
		}
		
		return total;
	}
	
	public Double calculaTotalGastosVendiveisPlataforma(SQLiteDatabase db, long plataformaId)
	{
		Double total = null;
				
		String sql = 
				"SELECT SUM(g." + KEY_VALOR + ")" +
				" FROM " + TABLE_GASTOS + " g" +
				" WHERE g." + KEY_PLATAFORMA_ID + " = ? " +
				" AND g." + KEY_VENDIVEL + " = 1";
		
		Cursor cursor = db.rawQuery(sql, new String[] {String.valueOf(plataformaId)});
		if (cursor != null) {
			
			if (cursor.moveToFirst()) {
				total = cursor.getDouble(0);
			} 
			
			cursor.close();
		}
		
		return total;
	}
	
	public Double calculaGastosUltimoMesPlataforma(SQLiteDatabase db, long plataformaId)
	{
		Double total = null;
		
		Calendar calUltimoMes = new GregorianCalendar();
		calUltimoMes.setTime(new Date());
		calUltimoMes.add(Calendar.MONTH, -1);
		Date ultimoMes = calUltimoMes.getTime();
		
		Date agora = new Date();
		
		/*cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);*/
		
		//Log.w("GG", cal.getTime() + "");
		//Log.w("GG", cal.getTimeInMillis() + "");
				
		String sql = 
				"SELECT SUM(g." + KEY_VALOR + ")" +
				" FROM " + TABLE_GASTOS + " g" +
				" WHERE g." + KEY_PLATAFORMA_ID + " = ? " +
				" AND g." + KEY_DATA + " >= ? AND g." + KEY_DATA + " <= ?";
		
		Cursor cursor = db.rawQuery(sql, new String[] {
				String.valueOf(plataformaId), 
				String.valueOf(ultimoMes.getTime()),
				String.valueOf(agora.getTime())
				});
		if (cursor != null) {
			
			if (cursor.moveToFirst()) {
				total = cursor.getDouble(0);
			} 
			
			cursor.close();
		}
		
		return total;
	}
	
	public List<Map<String, Object>> getGastosTotaisAgrupadosPorPlataforma(
			SQLiteDatabase db, 
			Date dataInicial, 
			Date dataFinal,
			Boolean apenasAtivas)
	{
		List<Map<String, Object>> gastos = new ArrayList<>();
		
		List<String> selectionArgs = new ArrayList<>();
		
		String sql = "SELECT p." + PlataformaDatabaseHandler.KEY_NOME + ", " +
				" SUM(g." + KEY_VALOR + ")" +
				" FROM " + TABLE_GASTOS + " g" +
				" INNER JOIN " +  PlataformaDatabaseHandler.TABLE_PLATAFORMAS + " p" +
				" ON g." + KEY_PLATAFORMA_ID + " = p." + PlataformaDatabaseHandler.KEY_ID +
				" WHERE 1 = 1";
		
		if (apenasAtivas) {
			sql += " AND p." + PlataformaDatabaseHandler.KEY_ATIVA + " = 1";
		}
		
		if (dataInicial != null) {
			sql += " AND g." + KEY_DATA + " >= ?";
			selectionArgs.add(String.valueOf(dataInicial.getTime()));
		}
		
		if (dataFinal != null) {
			sql += " AND g." + KEY_DATA + " <= ?";
			selectionArgs.add(String.valueOf(dataFinal.getTime()));
		}
		
		sql +=	" GROUP BY p." + PlataformaDatabaseHandler.KEY_ID + ", p." + PlataformaDatabaseHandler.KEY_NOME;
						
		Cursor cursor = db.rawQuery(sql, selectionArgs.toArray(new String[selectionArgs.size()]));
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					String nome = cursor.getString(0);
					Double gastoPlat = cursor.getDouble(1);
					
					if (nome != null && gastoPlat != null) {
						
						Map<String, Object> gasto = new HashMap<>();
													
						gasto.put("plataforma", nome);
						gasto.put("gasto", gastoPlat);
						
						gastos.add(gasto);
					}
					
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		
		return gastos;
	}
	
	public List<Map<String, Object>> getGastosTotaisAgrupadosPorCategoria(SQLiteDatabase db, Date dataInicial, Date dataFinal, Plataforma plataforma)
	{
		List<Map<String, Object>> gastos = new ArrayList<>();
		
		List<String> selectionArgs = new ArrayList<>();
		
		String sql = "SELECT c." + CategoriaDatabaseHandler.KEY_NOME + "," +
				" SUM(g." + KEY_VALOR + ")" +
				" FROM " + TABLE_GASTOS + " g" +
				" LEFT JOIN " + CategoriaDatabaseHandler.TABLE_CATEGORIAS + " c" +
				" ON g." + KEY_CATEGORIA_ID + " = c." + CategoriaDatabaseHandler.KEY_ID +
				" WHERE 1 = 1";
		
		if (dataInicial != null) {
			sql += " AND g." + KEY_DATA + " >= ?";
			selectionArgs.add(String.valueOf(dataInicial.getTime()));
		}
		
		if (dataFinal != null) {
			sql += " AND g." + KEY_DATA + " <= ?";
			selectionArgs.add(String.valueOf(dataFinal.getTime()));
		}
		
		if (plataforma != null) {
			sql += " AND g." + KEY_PLATAFORMA_ID + " = ?";
			selectionArgs.add(String.valueOf(plataforma.getId()));
		}
		
		sql += " GROUP BY c." + CategoriaDatabaseHandler.KEY_ID + ", c." + CategoriaDatabaseHandler.KEY_NOME;
				
		Cursor cursor = db.rawQuery(sql, selectionArgs.toArray(new String[selectionArgs.size()]));
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					String nome = cursor.getString(0);
					Double gastoCat = cursor.getDouble(1);
										
					if (gastoCat != null) {
						
						Map<String, Object> gasto = new HashMap<>();
													
						gasto.put("categoria", nome);
						gasto.put("gasto", gastoCat);
						
						gastos.add(gasto);
					}
					
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		
		return gastos;
	}
	
	public Map<String, Map<String, Double>> getGastosTotaisAgrupadosPorPlataformaCategoria(
			SQLiteDatabase db, 
			Date dataInicial, 
			Date dataFinal, 
			Plataforma plataforma,
			Boolean apenasVendiveis,
			Boolean apenasPlataformasAtivas
			)
	{
		Map<String, Map<String, Double>> gastosTotais = new LinkedHashMap<>();
		
		List<String> selectionArgs = new ArrayList<>();
		
		String sql = "SELECT p." + PlataformaDatabaseHandler.KEY_NOME + "," +
				" c." + CategoriaDatabaseHandler.KEY_NOME + "," +
				" SUM(g." + KEY_VALOR + ")" +
				" FROM " + TABLE_GASTOS + " g" +
				" INNER JOIN " + PlataformaDatabaseHandler.TABLE_PLATAFORMAS + " p" +
				" ON g." + KEY_PLATAFORMA_ID + " = p." + PlataformaDatabaseHandler.KEY_ID +
				" LEFT JOIN " + CategoriaDatabaseHandler.TABLE_CATEGORIAS + " c" +
				" ON g." + KEY_CATEGORIA_ID + " = c." + CategoriaDatabaseHandler.KEY_ID +
				" WHERE 1 = 1";
		
		if (dataInicial != null) {
			sql += " AND g." + KEY_DATA + " >= ?";
			selectionArgs.add(String.valueOf(dataInicial.getTime()));
		}
		
		if (dataFinal != null) {
			sql += " AND g." + KEY_DATA + " <= ?";
			selectionArgs.add(String.valueOf(dataFinal.getTime()));
		}
		
		if (plataforma != null) {
			sql += " AND g." + KEY_PLATAFORMA_ID + " = ?";
			selectionArgs.add(String.valueOf(plataforma.getId()));
		}
		
		if (apenasVendiveis) {
			sql += " AND g." + KEY_VENDIVEL + " = 1";
		}
		
		if (apenasPlataformasAtivas) {
			sql += " AND p." + PlataformaDatabaseHandler.KEY_ATIVA + " = 1";
		}
		
		sql += " GROUP BY p." + PlataformaDatabaseHandler.KEY_ID + "," +
				" p." + PlataformaDatabaseHandler.KEY_NOME + "," +
				" c." + CategoriaDatabaseHandler.KEY_ID + "," +
				" c." + CategoriaDatabaseHandler.KEY_NOME;
		
		//Log.w("GG", sql);
		
		Cursor cursor = db.rawQuery(sql, selectionArgs.toArray(new String[selectionArgs.size()]));
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					String nomePlataforma = cursor.getString(0);
					String nomeCategoria = cursor.getString(1);
					Double gastoCat = cursor.getDouble(2);
										
					if (nomePlataforma != null && gastoCat != null) {
						if (null == nomeCategoria) {
							nomeCategoria = "";
						}
						
						Map<String, Double> gastosPlataforma = gastosTotais.get(nomePlataforma);
						if (null == gastosPlataforma) {
							gastosPlataforma = new LinkedHashMap<>();
							gastosTotais.put(nomePlataforma, gastosPlataforma);
						}
						
						gastosPlataforma.put(nomeCategoria, gastoCat);
					}
					
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		
		return gastosTotais;
	}
	
	public Cursor createCursorRelatorio(
			SQLiteDatabase db, 
			Date dataInicial, 
			Date dataFinal, 
			Plataforma plataforma, 
			Boolean apenasVendiveis,
			Boolean apenasPlataformasAtivas)
	{
		List<String> parametros = new ArrayList<>();
		
		String sql = 
				"SELECT g." + KEY_DESCRICAO + " as descricao, g." + KEY_VALOR + " as valor," + 
				" g." + KEY_DATA + " as data, g." + KEY_VENDIVEL + " as vendivel," +
				" p." + PlataformaDatabaseHandler.KEY_NOME + " as plataforma," +
                " c." + CategoriaDatabaseHandler.KEY_NOME + " as categoria" +   						
				" FROM " + TABLE_GASTOS + " g" +
				" INNER JOIN " + PlataformaDatabaseHandler.TABLE_PLATAFORMAS + " p" +
				" ON g." + KEY_PLATAFORMA_ID + " = p." + PlataformaDatabaseHandler.KEY_ID +
				" LEFT JOIN " + CategoriaDatabaseHandler.TABLE_CATEGORIAS + " c" +
				" ON g." + KEY_CATEGORIA_ID + " = c." + CategoriaDatabaseHandler.KEY_ID +
				" WHERE 1 = 1";
		
		if (dataInicial != null) {
			sql += " AND g." + KEY_DATA + " >= ?";
			parametros.add(String.valueOf(dataInicial.getTime()));
		}
		
		if (dataFinal != null) {
			sql += " AND g." + KEY_DATA + " <= ?";
			parametros.add(String.valueOf(dataFinal.getTime()));
		}
		
		if (plataforma != null) {
			sql += " AND g." + KEY_PLATAFORMA_ID + " = ?";
			parametros.add(String.valueOf(plataforma.getId()));
		}
		
		if (apenasVendiveis) {
			sql += " AND g." + KEY_VENDIVEL + " = 1";
		}
		
		if (apenasPlataformasAtivas) {
			sql += " AND p." + PlataformaDatabaseHandler.KEY_ATIVA + " = 1";
		}
		
		sql += " ORDER BY g." + KEY_DATA + " ASC";
		
		//Log.w("GG", sql);
				
		return db.rawQuery(sql, parametros.toArray(new String[parametros.size()]));
	}
	
	public Map<String, Double> getGastosTotaisAgrupadosPorMes(
			SQLiteDatabase db, 
			Date dataInicial, 
			Date dataFinal, 
			Plataforma plataforma,
			Boolean apenasVendiveis,
			Boolean apenasPlataformasAtivas
			)
	{
		Map<String, Double> gastos = new LinkedHashMap<>();
		
		List<String> selectionArgs = new ArrayList<>();
		
		//String sql = "SELECT strftime('%Y-%m', datetime(g.data / 1000, 'unixepoch')) as mes," +
		String sql = "SELECT strftime('%Y-%m', (g." + KEY_DATA + " / 1000), 'unixepoch') as mes," +
				" SUM(g." + KEY_VALOR + ")" +
				" FROM " + TABLE_GASTOS + " g" +
				" INNER JOIN " + PlataformaDatabaseHandler.TABLE_PLATAFORMAS + " p" +
				" ON g." + KEY_PLATAFORMA_ID + " = p." + PlataformaDatabaseHandler.KEY_ID +
				" WHERE 1 = 1";
		
		if (dataInicial != null) {
			sql += " AND g." + KEY_DATA + " >= ?";
			selectionArgs.add(String.valueOf(dataInicial.getTime()));
		}
		
		if (dataFinal != null) {
			sql += " AND g." + KEY_DATA + " <= ?";
			selectionArgs.add(String.valueOf(dataFinal.getTime()));
		}
		
		if (plataforma != null) {
			sql += " AND g." + KEY_PLATAFORMA_ID + " = ?";
			selectionArgs.add(String.valueOf(plataforma.getId()));
		}
		
		if (apenasVendiveis) {
			sql += " AND g." + KEY_VENDIVEL + " = 1";
		}
		
		if (apenasPlataformasAtivas) {
			sql += " AND p." + PlataformaDatabaseHandler.KEY_ATIVA + " = 1";
		}
		
		sql += " GROUP BY mes";
		
		sql += " ORDER BY mes ASC";
		
		//Log.w("GG", sql);
		
		Cursor cursor = db.rawQuery(sql, selectionArgs.toArray(new String[selectionArgs.size()]));
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					String mes = cursor.getString(0);
					Double gastosMes = cursor.getDouble(1);
					
					if (mes != null && gastosMes != null) {
						gastos.put(mes, gastosMes);
					}
					
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		
		return gastos;
	}
	
}
