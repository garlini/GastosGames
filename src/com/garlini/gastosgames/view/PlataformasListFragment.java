package com.garlini.gastosgames.view;

import java.util.List;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.database.PlataformaDatabaseHandler;
import com.garlini.gastosgames.model.Plataforma;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class PlataformasListFragment extends Fragment {
	
	private static final int REQUEST_CREATE_PLATAFORMA = 100;
	private static final int REQUEST_UPDATE_PLATAFORMA = 101;
		
	ListView listView;
	
	private DatabaseHandler dbHandler;
		
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
				
		View rootView = inflater.inflate(R.layout.fragment_plataformas, container,
				false);
		
		setHasOptionsMenu(true);
		
		listView = (ListView) rootView.findViewById(R.id.listView1);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent it = new Intent(getActivity(), PlataformaFormActivity.class);
				
				it.putExtra(PlataformaFormActivity.EXTRA_PLATAFORMA_ID, id);
				
				startActivityForResult(it, REQUEST_UPDATE_PLATAFORMA);
				
			}

			
		});
		
		registerForContextMenu(listView);
		
		dbHandler = DatabaseHandler.getInstance(getActivity().getApplicationContext());
				
		new LoadPlataformasTask().execute();
		
		return rootView;
	}
	
	@Override
	public void onDestroy() {
		
		//dbHandler.close();
		
		super.onDestroy();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == R.id.action_add) {
			
			Intent it = new Intent(getActivity(), PlataformaFormActivity.class);
			startActivityForResult(it, REQUEST_CREATE_PLATAFORMA);
			
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
				
		if (v.getId() == R.id.listView1) {
			menu.setHeaderTitle(getString(R.string.header_opoces));
			menu.add(Menu.NONE, 0, 0, getString(R.string.action_remover_plataforma));
			return;
		}
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		if (0 == item.getItemId()) {
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			
			removerPlataforma(menuInfo.id);
		}
		
		return super.onContextItemSelected(item);
	}
	
	private void removerPlataforma(long id)
	{
		final long plataformaId = id;
		
		new AlertDialog.Builder(getActivity())
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.action_remover_plataforma)
			.setMessage(getString(R.string.confirmation_remover_plataforma))
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					new RemovePlataformaTask().execute(new Long[] {plataformaId});
				}
				
			})
			.setNegativeButton(android.R.string.no, null)
			.show();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		
		switch (requestCode) {
		case REQUEST_CREATE_PLATAFORMA:
			ViewUtil.showInfo(getActivity(), getString(R.string.info_plataforma_criada));
			new LoadPlataformasTask().execute();
			break;
			
		case REQUEST_UPDATE_PLATAFORMA:	
			ViewUtil.showInfo(getActivity(), getString(R.string.info_plataforma_atualizada));
			new LoadPlataformasTask().execute();
			break;

		default:
			break;
		}
	}
	
	class LoadPlataformasTask extends AsyncTask<String, Integer, String>
	{

		List<Plataforma> plataformas;
		
		@Override
		protected String doInBackground(String... params) {
						
			plataformas = new PlataformaDatabaseHandler().getAllPlataformas(dbHandler.getReadableDatabase());
						
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
						
			if (getActivity() != null && plataformas != null) {
				listView.setAdapter(new PlataformaAdapter(plataformas));
			}
						
			super.onPostExecute(result);
		}
		
	}
	
	class PlataformaAdapter extends BaseAdapter 
	{
		List<Plataforma> plataformas;
		LayoutInflater inflater;
		
		public PlataformaAdapter(List<Plataforma> plataformas) {
			this.plataformas = plataformas;
			inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
				v = inflater.inflate(R.layout.plataforma_list_item, parent, false);
				
			} 
			
			TextView textNome = (TextView) v.findViewById(R.id.textNome);
			Switch switchAtiva = (Switch) v.findViewById(R.id.switchAtiva);
			
			Plataforma plataforma = plataformas.get(position);
			
			textNome.setText(plataforma.getNome());
			
			switchAtiva.setChecked(plataforma.isAtiva());
			switchAtiva.setTag(plataforma);
			switchAtiva.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Plataforma plataforma = (Plataforma) buttonView.getTag();
					plataforma.setAtiva(isChecked);
					
					PlataformaDatabaseHandler plataformaDB = new PlataformaDatabaseHandler();
					boolean salvou;
					try {
						salvou = plataformaDB.updatePlataforma(dbHandler.getWritableDatabase(), plataforma);
					} catch (SQLException ex) {
						salvou = false;
					}
					if (!salvou) {
						ViewUtil.showError(getActivity(), getString(R.string.error_salvar_plataforma));
					}
				}
			});
			
			return v;
		}
		
	}
	
	class RemovePlataformaTask extends AsyncTask<Long, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(Long... params) {
			
			return new PlataformaDatabaseHandler().deletePlataforma(dbHandler.getWritableDatabase(), params[0]);
		}

		
		@Override
		protected void onPostExecute(Integer result) {
			if (result > 0) {
				if (getActivity() != null) {
					ViewUtil.showInfo(getActivity(), getString(R.string.info_plataforma_removida));
					
					new LoadPlataformasTask().execute();
				}
			}
			
			super.onPostExecute(result);
		}
		
	}

}
