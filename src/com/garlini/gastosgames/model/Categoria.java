package com.garlini.gastosgames.model;

public class Categoria {
	
	protected Long id;
	protected String nome;
	protected Boolean vendivel;
	
	

	public Categoria(Long id, String nome, Boolean vendivel) {
		super();
		this.id = id;
		this.nome = nome;
		this.vendivel = vendivel;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Boolean getVendivel() {
		return vendivel;
	}

	public void setVendivel(Boolean vendivel) {
		this.vendivel = vendivel;
	}

	public Boolean isVendivel() {
		return vendivel;
	}
}
