package com.garlini.gastosgames.model;

import java.math.BigDecimal;
import java.util.Date;

public class Gasto {

	protected Long id;
	protected String descricao;
	protected BigDecimal valor;
	protected Date data;
	protected Boolean vendivel;
	protected Long plataformaId;
	protected Long categoriaId;
	
	protected Plataforma plataforma;
	protected Categoria categoria;
	
	
	public Gasto(Long id, String descricao, BigDecimal valor, Date data,
			Boolean vendivel, Plataforma plataforma, Categoria categoria) {
		super();
		this.id = id;
		this.descricao = descricao;
		this.valor = valor;
		this.data = data;
		this.vendivel = vendivel;
		this.plataforma = plataforma;
		this.categoria = categoria;
	}
	
	public Gasto(Long id, String descricao, BigDecimal valor, Date data,
			Boolean vendivel, Long plataformaId, Long categoriaId) {
		super();
		this.id = id;
		this.descricao = descricao;
		this.valor = valor;
		this.data = data;
		this.vendivel = vendivel;
		this.plataformaId = plataformaId;
		this.categoriaId = categoriaId;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public BigDecimal getValor() {
		return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	public Date getData() {
		return data;
	}
	public void setData(Date data) {
		this.data = data;
	}
	public Boolean getVendivel() {
		return vendivel;
	}
	public void setVendivel(Boolean vendivel) {
		this.vendivel = vendivel;
	}
	public Plataforma getPlataforma() {
		return plataforma;
	}
	public void setPlataforma(Plataforma plataforma) {
		this.plataforma = plataforma;
	}
	public Categoria getCategoria() {
		return categoria;
	}
	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public Long getPlataformaId() {
		return plataformaId;
	}

	public void setPlataformaId(Long plataformaId) {
		this.plataformaId = plataformaId;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}
	
	
	
}
