package br.com.dan.bean;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BOperacao {
	private String dataPregao;
	
	private Character tipoOperacao;
	
	private String tipoMercado;
	
	private String descricaoPapel;
	
	private String papel;
	
	private Integer quantidade;
	
	private BigDecimal valorCota;
	
	private BigDecimal valorTotal;
	
	private Character creditoOuDebito;

}
