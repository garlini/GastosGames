package com.garlini.gastosgames.view;

import java.util.List;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.CategoriaDatabaseHandler;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.model.Categoria;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;

public class CategoriasListFragment extends Fragment {
	
	private static final int REQUEST_CREATE_CATEGORIA = 100;
	private static final int REQUEST_UPDATE_CATEGORIA = 101;
	
	ListView listView;
	
	private DatabaseHandler dbHandler;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_categorias, container,
				false);
		
		setHasOptionsMenu(true);
		
		listView = (ListView) rootView.findViewById(R.id.listView1);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent it = new Intent(getActivity(), CategoriaFormActivity.class);
				
				it.putExtra(CategoriaFormActivity.EXTRA_CATEGORIA_ID, id);
				
				startActivityForResult(it, REQUEST_UPDATE_CATEGORIA);
				
			}

			
		});
		
		registerForContextMenu(listView);
		
		dbHandler = DatabaseHandler.getInstance(getActivity().getApplicationContext());
		
		new LoadCategoriasTask().execute();
		
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
			
			Intent it = new Intent(getActivity(), CategoriaFormActivity.class);
			startActivityForResult(it, REQUEST_CREATE_CATEGORIA);
						
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
				
		if (v.getId() == R.id.listView1) {
			menu.setHeaderTitle(getString(R.string.header_opoces));
			menu.add(Menu.NONE, 0, 0, getString(R.string.action_remover_categoria));
			return;
		}
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		if (0 == item.getItemId()) {
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			
			removerCategoria(menuInfo.id);
		}
		
		return super.onContextItemSelected(item);
	}
	
	private void removerCategoria(long id)
	{
		final long categoriaId = id;
		
		new AlertDialog.Builder(getActivity())
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.action_remover_categoria)
			.setMessage(getString(R.string.confirmation_remover_categoria))
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					new RemoveCategoriaTask().execute(new Long[] {categoriaId});
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
		case REQUEST_CREATE_CATEGORIA:
			ViewUtil.showInfo(getActivity(), getString(R.string.info_categoria_criada));
			new LoadCategoriasTask().execute();
			break;
			
		case REQUEST_UPDATE_CATEGORIA:	
			ViewUtil.showInfo(getActivity(), getString(R.string.info_categoria_atualizada));
			new LoadCategoriasTask().execute();
			break;

		default:
			break;
		}
	}
	
	class LoadCategoriasTask extends AsyncTask<String, Integer, String>
	{

		List<Categoria> categorias;
		
		@Override
		protected String doInBackground(String... params) {
						
			categorias = new CategoriaDatabaseHandler().getAllCategorias(dbHandler.getReadableDatabase());
						
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
						
			if (getActivity() != null && categorias != null) {
				listView.setAdapter(new CategoriaAdapter(categorias));
			}
						
			super.onPostExecute(result);
		}
		
	}
	
	class CategoriaAdapter extends BaseAdapter
	{
		List<Categoria> categorias;
		LayoutInflater inflater;
				
		public CategoriaAdapter(List<Categoria> categorias) {
			this.categorias = categorias;
			inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return categorias.size();
		}

		@Override
		public Object getItem(int position) {
			return categorias.get(position);
		}

		@Override
		public long getItemId(int position) {
			return categorias.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			
			if (null == v) {
				v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			}
			
			TextView textNome = (TextView) v.findViewById(android.R.id.text1);
			
			textNome.setText(categorias.get(position).getNome());
			
			return v;
		}
		
	}
	
	class RemoveCategoriaTask extends AsyncTask<Long, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(Long... params) {
			
			return new CategoriaDatabaseHandler().deleteCategoria(dbHandler.getWritableDatabase(), params[0]);
		}

		
		@Override
		protected void onPostExecute(Integer result) {
			if (result > 0) {
				if (getActivity() != null) {
					ViewUtil.showInfo(getActivity(), getString(R.string.info_categoria_removida));
					
					new LoadCategoriasTask().execute();
				}
			}
			
			super.onPostExecute(result);
		}
		
	}

}
