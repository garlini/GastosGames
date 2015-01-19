package com.garlini.gastosgames.view;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.DatabaseHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class ManutencaoListFragment extends Fragment {
	
	private static final int REQUEST_CREATE_GASTO = 100;
	
	private ListView listView;
	
	private DatabaseHandler dbHandler;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_manutencao, container,
				false);
		
		setHasOptionsMenu(true);
		
		dbHandler = DatabaseHandler.getInstance(getActivity().getApplicationContext());
		
		listView = (ListView) rootView.findViewById(R.id.listView1);
		
		String operacoes[] = new String[] {
			getString(R.string.exportar_db),
			getString(R.string.importar_db)
		};
		
		listView.setAdapter(new ArrayAdapter<>(
				getActivity(), 
				R.layout.relatorios_list_item, 
				android.R.id.text1, 
				operacoes));
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				
				switch (position) {
					case 0:
						exportarDB();
						break;
					case 1:
						importaDB();
						break;
				}
			}
			
		});
		
		return rootView;
	}
	
	private void exportarDB()
	{
		new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.action_exportar_db)
		.setMessage(getString(R.string.confirmation_exportar_db))
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				StringBuilder local = new StringBuilder();
				boolean exportou = dbHandler.exportDB(getActivity().getApplicationContext(), local);
				if (exportou) {
					String msg = getString(R.string.info_db_exportado_sucesso);
					msg = msg.replace("__local__", local.toString());
					ViewUtil.showInfo(getActivity(), msg);
				} else {
					ViewUtil.showError(getActivity(), getString(R.string.error_exportacao_db));
				}
				
			}
			
		})
		.setNegativeButton(android.R.string.no, null)
		.show();
	}
	
	private void importaDB()
	{
		new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.action_importar_db)
		.setMessage(getString(R.string.confirmation_importar_db))
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				StringBuilder msgErro = new StringBuilder();
				boolean importou = dbHandler.importDB(getActivity().getApplicationContext(), msgErro);
				if (importou) {
					ViewUtil.showInfo(getActivity(), getString(R.string.info_db_importado_sucesso));
				} else {
					String msg = msgErro.toString();
					if (msg.isEmpty()) {
						msg = getString(R.string.error_importacao_db);
					}
					ViewUtil.showError(getActivity(), msg);
				}
				
			}
			
		})
		.setNegativeButton(android.R.string.no, null)
		.show();
				
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
