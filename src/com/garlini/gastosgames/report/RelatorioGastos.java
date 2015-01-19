package com.garlini.gastosgames.report;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.GastoDatabaseHandler;
import com.garlini.gastosgames.model.Plataforma;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;

public class RelatorioGastos extends RelatorioBase {

	protected Date dataInicial;
	protected Date dataFinal;
	
	protected Plataforma plataforma;
	
	protected Boolean apenasVendiveis = false;
	
	private Boolean apenasPlataformasAtivas = false;
	
	protected BigDecimal totalGeral;
	protected BigDecimal totalVendivel;
	protected Integer gastosCount;
	
	public RelatorioGastos(Context context, SQLiteDatabase db) {
		super(context, db);
	}
	
	
	
	public Date getDataInicial() {
		return dataInicial;
	}



	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}



	public Date getDataFinal() {
		return dataFinal;
	}



	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}



	public Plataforma getPlataforma() {
		return plataforma;
	}



	public void setPlataforma(Plataforma plataforma) {
		this.plataforma = plataforma;
	}


	public Boolean getApenasVendiveis() {
		return apenasVendiveis;
	}

	public void setApenasVendiveis(Boolean apenasVendiveis) {
		this.apenasVendiveis = apenasVendiveis;
	}
	
	

	public Boolean getApenasPlataformasAtivas() {
		return apenasPlataformasAtivas;
	}



	public void setApenasPlataformasAtivas(Boolean apenasPlataformasAtivas) {
		this.apenasPlataformasAtivas = apenasPlataformasAtivas;
	}



	@Override
	protected void body() {
		
		//Log.w("GG", "Plataforma: " + (plataforma != null ? plataforma.getNome() : "null"));
		//Log.w("GG", "Data inicial: " + dataInicial);
		//Log.w("GG", "Data final: " + dataFinal);
		
		totalGeral = new BigDecimal(0.00);
		totalVendivel = new BigDecimal(0.00);
		gastosCount = 0;
		
		openEl("table", null, true);
		
		addHeader();
		
		openEl("tbody", null, true);
		
		Cursor cursor = new GastoDatabaseHandler().
				createCursorRelatorio(db, dataInicial, dataFinal, plataforma, apenasVendiveis, apenasPlataformasAtivas);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					
					addGastos(cursor);
					
				} else {
					openEl("tr");
					
					Map<String, String> attrs = new HashMap<>();
					attrs.put("class", "center");
					attrs.put("colspan", "6");
										
					appendEl("td", getString(R.string.info_nenhum_gasto_encontrado), attrs);
					
					closeEl("tr", true);
				}
			} finally {
				cursor.close();
			}
		}
		
		if (gastosCount > 0) {
			addTotais();
		}
		
		closeEl("tbody", true);
				
		closeEl("table", true);
	}
	
	protected void addHeader()
	{
		openEl("thead", null, true);
		
		openEl("tr");
		
		Map<String, String> attrs = new HashMap<>();
		attrs.put("colspan", "6");
		
		StringBuilder header = new StringBuilder();
		header.append(getString(apenasVendiveis ? 
				R.string.title_activity_relatorio_gastos_vendiveis :
				R.string.title_activity_relatorio_gastos));
		if (plataforma != null) {
			header.append(" ");
			header.append(plataforma.getNome());
		}
		
		appendPeriodoHeader(header, dataInicial, dataFinal);
				
		appendEl("th", header.toString(), attrs, true);
		
		closeEl("tr", true);
		
		openEl("tr");
		
		appendEl("th", getString(R.string.label_data), null, true);
		
		appendEl("th", getString(R.string.descricao_gasto), null, true);
		
		appendEl("th", getString(R.string.label_plataforma), null, true);
		
		appendEl("th", getString(R.string.label_categoria), null, true);
		
		appendEl("th", getString(R.string.label_vendivel), null, true);
		
		appendEl("th", getString(R.string.label_valor), null, true);
		
		closeEl("tr", true);
		
		closeEl("thead", true);
	}
	
	protected void addGastos(Cursor cursor)
	{
		NumberFormat format = NumberFormat.getCurrencyInstance();
		java.text.DateFormat dtFormat = DateFormat.getDateFormat(context);
		String sim = getString(R.string.option_sim);
		String nao = getString(R.string.option_nao);
		
		Map<String, String> attrsValor = new HashMap<>();
		attrsValor.put("class", "right");
				
		do {
			int col = 0;
			String descricao = cursor.getString(col++);
			Double valor = cursor.getDouble(col++);
			Date data =  new Date(cursor.getLong(col++));
			Boolean vendivel = cursor.getInt(col++) != 0;
			String plataforma = cursor.getString(col++);
			String categoria = cursor.getString(col++);
			
			openEl("tr", null, true);
						
			appendEl("td", dtFormat.format(data), null, true);
			
			appendEl("td", descricao, null, true);
			
			appendEl("td", plataforma, null, true);
			
			appendEl("td", categoria != null ? categoria : "", null, true);
			
			appendEl("td", vendivel ? sim : nao, null, true);
			
			appendEl("td", format.format(valor), attrsValor, true);
						
			closeEl("tr", true);
			
			BigDecimal bdValor = new BigDecimal(valor);
			
			totalGeral = totalGeral.add(bdValor);
			
			if (vendivel) {
				totalVendivel = totalVendivel.add(bdValor);
			}
			
			gastosCount += 1;
			
		} while (cursor.moveToNext());
	} 
	
	private void addTotais() 
	{
		NumberFormat format = NumberFormat.getCurrencyInstance();
		
		Map<String, String> attrsLabel = new HashMap<>();
		attrsLabel.put("colspan", "5");
		attrsLabel.put("class", "right");
		
		Map<String, String> attrsValue = new HashMap<>();
		attrsValue.put("class", "right");
		
		openEl("tr");
		appendEl("th", getString(R.string.label_vendivel_total), attrsLabel);
		appendEl("th", format.format(totalVendivel.doubleValue()), attrsValue);
		closeEl("tr", true);
		
		openEl("tr");
		appendEl("th", getString(R.string.label_gasto_total), attrsLabel);
		appendEl("th", format.format(totalGeral.doubleValue()), attrsValue);
		closeEl("tr", true);
	}
}
