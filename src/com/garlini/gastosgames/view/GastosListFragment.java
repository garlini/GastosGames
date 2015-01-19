package com.garlini.gastosgames.view;

import java.math.BigDecimal;
import java.text.NumberFormat;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.database.GastoDatabaseHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class GastosListFragment extends Fragment {
	
	private static final int REQUEST_CREATE_GASTO = 100;
	private static final int REQUEST_UPDATE_GASTO = 101;
	
	private String query;
	
	private ListView listView;
	
	private DatabaseHandler dbHandler;
	
	Cursor listCursor;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
				
		View rootView = inflater.inflate(R.layout.fragment_gastos, container,
				false);
		
		setHasOptionsMenu(true);
	
		listView = (ListView) rootView.findViewById(R.id.listView1);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent it = new Intent(getActivity(), GastoFormActivity.class);
				
				it.putExtra(GastoFormActivity.EXTRA_GASTO_ID, id);
				
				startActivityForResult(it, REQUEST_UPDATE_GASTO);
				
			}

			
		});
		
		registerForContextMenu(listView);
		
		dbHandler = DatabaseHandler.getInstance(getActivity().getApplicationContext());
				
		
		new LoadGastosTask().execute();
		
		return rootView;
	}
	
	@Override
	public void onDestroy() {
		
		if (listCursor != null) {
			listView.setAdapter(null);
			listCursor.close();
			listCursor = null;
		}
		
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
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
				
		if (v.getId() == R.id.listView1) {
			menu.setHeaderTitle(getString(R.string.header_opoces));
			menu.add(Menu.NONE, 0, 0, getString(R.string.action_remover_gasto));
			return;
		}
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		if (0 == item.getItemId()) {
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			
			removerGasto(menuInfo.id);
		}
		
		return super.onContextItemSelected(item);
	}
	
	private void removerGasto(long id)
	{
		final long gastoId = id;
		
		new AlertDialog.Builder(getActivity())
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.action_remover_gasto)
			.setMessage(getString(R.string.confirmation_remover_gasto))
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					new RemoveGastoTask().execute(new Long[] {gastoId});
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
		case REQUEST_CREATE_GASTO:
			ViewUtil.showInfo(getActivity(), getString(R.string.info_gasto_criado));
			new LoadGastosTask().execute();
			break;
			
		case REQUEST_UPDATE_GASTO:	
			ViewUtil.showInfo(getActivity(), getString(R.string.info_gasto_atualizado));
			new LoadGastosTask().execute();
			break;
			
		default:
			break;
		}
	}
		
	
	public void setQuery(String query) {
		this.query = query;
	}

	class LoadGastosTask extends AsyncTask<String, Integer, String>
	{

		Cursor cursor;
				
		@Override
		protected String doInBackground(String... params) {
							
			cursor = new GastoDatabaseHandler().createListCursor(dbHandler.getReadableDatabase(), query);
						
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			if (cursor != null) {
				if (getActivity() != null) {
										
					GastoListAdapter adapter = new GastoListAdapter(
							getActivity(), 
							//android.R.layout.simple_list_item_2,
							R.layout.gasto_list_item,
							cursor, 
							//new String[] {"descricao", "valor"}, new int[] {android.R.id.text1, android.R.id.text2},
							new String[] {"descricao", "valor", "plataforma"}, 
							new int[] {R.id.textDescricao, R.id.textPlataforma, R.id.textValor},
							0);
										
					listView.setAdapter(adapter);
															
					//Fecha o cursor anterior
					if (listCursor != null) {
						listCursor.close();
					}
					
					listCursor = cursor;
					
					
				} else {
					cursor.close();
				}
			}
					
						
			super.onPostExecute(result);
		}
		
	}
	
	class GastoListAdapter extends SimpleCursorAdapter
	{
		//private LayoutInflater inflater;
		
		
		public GastoListAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			
			//inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			return super.newView(context, cursor, parent);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//TextView textDesc = (TextView) view.findViewById(android.R.id.text1);
			//TextView textValor = (TextView) view.findViewById(android.R.id.text2);
			TextView textDesc = (TextView) view.findViewById(R.id.textDescricao);
			TextView textPlataforma = (TextView) view.findViewById(R.id.textPlataforma);
			TextView textValor = (TextView) view.findViewById(R.id.textValor);
			
			
			textDesc.setText(cursor.getString(1));
			
			BigDecimal valor = new BigDecimal(cursor.getDouble(2));
			NumberFormat format = NumberFormat.getCurrencyInstance();
			textValor.setText(format.format(valor.doubleValue()));
			
			textPlataforma.setText(cursor.getString(3));
		}
	}
	
	class RemoveGastoTask extends AsyncTask<Long, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(Long... params) {
			
			return new GastoDatabaseHandler().deleteGasto(dbHandler.getWritableDatabase(), params[0]);
		}

		
		@Override
		protected void onPostExecute(Integer result) {
			
			if (getActivity() != null) {
				if (result > 0) {
					ViewUtil.showInfo(getActivity(), getString(R.string.info_gasto_removido));
				}
				
				new LoadGastosTask().execute();
			}
			
			super.onPostExecute(result);
		}
		
	}

}
