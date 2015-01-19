package com.garlini.gastosgames.report;

import java.util.HashMap;
import java.util.Map;

import com.garlini.gastosgames.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class RelatorioLoading extends RelatorioBase {

	public RelatorioLoading(Context context, SQLiteDatabase db) {
		super(context, db);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void body() {
		Map<String, String> attrs = new HashMap<>();
		attrs.put("class", "center");
		
		appendEl("p", getString(R.string.loading_relatorio), attrs, true);
	}
}
