package com.garlini.gastosgames.view;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.database.GastoDatabaseHandler;
import com.garlini.gastosgames.database.PlataformaDatabaseHandler;
import com.garlini.gastosgames.model.Plataforma;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HomeFragment extends Fragment {
	
	private static final int REQUEST_CREATE_GASTO = 100;
	private static final int REQUEST_RELATORIO = 101;
	
	ListView listView;
	
	private DatabaseHandler dbHandler;
		
	Map<Long, String> cacheGastosTotais;
	Map<Long, String> cacheVendiveisTotais;
	Map<Long, String> cacheUltimoMes;
	
	private boolean needReload = false;
	
	private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_home, container,
				false);
		
		setHasOptionsMenu(true);
		
		listView = (ListView) rootView.findViewById(R.id.listView1);
		
		dbHandler = DatabaseHandler.getInstance(getActivity().getApplicationContext());
		
		cacheGastosTotais = new HashMap<>();
		cacheVendiveisTotais = new HashMap<>();
		cacheUltimoMes = new HashMap<>();
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				onPlataformaSelecionada(id);
			}
		});
		
		new LoadHome().execute();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		    	
		    	if (key.equals("exibir_plataformas_inativas_home")) {
		    		needReload = true;
		    	}
		    }
		};
		prefs.registerOnSharedPreferenceChangeListener(prefListener);
				
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (needReload) {
		
			new LoadHome().execute();
			
			needReload = false;
		}
	}
	
	@Override
	public void onDestroy() {
		
		//dbHandler.close();
				
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == R.id.action_add) {
			
			Intent it = new Intent(getActivity(), GastoFormActivity.class);
			startActivityForResult(it, REQUEST_CREATE_GASTO);
						
			return true;
			
		} 
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		
		switch (requestCode) {
		case REQUEST_CREATE_GASTO:
			ViewUtil.showInfo(getActivity(), getString(R.string.info_gasto_criado));
			/* case omitido propositalmente */
		case REQUEST_RELATORIO:
			new LoadHome().execute();
			break;

		default:
			break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void onPlataformaSelecionada(Long id) 
	{
		Intent it = new Intent(getActivity(), RelatorioGastosSinteticoActivity.class);
		
		it.putExtra(RelatorioGastosSinteticoActivity.EXTRA_PLATAFORMA, id);
		
		startActivityForResult(it, REQUEST_RELATORIO);
	}
	
	class LoadHome extends AsyncTask<String, Integer, String> {

		Boolean exibirInativas;
		
		List<Plataforma> plataformas;
		
		public LoadHome() {
			
		}
		
		@Override
		protected void onPreExecute() {
			
			cacheGastosTotais.clear();
			
			cacheVendiveisTotais.clear();
			
			cacheUltimoMes.clear();
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
			exibirInativas = preferences.getBoolean("exibir_plataformas_inativas_home", false);
			
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			
			SQLiteDatabase db = dbHandler.getReadableDatabase();
			
			if (exibirInativas) {
				plataformas = new PlataformaDatabaseHandler().getAllPlataformas(db);
			} else {
				plataformas = new PlataformaDatabaseHandler().getAllPlataformasAtivas(db);
			}
						
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			if (getActivity() != null && plataformas != null) {
				listView.setAdapter(new HomeAdapter(plataformas));
			}
			
			super.onPostExecute(result);
		}
		
	}
	
	class HomeAdapter extends BaseAdapter {
		
		List<Plataforma> plataformas;
		
		LayoutInflater inflater;
		
		String calculando;
				
		public HomeAdapter(List<Plataforma> plataformas) {
			this.plataformas = plataformas;
			
			inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			calculando = getString(R.string.placeholder_calculando);
		}

		@Override
		public int getCount() {
			return plataformas.size();
		}

		@Override
		public Object getItem(int position) {
			return plataformas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return plataformas.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			
			if (null == v) {
				v = inflater.inflate(R.layout.home_list_item, parent, false);
			}
			
			TextView textNome = (TextView) v.findViewById(R.id.textPlataforma);
			TextView textGastoTotal = (TextView) v.findViewById(R.id.textGastoTotal);
			TextView textVendivelTotal = (TextView) v.findViewById(R.id.textVendivelTotal);
			TextView textUltimoMes = (TextView) v.findViewById(R.id.textUltimoMes);
			
			Plataforma plataforma = plataformas.get(position);
			Long plataformaId = plataforma.getId();
			
			textNome.setText(plataforma.getNome());
			
			String strGastoTotal = cacheGastosTotais.get(plataformaId);
			String strVendivelTotal = cacheVendiveisTotais.get(plataformaId);
			String strUltimoMes = cacheUltimoMes.get(plataformaId);
			
			if (strGastoTotal == null || strVendivelTotal == null || strUltimoMes == null) {
				textGastoTotal.setText(calculando);
				textVendivelTotal.setText(calculando);
				textUltimoMes.setText(calculando);
				
				new LoadTotaisPlataforma(plataformaId, textGastoTotal, textVendivelTotal, textUltimoMes).execute();
				
			} else {
				textGastoTotal.setText(strGastoTotal);
				textVendivelTotal.setText(strVendivelTotal);
				textUltimoMes.setText(strUltimoMes);
			}
			
			return v;
		}
	}
	
	class LoadTotaisPlataforma extends AsyncTask<String, Integer, String>
	{
		Long plataformaId;
		
		private TextView textGastoTotal;
		private TextView textVendivelTotal;
		private TextView textUltimoMes;
		
		private Double gastoTotal;
		private Double vendivelTotal;
		private Double ultimoMes;
		
		public LoadTotaisPlataforma(Long plataformaId, TextView textGastoTotal, TextView textVendivelTotal, TextView textUltimoMes)
		{
			this.plataformaId = plataformaId;
			
			this.textGastoTotal = textGastoTotal;
			this.textVendivelTotal = textVendivelTotal;
			this.textUltimoMes = textUltimoMes;
		}

		@Override
		protected String doInBackground(String... params) {
			
			SQLiteDatabase db = dbHandler.getReadableDatabase();
			GastoDatabaseHandler gastoDB = new GastoDatabaseHandler();
			
			gastoTotal = gastoDB.calculaTotalGastosPlataforma(db, plataformaId);
			
			vendivelTotal = gastoDB.calculaTotalGastosVendiveisPlataforma(db, plataformaId);
			
			ultimoMes = gastoDB.calculaGastosUltimoMesPlataforma(db, plataformaId);
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {

			NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
			
			if (gastoTotal != null) {
				String total = numberFormat.format(gastoTotal.doubleValue());
				textGastoTotal.setText(total);
				cacheGastosTotais.put(plataformaId, total);
			}
			
			if (vendivelTotal != null) {
				String total = numberFormat.format(vendivelTotal.doubleValue());
				textVendivelTotal.setText(total);
				cacheVendiveisTotais.put(plataformaId, total);
			}
			
			if (ultimoMes != null) {
				String valor = numberFormat.format(ultimoMes.doubleValue());
				textUltimoMes.setText(valor);
				cacheUltimoMes.put(plataformaId, valor);
			}
			
			super.onPostExecute(result);
		}
	}
}
