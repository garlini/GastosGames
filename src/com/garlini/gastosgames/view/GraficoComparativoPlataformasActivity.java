package com.garlini.gastosgames.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.androidplot.pie.PieRenderer.DonutMode;
import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.database.GastoDatabaseHandler;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class GraficoComparativoPlataformasActivity extends ActionBarActivity {

	private static final int REQUEST_FILTRO = 100;
	
	private DatabaseHandler dbHandler;
	
	private Date dataInicial;
	private Date dataFinal;
	
	private Boolean apenasPlataformasAtivas = false;
	
	private PieChart pie;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grafico_comparativo_plataformas);
		
		pie = (PieChart) findViewById(R.id.pieChart);
		
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
		
		
		pie.getBackgroundPaint().setColor(Color.WHITE);
		
		loadGrafico(false);
		
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
	
	private void loadGrafico(boolean isReload)
	{
		if (isReload) {
			pie.clear();
		}
		
		List<Map<String, Object>>  gastos = new GastoDatabaseHandler().getGastosTotaisAgrupadosPorPlataforma(
				dbHandler.getReadableDatabase(),
				dataInicial,
				dataFinal,
				apenasPlataformasAtivas
		);

		if (gastos.size() > 0) {
		
			List<Segment> segments = new ArrayList<>();
			
			for (Map<String, Object> gastoPlat : gastos) {
				String plataforma = (String) gastoPlat.get("plataforma");
				Double gasto = (Double) gastoPlat.get("gasto");
				
				segments.add(new Segment(plataforma, gasto));
				
			}
					
			for (int i = 0; i < segments.size(); i++) {
				
				SegmentFormatter sf = new SegmentFormatter();
				
				int mod = (i % 5);
				
				switch (mod) {
				case 0:
					sf.configure(getApplicationContext(), R.xml.pie_segment_formatter1);
					break;
				case 1:
					sf.configure(getApplicationContext(), R.xml.pie_segment_formatter2);
					break;
				case 2:
					sf.configure(getApplicationContext(), R.xml.pie_segment_formatter3);
					break;
				case 3:
					sf.configure(getApplicationContext(), R.xml.pie_segment_formatter4);
					break;
				default:
					sf.configure(getApplicationContext(), R.xml.pie_segment_formatter5);
					break;	
				}
				
				pie.addSeries(segments.get(i), sf);
			}
		
			pie.getRenderer(PieRenderer.class).setDonutSize(0, DonutMode.PERCENT);
		
		} else {
			ViewUtil.showInfo(this, getString(R.string.info_nenhum_gasto_encontrado));
		}
		
		pie.redraw();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.grafico_comparativo_plataformas, menu);
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
		
		loadGrafico(true);
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
			
			loadGrafico(true);
			
		}
		
		super.onActivityResult(requestCode, requestCode, data);
	}
}
