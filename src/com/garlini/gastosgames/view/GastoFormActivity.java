package com.garlini.gastosgames.view;

import java.math.BigDecimal;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.CategoriaDatabaseHandler;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.database.GastoDatabaseHandler;
import com.garlini.gastosgames.database.PlataformaDatabaseHandler;
import com.garlini.gastosgames.model.Categoria;
import com.garlini.gastosgames.model.Gasto;
import com.garlini.gastosgames.model.Plataforma;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class GastoFormActivity extends ActionBarActivity {
	
	public static final String EXTRA_GASTO_ID = "extra-gasto-id";
	
	private DatabaseHandler dbHandler;
	
	private Gasto gastoUpdate;
	
	private Date dataSelecionada;
	
	private Plataforma plataformaSelecionada;
	
	private Categoria categoriaSelecionada;
	
	private List<Plataforma> plataformas;
	
	private List<Categoria> categorias;
	
	private TextView textDescricao;
	private TextView textValor;
	private Switch switchVendivel;
	private Spinner spinnerPlataforma;
	private Spinner spinnerCategoria;
	private Button buttonData;
	private ImageButton buttonHelpVendivel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gasto_form);
				
		textDescricao = (TextView) findViewById(R.id.editTextDescricao);
		textValor = (TextView) findViewById(R.id.editTextValor);
		spinnerPlataforma = (Spinner) findViewById(R.id.spinnerPlataforma);
		spinnerCategoria = (Spinner) findViewById(R.id.spinnerCategoria);
		switchVendivel = (Switch) findViewById(R.id.switchVendivel);
		buttonData = (Button) findViewById(R.id.buttonData);
		buttonHelpVendivel = (ImageButton) findViewById(R.id.imageButtonHelp);
		
		dbHandler = DatabaseHandler.getInstance(getApplicationContext());
		
		long id = getIntent().getLongExtra(EXTRA_GASTO_ID, 0);
		if (id > 0) {
			gastoUpdate = new GastoDatabaseHandler().getGasto(dbHandler.getReadableDatabase(), id);
			
			if (gastoUpdate == null) {
				ViewUtil.showError(getApplicationContext(), getString(R.string.error_gasto_nao_encontrado));
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			
			textDescricao.setText(gastoUpdate.getDescricao());
			switchVendivel.setChecked(gastoUpdate.getVendivel());
			
			NumberFormat nf = NumberFormat.getCurrencyInstance();
			DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) nf).getDecimalFormatSymbols();
			decimalFormatSymbols.setCurrencySymbol("");
			nf.setGroupingUsed(false);
			((DecimalFormat) nf).setDecimalFormatSymbols(decimalFormatSymbols);
			textValor.setText(nf.format(gastoUpdate.getValor().doubleValue()));
						
			dataSelecionada = gastoUpdate.getData();
			
			String strData = DateFormat.getDateFormat(getApplicationContext()).format(dataSelecionada);
			buttonData.setText(strData);
						
			setTitle(getString(R.string.title_editar));
		} else {
			setTitle(getString(R.string.title_criar));
			
			switchVendivel.setChecked(false);
			
			dataSelecionada = new Date();
			
			String strData = DateFormat.getDateFormat(getApplicationContext()).format(dataSelecionada);
			buttonData.setText(strData);
			
			
		}
		
		buttonData.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");				
			}
			
		});
		
		buttonHelpVendivel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHelpVendivel();
			}
		});
		
		/*textValor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					salvar();
				} else if (actionId == EditorInfo.IME_ACTION_NEXT) {
					spinnerCategoria.performClick();
				}
				return false;
			}
		}); */
		
		if (!configuraSpinnerPlataformas()) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		
		configuraSpinnerCategorias();
		
		if (gastoUpdate == null && plataformas.size() > 1) {
			spinnerPlataforma.performClick();
		}
		
	}
	
	@Override
	protected void onDestroy() {
		
		//dbHandler.close();
		
		super.onDestroy();
	}
	
	private void showHelpVendivel()
	{
		new AlertDialog.Builder(this)
			.setTitle(R.string.help)
			.setPositiveButton(android.R.string.ok, null)
			.setMessage(getString(R.string.help_vendivel))
			.show();
	}
	
	private boolean configuraSpinnerPlataformas()
	{
		PlataformaDatabaseHandler plataformaDB = new PlataformaDatabaseHandler();
		SQLiteDatabase db = dbHandler.getReadableDatabase();
		plataformas = plataformaDB.getAllPlataformasAtivas(db);
		
		if (null == gastoUpdate && plataformas.size() < 1) {
			//nao é possivel inserir novos gastos sem plataformas ativas
			ViewUtil.showError(this, getString(R.string.error_nenhuma_plataforma_ativa));
			return false;
		}
		
		Integer selecionado = null;
		
		List<String> dataAdapter = new ArrayList<>();
		
		for (int pos = 0; pos < plataformas.size(); pos++) {
			Plataforma p = plataformas.get(pos);
			dataAdapter.add(p.getNome());
			if (gastoUpdate != null && gastoUpdate.getPlataformaId() == p.getId()) {
				selecionado = pos;
			}
		}
		
		ArrayAdapter<String> platAdapter;
		if (gastoUpdate == null) {
			
			if (plataformas.size() != 1) {
				//Gambira para mostrar a opção "Plataforma" no formulario, mas nao mostrar nas opções (apenas quando for create)
				dataAdapter.add(getString(R.string.option_plataforma));
				
				platAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataAdapter) {
					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						View v = super.getView(position, convertView, parent);
				        if (position == getCount()) {
				            ((TextView)v.findViewById(android.R.id.text1)).setText("");
				            ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); 
				        }
	
				        return v;
					}
					
					@Override
					public int getCount() {
						return super.getCount()-1; 
					}
				};
			} else {
				//Se possuir exatamente 1 plataforma ativa não vamos fazer a gambiarra acima. 
				//Neste caso ja vamos trazer esta plataforma selecionada (mais abaixo).
				
				platAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dataAdapter);
			}
			
		} else {
			//Isso pode acontecer caso o usuario esteja editando um gasto associado a uma plataforma inativa.
			//Neste caso vamos adicionar a plataforma a lista e seleciona-la.
			if (selecionado == null) {
				Plataforma plat = plataformaDB.getPlataforma(db, gastoUpdate.getPlataformaId());
				if (plat != null) {
					plataformas.add(plat);
					dataAdapter.add(plat.getNome());
					selecionado = dataAdapter.size() - 1;
				}
			}
			
			platAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dataAdapter);
		}
		
		platAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerPlataforma.setAdapter(platAdapter);
		
		if (null == gastoUpdate) {
			if (plataformas.size() != 1) {
				//continuação da gambiarra
				spinnerPlataforma.setSelection(platAdapter.getCount());
			} else {
				//Apenas uma plataforma ativa. Seleciona ela.
				spinnerPlataforma.setSelection(0);
			}
		} else  if (selecionado != null) { //seleciona a plataforma selecionada anteriormente
			spinnerPlataforma.setSelection(selecionado);
		}
						
		spinnerPlataforma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				if (position < plataformas.size()) {
					plataformaSelecionada = plataformas.get(position);
					Log.w("GG", "Plataforma selecionada: " + plataformas.get(position).getNome());
				}
				
				if (textDescricao.getText().length() == 0) {
					textDescricao.requestFocus(View.FOCUS_DOWN);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(textDescricao, InputMethodManager.SHOW_IMPLICIT);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		return true;
	}
	
	private void configuraSpinnerCategorias()
	{
		categorias = new CategoriaDatabaseHandler().getAllCategorias(dbHandler.getReadableDatabase());
		
		Integer selecionado = null;
		
		List<String> dataAdapter = new ArrayList<>();
		
		dataAdapter.add(getString(R.string.option_sem_categoria));
		
		for (int pos = 0; pos < categorias.size(); pos++) {
			Categoria c = categorias.get(pos);
			dataAdapter.add(c.getNome());
			if (gastoUpdate != null && gastoUpdate.getCategoriaId() == c.getId()) {
				selecionado = pos;
			}
		}
		
		ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dataAdapter);
		catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinnerCategoria.setAdapter(catAdapter);
		
		if (selecionado != null) { //seleciona a categoria selecionada anteriormente
			spinnerCategoria.setSelection(selecionado + 1); //pula a primeira opção "sem categoria"
		}
		
		spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				if (0 == position) {
					categoriaSelecionada = null;
				} else {
					int pos = position - 1; /* ignora a primeira que é a opção "sem categoria" */
				
					if (pos < categorias.size()) {
						categoriaSelecionada = categorias.get(pos);
						
						if (gastoUpdate == null || gastoUpdate.getCategoriaId() != categoriaSelecionada.getId()) {
							//Vamos setar o campo "vendivel" com o valor que está na categoria selecionada
							switchVendivel.setChecked(categoriaSelecionada.isVendivel());
						}
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gasto_form, menu);
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
		imm.hideSoftInputFromWindow(textValor.getWindowToken(), 0);
		
		String descricao = textDescricao.getText().toString().trim();
		if (descricao.length() < 1) {
			ViewUtil.showValidationError(this, getString(R.string.validation_informe_descricao));
			return;
		}
		
		String strValor = textValor.getText().toString().trim();
		if (strValor.length() < 1) {
			ViewUtil.showValidationError(this, getString(R.string.validation_informe_valor));
			return;
		}
		
		strValor = strValor.replace(",", ".");
		try {
			Double dValor = Double.parseDouble(strValor);
			if (dValor <= 0.0) {
				ViewUtil.showValidationError(this, getString(R.string.validation_valor_maior_zero));
				return;
			}
		} catch (NumberFormatException ex) {
			ViewUtil.showValidationError(this, getString(R.string.validation_valor_invalido));
			return;
		}
		
		if (null == plataformaSelecionada) {
			ViewUtil.showValidationError(this, getString(R.string.validation_informe_plataforma));
			return;
		}
		
		SQLiteDatabase db = dbHandler.getWritableDatabase();
		
		Boolean vendivel = switchVendivel.isChecked();
		
		BigDecimal valor = new BigDecimal(strValor);
		Date data = dataSelecionada;
		Long plataformaId = plataformaSelecionada.getId();
		Long categoriaId = (null != categoriaSelecionada) ? categoriaSelecionada.getId() : null;
				
		GastoDatabaseHandler gastoDB = new GastoDatabaseHandler();
		
		boolean salvou;
		try {
			if (null != gastoUpdate) {
				//update 
				gastoUpdate.setDescricao(descricao);
				gastoUpdate.setVendivel(vendivel);
				gastoUpdate.setValor(valor);
				gastoUpdate.setData(data);
				gastoUpdate.setPlataformaId(plataformaId);
				gastoUpdate.setCategoriaId(categoriaId);
				
				salvou = gastoDB.updateGasto(db, gastoUpdate);
							
			} else {
				//insert
				Gasto gasto = new Gasto(null, descricao, valor, data, vendivel, plataformaId, categoriaId);
				
				salvou = gastoDB.insertGasto(db, gasto);
											
			}
		} catch (SQLException ex) {
			salvou = false;
		}
		
		if (!salvou) {
			ViewUtil.showError(getApplicationContext(), getString(R.string.error_salvar_gasto));
			return;
		}
		
		if (categoriaSelecionada != null) {
			//Verifica se o usuario escolheu um atributo "vendivel" diferente do que está salvo na categoria escolhida.
			//Em caso positivo, vamos atualizar o atributo na tabela de categorias, para da proxima vez sugerir a ultima opção escolhida.
			if (categoriaSelecionada.getVendivel() != vendivel) {
				categoriaSelecionada.setVendivel(vendivel);
				try {
					new CategoriaDatabaseHandler().updateCategoria(db, categoriaSelecionada);
				} catch (SQLException ex) {
				}
			}
		}
		
		setResult(RESULT_OK);
		finish();
	}
	
	private void cancelar()
	{
		finish();
	}
	
	public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
	{

		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the current date as the default date in the picker
	        final Calendar c = Calendar.getInstance();
	        c.setTime(dataSelecionada);
	        int year = c.get(Calendar.YEAR);
	        int month = c.get(Calendar.MONTH);
	        int day = c.get(Calendar.DAY_OF_MONTH);

	        // Create a new instance of DatePickerDialog and return it
	        return new DatePickerDialog(getActivity(), this, year, month, day);
	    }
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			
			Calendar cal = Calendar.getInstance();
		    cal.set(Calendar.YEAR, year);
		    cal.set(Calendar.MONTH, monthOfYear);
		    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		    Date dateRepresentation = cal.getTime();
			
			dataSelecionada = dateRepresentation;
			
			String strData = DateFormat.getDateFormat(getApplicationContext()).format(dataSelecionada);
			buttonData.setText(strData);
			
		}
		
	}
}
