package com.garlini.gastosgames.view;

import com.garlini.gastosgames.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GraficosListFragment extends Fragment {
	
	private static final int REQUEST_CREATE_GASTO = 100;
	
	private ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_graficos, container,
				false);
		
		setHasOptionsMenu(true);
		
		listView = (ListView) rootView.findViewById(R.id.listView1);
		
		String relatorios[] = new String[] {
			getString(R.string.title_relatorio_comparativo_plataformas),
			getString(R.string.title_relatorio_comparativo_categorias),
			getString(R.string.title_relatorio_comparativo_plataformas_e_categorias)
		};
		
		listView.setAdapter(new ArrayAdapter<>(
				getActivity(), 
				R.layout.graficos_list_item, 
				android.R.id.text1, 
				relatorios));
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent intent;
				switch (position) {
				case 0:
					intent = new Intent(getActivity(), GraficoComparativoPlataformasActivity.class);
					break;
				case 1:
					intent = new Intent(getActivity(), GraficoComparativoCategoriasActivity.class);
					break;
				case 2:
					intent = new Intent(getActivity(), GraficoComparativoPlataformasCategoriasActivity.class);
					break;
				default:
					intent = null;
					break;
				}
				
				if (intent != null) {
					startActivity(intent);
				}
				
			}
			
		});
		
		return rootView;
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
			break;
			
			
		default:
			break;
		}
	}
}
