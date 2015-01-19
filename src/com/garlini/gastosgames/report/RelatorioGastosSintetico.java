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
import android.database.sqlite.SQLiteDatabase;

public class RelatorioGastosSintetico extends RelatorioBase {

	protected Date dataInicial;
	protected Date dataFinal;
	
	protected Plataforma plataforma;
	
	protected Boolean apenasVendiveis = false;
	
	private Boolean apenasPlataformasAtivas = false;
	
	Map<String, Map<String, Double>> gastos;
	
	protected BigDecimal totalGeral;
	
	public RelatorioGastosSintetico(Context context, SQLiteDatabase db) {
		super(context, db);
		// TODO Auto-generated constructor stub
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
		
		totalGeral = new BigDecimal(0.0);
		
		openEl("table", null, true);
		
		addHeader();
		
		openEl("tbody", null, true);
				
		gastos = new GastoDatabaseHandler().
				getGastosTotaisAgrupadosPorPlataformaCategoria(
						db, dataInicial, dataFinal, plataforma, apenasVendiveis, apenasPlataformasAtivas);
		
		if (gastos.size() > 0) {
			boolean primeiraPlat = true;
			for (String nomePlaforma : gastos.keySet()) {
				addGastosPlataforma(nomePlaforma, primeiraPlat);
				primeiraPlat = false;
			}
			
			//se for um relatorio de uma só plataforma nao exibe o total geral
			if (null == plataforma) {
				addTotalGeral();
			}
			
		} else {
			openEl("tr");
			
			Map<String, String> attrs = new HashMap<>();
			attrs.put("class", "center");
			attrs.put("colspan", "2");
								
			appendEl("td", getString(R.string.info_nenhum_gasto_encontrado), attrs);
			
			closeEl("tr", true);
		}
		
		
		closeEl("tbody", true);
		
		closeEl("table", true);
		
	}

	private void addHeader() 
	{
		openEl("thead", null, true);
		
		Map<String, String> attrs = new HashMap<>();
		attrs.put("colspan", "2");
		
		openEl("tr");
		
		StringBuilder header = new StringBuilder();
		header.append(getString(apenasVendiveis ? 
				R.string.title_activity_relatorio_gastos_sintetico_vendiveis : 
				R.string.title_activity_relatorio_gastos_sintetico));
		if (plataforma != null) {
			header.append(" ");
			header.append(plataforma.getNome());
		}
		
		appendPeriodoHeader(header, dataInicial, dataFinal);
				
		appendEl("th", header.toString(), attrs, true);
		
		closeEl("tr", true);
		
		closeEl("thead", true);
		
	}
	
	private void addLinhaSeparadora()
	{
		openEl("tr", null, true);
		
		Map<String, String> attrs = new HashMap<>();
		attrs.put("colspan", "2");
		
		appendEl("td", "&nbsp;", attrs, true, false);
		
		closeEl("tr", true);
	}
	
	private void addGastosPlataforma(String plataforma, boolean primeira)
	{
		if (!primeira) {
			//adiciona uma linha separadora
			addLinhaSeparadora();
		}
		
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		
		BigDecimal totalPlataforma = new BigDecimal(0.0);
		
		//Nao insere o header com o nome da plataforma se for um relatorio só de uma plataforma
		if (this.plataforma == null) { 
			
			Map<String, String> attrsHeaderPlat = new HashMap<>();
			attrsHeaderPlat.put("colspan", "2");
			
			openEl("tr", null, true);
			
			appendEl("th", plataforma, attrsHeaderPlat, true);
			
			closeEl("tr", true);
		}
		
		Map<String, String> attrsValor = new HashMap<>();
		attrsValor.put("class", "right");
		
		openEl("tr", null, true);
		
		appendEl("th", getString(R.string.label_categoria), null, true);
		appendEl("th", getString(R.string.label_total), null, true);
		
		closeEl("tr", true);
		
		Map<String, Double> gastosPlat = gastos.get(plataforma);
		
		for (String nomeCategoria : gastosPlat.keySet()) {
			Double gastoCat = gastosPlat.get(nomeCategoria);
			
			openEl("tr", null, true);
			
			appendEl("td", nomeCategoria.equals("") ? getString(R.string.option_sem_categoria) : nomeCategoria);
			appendEl("td", nf.format(gastoCat), attrsValor);
			
			closeEl("tr", true);
			
			BigDecimal bdValor = new BigDecimal(gastoCat);
			
			totalPlataforma = totalPlataforma.add(bdValor);
			
			totalGeral = totalGeral.add(bdValor);
		}
		
		//total plataforma
		Map<String, String> attrsTotal = new HashMap<>();
		attrsTotal.put("class", "right");
		
		openEl("tr", null, true);
		
		appendEl("th", getString(R.string.label_total), attrsTotal, true);
		
		appendEl("th", nf.format(totalPlataforma.doubleValue()), attrsTotal, true);
		
		closeEl("tr", true);
	}
	
	void addTotalGeral()
	{
		addLinhaSeparadora();
		
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		
		Map<String, String> attrsTotal = new HashMap<>();
		attrsTotal.put("class", "right");
		
		openEl("tr", null, true);
		
		appendEl("th", getString(R.string.label_total_geral), attrsTotal, true);
		
		appendEl("th", nf.format(totalGeral.doubleValue()), attrsTotal, true);
		
		closeEl("tr", true);
	}

}
