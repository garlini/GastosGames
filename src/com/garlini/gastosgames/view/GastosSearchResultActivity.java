package com.garlini.gastosgames.view;

import com.garlini.gastosgames.R;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class GastosSearchResultActivity extends ActionBarActivity {

	private String query;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gastos_search_result);
		
		handleIntent(getIntent());
		
		if (query != null) {
			String title = getString(R.string.title_resultado_busca_por);
			setTitle(title.replace("__query__", "\"" + query + "\""));
		}
		
		if (savedInstanceState == null) {
			GastosListFragment frag = new GastosListFragment();
			frag.setQuery(query);
			
			FragmentManager fragmentManager = getSupportFragmentManager();
			
			fragmentManager.beginTransaction()
					.add(R.id.container, frag).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gastos_search_result, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null) {
            	this.query = query.trim();
            }
        }
    }

}
