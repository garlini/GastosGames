package com.garlini.gastosgames.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.garlini.gastosgames.R;
import com.garlini.gastosgames.database.DatabaseHandler;
import com.garlini.gastosgames.database.PlataformaDatabaseHandler;
import com.garlini.gastosgames.model.Plataforma;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class FiltroActivity extends ActionBarActivity  {

	public static final String EXTRA_DATA_INICIAL = "extra-data-inicial";
	public static final String EXTRA_DATA_FINAL = "extra-data-final";
	public static final String EXTRA_FILTRAR_PLATAFORMA = "extra-filtrar-plataforma";
	public static final String EXTRA_PLATAFORMA = "extra-plataforma";
	public static final String EXTRA_FILTRAR_APENAS_VENDIVEIS = "extra-filtrar-apenas-vendiveis";
	public static final String EXTRA_APENAS_VENDIVEIS = "extra-apenas-vendiveis";
	public static final String EXTRA_FILTRAR_APENAS_PLATAFORMAS_ATIVAS = "extra-filtrar-apenas-plataformas-ativas";
	public static final String EXTRA_APENAS_PLATAFORMAS_ATIVAS = "extra-apenas-plataformas-ativas";
	
	Button buttonDataInicial;
	ImageButton buttonCancelDataInicial;
	
	Button buttonDataFinal;
	ImageButton buttonCancelDataFinal;
	
	Spinner spinnerPlataforma;
	ImageButton buttonCancelPlataforma;
	
	Switch switchApenasVendiveis;
	
	Switch switchApenasPlataformasAtivas;
	
	private Date dataInicial;
	private Date dataFinal;
	private Plataforma plataforma;
	private Boolean apenasVendiveis;
	private Boolean apenasPlataformasAtivas;
	
	private List<Plataforma> plataformasAtivas;
	
	private DatabaseHandler dbHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filtro);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		buttonDataInicial = (Button) findViewById(R.id.buttonDataInicial);
		buttonCancelDataInicial = (ImageButton) findViewById(R.id.imageButtonCancelDataInicial);
		buttonDataFinal = (Button) findViewById(R.id.buttonDataFinal);
		buttonCancelDataFinal = (ImageButton) findViewById(R.id.imageButtonCancelDataFinal);
		
		spinnerPlataforma = (Spinner) findViewById(R.id.spinnerPlataforma);
		buttonCancelPlataforma = (ImageButton) findViewById(R.id.imageButtonCancelPlataforma);
		
		switchApenasVendiveis = (Switch) findViewById(R.id.switchApenasVendiveis);
		
		switchApenasPlataformasAtivas = (Switch) findViewById(R.id.switchApenasAtivas);
		
		Intent it = getIntent();
		
		if (it.hasExtra(EXTRA_DATA_INICIAL)) {
			dataInicial = new Date(it.getLongExtra(EXTRA_DATA_INICIAL, 0));
			String strData = DateFormat.getDateFormat(getApplicationContext()).format(dataInicial);
			buttonDataInicial.setText(strData);
		}
		
		if (it.hasExtra(EXTRA_DATA_FINAL)) {
			dataFinal = new Date(it.getLongExtra(EXTRA_DATA_FINAL, 0));
			String strData = DateFormat.getDateFormat(getApplicationContext()).format(dataFinal);
			buttonDataFinal.setText(strData);
		}
		
		buttonDataInicial.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DataInicialPickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");				
			}
			
		});
		
		buttonCancelDataInicial.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonDataInicial.setText(getString(R.string.label_data_inicial));
				dataInicial = null;				
			}
			
		});
		
		buttonDataFinal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DataFinalPickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");				
			}
			
		});
		
		buttonCancelDataFinal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonDataFinal.setText(getString(R.string.label_data_final));
				dataFinal = null;					
			}
			
		});
		
		buttonCancelPlataforma.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				spinnerPlataforma.setSelection(plataformasAtivas.size());
				plataforma = null;					
			}
			
		});
		
		setTitle(getString(R.string.title_filtrar));
		
		dbHandler = DatabaseHandler.getInstance(getApplicationContext());
		
		if (it.getBooleanExtra(EXTRA_FILTRAR_PLATAFORMA, false)) {
			if (it.hasExtra(EXTRA_PLATAFORMA)) {
				plataforma = new PlataformaDatabaseHandler().
						getPlataforma(dbHandler.getReadableDatabase(), it.getLongExtra(EXTRA_PLATAFORMA, 0));
			}
			
			configuraSpinnerPlataformas();
		} else {
			spinnerPlataforma.setVisibility(View.GONE);
			buttonCancelPlataforma.setVisibility(View.GONE);
		}
		
		if (it.getBooleanExtra(EXTRA_FILTRAR_APENAS_VENDIVEIS, false)) {
			apenasVendiveis = it.getBooleanExtra(EXTRA_APENAS_VENDIVEIS, false);
			switchApenasVendiveis.setChecked(apenasVendiveis);
		} else {
			switchApenasVendiveis.setVisibility(View.GONE);
		}
		
		if (it.getBooleanExtra(EXTRA_FILTRAR_APENAS_PLATAFORMAS_ATIVAS, false)) {
			apenasPlataformasAtivas = it.getBooleanExtra(EXTRA_APENAS_PLATAFORMAS_ATIVAS, false);
			switchApenasPlataformasAtivas.setChecked(apenasPlataformasAtivas);
		} else {
			switchApenasPlataformasAtivas.setVisibility(View.GONE);
		}
	}
	
	private void configuraSpinnerPlataformas()
	{
		plataformasAtivas = new PlataformaDatabaseHandler().getAllPlataformasAtivas(dbHandler.getReadableDatabase());
		
		Integer selecionado = null;
		
		List<String> dataAdapter = new ArrayList<>();
		
		for (int pos = 0; pos < plataformasAtivas.size(); pos++) {
			Plataforma p = plataformasAtivas.get(pos);
			dataAdapter.add(p.getNome());
			if (plataforma != null && plataforma.getId() == p.getId()) {
				selecionado = pos;
			}
		}
		
		ArrayAdapter<String> platAdapter;
				
		//Gambira para mostrar a opção "Plataforma" no formulario, mas nao mostrar nas opções
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
		 
		platAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerPlataforma.setAdapter(platAdapter);
		
		if (selecionado != null) { //seleciona a plataforma selecionada anteriormente
			spinnerPlataforma.setSelection(selecionado);
		} else {
			//continuação da gambiarra
			spinnerPlataforma.setSelection(platAdapter.getCount());
		}
						
		spinnerPlataforma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				if (position < plataformasAtivas.size()) {
					plataforma = plataformasAtivas.get(position);
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
		getMenuInflater().inflate(R.menu.filtro, menu);
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
		} else if (id == android.R.id.home || id == R.id.action_cancelar) {
			cancelar();
			return true;
		} else if (id == R.id.action_filtrar) {
			filtrar();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void cancelar()
	{
		finish();
	}
	
	private void filtrar()
	{
		Intent result = new Intent();
		
		if (dataInicial != null) {
			result.putExtra(EXTRA_DATA_INICIAL, dataInicial.getTime());
		}
		
		if (dataFinal != null) {
			result.putExtra(EXTRA_DATA_FINAL, dataFinal.getTime());
		}
		
		if (plataforma != null) {
			result.putExtra(EXTRA_PLATAFORMA, plataforma.getId());
		}
		
		if (apenasVendiveis != null) {
			result.putExtra(EXTRA_APENAS_VENDIVEIS, switchApenasVendiveis.isChecked());
		}
		
		if (apenasPlataformasAtivas != null) {
			result.putExtra(EXTRA_APENAS_PLATAFORMAS_ATIVAS, switchApenasPlataformasAtivas.isChecked());
		}
				
		setResult(RESULT_OK, result);
		finish();
	}
	
	public class DataInicialPickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
	{

		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the current date as the default date in the picker
	        final Calendar c = Calendar.getInstance();
	        
	        if (dataInicial != null) {
	        	c.setTime(dataInicial);
	        } else {
	        	c.setTime(new Date());
	        }
	        
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
		    cal.set(Calendar.HOUR_OF_DAY, 0);
		    cal.set(Calendar.MINUTE, 0);
		    cal.set(Calendar.SECOND, 0);
		    Date dateRepresentation = cal.getTime();
			
		    if (dataFinal != null && dataFinal.getTime() < dateRepresentation.getTime()) {
		    	
		    	ViewUtil.showError(getActivity(), getString(R.string.error_data_inicial_maior_data_final));
		    	
		    } else {
		    	
				dataInicial = dateRepresentation;
				
				String strData = DateFormat.getDateFormat(getApplicationContext()).format(dataInicial);
				buttonDataInicial.setText(strData);
		    }
			
		}
		
	}
	
	public class DataFinalPickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
	{

		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the current date as the default date in the picker
	        final Calendar c = Calendar.getInstance();
	        
	        if (dataFinal != null) {
	        	c.setTime(dataFinal);
	        } else {
	        	c.setTime(new Date());
	        }
	        
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
		    cal.set(Calendar.HOUR_OF_DAY, 23);
		    cal.set(Calendar.MINUTE, 59);
		    cal.set(Calendar.SECOND, 59);
		    cal.set(Calendar.MILLISECOND, 1000 - 1);
		    Date dateRepresentation = cal.getTime();

		    if (dataInicial != null && dataInicial.getTime() > dateRepresentation.getTime()) {
		    	
		    	ViewUtil.showError(getActivity(), getString(R.string.error_data_final_menor_inicial));
		    	
		    } else {
		    
				dataFinal = dateRepresentation;
				
				String strData = DateFormat.getDateFormat(getApplicationContext()).format(dataFinal);
				buttonDataFinal.setText(strData);
		    }
			
		}
		
	}
}
