package com.garlini.gastosgames.view;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BarRenderer.BarRenderStyle;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYLegendWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.database.GastoDatabaseHandler;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

public class GraficoComparativoPlataformasCategoriasActivity extends ActionBarActivity {

	private static final int REQUEST_FILTRO = 100;
	
	private XYPlot plot;
	
	DatabaseHandler dbHandler;
	
	private Date dataInicial;
	private Date dataFinal;
	
	private Boolean apenasPlataformasAtivas = false;
	
	List<BarFormatter> barFormatters;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grafico_comparativo_plataformas_categorias);
		
		plot = (XYPlot) findViewById(R.id.XYPlot);
		
		dbHandler = DatabaseHandler.getInstance(getApplicationContext());
		
		if (savedInstanceState != null) {
			
			Long time = savedInstanceState.getLong("dataInicial", Long.MIN_VALUE);
			if (time != Long.MIN_VALUE) {
				dataInicial = new Date(time);
			}
			
			time = savedInstanceState.getLong("dataFinal", Long.MIN_VALUE);
			if (time != Long.MIN_VALUE) {
				dataFinal = new Date(time);
			}
			
			apenasPlataformasAtivas = savedInstanceState.getBoolean("apenasPlataformasAtivas", false);
			
		}
		
		createBarFormatters();
		
		loadGrafico();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		if (dataInicial != null) {
			outState.putLong("dataInicial", dataInicial.getTime());
		}
		
		if (dataFinal != null) {
			outState.putLong("dataFinal", dataFinal.getTime());
		}
		
		if (apenasPlataformasAtivas != null) {
			outState.putBoolean("apenasPlataformasAtivas", apenasPlataformasAtivas);
		}
				
		super.onSaveInstanceState(outState);
	}
	
	private void loadGrafico()
	{
		Iterator<XYSeries> iterator1 = plot.getSeriesSet().iterator();
        while(iterator1.hasNext()) { 
        	XYSeries setElement = iterator1.next();
        	plot.removeSeries(setElement);
        }
        
        Map<String, Map<String, Double>>  gastosTotais = new GastoDatabaseHandler()
        		.getGastosTotaisAgrupadosPorPlataformaCategoria(
        				dbHandler.getReadableDatabase(), 
        				dataInicial, 
        				dataFinal, 
        				null, 
        				false,
        				apenasPlataformasAtivas);
        
        if (gastosTotais.size() == 0) {
        	ViewUtil.showInfo(this, getString(R.string.info_nenhum_gasto_encontrado));
        }
        
        //Vamos definir quais plataformas e categorias aparecerão no grafico
        Set<String> setPlataformas = new LinkedHashSet<>();
        Set<String> setCategorias = new LinkedHashSet<>();
        for (String nomePlaforma : gastosTotais.keySet()) {
        	setPlataformas.add(nomePlaforma);
        	
        	Map<String, Double> gastosPlataforma = gastosTotais.get(nomePlaforma);
        	
        	for (String nomeCategoria : gastosPlataforma.keySet()) {
        		
        		setCategorias.add(nomeCategoria);
        	}
        }
                
        //O eixo X será o mesmo em todas as séries: o indice de cada plataforma
        List<Integer> xVals = new ArrayList<>();
        final String nomesPlataformas[] = new String[setPlataformas.size()];
        int i = 0;
        for (String nomePlataforma: setPlataformas) {
        	nomesPlataformas[i] = nomePlataforma;
        	xVals.add(i);
        	i += 1;
        }
                
        //Vamos criar uma serie para cada categoria encontrada
        int countSeries = 0;
        for (String nomeCategoria: setCategorias) {
        	
        	List<Double> yVals = new ArrayList<>();
        	        	
        	//Adiciona o gasto que cada plataforma teve nesta categoria
        	for (String nomePlataforma : setPlataformas) {
        		Map<String, Double> gastosPlat = gastosTotais.get(nomePlataforma);
        		Double dGasto = gastosPlat.get(nomeCategoria);
        		if (null == dGasto) {
        			dGasto = 0.0;
        		}
        		yVals.add(dGasto);
        	}
        	
        	if (nomeCategoria.equals("")){
        		nomeCategoria = getString(R.string.option_sem_categoria);
        	}
        	
        	XYSeries series = new SimpleXYSeries(xVals, yVals, nomeCategoria);
        	
        	plot.addSeries(series, barFormatters.get(countSeries % barFormatters.size()));
        	
        	countSeries += 1;
        }
                
        //Gambis para sa barras não ficarem grudadas nas laterais ou embaixo
		plot.setDomainBoundaries(-1, nomesPlataformas.length, BoundaryMode.FIXED);
		plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
		
		plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
		
		BarRenderer<?> barRenderer = (BarRenderer<?>) plot.getRenderer(BarRenderer.class);
		if (barRenderer != null) {
			barRenderer.setBarRenderStyle(BarRenderStyle.SIDE_BY_SIDE);
	 	    barRenderer.setBarWidth(ViewUtil.convertDpToPixel(100, getApplicationContext()));
			//barRenderer.setBarWidthStyle(BarWidthStyle.VARIABLE_WIDTH);
	 	    //barRenderer.setBarGap(20);
		}
		
		//Mostrar o nome da plataforma em vez de um número no eixo X
 	    plot.setDomainValueFormat(new NumberFormat() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public Number parse(String string, ParsePosition position) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public StringBuffer format(long value, StringBuffer buffer,
					FieldPosition field) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public StringBuffer format(double value, StringBuffer buffer,
					FieldPosition field) {
				
				int indexPlataforma = (int) Math.floor(value);
								
				if (indexPlataforma >= 0 && indexPlataforma < nomesPlataformas.length) {
					return new StringBuffer(nomesPlataformas[indexPlataforma]);
				}
				
				return new StringBuffer("");
			}
		});
 	    
 	    //No eixo Y formata os gastos como moeda
 	    final NumberFormat nf = NumberFormat.getCurrencyInstance();
		DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) nf).getDecimalFormatSymbols();
		decimalFormatSymbols.setCurrencySymbol("");
		((DecimalFormat) nf).setDecimalFormatSymbols(decimalFormatSymbols);
 	    plot.setRangeValueFormat(new NumberFormat() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public Number parse(String string, ParsePosition position) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public StringBuffer format(long value, StringBuffer buffer,
					FieldPosition field) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public StringBuffer format(double value, StringBuffer buffer,
					FieldPosition field) {
								
				return new StringBuffer(nf.format(value));
			}
		});
		
 	    plot.getBackgroundPaint().setColor(Color.WHITE);
 	   
 	    XYGraphWidget graphWidget = plot.getGraphWidget(); 
 			   
 	    graphWidget.getBackgroundPaint().setColor(Color.WHITE);
 	     	    
 	    int color = Color.parseColor("#167AC6");
 	    graphWidget.getDomainLabelPaint().setColor(color);
 	    graphWidget.getRangeLabelPaint().setColor(color);
 	    graphWidget.getRangeLabelPaint().setTextSize(ViewUtil.convertDpToPixel(10, getApplicationContext()));
 	    
 	    XYLegendWidget legendWidget = plot.getLegendWidget(); 
 	    legendWidget.getTextPaint().setColor(color);
 	    legendWidget.getTextPaint().setTextSize(ViewUtil.convertDpToPixel(10, getApplicationContext()));
 	    
 	   Display display = getWindowManager().getDefaultDisplay();
 	   Point size = new Point();
 	   display.getSize(size);
 	    
 	    legendWidget.setSize(new SizeMetrics(
 	    		ViewUtil.convertDpToPixel(20, getApplicationContext()), SizeLayoutType.ABSOLUTE, 
 	    		size.x - ViewUtil.convertDpToPixel(75, getApplicationContext()), SizeLayoutType.ABSOLUTE));
 	    
 	    legendWidget.position(ViewUtil.convertDpToPixel(54, getApplicationContext()), XLayoutStyle.ABSOLUTE_FROM_LEFT, 
 	    		ViewUtil.convertDpToPixel(18, getApplicationContext()), YLayoutStyle.ABSOLUTE_FROM_BOTTOM);
 	     	    
		plot.redraw();
	}
	
	void createBarFormatters()
	{
		barFormatters = new ArrayList<>();
		
		BarFormatter formatter = new BarFormatter(Color.argb(200, 100, 150, 100), Color.LTGRAY);
		barFormatters.add(formatter);
		
		BarFormatter formatter2 = new BarFormatter(Color.argb(200, 100, 100, 150), Color.LTGRAY);
		barFormatters.add(formatter2);
		
		BarFormatter formatter3 = new BarFormatter(Color.rgb(255, 0, 0), Color.LTGRAY);
		barFormatters.add(formatter3);
		
		BarFormatter formatter4 = new BarFormatter(Color.rgb(0, 168, 89), Color.LTGRAY);
		barFormatters.add(formatter4);
		
		BarFormatter formatter5 = new BarFormatter(Color.rgb(0, 0, 255), Color.LTGRAY);
		barFormatters.add(formatter5);
		
		BarFormatter formatter6 = new BarFormatter(Color.rgb(255, 114, 18), Color.LTGRAY);
		barFormatters.add(formatter6);
		
		BarFormatter formatter7 = new BarFormatter(Color.rgb(31, 26, 23), Color.LTGRAY);
		barFormatters.add(formatter7);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(
				R.menu.grafico_comparativo_plataformas_categorias, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.action_filtro) {
			startFiltro();
			return true;
		} else if (id == R.id.action_limpar_filtros) {
			limparFiltros();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void limparFiltros()
	{
		dataInicial = null;
		dataFinal = null;
		
		apenasPlataformasAtivas = false;
		
		loadGrafico();
	}
	
	private void startFiltro()
	{
		Intent it = new Intent(this, FiltroActivity.class);
		
		if (dataInicial != null) {
			it.putExtra(FiltroActivity.EXTRA_DATA_INICIAL, dataInicial.getTime());
		}
		
		if (dataFinal != null) {
			it.putExtra(FiltroActivity.EXTRA_DATA_FINAL, dataFinal.getTime());
		}
		
		it.putExtra(FiltroActivity.EXTRA_FILTRAR_APENAS_PLATAFORMAS_ATIVAS, true);
		if (apenasPlataformasAtivas != null) {
			it.putExtra(FiltroActivity.EXTRA_APENAS_PLATAFORMAS_ATIVAS, apenasPlataformasAtivas);
		}
		
		startActivityForResult(it, REQUEST_FILTRO);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		
		if (requestCode == REQUEST_FILTRO) {
			
			if (data.hasExtra(FiltroActivity.EXTRA_DATA_INICIAL)) {
				dataInicial = new Date(data.getLongExtra(FiltroActivity.EXTRA_DATA_INICIAL, 0));
			} else {
				dataInicial = null;
			}
			
			if (data.hasExtra(FiltroActivity.EXTRA_DATA_FINAL)) {
				dataFinal = new Date(data.getLongExtra(FiltroActivity.EXTRA_DATA_FINAL, 0));
			} else {
				dataFinal = null;
			}
			
			if (data.hasExtra(FiltroActivity.EXTRA_APENAS_PLATAFORMAS_ATIVAS)) {
				apenasPlataformasAtivas = data.getBooleanExtra(FiltroActivity.EXTRA_APENAS_PLATAFORMAS_ATIVAS, false);
			} else {
				apenasPlataformasAtivas = null;
			}
			
			loadGrafico();
			
		}
		
		super.onActivityResult(requestCode, requestCode, data);
	}
}
