package com.garlini.gastosgames.report;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.GastoDatabaseHandler;
import com.garlini.gastosgames.model.Plataforma;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class RelatorioGastosSinteticoMensal extends RelatorioBase {

	protected Date dataInicial;
	protected Date dataFinal;
	
	protected Plataforma plataforma;
	
	protected Boolean apenasVendiveis = false;
	
	private Boolean apenasPlataformasAtivas = false;
	
	Map<String, Double> gastos;
	
	protected BigDecimal totalGeral;
	
	public RelatorioGastosSinteticoMensal(Context context, SQLiteDatabase db) {
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
	protected void body() 
	{
		totalGeral = new BigDecimal(0.0);
		
		openEl("table", null, true);
		
		addHeader();
		
		openEl("tbody", null, true);
		
		gastos = new GastoDatabaseHandler().getGastosTotaisAgrupadosPorMes(
				db, dataInicial, dataFinal, plataforma, apenasVendiveis, apenasPlataformasAtivas);
		
		if (gastos.size() > 0) {
			
			addGastos();
			
			addTotalGeral();
			
		} else {
			openEl("tr");
			
			Map<String, String> attrs = new HashMap<>();
			attrs.put("class", "center");
			attrs.put("colspan", "3");
								
			appendEl("td", getString(R.string.info_nenhum_gasto_encontrado), attrs);
			
			closeEl("tr", true);
		}
		
		closeEl("tbody", true);
		
		closeEl("table", true);
		
	}
	
	protected void addHeader()
	{
		openEl("thead", null, true);
		
		Map<String, String> attrs = new HashMap<>();
		attrs.put("colspan", "3");
		
		openEl("tr");
		
		StringBuilder header = new StringBuilder();
		header.append(getString(apenasVendiveis ? 
				R.string.title_activity_relatorio_gastos_sintetico_mensal_vendiveis : 
				R.string.title_activity_relatorio_gastos_sintetico_mensal));
		if (plataforma != null) {
			header.append(" ");
			header.append(plataforma.getNome());
		}
		
		appendPeriodoHeader(header, dataInicial, dataFinal);
				
		appendEl("th", header.toString(), attrs, true);
		
		closeEl("tr", true);
		
		openEl("tr", null, true);
		
		appendEl("th", getString(R.string.label_mes), null, true);
		appendEl("th", getString(R.string.label_ano), null, true);
		appendEl("th", getString(R.string.label_total), null, true);
		
		closeEl("tr", true);		
		
		closeEl("thead", true);
	}
	
	protected void addGastos()
	{
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		SimpleDateFormat dtFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
		
		Map<String, String> attrsValor = new HashMap<>();
		attrsValor.put("class", "right");
		
		Set<String> meses = gastos.keySet();
		for (String mes : meses) {
			Double gastoMes = gastos.get(mes);
			
			// partes[0] = ano, partes[1] = mes
			String[] partes = mes.split("-");
						
			GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(partes[0]), (Integer.parseInt(partes[1]) - 1) , 1);
			
			openEl("tr", null, true);
							
			appendEl("td", capitalize(dtFormat.format(cal.getTime())), null, true);
			appendEl("td", partes[0], null, true);
			appendEl("td", nf.format(gastoMes), attrsValor, true);
			
			closeEl("tr", true);
			
			BigDecimal bdGasto = new BigDecimal(gastoMes);
			
			totalGeral = totalGeral.add(bdGasto);
		}
	}
	
	void addTotalGeral()
	{
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		
		Map<String, String> attrsLabelTotal = new HashMap<>();
		attrsLabelTotal.put("class", "right");
		attrsLabelTotal.put("colspan", "2");
		
		Map<String, String> attrsTotal = new HashMap<>();
		attrsTotal.put("class", "right");
		
		openEl("tr", null, true);
		
		appendEl("th", getString(R.string.label_total_geral), attrsLabelTotal, true);
		
		appendEl("th", nf.format(totalGeral.doubleValue()), attrsTotal, true);
		
		closeEl("tr", true);
	}

}
