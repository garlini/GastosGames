package com.garlini.gastosgames.view;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.CategoriaDatabaseHandler;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.model.Categoria;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class CategoriaFormActivity extends ActionBarActivity {
	
	public static final String EXTRA_CATEGORIA_ID = "extra-categoria-id";
	
	Categoria categoriaUpdate;
	
	private DatabaseHandler dbHandler;
	
	private TextView textNome;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_categoria_form);
		
		textNome = (TextView) findViewById(R.id.editTextNome);
		
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
		
		long id = getIntent().getLongExtra(EXTRA_CATEGORIA_ID, 0);
		if (id > 0) {
			categoriaUpdate = new CategoriaDatabaseHandler().getCategoria(dbHandler.getReadableDatabase(), id);
			
			if (categoriaUpdate == null) {
				ViewUtil.showError(getApplicationContext(), getString(R.string.error_categoria_nao_encontrada));
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			
			setTitle(getString(R.string.title_editar));
			
			textNome.setText(categoriaUpdate.getNome());
			
		} else {
			setTitle(getString(R.string.title_criar));
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
		getMenuInflater().inflate(R.menu.categoria_form, menu);
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
			ViewUtil.showValidationError(getApplicationContext(), getString(R.string.validation_informe_nome_categoria));
			return;
		}
		
		CategoriaDatabaseHandler categoriaDB = new CategoriaDatabaseHandler();
		
		SQLiteDatabase db = dbHandler.getWritableDatabase();
		
		//verifica se ja existe uma categoria com este nome
		Categoria categoriaTemp = categoriaDB.getCategoriaPeloNome(db, nome);
		if (categoriaTemp != null && 
				(null == categoriaUpdate || categoriaUpdate.getId() != categoriaTemp.getId())) {
			
			ViewUtil.showValidationError(getApplicationContext(), getString(R.string.validation_categoria_ja_existe_nome));
			return;
		}
		
		boolean salvou;
		try {
			if (null != categoriaUpdate) {
				//update 
				categoriaUpdate.setNome(nome);
				
				salvou = categoriaDB.updateCategoria(db, categoriaUpdate);
							
			} else {
				//insert
				Categoria categoria = new Categoria(null, nome, false);
				
				salvou = categoriaDB.insertCategoria(db, categoria);
															
			}
		} catch (SQLException ex) {
			salvou = false;
		}
		
		if (!salvou) {
			ViewUtil.showError(getApplicationContext(), getString(R.string.error_salvar_categoria));
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
