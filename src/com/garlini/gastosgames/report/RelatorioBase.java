package com.garlini.gastosgames.report;

import java.util.Date;
import java.util.Map;

import com.garlini.gastosgames.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;

public abstract class RelatorioBase {
	
	Context context;
	SQLiteDatabase db;
	
	StringBuilder relatorio;
	
	public RelatorioBase(Context context, SQLiteDatabase db) 
	{
		this.context = context;
		this.db = db;
	}
	
	protected String getString(int id)
	{
		return context.getResources().getString(id);
	}
	
		
	public String geraRelatorio()
	{
		relatorio = new StringBuilder();
		
		relatorio.append("<!DOCTYPE html>\n");
		openEl("html", null, true);
		openEl("head", null, true);
		
		head();
		
		closeEl("head", true);
		
		openEl("body", null, true);
		
		body();
		
		closeEl("body", true);
		closeEl("html");
		
		return relatorio.toString();
	}
	
	protected void head()
	{
		styles();
	}
	
	protected void body()
	{
		
	}
	
	protected void styles()
	{
		openEl("style", null, true);
		
		relatorio.append(".center { text-align: center; }\n");
		relatorio.append("table { color: #404040; font-family: verdana, helvetica, arial; }\n");
		relatorio.append("table { width: 100%; border: 2px solid #ededed; border-collapse: collapse; }\n");
		relatorio.append("td, th {border: 1px solid #D2D2D2; padding: 7px; }\n");
		relatorio.append("th {background-color: #F1F1F1;}");
		relatorio.append("th { text-align: center; }\n");
		relatorio.append("td.right, th.right { text-align: right; }\n");
		
		closeEl("style", true);
	}
	
	protected void appendEl(String el, String value)
	{
		appendEl(el, value, null, false, true);
	}
	
	
	protected void appendEl(String el, String value, Map<String, String> attrs)
	{
		appendEl(el, value, attrs, false, true);
	}
	
	protected void appendEl(String el, String value, Map<String, String> attrs, boolean nl)
	{
		appendEl(el, value, attrs, nl, true);
	}
	
	protected void appendEl(String el, String value, Map<String, String> attrs, boolean nl, boolean escape)
	{
		openEl(el, attrs);
		
		if (escape) {
			value = value.replace("&", "&amp;");
			value = value.replace("\"", "&quot;");
			value = value.replace("'", "&apos;");
			value = value.replace("<", "&lt;");
			value = value.replace(">", "&gt;");
		}
		
		relatorio.append(value);
		
		closeEl(el, nl);
	}
	
	protected void openEl(String el)
	{
		openEl(el, null, false);
	}
	
	protected void openEl(String el, Map<String, String> attrs)
	{
		openEl(el, attrs, false);
	}
	
	protected void openEl(String el, Map<String, String> attrs, boolean nl)
	{
		relatorio.append("<");
		relatorio.append(el);
		
		if (attrs != null) {
			for (String key : attrs.keySet()) {
				relatorio.append(" ");
				relatorio.append(key);
				relatorio.append("=\"");
				relatorio.append(attrs.get(key));
				relatorio.append("\"");
			}
		}
		
		relatorio.append(">");
		
		if (nl) {
			relatorio.append("\n");
		}
	}
	
	protected void closeEl(String el)
	{
		closeEl(el, false);
	}
	
	protected void closeEl(String el, boolean nl)
	{
		relatorio.append("</");
		relatorio.append(el);
		relatorio.append(">");
		if (nl) {
			relatorio.append("\n");
		}
	}
	
	protected void appendPeriodoHeader(StringBuilder header, Date dataInicial, Date dataFinal)
	{
		java.text.DateFormat dtFormat = DateFormat.getDateFormat(context);
		
		if (dataInicial != null && dataFinal != null) {
			String periodo = getString(R.string.label_periodo);
			periodo = periodo.replace("__inicio__", dtFormat.format(dataInicial));
			periodo = periodo.replace("__fim__", dtFormat.format(dataFinal));
			header.append(" - ");
			header.append(periodo);
		} else if (dataInicial != null) {
			String periodo = getString(R.string.label_periodo_inicio);
			periodo = periodo.replace("__inicio__", dtFormat.format(dataInicial));
			header.append(" - ");
			header.append(periodo);
		} else if (dataFinal != null) {
			String periodo = getString(R.string.label_periodo_fim);
			periodo = periodo.replace("__fim__", dtFormat.format(dataFinal));
			header.append(" - ");
			header.append(periodo);
		}
	}
	
	protected String capitalize(String line)
	{
	  return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}
	
}
