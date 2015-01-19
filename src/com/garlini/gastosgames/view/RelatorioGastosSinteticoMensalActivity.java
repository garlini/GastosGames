package com.garlini.gastosgames.view;

import java.util.Date;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.database.PlataformaDatabaseHandler;
import com.garlini.gastosgames.model.Plataforma;
import com.garlini.gastosgames.report.RelatorioGastosSinteticoMensal;
import com.garlini.gastosgames.report.RelatorioLoading;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class RelatorioGastosSinteticoMensalActivity extends ActionBarActivity {

	private static final int REQUEST_FILTRO = 100;
	
	private DatabaseHandler dbHandler;
	
	private Date dataInicial;
	private Date dataFinal;
	
	private Plataforma plataforma;
	
	private Boolean apenasVendiveis = false;
	
	private Boolean apenasPlataformasAtivas = false;
	
	private WebView web;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_relatorio_gastos_sintetico_mensal);
		
		web = (WebView) findViewById(R.id.webView1);
		
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
			
			Long plataformaId = savedInstanceState.getLong("plataforma", 0);
			if (plataformaId != 0) {
				plataforma = new PlataformaDatabaseHandler().getPlataforma(dbHandler.getReadableDatabase(), plataformaId); 
			}
			
			apenasVendiveis = savedInstanceState.getBoolean("apenasVendiveis", false);
			
			apenasPlataformasAtivas = savedInstanceState.getBoolean("apenasPlataformasAtivas", false);
		}
						
		
	   loadRelatorio();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		if (dataInicial != null) {
			outState.putLong("dataInicial", dataInicial.getTime());
		}
		
		if (dataFinal != null) {
			outState.putLong("dataFinal", dataFinal.getTime());
		}
		
		if (plataforma != null) {
			outState.putLong("plataforma", plataforma.getId());
		}
		
		outState.putBoolean("apenasVendiveis", apenasVendiveis);
		
		if (apenasPlataformasAtivas != null) {
			outState.putBoolean("apenasPlataformasAtivas", apenasPlataformasAtivas);
		}
		
		super.onSaveInstanceState(outState);
	}
	
	private void loadRelatorio()
	{
		new LoadRelatorioTask().execute();
	}
	
	private void loadHTML(String html)
	{
		web.loadDataWithBaseURL(null, html, "text/html; charset=UTF-8", "UTF-8", null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.relatorio_gastos_sintetico_mensal,
				menu);
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
		plataforma = null;
		apenasVendiveis = false;
		apenasPlataformasAtivas = false;
		
		loadRelatorio();
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
		
		//Este relatorio tem filtro de plataforma
		it.putExtra(FiltroActivity.EXTRA_FILTRAR_PLATAFORMA, true);
		
		if (plataforma != null) {
			it.putExtra(FiltroActivity.EXTRA_PLATAFORMA, plataforma.getId());
		}
		
		it.putExtra(FiltroActivity.EXTRA_FILTRAR_APENAS_VENDIVEIS, true);
		if (apenasVendiveis != null) {
			it.putExtra(FiltroActivity.EXTRA_APENAS_VENDIVEIS, apenasVendiveis);
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
			
			if (data.hasExtra(FiltroActivity.EXTRA_PLATAFORMA)) {
				plataforma = new PlataformaDatabaseHandler()
					.getPlataforma(dbHandler.getReadableDatabase(), data.getLongExtra(FiltroActivity.EXTRA_PLATAFORMA, 0));
				
			} else {
				plataforma = null;
			}
			
			if (data.hasExtra(FiltroActivity.EXTRA_APENAS_VENDIVEIS)) {
				apenasVendiveis = data.getBooleanExtra(FiltroActivity.EXTRA_APENAS_VENDIVEIS, false);
			} else {
				apenasVendiveis = false;
			}
			
			if (data.hasExtra(FiltroActivity.EXTRA_APENAS_PLATAFORMAS_ATIVAS)) {
				apenasPlataformasAtivas = data.getBooleanExtra(FiltroActivity.EXTRA_APENAS_PLATAFORMAS_ATIVAS, false);
			} else {
				apenasPlataformasAtivas = null;
			}
			
			loadRelatorio();
			
		}
		
		super.onActivityResult(requestCode, requestCode, data);
	}
	
	class LoadRelatorioTask extends AsyncTask<String, Integer, String>
	{
		@Override
		protected void onPreExecute() {
			
			String htmlLoading = new RelatorioLoading(getApplicationContext(), null).geraRelatorio();
			
			loadHTML(htmlLoading);
						
			super.onPreExecute();
		}
		

		@Override
		protected String doInBackground(String... params) {
			
			RelatorioGastosSinteticoMensal relatorio = new RelatorioGastosSinteticoMensal(getApplicationContext(), dbHandler.getReadableDatabase());
						
			if (dataInicial != null) {
				relatorio.setDataInicial(dataInicial);
			}
			
			if (dataFinal != null) {
				relatorio.setDataFinal(dataFinal);
			}
			
			if (plataforma != null) {
				relatorio.setPlataforma(plataforma);
			}
			
			relatorio.setApenasVendiveis(apenasVendiveis);
			
			relatorio.setApenasPlataformasAtivas(apenasPlataformasAtivas);
			
			String html = relatorio.geraRelatorio(); 
			
			//Log.w("GG", html);
						
			return html;
		}
		
		@Override
		protected void onPostExecute(String html) {
			
			loadHTML(html);
			
			super.onPostExecute(html);
		}
		
	}
}
