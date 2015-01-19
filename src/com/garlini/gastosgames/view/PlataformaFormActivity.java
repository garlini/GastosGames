package com.garlini.gastosgames.view;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.database.PlataformaDatabaseHandler;
import com.garlini.gastosgames.model.Plataforma;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Switch;
import android.widget.TextView;

public class PlataformaFormActivity extends ActionBarActivity {

	public static final String EXTRA_PLATAFORMA_ID = "extra-plataforma-id";
	
	private Plataforma plataformaUpdate;
	
	private DatabaseHandler dbHandler;
	
	private TextView textNome;
	private Switch switchAtiva;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plataforma_form);
		
		textNome = (TextView) findViewById(R.id.editTextNome);
		switchAtiva = (Switch) findViewById(R.id.switchAtiva);
		
		textNome.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					salvar();
				}
				return false;
			}
		});
		
		dbHandler = DatabaseHandler.getInstance(getApplicationContext());
		
		long id = getIntent().getLongExtra(EXTRA_PLATAFORMA_ID, 0);
		if (id > 0) {
			plataformaUpdate = new PlataformaDatabaseHandler().getPlataforma(dbHandler.getReadableDatabase(), id);
			
			if (plataformaUpdate == null) {
				ViewUtil.showError(getApplicationContext(), getString(R.string.error_platforma_nao_encontrada));
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			
			setTitle(getString(R.string.title_editar));
			
			textNome.setText(plataformaUpdate.getNome());
			switchAtiva.setChecked(plataformaUpdate.getAtiva());
			
		} else {
			setTitle(getString(R.string.title_criar));
			
			switchAtiva.setChecked(true);
		}
		
	}
	
	
	@Override
	protected void onDestroy() {
		
		//dbHandler.close();
		
		super.onDestroy();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.plataforma_form, menu);
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
		} else if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		} else if (id == R.id.action_salvar) {
			salvar();
			return true;
		} else if (id == R.id.action_cancelar) {
			cancelar();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	private void salvar()
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(textNome.getWindowToken(), 0);
		
		String nome = textNome.getText().toString().trim();
		if (nome.length() < 1) {
			ViewUtil.showValidationError(getApplicationContext(), getString(R.string.validation_informe_nome_plataforma));
			return;
		}
		
		boolean ativa = switchAtiva.isChecked();
						
		PlataformaDatabaseHandler plataformaDB = new PlataformaDatabaseHandler();
		
		SQLiteDatabase db = dbHandler.getWritableDatabase();
		
		//verifica se ja existe uma plataforma com este nome
		Plataforma plataformaTemp = plataformaDB.getPlataformaPeloNome(db, nome);
		if (plataformaTemp != null && 
				(null == plataformaUpdate || plataformaUpdate.getId() != plataformaTemp.getId())) {
			
			ViewUtil.showValidationError(getApplicationContext(), getString(R.string.validation_plataforma_ja_existe_nome));
			return;
		}
		
		boolean salvou;
		try {
			if (null != plataformaUpdate) {
				//update 
				plataformaUpdate.setNome(nome);
				plataformaUpdate.setAtiva(ativa);
				
				salvou = plataformaDB.updatePlataforma(db, plataformaUpdate);
							
			} else {
				//insert
				Plataforma plataforma = new Plataforma(null, nome, ativa);
				
				salvou = plataformaDB.insertPlataforma(db, plataforma);
											
			}
		} catch (SQLException ex) {
			salvou = false;
		}
		
		if (!salvou) {
			ViewUtil.showError(getApplicationContext(), getString(R.string.error_salvar_plataforma));
			return;
		}
			
				
		setResult(RESULT_OK);
		finish();
	}
	
	private void cancelar()
	{
		finish();
	}
}
