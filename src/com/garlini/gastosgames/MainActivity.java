package com.garlini.gastosgames;

import com.garlini.gastosgames.view.CategoriasListFragment;
import com.garlini.gastosgames.view.GastosListFragment;
import com.garlini.gastosgames.view.GraficosListFragment;
import com.garlini.gastosgames.view.HomeFragment;
import com.garlini.gastosgames.view.ManutencaoListFragment;
import com.garlini.gastosgames.view.PlataformasListFragment;
import com.garlini.gastosgames.view.RelatoriosListFragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	
	private int itemSelected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		
		Fragment frag;
		
		switch (position) {
		case 1:
			frag = new PlataformasListFragment();
			mTitle = getString(R.string.title_section_plataformas);
			break;
			
		case 2:
			frag = new CategoriasListFragment();
			mTitle = getString(R.string.title_section_categorias);
			break;
			
		case 3:
			frag = new GastosListFragment();
			mTitle = getString(R.string.title_section_gastos);
			break;
			
		case 4:
			frag = new GraficosListFragment();
			mTitle = getString(R.string.title_section_graficos);
			break;
			
		case 5:
			frag = new RelatoriosListFragment();
			mTitle = getString(R.string.title_section_relatorios);
			break;
			
		case 6:
			frag = new ManutencaoListFragment();
			mTitle = getString(R.string.title_section_manutencao);
			break;

		default:
			frag = new HomeFragment();
			mTitle = getString(R.string.app_name);
			break;
		}
		
		itemSelected = position;
		
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager
				.beginTransaction()
				.replace(R.id.container, frag).commit();
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section_home);
			break;
		case 2:
			mTitle = getString(R.string.title_section_plataformas);
			break;
		case 3:
			mTitle = getString(R.string.title_section_categorias);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			
			switch (itemSelected) {
			case 3:
				getMenuInflater().inflate(R.menu.gasto_list, menu);
				
				// Associate searchable configuration with the SearchView
			    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
			    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
				
			    break;

			default:
				getMenuInflater().inflate(R.menu.main, menu);
				break;
			}
			
			
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}
		
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent it = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(it);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}

}
